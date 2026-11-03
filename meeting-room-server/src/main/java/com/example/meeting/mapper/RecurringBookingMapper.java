package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.RecurringBooking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 周期预约组Mapper接口
 */
@Mapper
public interface RecurringBookingMapper extends BaseMapper<RecurringBooking> {

    /**
     * 查询用户的有效周期预约
     */
    @Select("SELECT * FROM recurring_booking WHERE user_id = #{userId} AND enabled = 1 ORDER BY start_date")
    List<RecurringBooking> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询指定会议室的有效周期预约
     */
    @Select("SELECT * FROM recurring_booking WHERE room_id = #{roomId} AND enabled = 1 " +
            "AND start_date <= #{endDate} AND end_date >= #{startDate}")
    List<RecurringBooking> selectByRoomAndDateRange(@Param("roomId") Long roomId,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate);
}
