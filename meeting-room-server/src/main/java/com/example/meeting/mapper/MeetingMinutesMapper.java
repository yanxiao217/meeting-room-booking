package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.MeetingMinutes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 会议纪要Mapper接口
 */
@Mapper
public interface MeetingMinutesMapper extends BaseMapper<MeetingMinutes> {

    /**
     * 查询预约的纪要
     */
    @Select("SELECT * FROM meeting_minutes WHERE booking_id = #{bookingId}")
    MeetingMinutes selectByBookingId(@Param("bookingId") Long bookingId);

    /**
     * 查询用户的纪要
     */
    @Select("SELECT * FROM meeting_minutes WHERE author_id = #{authorId}")
    java.util.List<MeetingMinutes> selectByAuthorId(@Param("authorId") Long authorId);
}
