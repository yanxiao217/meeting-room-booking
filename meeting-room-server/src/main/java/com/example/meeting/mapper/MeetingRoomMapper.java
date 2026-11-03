package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.MeetingRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 会议室Mapper接口
 */
@Mapper
public interface MeetingRoomMapper extends BaseMapper<MeetingRoom> {

    @Select("SELECT * FROM meeting_room WHERE room_name = #{roomName} AND floor = #{floor}")
    MeetingRoom selectByNameAndFloor(String roomName, String floor);
}
