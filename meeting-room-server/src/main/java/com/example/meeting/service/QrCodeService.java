package com.example.meeting.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.meeting.common.constant.BookingConstant;
import com.example.meeting.common.exception.BusinessException;
import com.example.meeting.common.result.ResultCode;
import com.example.meeting.entity.Booking;
import com.example.meeting.entity.MeetingRoom;
import com.example.meeting.entity.SysConfig;
import com.example.meeting.entity.User;
import com.example.meeting.mapper.BookingMapper;
import com.example.meeting.mapper.MeetingRoomMapper;
import com.example.meeting.mapper.SysConfigMapper;
import com.example.meeting.mapper.UserMapper;
import com.example.meeting.vo.BookingVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 签到二维码服务 — AES 加密/解密 token
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final BookingMapper bookingMapper;
    private final MeetingRoomMapper meetingRoomMapper;
    private final UserMapper userMapper;
    private final SysConfigMapper sysConfigMapper;

    @Value("${qrcode.secret}")
    private String secret;

    /**
     * 生成签到二维码 token
     * 内容: bookingNo|过期时间戳(毫秒)
     */
    public String generateToken(Booking booking) {
        long expireAt = computeExpireAt(booking).toEpochMilli();
        String plain = booking.getBookingNo() + "|" + expireAt;
        return encrypt(plain);
    }

    /**
     * 校验 token，返回解密后的 Booking
     */
    public Booking validateToken(String token) {
        String plain;
        try {
            plain = decrypt(token);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "二维码无效");
        }

        String[] parts = plain.split("\\|");
        if (parts.length != 2) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "二维码格式错误");
        }

        String bookingNo = parts[0];
        long expireAt;
        try {
            expireAt = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "二维码已损坏");
        }

        // 查询预约
        Booking booking = bookingMapper.selectOne(
                new LambdaQueryWrapper<Booking>().eq(Booking::getBookingNo, bookingNo));
        if (booking == null) {
            throw new BusinessException(ResultCode.BOOKING_NOT_FOUND);
        }

        // 校验有效期
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = Instant.ofEpochMilli(expireAt).atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (now.isAfter(expireTime)) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "二维码已过期");
        }

        return booking;
    }

    /**
     * 校验获取二维码的权限（仅预约人、confirmed、未签到）
     */
    public Booking validateAndGetBooking(Long bookingId, Long userId) {
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(ResultCode.BOOKING_NOT_FOUND);
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!BookingConstant.STATUS_CONFIRMED.equals(booking.getStatus())) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "仅已确认且未签到的预约可获取二维码");
        }
        return booking;
    }

    /**
     * 获取二维码展示信息（会议室名 + 时段 + 主题）
     */
    public BookingVO getQrBookingInfo(Booking booking) {
        MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());
        User user = userMapper.selectById(booking.getUserId());

        BookingVO vo = new BookingVO();
        vo.setId(booking.getId());
        vo.setBookingNo(booking.getBookingNo());
        vo.setRoomId(booking.getRoomId());
        vo.setRoomName(room != null ? room.getRoomName() : "");
        vo.setUserId(booking.getUserId());
        vo.setUserName(user != null ? user.getRealName() : "");
        vo.setBookingDate(booking.getBookingDate());
        vo.setStartTime(booking.getStartTime());
        vo.setEndTime(booking.getEndTime());
        vo.setTopic(booking.getTopic());
        vo.setStatus(booking.getStatus());
        vo.setStatusName(resolveStatusName(booking.getStatus()));
        return vo;
    }

    // ── 内部方法 ───────────────────────────────────────────────

    /**
     * 计算二维码过期时间 = 会议开始后自动释放超时
     */
    private Instant computeExpireAt(Booking booking) {
        LocalDateTime bookingStart = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        int releaseMin = 15;
        try {
            SysConfig cfg = sysConfigMapper.selectByConfigKey("booking.auto_release_min");
            if (cfg != null && cfg.getConfigValue() != null) {
                releaseMin = Integer.parseInt(cfg.getConfigValue());
            }
        } catch (Exception ignored) {}
        return bookingStart.plusMinutes(releaseMin).atZone(ZoneId.systemDefault()).toInstant();
    }

    private String encrypt(String plain) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAILED.getCode(), "二维码生成失败");
        }
    }

    private String decrypt(String token) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getUrlDecoder().decode(token);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "二维码无效");
        }
    }

    private String resolveStatusName(String status) {
        if (status == null) return "";
        return switch (status) {
            case "pending" -> "待审批";
            case "confirmed" -> "已确认";
            case "signed" -> "已签到";
            case "completed" -> "已完成";
            case "cancelled" -> "已取消";
            case "expired" -> "已过期";
            default -> status;
        };
    }
}
