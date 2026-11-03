package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.entity.CreditRecord;

import java.util.List;

/**
 * 信用分服务接口
 */
public interface CreditService extends IService<CreditRecord> {

    /**
     * 变更信用分（事务：写记录 + 更新用户分值）
     * @param userId 用户ID
     * @param changeType 变动类型: no_show/complete/weekly_recover/cancel_late/admin_adjust
     * @param changeValue 变动分值（正数加分，负数扣分）
     * @param bizType 关联业务类型
     * @param bizId 关联业务ID
     * @param remark 备注
     * @return 变动后的信用分
     */
    int change(Long userId, String changeType, int changeValue, String bizType, Long bizId, String remark);

    /**
     * 获取用户当前信用分
     */
    int getScore(Long userId);

    /**
     * 获取用户信用变动记录
     */
    List<CreditRecord> getRecords(Long userId, int page, int size);

    /**
     * 管理员手动调整信用分
     */
    void adminAdjust(Long userId, int value, String reason);

    /**
     * 每周恢复信用分（定时任务调用）
     */
    void weeklyRecover();

    /**
     * 读取信用分配置值
     */
    String getConfigValue(String key, String defaultValue);
}
