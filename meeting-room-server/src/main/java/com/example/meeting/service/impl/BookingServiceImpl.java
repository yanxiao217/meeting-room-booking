package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.common.constant.BookingConstant;
import com.example.meeting.common.exception.BusinessException;
import com.example.meeting.common.result.PageResult;
import com.example.meeting.common.result.ResultCode;
import com.example.meeting.dto.AdminBookingDTO;
import com.example.meeting.dto.BookingCancelDTO;
import com.example.meeting.dto.BookingCreateDTO;
import com.example.meeting.dto.BookingQueryDTO;
import com.example.meeting.dto.BookingUpdateDTO;
import com.example.meeting.entity.Booking;
import com.example.meeting.entity.MeetingRoom;
import com.example.meeting.entity.SysConfig;
import com.example.meeting.entity.User;
import com.example.meeting.entity.WorkCalendar;
import com.example.meeting.mapper.BookingMapper;
import com.example.meeting.mapper.MeetingRoomMapper;
import com.example.meeting.mapper.SysConfigMapper;
import com.example.meeting.mapper.UserMapper;
import com.example.meeting.mapper.WorkCalendarMapper;
import com.example.meeting.service.BookingService;
import com.example.meeting.service.CreditService;
import com.example.meeting.service.DeptQuotaService;
import com.example.meeting.service.NotificationService;
import com.example.meeting.vo.AvailabilityVO;
import com.example.meeting.vo.BookingVO;
import com.example.meeting.vo.CalendarVO;
import com.example.meeting.vo.RoomCalendarVO;
import com.example.meeting.vo.TimeSlotVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 预约服务实现
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {

    private final MeetingRoomMapper meetingRoomMapper;
    private final UserMapper userMapper;
    private final WorkCalendarMapper workCalendarMapper;
    private final SysConfigMapper sysConfigMapper;
    private final NotificationService notificationService;
    private final CreditService creditService;
    private final DeptQuotaService deptQuotaService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingVO createBooking(Long userId, BookingCreateDTO dto) {
        // 1. 校验会议室是否存在
        MeetingRoom room = meetingRoomMapper.selectById(dto.getRoomId());
        if (room == null || room.getStatus() != 1) {
            throw new BusinessException(ResultCode.ROOM_UNAVAILABLE);
        }

        // 2. 校验日期是否可预约
        validateBookingDate(dto.getBookingDate());

        // 3. 校验时间
        validateBookingTime(dto.getStartTime(), dto.getEndTime());

        // 4. 校验规则
        validateBookingRules(userId, dto.getBookingDate(), dto.getStartTime(), dto.getEndTime());

        // 4.5 部门配额校验：本部门周/月额度用尽则拦截
        User user = userMapper.selectById(userId);
        if (user != null && user.getDeptId() != null
                && deptQuotaService.isQuotaExceeded(user.getDeptId(), null)) {
            throw new BusinessException("本部门预约额度已用完，请联系管理员调整配额");
        }

        // 5. 检查冲突（使用数据库行锁）
        int conflict = baseMapper.countConflictBookings(
                dto.getRoomId(), dto.getBookingDate(),
                dto.getStartTime(), dto.getEndTime(), 0L);
        if (conflict > 0) {
            throw new BusinessException(ResultCode.BOOKING_CONFLICT);
        }

        // 6. 创建预约
        Booking booking = new Booking();
        booking.setBookingNo(generateBookingNo());
        booking.setRoomId(dto.getRoomId());
        booking.setUserId(userId);
        booking.setBookingDate(dto.getBookingDate());
        booking.setStartTime(dto.getStartTime());
        booking.setEndTime(dto.getEndTime());
        booking.setDurationMin((int) Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes());
        booking.setTopic(dto.getTopic());
        booking.setAttendeeCount(dto.getAttendeeCount() != null ? dto.getAttendeeCount() : 1);
        // 开启审批时进入待审批，否则直接确认
        booking.setStatus(isApprovalRequired(room) ? BookingConstant.STATUS_PENDING : BookingConstant.STATUS_CONFIRMED);

        if (dto.getEquipmentNeed() != null) {
            try {
                booking.setEquipmentNeed(objectMapper.writeValueAsString(dto.getEquipmentNeed()));
            } catch (Exception e) {
                // ignore
            }
        }
        booking.setRemark(dto.getRemark());

        save(booking);

        // 7. 发送通知
        if (BookingConstant.STATUS_PENDING.equals(booking.getStatus())) {
            notificationService.sendBookingPendingNotification(booking);
        } else {
            notificationService.sendBookingSuccessNotification(booking);
        }

        return convertToVO(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingVO updateBooking(Long userId, Long bookingId, BookingUpdateDTO dto) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(ResultCode.BOOKING_NOT_FOUND);
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!BookingConstant.STATUS_CONFIRMED.equals(booking.getStatus())) {
            throw new BusinessException(ResultCode.BOOKING_MODIFY_NOT_ALLOWED);
        }

        // 更新字段
        if (dto.getRoomId() != null) {
            MeetingRoom room = meetingRoomMapper.selectById(dto.getRoomId());
            if (room == null || room.getStatus() != 1) {
                throw new BusinessException(ResultCode.ROOM_UNAVAILABLE);
            }
            booking.setRoomId(dto.getRoomId());
        }
        if (dto.getBookingDate() != null) {
            booking.setBookingDate(dto.getBookingDate());
        }
        if (dto.getStartTime() != null) {
            booking.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            booking.setEndTime(dto.getEndTime());
        }
        if (dto.getTopic() != null) {
            booking.setTopic(dto.getTopic());
        }
        if (dto.getAttendeeCount() != null) {
            booking.setAttendeeCount(dto.getAttendeeCount());
        }
        if (dto.getEquipmentNeed() != null) {
            try {
                booking.setEquipmentNeed(objectMapper.writeValueAsString(dto.getEquipmentNeed()));
            } catch (Exception e) {
                // ignore
            }
        }
        if (dto.getRemark() != null) {
            booking.setRemark(dto.getRemark());
        }

        // 重新计算时长
        booking.setDurationMin((int) Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes());

        // 检查冲突
        int conflict = baseMapper.countConflictBookings(
                booking.getRoomId(), booking.getBookingDate(),
                booking.getStartTime(), booking.getEndTime(), booking.getId());
        if (conflict > 0) {
            throw new BusinessException(ResultCode.BOOKING_CONFLICT);
        }

        updateById(booking);
        return convertToVO(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long userId, Long bookingId, BookingCancelDTO dto) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(ResultCode.BOOKING_NOT_FOUND);
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!BookingConstant.STATUS_CONFIRMED.equals(booking.getStatus()) &&
            !BookingConstant.STATUS_PENDING.equals(booking.getStatus())) {
            throw new BusinessException(ResultCode.BOOKING_MODIFY_NOT_ALLOWED);
        }

        // 检查取消时限
        LocalDateTime bookingStartTime = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        int cancelLimitMin = getConfigInt("booking.cancel_limit_min", 15);
        boolean isLateCancel = bookingStartTime.isBefore(LocalDateTime.now().plusMinutes(cancelLimitMin));

        if (isLateCancel) {
            // 超过取消时限取消 → 扣信用分
            String creditEnabled = creditService.getConfigValue("credit.enabled", "true");
            if ("true".equalsIgnoreCase(creditEnabled)) {
                int deduct = getConfigInt("credit.cancel_late_deduct", 5);
                creditService.change(userId, "cancel_late", -deduct, "booking", booking.getId(),
                        "超过取消时限取消预约");
            }
        }

        booking.setStatus(BookingConstant.STATUS_CANCELLED);
        booking.setCancelReason(dto.getReason());
        booking.setCancelBy(userId);
        booking.setCancelTime(LocalDateTime.now());
        updateById(booking);

        // 发送取消通知
        notificationService.sendCancelNotification(booking);
    }

    @Override
    public PageResult<BookingVO> getMyBookings(Long userId, String status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getUserId, userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Booking::getStatus, status);
        }
        wrapper.orderByDesc(Booking::getBookingDate, Booking::getStartTime);

        Page<Booking> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<BookingVO> records = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public BookingVO getBookingDetail(Long userId, Long bookingId) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(ResultCode.BOOKING_NOT_FOUND);
        }
        return convertToVO(booking);
    }

    @Override
    public CalendarVO getCalendarData(LocalDate startDate, LocalDate endDate, String floor, Integer capacity) {
        CalendarVO calendarVO = new CalendarVO();
        calendarVO.setDateRange(startDate + " ~ " + endDate);

        // 查询会议室
        LambdaQueryWrapper<MeetingRoom> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(MeetingRoom::getStatus, 1);
        if (floor != null && !floor.isEmpty()) {
            roomWrapper.eq(MeetingRoom::getFloor, floor);
        }
        if (capacity != null) {
            roomWrapper.ge(MeetingRoom::getCapacity, capacity);
        }
        roomWrapper.orderByAsc(MeetingRoom::getSort);

        List<MeetingRoom> rooms = meetingRoomMapper.selectList(roomWrapper);

        // 查询所有预约
        List<Booking> allBookings = baseMapper.selectBookingsByDateRange(startDate, endDate);

        List<RoomCalendarVO> roomCalendars = new ArrayList<>();
        for (MeetingRoom room : rooms) {
            RoomCalendarVO roomCalendar = new RoomCalendarVO();
            roomCalendar.setRoomId(room.getId());
            roomCalendar.setRoomName(room.getRoomName());
            roomCalendar.setFloor(room.getFloor());
            roomCalendar.setCapacity(room.getCapacity());
            roomCalendar.setStatus(room.getStatus());

            // 生成该会议室的时段
            List<TimeSlotVO> bookedSlots = new ArrayList<>();
            LocalTime currentTime = LocalTime.of(8, 0);
            LocalTime endTime = LocalTime.of(22, 0);

            while (currentTime.isBefore(endTime)) {
                TimeSlotVO slot = new TimeSlotVO();
                slot.setStartTime(currentTime.toString());
                slot.setEndTime(currentTime.plusMinutes(30).toString());

                // 检查是否已预约
                final LocalTime slotStart = currentTime;
                final LocalTime slotEnd = currentTime.plusMinutes(30);
                boolean isBooked = allBookings.stream()
                        .anyMatch(b -> b.getRoomId().equals(room.getId()) &&
                                b.getStartTime().isBefore(slotEnd) &&
                                b.getEndTime().isAfter(slotStart));

                slot.setBooked(isBooked);
                if (isBooked) {
                    Booking booking = allBookings.stream()
                            .filter(b -> b.getRoomId().equals(room.getId()) &&
                                    b.getStartTime().isBefore(slotEnd) &&
                                    b.getEndTime().isAfter(slotStart))
                            .findFirst().orElse(null);
                    if (booking != null) {
                        slot.setBookingId(booking.getId());
                    }
                }

                bookedSlots.add(slot);
                currentTime = currentTime.plusMinutes(30);
            }

            roomCalendar.setBookedSlots(bookedSlots);
            roomCalendars.add(roomCalendar);
        }

        calendarVO.setRooms(roomCalendars);
        return calendarVO;
    }

    @Override
    public AvailabilityVO getAvailability(Long roomId, LocalDate date) {
        AvailabilityVO vo = new AvailabilityVO();
        vo.setRoomId(roomId);
        vo.setDate(date.toString());

        List<Booking> bookings = baseMapper.selectBookingsByRoomAndDate(roomId, date, date);

        List<AvailabilityVO.TimeSlot> availableSlots = new ArrayList<>();
        LocalTime currentTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(22, 0);

        while (currentTime.isBefore(endTime)) {
            AvailabilityVO.TimeSlot slot = new AvailabilityVO.TimeSlot();
            slot.setStartTime(currentTime.toString());
            slot.setEndTime(currentTime.plusMinutes(30).toString());

            final LocalTime slotStart = currentTime;
            final LocalTime slotEnd = currentTime.plusMinutes(30);
            boolean isAvailable = bookings.stream()
                    .noneMatch(b -> b.getStartTime().isBefore(slotEnd) && b.getEndTime().isAfter(slotStart));

            slot.setAvailable(isAvailable);
            availableSlots.add(slot);
            currentTime = currentTime.plusMinutes(30);
        }

        vo.setAvailableSlots(availableSlots);
        return vo;
    }

    @Override
    public List<TimeSlotVO> getSuggestedSlots(Long roomId, LocalDate date,
                                              LocalTime startTime, LocalTime endTime) {
        AvailabilityVO availability = getAvailability(roomId, date);
        List<TimeSlotVO> suggested = new ArrayList<>();

        // 寻找连续的可用时段
        List<AvailabilityVO.TimeSlot> available = availability.getAvailableSlots().stream()
                .filter(AvailabilityVO.TimeSlot::getAvailable)
                .collect(Collectors.toList());

        int duration = (int) Duration.between(startTime, endTime).toMinutes();
        int slotsNeeded = duration / 30;

        for (int i = 0; i <= available.size() - slotsNeeded; i++) {
            boolean contiguous = true;
            for (int j = 0; j < slotsNeeded - 1; j++) {
                if (!available.get(i + j + 1).getStartTime().equals(available.get(i + j).getEndTime())) {
                    contiguous = false;
                    break;
                }
            }
            if (contiguous) {
                TimeSlotVO suggestedSlot = new TimeSlotVO();
                suggestedSlot.setStartTime(available.get(i).getStartTime());
                suggestedSlot.setEndTime(available.get(i + slotsNeeded - 1).getEndTime());
                suggested.add(suggestedSlot);
                if (suggested.size() >= 3) break;
            }
        }

        return suggested;
    }

    @Override
    public PageResult<BookingVO> getAllBookings(BookingQueryDTO dto) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();

        if (dto.getStartDate() != null && !dto.getStartDate().isEmpty()) {
            wrapper.ge(Booking::getBookingDate, dto.getStartDate());
        }
        if (dto.getEndDate() != null && !dto.getEndDate().isEmpty()) {
            wrapper.le(Booking::getBookingDate, dto.getEndDate());
        }
        if (dto.getRoomId() != null) {
            wrapper.eq(Booking::getRoomId, dto.getRoomId());
        }
        if (dto.getUserId() != null) {
            wrapper.eq(Booking::getUserId, dto.getUserId());
        }
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            wrapper.eq(Booking::getStatus, dto.getStatus());
        } else if (Boolean.TRUE.equals(dto.getActiveOnly())) {
            // 进行中默认状态：待审批、已确认、已签到
            wrapper.in(Booking::getStatus,
                    BookingConstant.STATUS_PENDING,
                    BookingConstant.STATUS_CONFIRMED,
                    BookingConstant.STATUS_SIGNED);
        } else if (Boolean.FALSE.equals(dto.getActiveOnly())) {
            // 历史默认状态：已完成、已取消、已过期
            wrapper.in(Booking::getStatus,
                    BookingConstant.STATUS_COMPLETED,
                    BookingConstant.STATUS_CANCELLED,
                    BookingConstant.STATUS_EXPIRED);
        }

        // 时间范围：true=仅未结束 false=仅已结束 不传=全部
        // 已结束：booking_date < today OR (booking_date = today AND end_time <= now)
        if (dto.getActiveOnly() != null) {
            String today = LocalDate.now().toString();
            String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            if (Boolean.TRUE.equals(dto.getActiveOnly())) {
                wrapper.and(w -> w.gt(Booking::getBookingDate, today)
                        .or(w2 -> w2.eq(Booking::getBookingDate, today).gt(Booking::getEndTime, nowTime)));
            } else {
                wrapper.and(w -> w.lt(Booking::getBookingDate, today)
                        .or(w2 -> w2.eq(Booking::getBookingDate, today).le(Booking::getEndTime, nowTime)));
            }
        }

        wrapper.orderByDesc(Booking::getBookingDate, Booking::getStartTime);

        Page<Booking> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<BookingVO> records = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, page.getTotal(), dto.getPageNum(), dto.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceCancelBooking(Long adminId, Long bookingId, String reason) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(ResultCode.BOOKING_NOT_FOUND);
        }
        if (!BookingConstant.STATUS_CONFIRMED.equals(booking.getStatus()) &&
            !BookingConstant.STATUS_PENDING.equals(booking.getStatus())) {
            throw new BusinessException(ResultCode.BOOKING_MODIFY_NOT_ALLOWED);
        }

        booking.setStatus(BookingConstant.STATUS_CANCELLED);
        booking.setCancelReason(reason);
        booking.setCancelBy(adminId);
        booking.setCancelTime(LocalDateTime.now());
        updateById(booking);

        // 发送通知
        notificationService.sendCancelNotification(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingVO approveBooking(Long adminId, Long bookingId) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(ResultCode.BOOKING_NOT_FOUND);
        }
        if (!BookingConstant.STATUS_PENDING.equals(booking.getStatus())) {
            throw new BusinessException(ResultCode.BOOKING_MODIFY_NOT_ALLOWED.getCode(), "只有待审批的预约可以审批");
        }

        // 审批通过前再次校验冲突，防止待审批期间时段被占用
        int conflict = baseMapper.countConflictBookings(
                booking.getRoomId(), booking.getBookingDate(),
                booking.getStartTime(), booking.getEndTime(), booking.getId());
        if (conflict > 0) {
            throw new BusinessException(ResultCode.BOOKING_CONFLICT);
        }

        booking.setStatus(BookingConstant.STATUS_CONFIRMED);
        updateById(booking);

        notificationService.sendBookingApprovedNotification(booking);
        return convertToVO(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectBooking(Long adminId, Long bookingId, String reason) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(ResultCode.BOOKING_NOT_FOUND);
        }
        if (!BookingConstant.STATUS_PENDING.equals(booking.getStatus())) {
            throw new BusinessException(ResultCode.BOOKING_MODIFY_NOT_ALLOWED.getCode(), "只有待审批的预约可以审批");
        }

        String rejectReason = (reason != null && !reason.trim().isEmpty()) ? reason : "审批拒绝";
        booking.setStatus(BookingConstant.STATUS_CANCELLED);
        booking.setCancelReason(rejectReason);
        booking.setCancelBy(adminId);
        booking.setCancelTime(LocalDateTime.now());
        updateById(booking);

        notificationService.sendBookingRejectedNotification(booking, rejectReason);
    }

    /**
     * 是否需要审批：全局审批开关开启 且 该会议室标记为需审批
     */
    private boolean isApprovalRequired(MeetingRoom room) {
        SysConfig config = sysConfigMapper.selectByConfigKey("approval.enabled");
        if (config == null || !"true".equalsIgnoreCase(config.getConfigValue())) {
            return false;
        }
        return room != null && room.getNeedApproval() != null && room.getNeedApproval() == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingVO createBookingByAdmin(Long adminId, AdminBookingDTO dto) {
        // 校验会议室
        MeetingRoom room = meetingRoomMapper.selectById(dto.getRoomId());
        if (room == null || room.getStatus() != 1) {
            throw new BusinessException(ResultCode.ROOM_UNAVAILABLE);
        }

        // 校验用户
        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验时间
        validateBookingTime(dto.getStartTime(), dto.getEndTime());

        // 检查冲突
        int conflict = baseMapper.countConflictBookings(
                dto.getRoomId(), dto.getBookingDate(),
                dto.getStartTime(), dto.getEndTime(), 0L);
        if (conflict > 0) {
            throw new BusinessException(ResultCode.BOOKING_CONFLICT);
        }

        // 创建预约
        Booking booking = new Booking();
        booking.setBookingNo(generateBookingNo());
        booking.setRoomId(dto.getRoomId());
        booking.setUserId(dto.getUserId());
        booking.setBookingDate(dto.getBookingDate());
        booking.setStartTime(dto.getStartTime());
        booking.setEndTime(dto.getEndTime());
        booking.setDurationMin((int) Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes());
        booking.setTopic(dto.getTopic());
        booking.setAttendeeCount(dto.getAttendeeCount() != null ? dto.getAttendeeCount() : 1);
        booking.setStatus(BookingConstant.STATUS_CONFIRMED);

        if (dto.getEquipmentNeed() != null) {
            try {
                booking.setEquipmentNeed(objectMapper.writeValueAsString(dto.getEquipmentNeed()));
            } catch (Exception e) {
                // ignore
            }
        }
        booking.setRemark(dto.getRemark());

        save(booking);

        // 发送通知
        notificationService.sendBookingSuccessNotification(booking);

        return convertToVO(booking);
    }

    private void validateBookingDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "不能预约过去的日期");
        }

        // 信用分限制：低分用户只能预约未来N天
        int advanceDays = getConfigInt("booking.advance_days", 14);
        if (date.isAfter(LocalDate.now().plusDays(advanceDays))) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "只能预约未来" + advanceDays + "天内的时段");
        }

        // 检查是否是工作日
        WorkCalendar calendar = workCalendarMapper.selectByDate(date);
        if (calendar != null) {
            if (calendar.getDateType() == 3 || calendar.getDateType() == 5) {
                throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "节假日不可预约");
            }
        } else {
            // 检查是否是周末
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "周末不可预约");
            }
        }
    }

    private void validateBookingTime(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "结束时间必须晚于开始时间");
        }

        LocalTime minTime = LocalTime.of(8, 0);
        LocalTime maxTime = LocalTime.of(22, 0);

        if (startTime.isBefore(minTime) || endTime.isAfter(maxTime)) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "可预约时段为08:00-22:00");
        }

        // 检查时间粒度
        if (startTime.getMinute() % 30 != 0 || endTime.getMinute() % 30 != 0) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "时间粒度为30分钟");
        }

        // 检查时长
        long durationMinutes = Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes < 30) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "最小预约时长为30分钟");
        }
        if (durationMinutes > 240) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "最大预约时长为4小时");
        }
    }

    private void validateBookingRules(Long userId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // 检查每人每日预约上限（从配置读取）
        int dailyLimit = getConfigInt("booking.daily_limit", 3);
        if (dailyLimit > 0) {
            int dailyCount = baseMapper.countUserDailyBookings(userId, date);
            if (dailyCount >= dailyLimit) {
                throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "每人每日最多预约" + dailyLimit + "次");
            }
        }

        // 信用分限制：低分用户可预约天数受限
        User user = userMapper.selectById(userId);
        if (user != null) {
            int score = user.getCreditScore() != null ? user.getCreditScore() : 100;
            int threshold = getConfigInt("credit.threshold", 60);
            int restrictDays = getConfigInt("credit.restrict_days", 3);
            if (score < threshold && date.isAfter(LocalDate.now().plusDays(restrictDays))) {
                throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(),
                        "信用分低于" + threshold + "，仅可预约未来" + restrictDays + "天内时段");
            }
        }
    }

    private String generateBookingNo() {
        return BookingConstant.BOOKING_NO_PREFIX +
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now()) +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private BookingVO convertToVO(Booking booking) {
        BookingVO vo = new BookingVO();
        vo.setId(booking.getId());
        vo.setBookingNo(booking.getBookingNo());
        vo.setRoomId(booking.getRoomId());
        vo.setUserId(booking.getUserId());
        vo.setBookingDate(booking.getBookingDate());
        vo.setStartTime(booking.getStartTime());
        vo.setEndTime(booking.getEndTime());
        vo.setDurationMin(booking.getDurationMin());
        vo.setTopic(booking.getTopic());
        vo.setAttendeeCount(booking.getAttendeeCount());
        vo.setRemark(booking.getRemark());
        vo.setStatus(booking.getStatus());
        vo.setStatusName(getStatusName(booking.getStatus()));
        vo.setCancelReason(booking.getCancelReason());
        vo.setCreateTime(booking.getCreateTime());

        // 解析设备JSON
        if (booking.getEquipmentNeed() != null) {
            try {
                List<String> equipment = objectMapper.readValue(booking.getEquipmentNeed(),
                        new TypeReference<List<String>>() {});
                vo.setEquipmentNeed(equipment);
            } catch (Exception e) {
                vo.setEquipmentNeed(List.of());
            }
        }

        // 查询会议室信息
        MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());
        if (room != null) {
            vo.setRoomName(room.getRoomName());
            vo.setRoomLocation(room.getFloor() + " " + room.getLocation());
        }

        // 查询用户信息
        User user = userMapper.selectById(booking.getUserId());
        if (user != null) {
            vo.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
        }

        return vo;
    }

    private String getStatusName(String status) {
        if (status == null) return "";
        return switch (status) {
            case "pending" -> "待审批";
            case "confirmed" -> "已确认";
            case "signed" -> "已签到";
            case "completed" -> "已完成";
            case "cancelled" -> "已取消";
            case "expired" -> "已过期";
            default -> "";
        };
    }

    /**
     * 自动释放过期预约并扣信用分（由定时任务调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void autoReleaseExpiredBookings() {
        String autoRelease = getConfigValue("booking.auto_release", "true");
        if (!"true".equalsIgnoreCase(autoRelease)) return;

        int releaseMin = getConfigInt("booking.auto_release_min", 15);
        String creditEnabled = creditService.getConfigValue("credit.enabled", "true");
        int noShowDeduct = getConfigInt("credit.no_show_deduct", 10);

        // 查找已确认但未签到、且会议开始已超过 releaseMin 分钟的预约
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(releaseMin);
        List<Booking> expiredBookings = baseMapper.selectExpiredBookings(deadline);

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingConstant.STATUS_EXPIRED);
            booking.setReleaseTime(LocalDateTime.now());
            updateById(booking);

            // 爽约扣信用分
            if ("true".equalsIgnoreCase(creditEnabled)) {
                creditService.change(booking.getUserId(), "no_show", -noShowDeduct,
                        "booking", booking.getId(), "会议未签到，自动释放");
            }

            notificationService.sendSystemNotification(booking.getUserId(),
                    "预约已过期", "您预约的会议已过期未签到，系统已自动释放时段");
        }
    }

    /**
     * 完成已结束的预约并加信用分（由定时任务调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeFinishedBookings() {
        String creditEnabled = creditService.getConfigValue("credit.enabled", "true");
        int completeAdd = getConfigInt("credit.complete_add", 2);

        // 查找已签到且会议结束时间已到的预约
        List<Booking> finishedBookings = baseMapper.selectFinishedBookings(LocalDateTime.now());

        for (Booking booking : finishedBookings) {
            booking.setStatus(BookingConstant.STATUS_COMPLETED);
            updateById(booking);

            // 完成加分
            if ("true".equalsIgnoreCase(creditEnabled)) {
                creditService.change(booking.getUserId(), "complete", completeAdd,
                        "booking", booking.getId(), "会议正常完成");
            }
        }
    }

    /**
     * 自动过期待审批的预约（由定时任务调用）：时间已过仍未审批的 pending 预约置为已过期，不扣信用分
     */
    @Transactional(rollbackFor = Exception.class)
    public void autoExpirePendingBookings() {
        String autoRelease = getConfigValue("booking.auto_release", "true");
        if (!"true".equalsIgnoreCase(autoRelease)) return;

        int releaseMin = getConfigInt("booking.auto_release_min", 15);
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(releaseMin);
        List<Booking> pendingExpired = baseMapper.selectPendingExpiredBookings(deadline);

        for (Booking booking : pendingExpired) {
            booking.setStatus(BookingConstant.STATUS_EXPIRED);
            booking.setReleaseTime(LocalDateTime.now());
            updateById(booking);

            notificationService.sendSystemNotification(booking.getUserId(),
                    "预约已过期", "您预约的会议在待审批期间已超时，系统已自动释放时段");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signBooking(Long userId, Long bookingId, String signSource) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(ResultCode.BOOKING_NOT_FOUND);
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!BookingConstant.STATUS_CONFIRMED.equals(booking.getStatus())) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "当前状态不可签到");
        }

        // 校验签到窗口：会议开始前30分钟 ~ 会议结束后（含自动释放窗口）
        LocalDateTime bookingStart = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        LocalDateTime bookingEnd = LocalDateTime.of(booking.getBookingDate(), booking.getEndTime());
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(bookingStart.minusMinutes(30))) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "签到窗口未开放（会议开始前30分钟方可签到）");
        }
        if (now.isAfter(bookingEnd.plusMinutes(30))) {
            throw new BusinessException(ResultCode.BOOKING_RULE_FAILED.getCode(), "签到窗口已过");
        }

        booking.setStatus(BookingConstant.STATUS_SIGNED);
        booking.setSignTime(LocalDateTime.now());
        booking.setSignSource(signSource != null ? signSource : "manual");
        updateById(booking);
    }

    @Override
    public void sendMeetingStartReminders() {
        // 扫描当天已确认、开始时间在未来15分钟内的预约，发送提醒
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusMinutes(15);

        List<Booking> upcoming = baseMapper.selectUpcomingBookings(now, deadline);
        for (Booking booking : upcoming) {
            // 检查是否已发过提醒（通过通知表去重：biz_type=booking, biz_id=booking.id, type=remind）
            // 简化处理：直接发送，由前端展示去重
            MeetingRoom room = meetingRoomMapper.selectById(booking.getRoomId());
            String roomName = room != null ? room.getRoomName() : "会议室";
            notificationService.sendSystemNotification(booking.getUserId(),
                    "会议即将开始",
                    "您预约的 " + roomName + " 会议将在15分钟内开始（"
                            + booking.getBookingDate() + " "
                            + booking.getStartTime() + "–" + booking.getEndTime() + "），请准时到场签到。");
        }
    }

    private String getConfigValue(String key, String defaultValue) {
        SysConfig config = sysConfigMapper.selectByConfigKey(key);
        return config != null ? config.getConfigValue() : defaultValue;
    }

    private int getConfigInt(String key, int defaultValue) {
        String val = getConfigValue(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
