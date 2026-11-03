package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.RoomRatingCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 低分检查记录Mapper
 */
@Mapper
public interface RoomRatingCheckMapper extends BaseMapper<RoomRatingCheck> {

    /**
     * 查询待检查记录（含评价详情和用户名）
     */
    @Select("SELECT rc.*, r.rating, r.comment, r.tags, u.real_name AS userName, " +
            "mr.room_name AS roomName " +
            "FROM room_rating_check rc " +
            "LEFT JOIN room_rating r ON rc.rating_id = r.id " +
            "LEFT JOIN sys_user u ON r.user_id = u.id " +
            "LEFT JOIN meeting_room mr ON rc.room_id = mr.id " +
            "WHERE rc.status = #{status} ORDER BY rc.create_time DESC")
    List<Map<String, Object>> selectPendingWithDetail(@Param("status") String status);
}
