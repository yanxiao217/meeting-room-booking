package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.BookingTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会议模板Mapper接口
 */
@Mapper
public interface BookingTemplateMapper extends BaseMapper<BookingTemplate> {

    /**
     * 查询用户的模板
     */
    @Select("SELECT * FROM booking_template WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<BookingTemplate> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的模板数量
     */
    @Select("SELECT COUNT(*) FROM booking_template WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}
