package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.entity.DeptQuota;

import java.util.List;

/**
 * 部门配额服务接口
 */
public interface DeptQuotaService extends IService<DeptQuota> {

    /**
     * 查询部门配额
     */
    DeptQuota getDeptQuota(Long deptId);

    /**
     * 查询所有启用的部门配额
     */
    List<DeptQuota> getAllEnabledQuotas();

    /**
     * 检查部门是否超过配额
     */
    boolean isQuotaExceeded(Long deptId, String period);

    /**
     * 更新部门配额
     */
    void updateDeptQuota(DeptQuota deptQuota);
}
