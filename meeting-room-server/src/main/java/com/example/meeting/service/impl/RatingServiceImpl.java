package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.common.exception.BusinessException;
import com.example.meeting.common.result.ResultCode;
import com.example.meeting.entity.Booking;
import com.example.meeting.entity.RepairOrder;
import com.example.meeting.entity.RoomRating;
import com.example.meeting.entity.RoomRatingCheck;
import com.example.meeting.mapper.BookingMapper;
import com.example.meeting.mapper.RoomRatingCheckMapper;
import com.example.meeting.mapper.RoomRatingMapper;
import com.example.meeting.service.RatingService;
import com.example.meeting.service.RepairOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 会议室评价服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl extends ServiceImpl<RoomRatingMapper, RoomRating> implements RatingService {

    private final RoomRatingMapper ratingMapper;
    private final RoomRatingCheckMapper checkMapper;
    private final BookingMapper bookingMapper;
    private final RepairOrderService repairOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRating(Long userId, Long bookingId, int rating, String tags, String comment) {
        // 1. 校验评分范围
        if (rating < 1 || rating > 5) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "评分必须在1-5之间");
        }

        // 2. 校验预约存在且属于当前用户
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(ResultCode.BOOKING_NOT_FOUND);
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 3. 校验预约状态为已完成
        if (!"completed".equals(booking.getStatus())) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "只有已完成的预约可以评价");
        }

        // 4. 校验24小时窗口：会议结束时间后24小时内
        LocalDateTime bookingEndTime = LocalDateTime.of(booking.getBookingDate(), booking.getEndTime());
        if (LocalDateTime.now().isAfter(bookingEndTime.plusHours(24))) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "评价窗口已过（会议结束后24小时内）");
        }

        // 5. 幂等检查：同一用户对同一预约只能评价一次
        Long count = lambdaQuery()
                .eq(RoomRating::getBookingId, bookingId)
                .eq(RoomRating::getUserId, userId)
                .count();
        if (count > 0) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "您已经评价过该预约");
        }

        // 6. 保存评价
        RoomRating roomRating = new RoomRating();
        roomRating.setBookingId(bookingId);
        roomRating.setRoomId(booking.getRoomId());
        roomRating.setUserId(userId);
        roomRating.setRating(rating);
        roomRating.setTags(tags);
        roomRating.setComment(comment);
        save(roomRating);

        log.info("用户{}评价预约{}: {}星", userId, bookingId, rating);

        // 7. 评分≤3星自动生成低分检查记录
        if (rating <= 3) {
            RoomRatingCheck check = new RoomRatingCheck();
            check.setRatingId(roomRating.getId());
            check.setRoomId(booking.getRoomId());
            check.setStatus("pending");
            check.setRemark("评分" + rating + "星，自动触发");
            checkMapper.insert(check);
            log.info("低分评价{}触发检查记录, roomId={}", roomRating.getId(), booking.getRoomId());
        }
    }

    @Override
    public List<Map<String, Object>> getRoomRatings(Long roomId, int limit) {
        return ratingMapper.selectRatingsWithUser(roomId, limit);
    }

    @Override
    public Double getRoomAvgRating(Long roomId) {
        Double avg = ratingMapper.selectAvgRating(roomId);
        return avg != null ? avg : 0.0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void convertToRepair(Long checkId, Long adminId) {
        // 1. 查询低分检查记录
        RoomRatingCheck check = checkMapper.selectById(checkId);
        if (check == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "检查记录不存在");
        }
        if ("converted".equals(check.getStatus())) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "该记录已转报修");
        }

        // 2. 查询评价详情
        RoomRating rating = getById(check.getRatingId());
        String ratingComment = rating != null ? rating.getComment() : "";

        // 3. 创建报修工单
        RepairOrder repair = new RepairOrder();
        repair.setRoomId(check.getRoomId());
        repair.setReporterId(adminId);
        repair.setDeviceType("other");
        repair.setFaultDesc("评价低分触发：" + (ratingComment != null ? ratingComment : "用户反馈评分较低"));
        repair.setStatus("pending");
        repair.setOrderNo("RP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        repairOrderService.save(repair);

        // 4. 更新检查记录状态
        check.setStatus("converted");
        check.setRepairId(repair.getId());
        checkMapper.updateById(check);

        log.info("低分检查记录{}转报修, repairId={}", checkId, repair.getId());
    }

    @Override
    public List<Map<String, Object>> getPendingChecks() {
        return checkMapper.selectPendingWithDetail("pending");
    }

    @Override
    public List<Map<String, Object>> getRoomRatingStats() {
        return ratingMapper.selectRoomRatingStats();
    }

    @Override
    public List<Map<String, Object>> getRatingDistribution(Long roomId) {
        return ratingMapper.selectRatingDistribution(roomId);
    }
}
