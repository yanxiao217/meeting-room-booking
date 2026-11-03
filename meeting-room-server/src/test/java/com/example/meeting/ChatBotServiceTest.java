package com.example.meeting;

import com.example.meeting.common.exception.BusinessException;
import com.example.meeting.common.result.PageResult;
import com.example.meeting.config.ChatBotProperties;
import com.example.meeting.dto.ChatIntent;
import com.example.meeting.entity.MeetingRoom;
import com.example.meeting.mapper.BookingMapper;
import com.example.meeting.mapper.ChatbotLogMapper;
import com.example.meeting.mapper.MeetingRoomMapper;
import com.example.meeting.mapper.SysConfigMapper;
import com.example.meeting.service.BookingService;
import com.example.meeting.service.MeetingRoomService;
import com.example.meeting.service.AiModelConfigService;
import com.example.meeting.service.impl.ChatBotServiceImpl;
import com.example.meeting.vo.BookingVO;
import com.example.meeting.vo.ChatBotAnswerVO;
import com.example.meeting.vo.RoomVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ChatBotService 单元测试
 * 覆盖：三类意图解析、参数白名单校验、限流、全链路降级、日志落库
 * 说明：api-key 为空时走规则解析兜底，因此测试不依赖任何大模型。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatBotServiceTest {

    @Mock
    private ChatbotLogMapper chatbotLogMapper;
    @Mock
    private MeetingRoomMapper meetingRoomMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private SysConfigMapper sysConfigMapper;
    @Mock
    private BookingService bookingService;
    @Mock
    private MeetingRoomService meetingRoomService;
    @Mock
    private AiModelConfigService aiModelConfigService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private ChatBotProperties props;

    @InjectMocks
    private ChatBotServiceImpl chatBotService;

    @BeforeEach
    void setUp() {
        props = new ChatBotProperties();
        props.setEnabled(true);
        props.setApiKey(""); // 关闭大模型，走规则解析
        props.setRateLimitPerMinute(10);
        ReflectionTestUtils.setField(chatBotService, "props", props);

        when(sysConfigMapper.selectByConfigKey(anyString())).thenReturn(null);
    }

    private MeetingRoom room(long id, String name, String floor, int capacity, String equipment) {
        MeetingRoom r = new MeetingRoom();
        r.setId(id);
        r.setRoomName(name);
        r.setFloor(floor);
        r.setLocation(floor + "东侧");
        r.setCapacity(capacity);
        r.setEquipment(equipment);
        r.setStatus(1);
        return r;
    }

    private RoomVO roomVO(long id, String name, String floor, int capacity) {
        RoomVO vo = new RoomVO();
        vo.setId(id);
        vo.setRoomName(name);
        vo.setFloor(floor);
        vo.setCapacity(capacity);
        vo.setEquipment(List.of("projector"));
        vo.setAvgRating(4.5);
        return vo;
    }

    private ChatIntent validate(ChatIntent it) {
        ReflectionTestUtils.invokeMethod(chatBotService, "normalizeAndValidate", it);
        return it;
    }

    // ================= 意图一：查空闲会议室 =================

    @Test
    @DisplayName("查空闲会议室：应识别意图并按人数/设备过滤，返回会议室卡片")
    void ask_findRooms_shouldReturnCards() {
        when(meetingRoomMapper.selectList(any())).thenReturn(List.of(
                room(1, "创新厅", "3楼", 20, "[\"projector\",\"tv\"]"),
                room(2, "洽谈室A", "1楼", 6, "[\"tv\"]")
        ));
        when(bookingMapper.countConflictBookings(anyLong(), any(), any(), any(), anyLong())).thenReturn(0);
        when(meetingRoomService.getRoomDetail(1L)).thenReturn(roomVO(1, "创新厅", "3楼", 20));

        ChatBotAnswerVO vo = chatBotService.ask(2L, "明天下午2点到4点，能坐10人、有投影仪的会议室有哪些");

        assertEquals("find_available_rooms", vo.getIntent());
        assertTrue(vo.getSuccess());
        assertNotNull(vo.getRooms());
        // 洽谈室A仅6人且无投影仪，应被过滤，只剩创新厅
        assertEquals(1, vo.getRooms().size());
        assertEquals("创新厅", vo.getRooms().get(0).getRoomName());
    }

    @Test
    @DisplayName("查空闲会议室：无结果时给出友好提示而非报错")
    void ask_findRooms_noResult() {
        when(meetingRoomMapper.selectList(any())).thenReturn(List.of(
                room(2, "洽谈室A", "1楼", 6, "[\"tv\"]")
        ));

        ChatBotAnswerVO vo = chatBotService.ask(2L, "明天下午2点到4点，能坐10人的会议室有哪些");

        assertEquals("find_available_rooms", vo.getIntent());
        assertTrue(vo.getRooms() == null || vo.getRooms().isEmpty());
        assertTrue(vo.getAnswer().contains("没有找到"));
    }

    // ================= 意图二：查我的预约 =================

    @Test
    @DisplayName("查我的预约：应调用预约服务并返回预约卡片")
    void ask_myBookings_shouldReturnCards() {
        BookingVO b = new BookingVO();
        b.setId(9L);
        b.setBookingNo("BK20260916001");
        b.setRoomName("创新厅");
        b.setBookingDate(LocalDate.now().plusDays(1));
        b.setStartTime(LocalTime.of(9, 0));
        b.setEndTime(LocalTime.of(10, 0));
        b.setTopic("周会");
        b.setStatus("confirmed");
        b.setStatusName("已确认");

        when(bookingService.getMyBookings(eq(2L), any(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(b), 1L, 1, 10));

        ChatBotAnswerVO vo = chatBotService.ask(2L, "我最近有哪些预约");

        assertEquals("my_bookings", vo.getIntent());
        assertNotNull(vo.getBookings());
        assertEquals(1, vo.getBookings().size());
        assertTrue(vo.getAnswer().contains("周会"));
        verify(bookingService, times(1)).getMyBookings(eq(2L), any(), anyInt(), anyInt());
    }

    // ================= 意图三：查会议室详情 =================

    @Test
    @DisplayName("查会议室详情：应命中会议室并返回位置、容量、设备")
    void ask_roomInfo_shouldReturnDetail() {
        MeetingRoom r = room(1, "创新厅", "3楼", 20, "[\"projector\"]");
        when(meetingRoomMapper.selectList(any())).thenReturn(List.of(r));
        when(meetingRoomService.getRoomDetail(1L)).thenReturn(roomVO(1, "创新厅", "3楼", 20));

        ChatBotAnswerVO vo = chatBotService.ask(2L, "创新厅在哪，能坐多少人");

        assertEquals("room_info", vo.getIntent());
        assertTrue(vo.getAnswer().contains("创新厅"));
        assertTrue(vo.getAnswer().contains("20"));
    }

    // ================= 参数白名单校验 =================

    @Test
    @DisplayName("人数超过上限应被白名单拦截")
    void ask_capacityOutOfRange_shouldReject() {
        ChatBotAnswerVO vo = chatBotService.ask(2L, "帮我找一间能坐99人的会议室");

        assertFalse(vo.getSuccess());
        assertTrue(vo.getAnswer().contains("人数"));
    }

    @Test
    @DisplayName("设备白名单：非法设备应被拒绝")
    void validate_unknownEquipment_shouldReject() {
        ChatIntent it = new ChatIntent();
        it.setIntent(ChatIntent.FIND_ROOMS);
        it.setEquipment(List.of("按摩椅"));

        assertThrows(BusinessException.class, () -> validate(it));
    }

    @Test
    @DisplayName("设备白名单：合法设备别名应被规范化为编码")
    void validate_equipmentAlias_shouldNormalize() {
        ChatIntent it = new ChatIntent();
        it.setIntent(ChatIntent.FIND_ROOMS);
        it.setEquipment(List.of("投影仪", "白板"));

        ChatIntent out = validate(it);

        assertTrue(out.getEquipment().contains("projector"));
        assertTrue(out.getEquipment().contains("whiteboard"));
    }

    @Test
    @DisplayName("时间白名单：结束早于开始应被拒绝")
    void validate_timeRange_shouldReject() {
        ChatIntent it = new ChatIntent();
        it.setIntent(ChatIntent.FIND_ROOMS);
        it.setStartTime("16:00");
        it.setEndTime("14:00");

        assertThrows(BusinessException.class, () -> validate(it));
    }

    @Test
    @DisplayName("日期白名单：早于今天应被纠正为今天")
    void validate_pastDate_shouldClampToToday() {
        ChatIntent it = new ChatIntent();
        it.setIntent(ChatIntent.FIND_ROOMS);
        it.setDate(LocalDate.now().minusDays(5).toString());

        ChatIntent out = validate(it);

        assertEquals(LocalDate.now().toString(), out.getDate());
    }

    @Test
    @DisplayName("日期白名单：超出可预约天数应被纠正到上限")
    void validate_farDate_shouldClampToLimit() {
        ChatIntent it = new ChatIntent();
        it.setIntent(ChatIntent.FIND_ROOMS);
        it.setDate(LocalDate.now().plusDays(365).toString());

        ChatIntent out = validate(it);

        LocalDate limit = LocalDate.now().plusDays(14); // booking.advance_days 默认 14
        assertEquals(limit.toString(), out.getDate());
    }

    // ================= 未知意图 =================

    @Test
    @DisplayName("无法理解的问题应返回引导文案")
    void ask_unknown_shouldGuide() {
        when(meetingRoomMapper.selectList(any())).thenReturn(List.of());

        ChatBotAnswerVO vo = chatBotService.ask(2L, "你好啊");

        assertEquals("unknown", vo.getIntent());
        assertTrue(vo.getAnswer().contains("可以帮您"));
        assertEquals("unknown_intent", vo.getFallback());
    }

    // ================= 限流 =================

    @Test
    @DisplayName("超过每分钟限流应返回失败提示")
    void ask_rateLimited() {
        props.setRateLimitPerMinute(2);
        when(meetingRoomMapper.selectList(any())).thenReturn(List.of());

        chatBotService.ask(777L, "你好");
        chatBotService.ask(777L, "你好");
        ChatBotAnswerVO third = chatBotService.ask(777L, "你好");

        assertFalse(third.getSuccess());
        assertTrue(third.getAnswer().contains("频繁"));
    }

    // ================= 全链路异常降级 =================

    @Test
    @DisplayName("底层查询异常时应返回降级文案且 success=false")
    void ask_exception_shouldDegrade() {
        when(bookingService.getMyBookings(any(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("DB down"));

        ChatBotAnswerVO vo = chatBotService.ask(2L, "我最近有哪些预约");

        assertFalse(vo.getSuccess());
        assertEquals("degraded", vo.getFallback());
        assertNotNull(vo.getAnswer());
    }

    // ================= 日志落库 =================

    @Test
    @DisplayName("每次提问都应写入问答日志")
    void ask_shouldWriteLog() {
        when(meetingRoomMapper.selectList(any())).thenReturn(List.of());

        chatBotService.ask(2L, "你好");

        verify(chatbotLogMapper, times(1)).insert(argThat(log ->
                log.getUserId().equals(2L) && log.getQuestion().equals("你好") && log.getCostMs() != null
        ));
    }
}
