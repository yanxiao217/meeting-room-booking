package com.example.meeting;

import com.example.meeting.config.ChatBotProperties;
import com.example.meeting.dto.ChatIntent;
import com.example.meeting.entity.ChatbotLog;
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
import com.example.meeting.vo.RoomVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 智能问答准确性抽检（20 条）
 * 通过捕获落库日志中的 params，校验意图与关键参数解析是否准确。
 * 不依赖大模型：api-key 为空，走规则解析。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatBotAccuracyTest {

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
        props.setApiKey("");
        props.setRateLimitPerMinute(100);
        ReflectionTestUtils.setField(chatBotService, "props", props);

        when(sysConfigMapper.selectByConfigKey(anyString())).thenReturn(null);
        when(meetingRoomMapper.selectList(any())).thenReturn(List.of(
                room(1, "创新厅", "3楼", 20),
                room(2, "洽谈室A", "1楼", 6),
                room(4, "培训室", "2楼", 30),
                room(7, "贵宾室", "3楼", 12)
        ));
        when(bookingMapper.countConflictBookings(anyLong(), any(), any(), any(), anyLong())).thenReturn(0);
        when(bookingService.getMyBookings(any(), any(), anyInt(), anyInt()))
                .thenReturn(new com.example.meeting.common.result.PageResult<>(List.<BookingVO>of(), 0L, 1, 10));
        when(meetingRoomService.getRoomDetail(anyLong())).thenAnswer(inv -> roomVO(inv.getArgument(0)));
    }

    private MeetingRoom room(long id, String name, String floor, int capacity) {
        MeetingRoom r = new MeetingRoom();
        r.setId(id);
        r.setRoomName(name);
        r.setFloor(floor);
        r.setLocation(floor + "东侧");
        r.setCapacity(capacity);
        r.setEquipment("[\"projector\",\"whiteboard\"]");
        r.setStatus(1);
        return r;
    }

    private RoomVO roomVO(long id) {
        RoomVO vo = new RoomVO();
        vo.setId(id);
        vo.setRoomName("创新厅");
        vo.setFloor("3楼");
        vo.setCapacity(20);
        vo.setEquipment(List.of("projector"));
        return vo;
    }

    /** 提问并解析出落库的意图参数 */
    private ChatIntent parse(String question) throws Exception {
        reset(chatbotLogMapper);
        chatBotService.ask(2L, question);
        ArgumentCaptor<ChatbotLog> cap = ArgumentCaptor.forClass(ChatbotLog.class);
        verify(chatbotLogMapper, times(1)).insert(cap.capture());
        String params = cap.getValue().getParams();
        assertNotNull(params, "参数未落库: " + question);
        return objectMapper.readValue(params, ChatIntent.class);
    }

    private String today(int plus) {
        return LocalDate.now().plusDays(plus).toString();
    }

    @Test
    @DisplayName("抽检20条：意图与关键参数解析准确性")
    void accuracy_20_samples() throws Exception {
        int pass = 0;

        // ---- 1~8 查空闲会议室 ----
        ChatIntent c1 = parse("明天下午2点到4点，能坐10人、有投影仪的会议室有哪些");
        assertEquals("find_available_rooms", c1.getIntent());
        assertEquals(today(1), c1.getDate());
        assertEquals("14:00", c1.getStartTime());
        assertEquals("16:00", c1.getEndTime());
        assertEquals(10, c1.getCapacity());
        assertTrue(c1.getEquipment().contains("projector"));
        pass++;

        ChatIntent c2 = parse("今天下午3点有空会议室吗");
        assertEquals("find_available_rooms", c2.getIntent());
        assertEquals(today(0), c2.getDate());
        assertEquals("15:00", c2.getStartTime());
        pass++;

        ChatIntent c3 = parse("帮我找一间能坐20人的会议室");
        assertEquals("find_available_rooms", c3.getIntent());
        assertEquals(20, c3.getCapacity());
        pass++;

        ChatIntent c4 = parse("后天上午10点到11点半，有白板的会议室");
        assertEquals("find_available_rooms", c4.getIntent());
        assertEquals(today(2), c4.getDate());
        assertEquals("10:00", c4.getStartTime());
        assertEquals("11:30", c4.getEndTime());
        assertTrue(c4.getEquipment().contains("whiteboard"));
        pass++;

        ChatIntent c5 = parse("3楼有没有空闲的会议室");
        assertEquals("find_available_rooms", c5.getIntent());
        assertEquals("3", c5.getFloor());
        pass++;

        ChatIntent c6 = parse("推荐一间有视频会议的会议室");
        assertEquals("find_available_rooms", c6.getIntent());
        assertTrue(c6.getEquipment().contains("video_conference"));
        pass++;

        ChatIntent c7 = parse("9月20日下午2点到3点，找一间能坐6人的会议室");
        assertEquals("find_available_rooms", c7.getIntent());
        assertEquals(LocalDate.now().getYear() + "-09-20", c7.getDate());
        assertEquals("14:00", c7.getStartTime());
        assertEquals(6, c7.getCapacity());
        pass++;

        ChatIntent c8 = parse("周五上午9点到10点有哪些空会议室");
        assertEquals("find_available_rooms", c8.getIntent());
        assertEquals("09:00", c8.getStartTime());
        assertEquals("10:00", c8.getEndTime());
        pass++;

        // ---- 9~13 查我的预约 ----
        assertEquals("my_bookings", parse("我最近有哪些预约").getIntent());
        pass++;
        assertEquals("my_bookings", parse("我的预约").getIntent());
        pass++;
        assertEquals("my_bookings", parse("帮我查一下我订的会议").getIntent());
        pass++;

        ChatIntent c12 = parse("我已完成的预约有哪些");
        assertEquals("my_bookings", c12.getIntent());
        assertEquals("completed", c12.getStatus());
        pass++;

        ChatIntent c13 = parse("我取消过的预约");
        assertEquals("my_bookings", c13.getIntent());
        assertEquals("cancelled", c13.getStatus());
        pass++;

        // ---- 14~18 查会议室详情 ----
        ChatIntent c14 = parse("创新厅在哪");
        assertEquals("room_info", c14.getIntent());
        assertEquals("创新厅", c14.getKeyword());
        pass++;

        ChatIntent c15 = parse("洽谈室A能坐多少人");
        assertEquals("room_info", c15.getIntent());
        assertEquals("洽谈室A", c15.getKeyword());
        pass++;

        ChatIntent c16 = parse("培训室的位置和设备");
        assertEquals("room_info", c16.getIntent());
        assertEquals("培训室", c16.getKeyword());
        pass++;

        ChatIntent c17 = parse("贵宾室怎么样");
        assertEquals("room_info", c17.getIntent());
        assertEquals("贵宾室", c17.getKeyword());
        pass++;

        ChatIntent c18 = parse("创新厅能容纳多少人");
        assertEquals("room_info", c18.getIntent());
        assertEquals("创新厅", c18.getKeyword());
        pass++;

        // ---- 19~20 未知意图 ----
        assertEquals("unknown", parse("你好").getIntent());
        pass++;
        assertEquals("unknown", parse("谢谢你的帮助").getIntent());
        pass++;

        assertEquals(20, pass, "抽检应覆盖20条");
        System.out.println("【智能问答抽检】20/20 条意图与关键参数解析正确");
    }
}
