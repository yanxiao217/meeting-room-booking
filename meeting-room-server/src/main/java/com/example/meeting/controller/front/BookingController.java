package com.example.meeting.controller.front;

import com.example.meeting.common.result.PageResult;
import com.example.meeting.common.result.Result;
import com.example.meeting.dto.BookingCancelDTO;
import com.example.meeting.dto.BookingCreateDTO;
import com.example.meeting.dto.BookingUpdateDTO;
import com.example.meeting.entity.Booking;
import com.example.meeting.service.BookingService;
import com.example.meeting.service.QrCodeService;
import com.example.meeting.vo.AvailabilityVO;
import com.example.meeting.vo.BookingVO;
import com.example.meeting.vo.CalendarVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 预约接口
 */
@Tag(name = "预约接口")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final QrCodeService qrCodeService;

    @Operation(summary = "创建预约")
    @PostMapping
    public Result<BookingVO> createBooking(@Valid @RequestBody BookingCreateDTO dto) {
        Long userId = getCurrentUserId();
        BookingVO bookingVO = bookingService.createBooking(userId, dto);
        return Result.success(bookingVO);
    }

    @Operation(summary = "修改预约")
    @PutMapping("/{id}")
    public Result<BookingVO> updateBooking(@PathVariable Long id,
                                           @Valid @RequestBody BookingUpdateDTO dto) {
        Long userId = getCurrentUserId();
        BookingVO bookingVO = bookingService.updateBooking(userId, id, dto);
        return Result.success(bookingVO);
    }

    @Operation(summary = "取消预约")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelBooking(@PathVariable Long id,
                                      @RequestBody BookingCancelDTO dto) {
        Long userId = getCurrentUserId();
        bookingService.cancelBooking(userId, id, dto);
        return Result.success();
    }

    @Operation(summary = "我的预约列表")
    @GetMapping("/mine")
    public Result<PageResult<BookingVO>> getMyBookings(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        PageResult<BookingVO> result = bookingService.getMyBookings(userId, status, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "预约详情")
    @GetMapping("/{id}")
    public Result<BookingVO> getBookingDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        BookingVO bookingVO = bookingService.getBookingDetail(userId, id);
        return Result.success(bookingVO);
    }

    @Operation(summary = "手动签到")
    @PostMapping("/{id}/sign")
    public Result<Void> signBooking(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        bookingService.signBooking(userId, id, "manual");
        return Result.success();
    }

    // ── 二维码签到 ──────────────────────────────────────────────

    @Operation(summary = "获取签到二维码token")
    @GetMapping("/{id}/qrcode")
    public Result<Map<String, String>> getQrCode(@PathVariable Long id) {
        Long userId = getCurrentUserId();

        Booking booking = qrCodeService.validateAndGetBooking(id, userId);
        String token = qrCodeService.generateToken(booking);

        return Result.success(Map.of("token", token));
    }

    @Operation(summary = "扫码校验（获取会议信息）")
    @GetMapping("/scan/{token}")
    public Result<BookingVO> scanQrCode(@PathVariable String token) {
        Booking booking = qrCodeService.validateToken(token);
        BookingVO vo = qrCodeService.getQrBookingInfo(booking);
        return Result.success(vo);
    }

    @Operation(summary = "二维码签到")
    @PostMapping("/sign-by-qrcode")
    public Result<Void> signByQrCode(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return Result.error(400, "缺少二维码token");
        }

        Booking booking = qrCodeService.validateToken(token);

        // 校验当前用户是否为预约人
        if (!booking.getUserId().equals(userId)) {
            return Result.error(403, "您不是该预约的申请人");
        }

        bookingService.signBooking(userId, booking.getId(), "qrcode");
        return Result.success();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}
