package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.common.exception.BusinessException;
import com.example.meeting.common.result.ResultCode;
import com.example.meeting.dto.RoomDTO;
import com.example.meeting.entity.Booking;
import com.example.meeting.entity.MeetingRoom;
import com.example.meeting.mapper.BookingMapper;
import com.example.meeting.mapper.MeetingRoomMapper;
import com.example.meeting.service.MeetingRoomService;
import com.example.meeting.service.RatingService;
import com.example.meeting.vo.RoomVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会议室服务实现
 */
@Service
@RequiredArgsConstructor
public class MeetingRoomServiceImpl extends ServiceImpl<MeetingRoomMapper, MeetingRoom> implements MeetingRoomService {

    private final BookingMapper bookingMapper;
    private final RatingService ratingService;
    private final ObjectMapper objectMapper;

    @Override
    public List<RoomVO> getRoomList(String floor, Integer capacity, Integer status) {
        LambdaQueryWrapper<MeetingRoom> wrapper = new LambdaQueryWrapper<>();
        if (floor != null && !floor.isEmpty()) {
            wrapper.eq(MeetingRoom::getFloor, floor);
        }
        if (capacity != null) {
            wrapper.ge(MeetingRoom::getCapacity, capacity);
        }
        if (status != null) {
            wrapper.eq(MeetingRoom::getStatus, status);
        }
        wrapper.orderByAsc(MeetingRoom::getSort);

        List<MeetingRoom> rooms = list(wrapper);
        return rooms.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public RoomVO getRoomDetail(Long roomId) {
        MeetingRoom room = getById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }
        return convertToVO(room);
    }

    @Override
    public RoomVO createRoom(RoomDTO dto) {
        // 检查名称是否重复
        MeetingRoom existing = baseMapper.selectByNameAndFloor(dto.getRoomName(), dto.getFloor());
        if (existing != null) {
            throw new BusinessException(ResultCode.ROOM_NAME_EXISTS);
        }

        MeetingRoom room = new MeetingRoom();
        room.setRoomName(dto.getRoomName());
        room.setFloor(dto.getFloor());
        room.setLocation(dto.getLocation());
        room.setCapacity(dto.getCapacity());
        room.setRoomType(dto.getRoomType() != null ? dto.getRoomType() : "meeting");
        room.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        room.setNeedApproval(dto.getNeedApproval() != null ? dto.getNeedApproval() : 0);
        room.setSort(dto.getSort() != null ? dto.getSort() : 0);

        // 转换设备列表为JSON
        if (dto.getEquipment() != null) {
            try {
                room.setEquipment(objectMapper.writeValueAsString(dto.getEquipment()));
            } catch (Exception e) {
                room.setEquipment("[]");
            }
        }
        if (dto.getImages() != null) {
            try {
                room.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (Exception e) {
                room.setImages("[]");
            }
        }
        room.setDescription(dto.getDescription());

        save(room);
        return convertToVO(room);
    }

    @Override
    public RoomVO updateRoom(Long roomId, RoomDTO dto) {
        MeetingRoom room = getById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }

        // 检查名称是否重复
        if (!room.getRoomName().equals(dto.getRoomName()) || !room.getFloor().equals(dto.getFloor())) {
            MeetingRoom existing = baseMapper.selectByNameAndFloor(dto.getRoomName(), dto.getFloor());
            if (existing != null) {
                throw new BusinessException(ResultCode.ROOM_NAME_EXISTS);
            }
        }

        room.setRoomName(dto.getRoomName());
        room.setFloor(dto.getFloor());
        room.setLocation(dto.getLocation());
        room.setCapacity(dto.getCapacity());
        if (dto.getRoomType() != null) {
            room.setRoomType(dto.getRoomType());
        }
        if (dto.getStatus() != null) {
            room.setStatus(dto.getStatus());
        }
        if (dto.getNeedApproval() != null) {
            room.setNeedApproval(dto.getNeedApproval());
        }
        if (dto.getSort() != null) {
            room.setSort(dto.getSort());
        }
        if (dto.getEquipment() != null) {
            try {
                room.setEquipment(objectMapper.writeValueAsString(dto.getEquipment()));
            } catch (Exception e) {
                // ignore
            }
        }
        if (dto.getImages() != null) {
            try {
                room.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (Exception e) {
                // ignore
            }
        }
        if (dto.getDescription() != null) {
            room.setDescription(dto.getDescription());
        }

        updateById(room);
        return convertToVO(room);
    }

    @Override
    public void deleteRoom(Long roomId) {
        MeetingRoom room = getById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }

        // 检查是否有未完成的预约
        int bookingCount = bookingMapper.countRoomDailyBookings(roomId, LocalDate.now());
        if (bookingCount > 0) {
            throw new BusinessException(ResultCode.ROOM_IN_USE);
        }

        removeById(roomId);
    }

    @Override
    public void updateRoomStatus(Long roomId, Integer status) {
        MeetingRoom room = getById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }
        room.setStatus(status);
        updateById(room);
    }

    private RoomVO convertToVO(MeetingRoom room) {
        RoomVO vo = new RoomVO();
        vo.setId(room.getId());
        vo.setRoomName(room.getRoomName());
        vo.setFloor(room.getFloor());
        vo.setLocation(room.getLocation());
        vo.setCapacity(room.getCapacity());
        vo.setRoomType(room.getRoomType());
        vo.setStatus(room.getStatus());
        vo.setNeedApproval(room.getNeedApproval());
        vo.setSort(room.getSort());
        vo.setDescription(room.getDescription());
        vo.setCreateTime(room.getCreateTime());

        // 解析设备JSON
        if (room.getEquipment() != null) {
            try {
                List<String> equipment = objectMapper.readValue(room.getEquipment(),
                        new TypeReference<List<String>>() {});
                vo.setEquipment(equipment);
            } catch (Exception e) {
                vo.setEquipment(List.of());
            }
        }

        // 解析图片JSON
        if (room.getImages() != null) {
            try {
                List<String> images = objectMapper.readValue(room.getImages(),
                        new TypeReference<List<String>>() {});
                vo.setImages(images);
            } catch (Exception e) {
                vo.setImages(List.of());
            }
        }

        // 状态名称
        vo.setStatusName(getStatusName(room.getStatus()));
        // 类型名称
        vo.setRoomTypeName(getRoomTypeName(room.getRoomType()));

        // 今日预约数
        vo.setTodayBookingCount(bookingMapper.countRoomTodayBookings(room.getId()));

        // 平均评分
        Double avgRating = ratingService.getRoomAvgRating(room.getId());
        vo.setAvgRating(avgRating != null ? avgRating : 0.0);

        return vo;
    }

    private String getStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "维护中";
            case 1 -> "可用";
            case 2 -> "停用";
            default -> "";
        };
    }

    private String getRoomTypeName(String type) {
        if (type == null) return "";
        return switch (type) {
            case "meeting" -> "会议室";
            case "training" -> "培训室";
            case "phone" -> "电话亭";
            default -> "";
        };
    }
}
