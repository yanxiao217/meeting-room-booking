package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.meeting.common.exception.BusinessException;
import com.example.meeting.common.result.PageResult;
import com.example.meeting.config.ChatBotProperties;
import com.example.meeting.dto.ChatIntent;
import com.example.meeting.entity.AiModelConfig;
import com.example.meeting.entity.Booking;
import com.example.meeting.entity.ChatbotLog;
import com.example.meeting.entity.MeetingRoom;
import com.example.meeting.entity.SysConfig;
import com.example.meeting.mapper.BookingMapper;
import com.example.meeting.mapper.ChatbotLogMapper;
import com.example.meeting.mapper.MeetingRoomMapper;
import com.example.meeting.mapper.SysConfigMapper;
import com.example.meeting.service.AiModelConfigService;
import com.example.meeting.service.BookingService;
import com.example.meeting.service.ChatBotService;
import com.example.meeting.service.MeetingRoomService;
import com.example.meeting.vo.BookingVO;
import com.example.meeting.vo.ChatBotAnswerVO;
import com.example.meeting.vo.RoomVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 智能问答助手服务实现
 *
 * 安全约束：大模型只负责解析「意图 + 参数」，不接触任何SQL；
 * 参数经白名单校验后，由服务端调用既有查询服务取数，再把结果回填生成回答。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatBotServiceImpl implements ChatBotService {

    private final ChatBotProperties props;
    private final ChatbotLogMapper chatbotLogMapper;
    private final MeetingRoomMapper meetingRoomMapper;
    private final BookingMapper bookingMapper;
    private final SysConfigMapper sysConfigMapper;
    private final BookingService bookingService;
    private final MeetingRoomService meetingRoomService;
    private final AiModelConfigService aiModelConfigService;
    private final ObjectMapper objectMapper;

    private static final int MAX_ROOM_CARDS = 5;
    private static final int MAX_BOOKING_CARDS = 10;
    private static final int MIN_CAPACITY = 1;
    private static final int MAX_CAPACITY = 50;

    private static final String DEGRADED_TEXT =
            "抱歉，智能助手暂时开小差了，请稍后再试。您也可以直接使用「今日空闲」页面手动预约。";

    /** 设备编码 → 中文名 */
    private static final Map<String, String> EQUIP_LABEL = new LinkedHashMap<>();
    /** 中文/别名 → 设备编码 */
    private static final Map<String, String> EQUIP_ALIAS = new HashMap<>();

    static {
        EQUIP_LABEL.put("projector", "投影仪");
        EQUIP_LABEL.put("tv", "电视");
        EQUIP_LABEL.put("whiteboard", "白板");
        EQUIP_LABEL.put("video_conference", "视频会议");
        EQUIP_LABEL.put("phone", "电话");
        EQUIP_LABEL.put("speaker", "音响");
        EQUIP_LABEL.put("coffee_machine", "咖啡机");

        alias("projector", "投影仪", "投影", "projector", "幻灯");
        alias("tv", "电视", "显示屏", "大屏", "屏幕", "tv", "display");
        alias("whiteboard", "白板", "whiteboard", "写字板");
        alias("video_conference", "视频会议", "视频", "远程会议", "video", "会议摄像头");
        alias("phone", "电话", "电话亭", "phone");
        alias("speaker", "音响", "音箱", "speaker");
        alias("coffee_machine", "咖啡机", "咖啡", "coffee");
    }

    private static void alias(String code, String... names) {
        for (String n : names) {
            EQUIP_ALIAS.put(n.toLowerCase(), code);
        }
    }

    /** 单用户滑动窗口限流 */
    private final Map<Long, Deque<Long>> rateWindow = new ConcurrentHashMap<>();

    /** 大模型熔断：连续失败后冷却，避免每次提问都长时间等待 */
    private volatile long llmDownUntil = 0L;
    private static final long LLM_COOLDOWN_MS = 60_000L;

    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(上午|早上|凌晨|中午|下午|傍晚|晚上|夜里)?\\s*(\\d{1,2})\\s*(?:[:：]\\s*(\\d{1,2})|点(半|\\d{1,2})?分?|时(半|\\d{1,2})?分?)");

    // ============================================================
    // 对外接口
    // ============================================================

    @Override
    public ChatBotAnswerVO ask(Long userId, String question) {
        long start = System.currentTimeMillis();
        ChatbotLog logRow = new ChatbotLog();
        logRow.setUserId(userId);
        logRow.setQuestion(question);

        try {
            // 0. 限流
            if (!tryAcquire(userId)) {
                throw new BusinessException(400, "提问太频繁啦，请休息一下再问（每分钟最多"
                        + rateLimit() + "次）");
            }

            // 1. 解析意图（LLM 优先，失败降级规则解析）
            ChatIntent intent = parseIntent(question);
            logRow.setIntent(intent.getIntent());
            logRow.setParams(objectMapper.writeValueAsString(intent));

            // 2. 白名单参数校验
            normalizeAndValidate(intent);

            // 3. 按意图调度既有查询服务
            ChatBotAnswerVO vo = dispatch(userId, question, intent);

            logRow.setAnswer(vo.getAnswer());
            logRow.setResultCount(countResult(vo));
            logRow.setSuccess(1);
            return vo;
        } catch (BusinessException be) {
            ChatBotAnswerVO vo = degraded(be.getMessage());
            logRow.setAnswer(vo.getAnswer());
            logRow.setResultCount(0);
            logRow.setSuccess(0);
            return vo;
        } catch (Exception e) {
            log.error("智能问答处理失败: {}", e.getMessage(), e);
            ChatBotAnswerVO vo = degraded(DEGRADED_TEXT);
            logRow.setAnswer(vo.getAnswer());
            logRow.setResultCount(0);
            logRow.setSuccess(0);
            return vo;
        } finally {
            logRow.setCostMs(System.currentTimeMillis() - start);
            try {
                chatbotLogMapper.insert(logRow);
            } catch (Exception e) {
                log.warn("写入问答日志失败: {}", e.getMessage());
            }
        }
    }

    @Override
    public PageResult<ChatbotLog> getLogs(String intent, Integer success, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ChatbotLog> wrapper = new LambdaQueryWrapper<>();
        if (intent != null && !intent.isBlank()) {
            wrapper.eq(ChatbotLog::getIntent, intent);
        }
        if (success != null) {
            wrapper.eq(ChatbotLog::getSuccess, success);
        }
        wrapper.orderByDesc(ChatbotLog::getCreateTime);

        Page<ChatbotLog> page = chatbotLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    // ============================================================
    // 意图解析
    // ============================================================

    private ChatIntent parseIntent(String question) {
        LocalDate today = LocalDate.now();
        // 优先大模型
        try {
            ChatIntent it = llmParse(question, today);
            if (it != null && it.getIntent() != null) {
                return it;
            }
        } catch (Exception e) {
            log.info("大模型解析不可用，降级规则解析: {}", e.getMessage());
        }
        // 规则兜底
        ChatIntent it = ruleParse(question, today);
        it.setSource("rule");
        return it;
    }

    private ChatIntent llmParse(String question, LocalDate today) throws Exception {
        if (!enabled() || !llmAvailable() || settings() == null) {
            throw new IllegalStateException("LLM未配置或冷却中");
        }

        String userPrompt = "今天是 " + today + "（" + weekdayCN(today) + "）。用户问题："
                + question + "\n只输出JSON：";
        String content = chat(SYSTEM_PROMPT, userPrompt);
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("LLM空响应");
        }

        // 容错：剥离代码块，截取第一个 JSON 对象
        content = content.replace("```json", "").replace("```", "").trim();
        int a = content.indexOf('{');
        int b = content.lastIndexOf('}');
        if (a >= 0 && b > a) {
            content = content.substring(a, b + 1);
        }

        ChatIntent it = objectMapper.readValue(content, ChatIntent.class);
        it.setSource("llm");
        return it;
    }

    private ChatIntent ruleParse(String question, LocalDate today) {
        ChatIntent it = new ChatIntent();
        String q = question;

        // 1. 我的预约
        if (containsAny(q, "我的预约", "我预约的", "我约的", "我订的", "我的会议", "我的预订",
                "我的日程", "我最近", "我有哪些预约", "查我的预约", "预约记录", "预约有哪些",
                "我取消的", "我取消过", "我完成的", "我已完成的", "看看我的", "查一下我的")) {
            it.setIntent(ChatIntent.MY_BOOKINGS);
            it.setStatus(parseStatus(q));
            return it;
        }

        String roomName = extractRoomKeyword(q);
        boolean infoSignal = containsAny(q, "在哪", "位置", "几楼", "多大", "容纳", "能坐", "详情",
                "介绍", "设备", "怎么样", "什么样", "什么配置", "有多少人", "在哪层", "什么设备");

        // 2. 明确指向某间会议室的详情
        if (roomName != null && infoSignal) {
            it.setIntent(ChatIntent.ROOM_INFO);
            it.setKeyword(roomName);
            return it;
        }

        // 3. 查空闲会议室
        List<String> equip = parseEquipment(q);
        Integer cap = parseCapacity(q);
        boolean findSignal = containsAny(q, "空闲", "空着", "有空", "可用", "可预约", "推荐", "哪间",
                "哪些", "有没有空", "有没有", "找会议室", "找一间", "帮我找", "帮我订", "帮我约",
                "哪间会议室", "预订会议室", "预约会议室", "有空吗", "空会议室", "空的会议室");
        if (findSignal || (q.contains("会议室") && (!equip.isEmpty() || cap != null))) {
            it.setIntent(ChatIntent.FIND_ROOMS);
            LocalDate d = parseDate(q, today);
            it.setDate(d != null ? d.toString() : null);
            LocalTime[] times = parseTimeRange(q);
            if (times != null) {
                it.setStartTime(times[0].toString());
                it.setEndTime(times[1].toString());
            }
            it.setCapacity(cap);
            it.setEquipment(equip);
            it.setFloor(parseFloor(q));
            return it;
        }

        // 4. 泛指会议室详情 / 命中会议室名
        if (infoSignal || roomName != null) {
            it.setIntent(ChatIntent.ROOM_INFO);
            it.setKeyword(roomName);
            return it;
        }

        it.setIntent(ChatIntent.UNKNOWN);
        return it;
    }

    // ============================================================
    // 白名单参数校验
    // ============================================================

    private void normalizeAndValidate(ChatIntent it) {
        String intent = it.getIntent() == null ? ChatIntent.UNKNOWN : it.getIntent();

        // 日期：必须是 yyyy-MM-dd，且在今天 ~ 今天+可预约天数内
        if (it.getDate() != null && !it.getDate().isBlank()) {
            try {
                LocalDate d = LocalDate.parse(it.getDate().trim());
                LocalDate today = LocalDate.now();
                int advanceDays = getConfigInt("booking.advance_days", 14);
                if (d.isBefore(today)) {
                    d = today;
                }
                if (d.isAfter(today.plusDays(advanceDays))) {
                    d = today.plusDays(advanceDays);
                }
                it.setDate(d.toString());
            } catch (Exception e) {
                throw new BusinessException(400, "日期格式无法识别，请使用“明天/2026-09-17”这类说法。");
            }
        }

        // 时段：HH:mm，08:00~22:00，start < end
        LocalTime start = parseTime(it.getStartTime());
        LocalTime end = parseTime(it.getEndTime());
        if (start != null) {
            it.setStartTime(start.toString());
        }
        if (end != null) {
            it.setEndTime(end.toString());
        }
        if (start != null && end != null && !end.isAfter(start)) {
            throw new BusinessException(400, "结束时间需要晚于开始时间，请重新描述。");
        }

        // 人数：1~50
        if (it.getCapacity() != null) {
            if (it.getCapacity() < MIN_CAPACITY || it.getCapacity() > MAX_CAPACITY) {
                throw new BusinessException(400, "参会人数需要在 " + MIN_CAPACITY + "~" + MAX_CAPACITY + " 之间。");
            }
        }

        // 设备：白名单
        if (it.getEquipment() != null && !it.getEquipment().isEmpty()) {
            List<String> clean = new ArrayList<>();
            for (String e : it.getEquipment()) {
                String code = normalizeEquip(e);
                if (code == null) {
                    throw new BusinessException(400, "暂不支持识别设备「" + e + "」，可选：投影仪、电视、白板、视频会议、电话、音响、咖啡机。");
                }
                if (!clean.contains(code)) {
                    clean.add(code);
                }
            }
            it.setEquipment(clean);
        }

        // 状态白名单
        if (it.getStatus() != null && !it.getStatus().isBlank()) {
            Set<String> allowed = Set.of("pending", "confirmed", "signed", "completed", "cancelled", "expired");
            if (!allowed.contains(it.getStatus())) {
                it.setStatus(null);
            }
        }

        it.setIntent(intent);
    }

    private LocalTime parseTime(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(s.trim());
        } catch (Exception e) {
            Matcher m = TIME_PATTERN.matcher(s);
            if (m.find()) {
                return LocalTime.of(Integer.parseInt(m.group(2)), 0);
            }
            return null;
        }
    }

    // ============================================================
    // 意图调度
    // ============================================================

    private ChatBotAnswerVO dispatch(Long userId, String question, ChatIntent it) {
        ChatBotAnswerVO vo;
        switch (it.getIntent()) {
            case ChatIntent.FIND_ROOMS -> vo = findRooms(it);
            case ChatIntent.MY_BOOKINGS -> vo = myBookings(userId, it);
            case ChatIntent.ROOM_INFO -> vo = roomInfo(it);
            default -> vo = unknownIntent();
        }
        vo.setIntent(it.getIntent());
        vo.setIntentName(intentName(it.getIntent()));
        vo.setSuccess(true);
        // 可选：大模型润色（不改变任何事实）
        if (vo.getFallback() == null) {
            vo.setAnswer(polish(question, vo.getAnswer()));
        }
        return vo;
    }

    private ChatBotAnswerVO findRooms(ChatIntent it) {
        ChatBotAnswerVO vo = new ChatBotAnswerVO();
        LocalDate date = it.getDate() != null ? LocalDate.parse(it.getDate()) : LocalDate.now();
        LocalTime start = it.getStartTime() != null ? LocalTime.parse(it.getStartTime()) : LocalTime.of(9, 0);
        LocalTime end = it.getEndTime() != null ? LocalTime.parse(it.getEndTime()) : start.plusHours(1);

        List<MeetingRoom> all = meetingRoomMapper.selectList(
                new LambdaQueryWrapper<MeetingRoom>()
                        .eq(MeetingRoom::getStatus, 1)
                        .orderByAsc(MeetingRoom::getSort));

        List<Long> hitIds = new ArrayList<>();
        for (MeetingRoom r : all) {
            if (it.getCapacity() != null && (r.getCapacity() == null || r.getCapacity() < it.getCapacity())) {
                continue;
            }
            if (it.getFloor() != null && (r.getFloor() == null || !r.getFloor().contains(it.getFloor()))) {
                continue;
            }
            if (it.getEquipment() != null && !it.getEquipment().isEmpty()) {
                Set<String> have = normalizeEquipSet(r.getEquipment());
                if (!have.containsAll(it.getEquipment())) {
                    continue;
                }
            }
            int conflict = bookingMapper.countConflictBookings(r.getId(), date, start, end, 0L);
            if (conflict > 0) {
                continue;
            }
            hitIds.add(r.getId());
            if (hitIds.size() >= MAX_ROOM_CARDS) {
                break;
            }
        }

        String range = date + " " + start + "–" + end;
        List<ChatBotAnswerVO.RoomCard> cards = new ArrayList<>();
        for (Long id : hitIds) {
            try {
                RoomVO room = meetingRoomService.getRoomDetail(id);
                ChatBotAnswerVO.RoomCard card = new ChatBotAnswerVO.RoomCard();
                card.setRoomId(room.getId());
                card.setRoomName(room.getRoomName());
                card.setFloor(room.getFloor());
                card.setLocation(room.getLocation());
                card.setCapacity(room.getCapacity());
                card.setEquipment(room.getEquipment());
                card.setEquipmentText(equipmentText(room.getEquipment()));
                card.setDate(date.toString());
                card.setTimeRange(start + "–" + end);
                card.setAvgRating(room.getAvgRating());
                cards.add(card);
            } catch (Exception ignored) {
                // 忽略单个会议室异常
            }
        }
        vo.setRooms(cards);

        if (cards.isEmpty()) {
            vo.setAnswer("没有找到 " + range + " 时段符合条件的空闲会议室。"
                    + "可以试着换个时间，或放宽人数、设备要求。");
        } else {
            String list = cards.stream()
                    .map(c -> c.getRoomName() + "（" + c.getFloor() + "·" + c.getCapacity() + "人"
                            + (c.getEquipmentText().isEmpty() ? "" : "·" + c.getEquipmentText()) + "）")
                    .collect(Collectors.joining("；"));
            vo.setAnswer(range + " 找到 " + cards.size() + " 间可用会议室：" + list
                    + "。需要我帮您预约哪一间吗？");
        }
        return vo;
    }

    private ChatBotAnswerVO myBookings(Long userId, ChatIntent it) {
        ChatBotAnswerVO vo = new ChatBotAnswerVO();
        PageResult<BookingVO> page = bookingService.getMyBookings(userId, it.getStatus(), 1, MAX_BOOKING_CARDS);
        List<BookingVO> list = page.getList() == null ? List.of() : page.getList();

        List<ChatBotAnswerVO.BookingCard> cards = new ArrayList<>();
        for (BookingVO b : list) {
            ChatBotAnswerVO.BookingCard card = new ChatBotAnswerVO.BookingCard();
            card.setId(b.getId());
            card.setBookingNo(b.getBookingNo());
            card.setRoomName(b.getRoomName());
            card.setBookingDate(b.getBookingDate() != null ? b.getBookingDate().toString() : "");
            card.setStartTime(b.getStartTime() != null ? b.getStartTime().toString() : "");
            card.setEndTime(b.getEndTime() != null ? b.getEndTime().toString() : "");
            card.setTopic(b.getTopic());
            card.setStatus(b.getStatus());
            card.setStatusName(b.getStatusName());
            cards.add(card);
        }
        vo.setBookings(cards);

        if (cards.isEmpty()) {
            vo.setAnswer("您目前没有" + (it.getStatus() != null ? "该状态的" : "") + "预约记录。"
                    + "需要的话，可以问我「明天下午有没有空闲会议室」。");
        } else {
            String listText = cards.stream()
                    .map(c -> c.getBookingDate() + " " + shortTime(c.getStartTime()) + "-" + shortTime(c.getEndTime())
                            + " " + c.getRoomName() + "《" + c.getTopic() + "》（" + c.getStatusName() + "）")
                    .collect(Collectors.joining("；"));
            vo.setAnswer("您共有 " + page.getTotal() + " 条预约：" + listText + "。");
        }
        return vo;
    }

    private ChatBotAnswerVO roomInfo(ChatIntent it) {
        ChatBotAnswerVO vo = new ChatBotAnswerVO();
        String kw = it.getKeyword();
        if (kw == null || kw.isBlank()) {
            vo.setFallback("no_keyword");
            vo.setAnswer("您想了解哪一间会议室？例如「创新厅在哪，能坐多少人」。");
            return vo;
        }

        List<MeetingRoom> rooms = meetingRoomMapper.selectList(
                new LambdaQueryWrapper<MeetingRoom>()
                        .like(MeetingRoom::getRoomName, kw)
                        .or()
                        .like(MeetingRoom::getLocation, kw)
                        .last("LIMIT 1"));

        if (rooms.isEmpty()) {
            List<MeetingRoom> all = meetingRoomMapper.selectList(
                    new LambdaQueryWrapper<MeetingRoom>().eq(MeetingRoom::getStatus, 1).orderByAsc(MeetingRoom::getSort));
            String names = all.stream().map(MeetingRoom::getRoomName).collect(Collectors.joining("、"));
            vo.setFallback("room_not_found");
            vo.setAnswer("没有找到「" + kw + "」这间会议室。目前可用的有：" + names + "。");
            return vo;
        }

        RoomVO room = meetingRoomService.getRoomDetail(rooms.get(0).getId());
        String equip = equipmentText(room.getEquipment());
        StringBuilder sb = new StringBuilder();
        sb.append(room.getRoomName()).append(" 位于 ").append(room.getFloor())
                .append(room.getLocation() == null ? "" : " " + room.getLocation())
                .append("，可容纳 ").append(room.getCapacity()).append(" 人");
        if (!equip.isEmpty()) {
            sb.append("，配备").append(equip);
        }
        if (room.getAvgRating() != null && room.getAvgRating() > 0) {
            sb.append("，平均评分 ").append(String.format("%.1f", room.getAvgRating())).append(" 分");
        }
        sb.append("。");
        vo.setAnswer(sb.toString());
        return vo;
    }

    private ChatBotAnswerVO unknownIntent() {
        ChatBotAnswerVO vo = new ChatBotAnswerVO();
        vo.setFallback("unknown_intent");
        vo.setAnswer("我是会议室预约助手，可以帮您：\n"
                + "1) 查空闲会议室，例如「明天下午2点到4点，找一间能坐10人的会议室」；\n"
                + "2) 查我的预约，例如「我最近有哪些预约」；\n"
                + "3) 查会议室信息，例如「创新厅在哪，能坐多少人」。");
        return vo;
    }

    private ChatBotAnswerVO degraded(String message) {
        ChatBotAnswerVO vo = new ChatBotAnswerVO();
        vo.setAnswer(message == null || message.isBlank() ? DEGRADED_TEXT : message);
        vo.setIntent(ChatIntent.UNKNOWN);
        vo.setIntentName(intentName(ChatIntent.UNKNOWN));
        vo.setSuccess(false);
        vo.setFallback("degraded");
        return vo;
    }

    // ============================================================
    // 大模型调用
    // ============================================================

    private static final String SYSTEM_PROMPT = """
            你是会议室预约平台的语义解析器。你的唯一任务是：把用户问题解析成一个JSON对象。
            只输出JSON本身，不要输出任何解释、前后缀或Markdown代码块。

            输出格式：
            {"intent":"意图","params":{"date":"日期","startTime":"开始时间","endTime":"结束时间","capacity":人数,"equipment":["设备"],"floor":"楼层","keyword":"关键词","status":"状态"}}

            支持的意图：
            - find_available_rooms：查找某时段空闲的会议室
            - my_bookings：查询用户自己的预约
            - room_info：查询某间会议室的详情
            - unknown：无法归类

            参数规则：
            - date 用 yyyy-MM-dd；"今天/明天/后天"要换算成具体日期
            - startTime/endTime 用 HH:mm（24小时制）
            - capacity 为整数人数（1-50）
            - equipment 取值只能是：projector(投影仪)、tv(电视)、whiteboard(白板)、video_conference(视频会议)、phone(电话)、speaker(音响)、coffee_machine(咖啡机)
            - status 取值：pending/confirmed/signed/completed/cancelled
            - 无法确定的参数请省略

            示例：
            用户：明天下午2点到4点，能坐10个人、有投影仪的会议室有哪些
            输出：{"intent":"find_available_rooms","params":{"date":"2026-09-17","startTime":"14:00","endTime":"16:00","capacity":10,"equipment":["projector"]}}

            用户：我最近有哪些预约
            输出：{"intent":"my_bookings","params":{}}

            用户：创新厅在哪，能坐多少人
            输出：{"intent":"room_info","params":{"keyword":"创新厅"}}

            用户：你好
            输出：{"intent":"unknown","params":{}}
            """;

    @SuppressWarnings("unchecked")
    private String chat(String system, String user) {
        LlmSettings s = settings();
        if (s == null) {
            throw new IllegalStateException("LLM未配置");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", s.model());
        body.put("temperature", s.temperature());
        body.put("messages", List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", user)
        ));

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(3000);
            factory.setReadTimeout(Math.max(5, s.timeoutSeconds()) * 1000);

            Map<String, Object> resp = RestClient.builder()
                    .baseUrl(s.baseUrl())
                    .defaultHeader("Authorization", "Bearer " + s.apiKey())
                    .requestFactory(factory)
                    .build()
                    .post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (resp == null) {
                return null;
            }
            Object choices = resp.get("choices");
            if (!(choices instanceof List<?> list) || list.isEmpty()) {
                return null;
            }
            Object first = list.get(0);
            if (!(first instanceof Map<?, ?> m)) {
                return null;
            }
            Object msg = m.get("message");
            if (msg instanceof Map<?, ?> mm) {
                Object content = mm.get("content");
                return content == null ? null : content.toString();
            }
            return null;
        } catch (Exception e) {
            // 熔断：一段时间内不再尝试大模型，直接走规则解析
            markLlmDown();
            throw e;
        }
    }

    private boolean llmAvailable() {
        return System.currentTimeMillis() >= llmDownUntil;
    }

    private void markLlmDown() {
        llmDownUntil = System.currentTimeMillis() + LLM_COOLDOWN_MS;
        log.warn("大模型不可用，已熔断 {} 秒，期间使用规则解析", LLM_COOLDOWN_MS / 1000);
    }

    /**
     * 当前生效的大模型连接参数：优先数据库默认配置，其次 application.yml
     */
    private LlmSettings settings() {
        try {
            AiModelConfig cfg = aiModelConfigService.getActive();
            if (cfg != null && cfg.getBaseUrl() != null && !cfg.getBaseUrl().isBlank()
                    && cfg.getApiKey() != null && !cfg.getApiKey().isBlank()) {
                double temp = cfg.getTemperature() != null ? cfg.getTemperature().doubleValue() : 0.2;
                int timeout = cfg.getTimeoutSeconds() != null ? cfg.getTimeoutSeconds() : 20;
                return new LlmSettings(cfg.getBaseUrl(), cfg.getApiKey(),
                        cfg.getModel() != null ? cfg.getModel() : props.getModel(), temp, timeout);
            }
        } catch (Exception e) {
            log.debug("读取AI模型配置失败，回退application.yml: {}", e.getMessage());
        }

        String key = config("chatbot.api-key", props.getApiKey());
        if (key == null || key.isBlank()) {
            return null;
        }
        return new LlmSettings(
                config("chatbot.base-url", props.getBaseUrl()),
                key,
                config("chatbot.model", props.getModel()),
                props.getTemperature(),
                props.getTimeoutSeconds());
    }

    private record LlmSettings(String baseUrl, String apiKey, String model,
                               double temperature, int timeoutSeconds) {
    }

    /**
     * 可选润色：仅在不改变事实的前提下改写措辞；失败则返回原文
     */
    private String polish(String question, String templateAnswer) {
        if (templateAnswer == null || templateAnswer.isBlank()) {
            return templateAnswer;
        }
        try {
            if (!enabled() || !llmAvailable() || settings() == null) {
                return templateAnswer;
            }
            String sys = "你是会议室预约平台助手。请用简洁自然的中文改写下面的回答，"
                    + "严禁增加、删除或修改任何事实（数字、名称、时间、状态必须与原回答完全一致）。只输出改写后的文字。";
            String out = chat(sys, "原回答：" + templateAnswer);
            if (out != null && !out.isBlank()) {
                return out.trim();
            }
        } catch (Exception e) {
            log.info("回答润色失败，使用模板回答: {}", e.getMessage());
        }
        return templateAnswer;
    }

    // ============================================================
    // 限流 / 配置
    // ============================================================

    private boolean tryAcquire(Long userId) {
        if (userId == null) {
            return true;
        }
        int limit = rateLimit();
        long now = System.currentTimeMillis();
        long windowStart = now - 60_000L;
        Deque<Long> dq = rateWindow.computeIfAbsent(userId, k -> new ArrayDeque<>());
        synchronized (dq) {
            while (!dq.isEmpty() && dq.peekFirst() < windowStart) {
                dq.pollFirst();
            }
            if (dq.size() >= limit) {
                return false;
            }
            dq.addLast(now);
            return true;
        }
    }

    private boolean enabled() {
        return "true".equalsIgnoreCase(config("chatbot.enabled", String.valueOf(props.isEnabled())));
    }

    private int rateLimit() {
        return getConfigInt("chatbot.rate_limit_per_minute", props.getRateLimitPerMinute());
    }

    private String config(String key, String defaultValue) {
        try {
            SysConfig cfg = sysConfigMapper.selectByConfigKey(key);
            if (cfg != null && cfg.getConfigValue() != null && !cfg.getConfigValue().isBlank()) {
                return cfg.getConfigValue();
            }
        } catch (Exception ignored) {
        }
        return defaultValue;
    }

    private int getConfigInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(config(key, String.valueOf(defaultValue)));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ============================================================
    // 解析辅助
    // ============================================================

    private static boolean containsAny(String text, String... keys) {
        for (String k : keys) {
            if (text.contains(k)) {
                return true;
            }
        }
        return false;
    }

    private LocalDate parseDate(String q, LocalDate today) {
        if (q.contains("大后天")) {
            return today.plusDays(3);
        }
        if (q.contains("后天")) {
            return today.plusDays(2);
        }
        if (q.contains("明天") || q.contains("明日")) {
            return today.plusDays(1);
        }
        if (q.contains("今天") || q.contains("今日")) {
            return today;
        }
        Matcher m = Pattern.compile("(20\\d{2})[-/年](\\d{1,2})[-/月](\\d{1,2})").matcher(q);
        if (m.find()) {
            try {
                return LocalDate.of(Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
            } catch (Exception ignored) {
            }
        }
        m = Pattern.compile("(\\d{1,2})月(\\d{1,2})[日号]").matcher(q);
        if (m.find()) {
            try {
                return LocalDate.of(today.getYear(),
                        Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
            } catch (Exception ignored) {
            }
        }
        // 周X / 星期X
        Map<String, Integer> week = Map.of("一", 1, "二", 2, "三", 3, "四", 4, "五", 5, "六", 6, "日", 7, "天", 7);
        m = Pattern.compile("(?:周|星期)([一二三四五六日天])").matcher(q);
        if (m.find()) {
            int target = week.get(m.group(1));
            int cur = today.getDayOfWeek().getValue();
            int delta = (target - cur + 7) % 7;
            return today.plusDays(delta);
        }
        return null;
    }

    private LocalTime[] parseTimeRange(String q) {
        List<LocalTime> times = extractTimes(q);
        if (times.isEmpty()) {
            return null;
        }
        LocalTime start = times.get(0);
        LocalTime end = times.size() >= 2 ? times.get(1) : start.plusHours(1);
        if (!end.isAfter(start)) {
            end = start.plusHours(1);
        }
        return new LocalTime[]{start, end};
    }

    private List<LocalTime> extractTimes(String q) {
        List<LocalTime> result = new ArrayList<>();
        Matcher m = TIME_PATTERN.matcher(q);
        String lastPeriod = null;
        while (m.find()) {
            String period = m.group(1) != null ? m.group(1) : lastPeriod;
            if (m.group(1) != null) {
                lastPeriod = m.group(1);
            }
            int hour = Integer.parseInt(m.group(2));
            int minute = 0;
            if (m.group(3) != null) {
                minute = Integer.parseInt(m.group(3));
            } else if (m.group(4) != null) {
                minute = "半".equals(m.group(4)) ? 30 : Integer.parseInt(m.group(4));
            } else if (m.group(5) != null) {
                minute = "半".equals(m.group(5)) ? 30 : Integer.parseInt(m.group(5));
            }
            if (period != null && hour < 12
                    && (period.contains("下午") || period.contains("傍晚")
                    || period.contains("晚上") || period.contains("夜里") || period.contains("中午"))) {
                hour += 12;
            }
            if (hour >= 0 && hour <= 23 && minute >= 0 && minute < 60) {
                result.add(LocalTime.of(hour, minute));
            }
        }
        return result;
    }

    private Integer parseCapacity(String q) {
        Matcher m = Pattern.compile("(\\d{1,2})\\s*(?:个?人|位)").matcher(q);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        m = Pattern.compile("(?:容纳|能坐|坐得下|可坐)\\s*(\\d{1,2})").matcher(q);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return null;
    }

    private String parseFloor(String q) {
        Matcher m = Pattern.compile("(\\d{1,2})\\s*(?:楼|层)").matcher(q);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private String parseStatus(String q) {
        if (containsAny(q, "已完成", "完成")) {
            return "completed";
        }
        if (containsAny(q, "已取消", "取消")) {
            return "cancelled";
        }
        if (containsAny(q, "待审批", "审批中", "审核中")) {
            return "pending";
        }
        if (containsAny(q, "已签到")) {
            return "signed";
        }
        if (containsAny(q, "已确认", "进行中", "即将")) {
            return "confirmed";
        }
        return null;
    }

    private List<String> parseEquipment(String q) {
        List<String> codes = new ArrayList<>();
        for (Map.Entry<String, String> e : EQUIP_ALIAS.entrySet()) {
            if (q.toLowerCase().contains(e.getKey()) && !codes.contains(e.getValue())) {
                codes.add(e.getValue());
            }
        }
        return codes;
    }

    private String extractRoomKeyword(String q) {
        // 优先匹配已知会议室名
        try {
            List<MeetingRoom> rooms = meetingRoomMapper.selectList(null);
            for (MeetingRoom r : rooms) {
                if (r.getRoomName() != null && q.contains(r.getRoomName())) {
                    return r.getRoomName();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String normalizeEquip(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim().toLowerCase();
        if (EQUIP_LABEL.containsKey(s)) {
            return s;
        }
        return EQUIP_ALIAS.get(s);
    }

    private Set<String> normalizeEquipSet(String equipmentJson) {
        Set<String> set = new HashSet<>();
        if (equipmentJson == null || equipmentJson.isBlank()) {
            return set;
        }
        try {
            List<String> raw = objectMapper.readValue(equipmentJson, new TypeReference<List<String>>() {});
            for (String r : raw) {
                String code = normalizeEquip(r);
                if (code != null) {
                    set.add(code);
                }
            }
        } catch (Exception ignored) {
        }
        return set;
    }

    private String equipmentText(List<String> equipment) {
        if (equipment == null || equipment.isEmpty()) {
            return "";
        }
        return equipment.stream()
                .map(e -> EQUIP_LABEL.getOrDefault(e, e))
                .collect(Collectors.joining("/"));
    }

    private String shortTime(String t) {
        if (t == null) {
            return "";
        }
        return t.length() >= 5 ? t.substring(0, 5) : t;
    }

    private int countResult(ChatBotAnswerVO vo) {
        int n = 0;
        if (vo.getRooms() != null) {
            n += vo.getRooms().size();
        }
        if (vo.getBookings() != null) {
            n += vo.getBookings().size();
        }
        return n;
    }

    private String intentName(String intent) {
        if (intent == null) {
            return "未知";
        }
        return switch (intent) {
            case ChatIntent.FIND_ROOMS -> "查询空闲会议室";
            case ChatIntent.MY_BOOKINGS -> "查询我的预约";
            case ChatIntent.ROOM_INFO -> "查询会议室详情";
            default -> "未知意图";
        };
    }

    private String weekdayCN(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "周一";
            case TUESDAY -> "周二";
            case WEDNESDAY -> "周三";
            case THURSDAY -> "周四";
            case FRIDAY -> "周五";
            case SATURDAY -> "周六";
            case SUNDAY -> "周日";
        };
    }
}
