package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.RepairOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 报修工单Mapper接口
 */
@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {

    /**
     * 查询用户的报修记录
     */
    @Select("SELECT * FROM repair_order WHERE reporter_id = #{reporterId} ORDER BY create_time DESC")
    List<RepairOrder> selectByReporterId(@Param("reporterId") Long reporterId);

    /**
     * 查询指定状态的报修记录
     */
    @Select("SELECT * FROM repair_order WHERE status = #{status} ORDER BY create_time DESC")
    List<RepairOrder> selectByStatus(@Param("status") String status);

    /**
     * 查询报修统计
     */
    @Select("SELECT status, COUNT(*) AS count FROM repair_order GROUP BY status")
    List<Map<String, Object>> selectRepairStats();

    /**
     * 查询指定会议室的报修记录
     */
    @Select("SELECT * FROM repair_order WHERE room_id = #{roomId} ORDER BY create_time DESC")
    List<RepairOrder> selectByRoomId(@Param("roomId") Long roomId);
}
