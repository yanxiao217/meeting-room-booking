package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.CreditRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 信用分记录Mapper
 */
@Mapper
public interface CreditRecordMapper extends BaseMapper<CreditRecord> {

    /**
     * 检查某条预约是否已扣过分（幂等）
     */
    @Select("SELECT COUNT(*) FROM credit_record WHERE user_id = #{userId} AND biz_type = #{bizType} AND biz_id = #{bizId} AND change_type = #{changeType}")
    int countByBiz(@Param("userId") Long userId, @Param("bizType") String bizType, @Param("bizId") Long bizId, @Param("changeType") String changeType);

    /**
     * 查询某用户最近N条信用记录
     */
    @Select("SELECT * FROM credit_record WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<CreditRecord> selectRecentByUser(@Param("userId") Long userId, @Param("limit") int limit);
}
