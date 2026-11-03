package com.example.meeting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.meeting.entity.DeptQuota;
import com.example.meeting.mapper.BookingMapper;
import com.example.meeting.mapper.DeptQuotaMapper;
import com.example.meeting.service.DeptQuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * 部门配额服务实现类
 */
@Service
public class DeptQuotaServiceImpl extends ServiceImpl<DeptQuotaMapper, DeptQuota> implements DeptQuotaService {

    @Autowired
    private DeptQuotaMapper deptQuotaMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Override
    public DeptQuota getDeptQuota(Long deptId) {
        return deptQuotaMapper.selectByDeptId(deptId);
    }

    @Override
    public List<DeptQuota> getAllEnabledQuotas() {
        return deptQuotaMapper.selectAllEnabled();
    }

    @Override
    public boolean isQuotaExceeded(Long deptId, String period) {
        DeptQuota quota = deptQuotaMapper.selectByDeptId(deptId);
        if (quota == null || quota.getEnabled() == null || quota.getEnabled() != 1) {
            return false;
        }
        // 周期起点：weekly 取本周一，monthly 取本月 1 号
        LocalDate today = LocalDate.now();
        LocalDate start;
        if ("month".equals(quota.getPeriod())) {
            start = today.withDayOfMonth(1);
        } else {
            start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        if ("weekly".equals(quota.getQuotaType())) {
            // weekly：统计本周有效预约次数，达到配额即拦截
            long used = bookingMapper.countValidByDeptSince(deptId, start);
            return used >= quota.getQuotaValue();
        } else {
            // monthly：统计本月有效预约总时长（小时），达到配额即拦截
            long usedMinutes = bookingMapper.sumDurationByDeptSince(deptId, start);
            return usedMinutes >= quota.getQuotaValue() * 60L;
        }
    }

    @Override
    public void updateDeptQuota(DeptQuota deptQuota) {
        updateById(deptQuota);
    }
}
