package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.entity.RepairOrder;

import java.util.List;

/**
 * 报修工单服务接口
 */
public interface RepairOrderService extends IService<RepairOrder> {

    /**
     * 发起报修
     */
    RepairOrder createRepair(Long userId, RepairOrder repairOrder);

    /**
     * 获取用户的报修记录
     */
    List<RepairOrder> getMyRepairs(Long userId);

    /**
     * 获取报修详情
     */
    RepairOrder getRepairDetail(Long repairId);

    /**
     * 管理员：接单/转派
     */
    void assignRepair(Long adminId, Long repairId, Long assigneeId);

    /**
     * 管理员：完成工单
     */
    void finishRepair(Long adminId, Long repairId, String handleDesc);
}
