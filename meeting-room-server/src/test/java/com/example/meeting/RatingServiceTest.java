package com.example.meeting;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.common.exception.BusinessException;
import com.example.meeting.entity.Booking;
import com.example.meeting.entity.RoomRating;
import com.example.meeting.entity.RoomRatingCheck;
import com.example.meeting.mapper.BookingMapper;
import com.example.meeting.mapper.RoomRatingCheckMapper;
import com.example.meeting.mapper.RoomRatingMapper;
import com.example.meeting.service.RepairOrderService;
import com.example.meeting.service.impl.RatingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RatingService 单元测试
 * 覆盖：唯一评价约束、低分转报修、状态/时间/权限校验
 */
@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RoomRatingMapper ratingMapper;
    @Mock
    private RoomRatingCheckMapper checkMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private RepairOrderService repairOrderService;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private Booking completedBooking;

    @BeforeEach
    void setUp() {
        completedBooking = new Booking();
        completedBooking.setId(1L);
        completedBooking.setRoomId(10L);
        completedBooking.setUserId(100L);
        completedBooking.setBookingDate(LocalDate.now().minusDays(1));
        completedBooking.setStartTime(LocalTime.of(9, 0));
        completedBooking.setEndTime(LocalTime.of(10, 0));
        completedBooking.setStatus("completed");
    }

    // ======== 唯一评价约束 ========

    @Test
    @DisplayName("同一用户对同一预约只能评价一次——重复提交应抛异常")
    void submitRating_duplicateShouldThrow() {
        when(bookingMapper.selectById(1L)).thenReturn(completedBooking);
        // 模拟已有评价记录
        when(ratingMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> ratingService.submitRating(100L, 1L, 5, null, "很好"));
        assertTrue(ex.getMessage().contains("已经评价"));
    }

    @Test
    @DisplayName("首次评价应成功，且低分(≤3)自动生成检查记录")
    void submitRating_firstTimeLowRatingCreatesCheck() {
        when(bookingMapper.selectById(1L)).thenReturn(completedBooking);
        when(ratingMapper.selectCount(any())).thenReturn(0L);
        when(ratingMapper.insert(any())).thenReturn(1);

        // 2星评价
        ratingService.submitRating(100L, 1L, 2, "[\"noise\"]", "隔音太差了");

        verify(ratingMapper, times(1)).insert(argThat(r ->
                r.getRating() == 2 && r.getBookingId().equals(1L)
        ));
        // 低分应自动生成检查记录
        verify(checkMapper, times(1)).insert(argThat(c ->
                "pending".equals(c.getStatus()) && c.getRoomId().equals(10L)
        ));
    }

    @Test
    @DisplayName("高分评价(4-5)不应生成检查记录")
    void submitRating_highRatingNoCheck() {
        when(bookingMapper.selectById(1L)).thenReturn(completedBooking);
        when(ratingMapper.selectCount(any())).thenReturn(0L);
        when(ratingMapper.insert(any())).thenReturn(1);

        ratingService.submitRating(100L, 1L, 5, null, "非常满意");

        verify(checkMapper, never()).insert(any());
    }

    // ======== 状态校验 ========

    @Test
    @DisplayName("非已完成预约不能评价")
    void submitRating_notCompletedShouldThrow() {
        Booking confirmedBooking = new Booking();
        confirmedBooking.setId(2L);
        confirmedBooking.setUserId(100L);
        confirmedBooking.setStatus("confirmed");
        when(bookingMapper.selectById(2L)).thenReturn(confirmedBooking);

        assertThrows(BusinessException.class,
                () -> ratingService.submitRating(100L, 2L, 4, null, null));
    }

    @Test
    @DisplayName("非本人预约不能评价")
    void submitRating_notOwnerShouldThrow() {
        when(bookingMapper.selectById(1L)).thenReturn(completedBooking);

        assertThrows(BusinessException.class,
                () -> ratingService.submitRating(999L, 1L, 4, null, null));
    }

    // ======== 低分转报修 ========

    @Test
    @DisplayName("低分记录转报修应成功创建repair_order并更新状态")
    void convertToRepair_shouldCreateRepairAndUpdateCheck() {
        RoomRatingCheck check = new RoomRatingCheck();
        check.setId(1L);
        check.setRatingId(10L);
        check.setRoomId(10L);
        check.setStatus("pending");

        RoomRating rating = new RoomRating();
        rating.setComment("投影仪不亮");

        when(checkMapper.selectById(1L)).thenReturn(check);
        when(ratingMapper.selectById(10L)).thenReturn(rating);
        when(repairOrderService.save(any())).thenReturn(true);

        ratingService.convertToRepair(1L, 1L);

        // 应创建报修工单
        verify(repairOrderService, times(1)).save(argThat(r ->
                "other".equals(r.getDeviceType()) &&
                r.getRoomId().equals(10L) &&
                r.getFaultDesc().contains("投影仪不亮")
        ));
        // 应更新检查记录状态
        verify(checkMapper, times(1)).updateById(argThat(c ->
                "converted".equals(c.getStatus()) && c.getId().equals(1L)
        ));
    }

    @Test
    @DisplayName("已转报修的记录不能重复转换")
    void convertToRepair_alreadyConvertedShouldThrow() {
        RoomRatingCheck check = new RoomRatingCheck();
        check.setId(2L);
        check.setStatus("converted");
        when(checkMapper.selectById(2L)).thenReturn(check);

        assertThrows(BusinessException.class,
                () -> ratingService.convertToRepair(2L, 1L));
    }

    // ======== 评分范围 ========

    @Test
    @DisplayName("评分超出1-5范围应抛异常")
    void submitRating_invalidRatingShouldThrow() {
        assertThrows(BusinessException.class,
                () -> ratingService.submitRating(100L, 1L, 6, null, null));
        assertThrows(BusinessException.class,
                () -> ratingService.submitRating(100L, 1L, 0, null, null));
    }
}
