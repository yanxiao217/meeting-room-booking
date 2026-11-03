package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.WorkCalendar;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 工作日历Mapper接口
 */
@Mapper
public interface WorkCalendarMapper extends BaseMapper<WorkCalendar> {

    @Select("SELECT * FROM work_calendar WHERE calendar_date = #{date}")
    WorkCalendar selectByDate(LocalDate date);
}
