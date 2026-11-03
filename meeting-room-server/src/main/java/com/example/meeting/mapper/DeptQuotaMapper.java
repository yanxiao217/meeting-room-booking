package com.example.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meeting.entity.DeptQuota;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门配额Mapper接口
 */
@Mapper
public interface DeptQuotaMapper extends BaseMapper<DeptQuota> {

    /**
     * 查询部门配额
     */
    @Select("SELECT * FROM dept_quota WHERE dept_id = #{deptId} AND enabled = 1")
    DeptQuota selectByDeptId(@Param("deptId") Long deptId);

    /**
     * 查询所有启用的部门配额
     */
    @Select("SELECT * FROM dept_quota WHERE enabled = 1")
    List<DeptQuota> selectAllEnabled();
}
