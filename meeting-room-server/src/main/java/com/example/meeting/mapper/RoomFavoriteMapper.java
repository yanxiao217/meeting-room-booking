package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.RoomFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会议室收藏Mapper接口
 */
@Mapper
public interface RoomFavoriteMapper extends BaseMapper<RoomFavorite> {

    /**
     * 查询用户收藏的会议室
     */
    @Select("SELECT * FROM room_favorite WHERE user_id = #{userId} ORDER BY sort")
    List<RoomFavorite> selectByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已收藏会议室
     */
    @Select("SELECT COUNT(*) FROM room_favorite WHERE user_id = #{userId} AND room_id = #{roomId}")
    int countByUserAndRoom(@Param("userId") Long userId, @Param("roomId") Long roomId);
}
