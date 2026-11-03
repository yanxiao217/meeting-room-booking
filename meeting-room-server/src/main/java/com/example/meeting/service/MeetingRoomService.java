package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.dto.RoomDTO;
import com.example.meeting.entity.MeetingRoom;
import com.example.meeting.vo.RoomVO;

import java.util.List;

/**
 * 会议室服务接口
 */
public interface MeetingRoomService extends IService<MeetingRoom> {

    /**
     * 获取会议室列表
     */
    List<RoomVO> getRoomList(String floor, Integer capacity, Integer status);

    /**
     * 获取会议室详情
     */
    RoomVO getRoomDetail(Long roomId);

    /**
     * 创建会议室
     */
    RoomVO createRoom(RoomDTO dto);

    /**
     * 更新会议室
     */
    RoomVO updateRoom(Long roomId, RoomDTO dto);

    /**
     * 删除会议室
     */
    void deleteRoom(Long roomId);

    /**
     * 更新会议室状态
     */
    void updateRoomStatus(Long roomId, Integer status);
}
