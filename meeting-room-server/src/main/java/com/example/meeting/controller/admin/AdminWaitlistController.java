package com.example.meeting.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.meeting.common.result.PageResult;
import com.example.meeting.common.result.Result;
import com.example.meeting.entity.BookingWaitlist;
import com.example.meeting.service.BookingWaitlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 后台管理 - 候补管理
 */
@Tag(name = "后台管理 - 候补管理")
@RestController
@RequestMapping("/api/v1/admin/waitlist")
@RequiredArgsConstructor
public class AdminWaitlistController {

    private final BookingWaitlistService waitlistService;

    @Operation(summary = "候补队列管理")
    @GetMapping
    public Result<PageResult<BookingWaitlist>> getWaitlist(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String bookingDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<BookingWaitlist> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BookingWaitlist> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) {
            wrapper.eq(BookingWaitlist::getRoomId, roomId);
        }
        if (bookingDate != null && !bookingDate.isEmpty()) {
            wrapper.eq(BookingWaitlist::getBookingDate, LocalDate.parse(bookingDate));
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BookingWaitlist::getStatus, status);
        } else if (Boolean.TRUE.equals(activeOnly)) {
            // 进行中候补：等待/已通知/已确认
            wrapper.in(BookingWaitlist::getStatus, "waiting", "notified", "confirmed");
        } else if (Boolean.FALSE.equals(activeOnly)) {
            // 历史候补：已放弃/已超时
            wrapper.in(BookingWaitlist::getStatus, "cancelled", "expired");
        }

        // 时间范围：true=仅未结束 false=仅已结束 不传=全部
        // 已结束：booking_date < today OR (booking_date = today AND end_time <= now)
        if (activeOnly != null) {
            String today = LocalDate.now().toString();
            String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            if (Boolean.TRUE.equals(activeOnly)) {
                wrapper.and(w -> w.gt(BookingWaitlist::getBookingDate, today)
                        .or(w2 -> w2.eq(BookingWaitlist::getBookingDate, today).gt(BookingWaitlist::getEndTime, nowTime)));
            } else {
                wrapper.and(w -> w.lt(BookingWaitlist::getBookingDate, today)
                        .or(w2 -> w2.eq(BookingWaitlist::getBookingDate, today).le(BookingWaitlist::getEndTime, nowTime)));
            }
        }

        wrapper.orderByAsc(BookingWaitlist::getRankNo);
        waitlistService.page(page, wrapper);
        return Result.success(new PageResult<>(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @Operation(summary = "手动补位")
    @PostMapping("/{id}/promote")
    public Result<Void> promoteWaitlist(@PathVariable Long id) {
        BookingWaitlist waitlist = waitlistService.getById(id);
        if (waitlist == null) {
            return Result.error("候补记录不存在");
        }
        waitlist.setStatus("notified");
        waitlist.setNotifyTime(java.time.LocalDateTime.now());
        waitlist.setConfirmDeadline(java.time.LocalDateTime.now().plusMinutes(10));
        waitlistService.updateById(waitlist);
        return Result.success();
    }
}
