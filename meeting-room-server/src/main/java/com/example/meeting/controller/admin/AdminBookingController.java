package com.example.meeting.controller.admin;

import com.example.meeting.common.result.PageResult;
import com.example.meeting.common.result.Result;
import com.example.meeting.dto.AdminBookingDTO;
import com.example.meeting.dto.BookingCancelDTO;
import com.example.meeting.dto.BookingQueryDTO;
import com.example.meeting.entity.User;
import com.example.meeting.service.BookingService;
import com.example.meeting.vo.BookingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理 - 预约管理
 */
@Tag(name = "后台管理 - 预约管理")
@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    @Operation(summary = "全部预约列表")
    @GetMapping
    public Result<PageResult<BookingVO>> getAllBookings(BookingQueryDTO dto) {
        PageResult<BookingVO> result = bookingService.getAllBookings(dto);
        return Result.success(result);
    }

    @Operation(summary = "强制取消预约")
    @PutMapping("/{id}/force-cancel")
    public Result<Void> forceCancelBooking(@PathVariable Long id,
                                           @RequestBody BookingCancelDTO dto) {
        Long adminId = getCurrentUserId();
        bookingService.forceCancelBooking(adminId, id, dto.getReason());
        return Result.success();
    }

    @Operation(summary = "审批通过")
    @PutMapping("/{id}/approve")
    public Result<BookingVO> approveBooking(@PathVariable Long id) {
        Long adminId = getCurrentUserId();
        BookingVO bookingVO = bookingService.approveBooking(adminId, id);
        return Result.success(bookingVO);
    }

    @Operation(summary = "审批拒绝")
    @PutMapping("/{id}/reject")
    public Result<Void> rejectBooking(@PathVariable Long id,
                                      @RequestBody(required = false) BookingCancelDTO dto) {
        Long adminId = getCurrentUserId();
        bookingService.rejectBooking(adminId, id, dto != null ? dto.getReason() : null);
        return Result.success();
    }

    @Operation(summary = "代客预约")
    @PostMapping("/create-by-admin")
    public Result<BookingVO> createBookingByAdmin(@Valid @RequestBody AdminBookingDTO dto) {
        Long adminId = getCurrentUserId();
        BookingVO bookingVO = bookingService.createBookingByAdmin(adminId, dto);
        return Result.success(bookingVO);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}
