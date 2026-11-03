package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.RoomRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 会议室评价Mapper
 */
@Mapper
public interface RoomRatingMapper extends BaseMapper<RoomRating> {

    /**
     * 查询会议室平均分
     */
    @Select("SELECT ROUND(AVG(rating), 1) FROM room_rating WHERE room_id = #{roomId}")
    Double selectAvgRating(@Param("roomId") Long roomId);

    /**
     * 查询会议室评价列表（含用户名）
     */
    @Select("SELECT r.*, u.real_name AS userName, u.username FROM room_rating r " +
            "LEFT JOIN sys_user u ON r.user_id = u.id " +
            "WHERE r.room_id = #{roomId} ORDER BY r.create_time DESC LIMIT #{limit}")
    List<Map<String, Object>> selectRatingsWithUser(@Param("roomId") Long roomId, @Param("limit") int limit);

    /**
     * 评价分布统计（按星级）
     */
    @Select("SELECT rating, COUNT(*) AS cnt FROM room_rating WHERE room_id = #{roomId} GROUP BY rating ORDER BY rating")
    List<Map<String, Object>> selectRatingDistribution(@Param("roomId") Long roomId);

    /**
     * 全部会议室评价统计
     */
    @Select("SELECT room_id AS roomId, COUNT(*) AS ratingCount, ROUND(AVG(rating),1) AS avgRating " +
            "FROM room_rating GROUP BY room_id ORDER BY avgRating ASC")
    List<Map<String, Object>> selectRoomRatingStats();
}
