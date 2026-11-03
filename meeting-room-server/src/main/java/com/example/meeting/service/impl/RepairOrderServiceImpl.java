package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.entity.RepairOrder;
import com.example.meeting.mapper.RepairOrderMapper;
import com.example.meeting.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 报修工单服务实现类
 */
@Service
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Override
    public RepairOrder createRepair(Long userId, RepairOrder repairOrder) {
        repairOrder.setReporterId(userId);
        repairOrder.setStatus("pending");
        repairOrder.setOrderNo(generateOrderNo());
        save(repairOrder);
        return repairOrder;
    }

    @Override
    public List<RepairOrder> getMyRepairs(Long userId) {
        return repairOrderMapper.selectByReporterId(userId);
    }

    @Override
    public RepairOrder getRepairDetail(Long repairId) {
        return getById(repairId);
    }

    @Override
    public void assignRepair(Long adminId, Long repairId, Long assigneeId) {
        lambdaUpdate()
                .eq(RepairOrder::getId, repairId)
                .set(RepairOrder::getAssigneeId, assigneeId)
                .set(RepairOrder::getStatus, "processing")
                .update();
    }

    @Override
    public void finishRepair(Long adminId, Long repairId, String handleDesc) {
        lambdaUpdate()
                .eq(RepairOrder::getId, repairId)
                .set(RepairOrder::getStatus, "finished")
                .set(RepairOrder::getHandleDesc, handleDesc)
                .set(RepairOrder::getFinishedTime, LocalDateTime.now())
                .update();
    }

    /**
     * 生成工单编号
     */
    private String generateOrderNo() {
        return "RP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
