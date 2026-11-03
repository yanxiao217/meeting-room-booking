package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.entity.CreditRecord;
import com.example.meeting.entity.SysConfig;
import com.example.meeting.entity.User;
import com.example.meeting.common.exception.BusinessException;
import com.example.meeting.common.result.ResultCode;
import com.example.meeting.mapper.CreditRecordMapper;
import com.example.meeting.mapper.SysConfigMapper;
import com.example.meeting.mapper.UserMapper;
import com.example.meeting.service.CreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 信用分服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditServiceImpl extends ServiceImpl<CreditRecordMapper, CreditRecord> implements CreditService {

    private final UserMapper userMapper;
    private final SysConfigMapper sysConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int change(Long userId, String changeType, int changeValue, String bizType, Long bizId, String remark) {
        // 幂等检查：同一业务只处理一次
        if (bizType != null && bizId != null) {
            int exists = baseMapper.countByBiz(userId, bizType, bizId, changeType);
            if (exists > 0) {
                log.info("信用分变更已执行过, userId={}, bizType={}, bizId={}, changeType={}", userId, bizType, bizId, changeType);
                User user = userMapper.selectById(userId);
                return user != null ? user.getCreditScore() : getConfigInt("credit.initial_score", 100);
            }
        }

        // 读取上限配置
        int maxScore = getConfigInt("credit.max_score", 100);

        // 查询当前分值
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        int beforeScore = user.getCreditScore() != null ? user.getCreditScore() : getConfigInt("credit.initial_score", 100);

        // 计算新分值（封顶封底）
        int afterScore = beforeScore + changeValue;
        afterScore = Math.max(0, Math.min(afterScore, maxScore));

        // 更新用户分值
        user.setCreditScore(afterScore);
        userMapper.updateById(user);

        // 写信用记录
        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setChangeType(changeType);
        record.setChangeValue(changeValue);
        record.setBeforeScore(beforeScore);
        record.setAfterScore(afterScore);
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setRemark(remark);
        save(record);

        log.info("信用分变更: userId={}, {} {} ({}→{}), biz={}/{}", userId, changeType, changeValue, beforeScore, afterScore, bizType, bizId);
        return afterScore;
    }

    @Override
    public int getScore(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return getConfigInt("credit.initial_score", 100);
        return user.getCreditScore() != null ? user.getCreditScore() : getConfigInt("credit.initial_score", 100);
    }

    @Override
    public List<CreditRecord> getRecords(Long userId, int page, int size) {
        LambdaQueryWrapper<CreditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditRecord::getUserId, userId);
        wrapper.orderByDesc(CreditRecord::getCreateTime);
        Page<CreditRecord> result = page(new Page<>(page, size), wrapper);
        return result.getRecords();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminAdjust(Long userId, int value, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "调整原因不能为空");
        }
        change(userId, "admin_adjust", value, "admin", null, "管理员调整: " + reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void weeklyRecover() {
        // 幂等检查：用 sys_config 记录上次执行的周
        SysConfig lastRunConfig = sysConfigMapper.selectByConfigKey("credit.last_recover_week");
        String currentWeek = LocalDate.now().format(DateTimeFormatter.ISO_WEEK_DATE); // e.g. 2026-W37-1
        // 取当前年的第几周
        String weekKey = LocalDate.now().getYear() + "-W" + String.format("%02d",
                LocalDate.now().getDayOfYear() / 7 + 1);

        if (lastRunConfig != null && weekKey.equals(lastRunConfig.getConfigValue())) {
            log.info("本周信用分恢复已执行, week={}", weekKey);
            return;
        }

        int recoverValue = getConfigInt("credit.weekly_recover", 5);
        int maxScore = getConfigInt("credit.max_score", 100);
        int threshold = getConfigInt("credit.threshold", 60);

        // 查询所有 credit_score < maxScore 的用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(User::getCreditScore, maxScore);
        wrapper.eq(User::getStatus, 1);
        List<User> users = userMapper.selectList(wrapper);

        int count = 0;
        for (User user : users) {
            int before = user.getCreditScore() != null ? user.getCreditScore() : getConfigInt("credit.initial_score", 100);
            int after = Math.min(before + recoverValue, maxScore);
            user.setCreditScore(after);
            userMapper.updateById(user);

            CreditRecord record = new CreditRecord();
            record.setUserId(user.getId());
            record.setChangeType("weekly_recover");
            record.setChangeValue(after - before);
            record.setBeforeScore(before);
            record.setAfterScore(after);
            record.setRemark("每周信用分恢复");
            save(record);
            count++;
        }

        // 更新执行记录
        if (lastRunConfig == null) {
            lastRunConfig = new SysConfig();
            lastRunConfig.setConfigKey("credit.last_recover_week");
            lastRunConfig.setConfigValue(weekKey);
            lastRunConfig.setConfigName("上次信用分恢复周");
            lastRunConfig.setDescription("自动记录，勿手动修改");
            sysConfigMapper.insert(lastRunConfig);
        } else {
            lastRunConfig.setConfigValue(weekKey);
            sysConfigMapper.updateById(lastRunConfig);
        }

        log.info("每周信用分恢复完成: 恢复{}人, recoverValue={}", count, recoverValue);
    }

    @Override
    public String getConfigValue(String key, String defaultValue) {
        SysConfig config = sysConfigMapper.selectByConfigKey(key);
        return config != null ? config.getConfigValue() : defaultValue;
    }

    private int getConfigInt(String key, int defaultValue) {
        String val = getConfigValue(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
