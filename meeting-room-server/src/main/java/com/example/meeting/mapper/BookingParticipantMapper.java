package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.BookingParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 参会人Mapper接口
 */
@Mapper
public interface BookingParticipantMapper extends BaseMapper<BookingParticipant> {

    /**
     * 查询预约的参会人
     */
    @Select("SELECT * FROM booking_participant WHERE booking_id = #{bookingId}")
    List<BookingParticipant> selectByBookingId(@Param("bookingId") Long bookingId);

    /**
     * 查询用户参与的预约
     */
    @Select("SELECT * FROM booking_participant WHERE user_id = #{userId}")
    List<BookingParticipant> selectByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否为预约参会人
     */
    @Select("SELECT COUNT(*) FROM booking_participant WHERE booking_id = #{bookingId} AND user_id = #{userId}")
    int countByBookingAndUser(@Param("bookingId") Long bookingId, @Param("userId") Long userId);
}
