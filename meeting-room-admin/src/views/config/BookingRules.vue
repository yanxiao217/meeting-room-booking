<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>预约规则配置</span>
          <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="formData"
        label-width="180px"
        v-loading="loading"
      >
        <el-divider content-position="left">时间规则</el-divider>
        
        <el-form-item label="最小预约时长(分钟)">
          <el-input-number v-model="formData.minDuration" :min="15" :step="15" />
          <span class="form-tip">单次预约不可低于此时长</span>
        </el-form-item>
        
        <el-form-item label="最大预约时长(分钟)">
          <el-input-number v-model="formData.maxDuration" :min="30" :step="30" />
          <span class="form-tip">单次预约不可超过此时长</span>
        </el-form-item>
        
        <el-form-item label="可预约天数">
          <el-input-number v-model="formData.advanceDays" :min="1" :max="90" />
          <span class="form-tip">只能预约今天起N天内的时段</span>
        </el-form-item>
        
        <el-form-item label="可预约时段">
          <el-time-picker
            v-model="formData.bookingTimeRange"
            is-range
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="HH:mm"
            value-format="HH:mm"
          />
          <span class="form-tip">每日可预约的时间范围</span>
        </el-form-item>
        
        <el-form-item label="时间粒度(分钟)">
          <el-select v-model="formData.timeGranularity" style="width: 120px">
            <el-option label="15分钟" :value="15" />
            <el-option label="30分钟" :value="30" />
            <el-option label="60分钟" :value="60" />
          </el-select>
          <span class="form-tip">开始/结束时间的最小粒度</span>
        </el-form-item>
        
        <el-divider content-position="left">取消规则</el-divider>
        
        <el-form-item label="提前取消时限(分钟)">
          <el-input-number v-model="formData.cancelLimitMin" :min="0" :step="5" />
          <span class="form-tip">会议开始前N分钟内不可自行取消</span>
        </el-form-item>
        
        <el-divider content-position="left">数量限制</el-divider>
        
        <el-form-item label="每人每日预约上限">
          <el-input-number v-model="formData.dailyLimit" :min="0" />
          <span class="form-tip">0表示不限</span>
        </el-form-item>
        
        <el-divider content-position="left">其他规则</el-divider>
        
        <el-form-item label="节假日是否可预约">
          <el-switch v-model="formData.holidayAllowed" />
          <span class="form-tip">开启后节假日也可预约</span>
        </el-form-item>
        
        <el-form-item label="开始前提醒(分钟)">
          <el-input-number v-model="formData.remindMin" :min="5" :step="5" />
          <span class="form-tip">会议开始前N分钟发送提醒</span>
        </el-form-item>
        
        <el-form-item label="是否开启自动释放">
          <el-switch v-model="formData.autoRelease" />
          <span class="form-tip">开启后未签到超时将自动释放时段</span>
        </el-form-item>
        
        <el-form-item v-if="formData.autoRelease" label="自动释放超时(分钟)">
          <el-input-number v-model="formData.autoReleaseMin" :min="5" :step="5" />
          <span class="form-tip">会议开始后N分钟未签到则释放</span>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getBookingRules, updateBookingRules } from '@/api'
import type { ConfigInfo } from '@/types'

const loading = ref(false)
const saving = ref(false)

const formData = reactive({
  minDuration: 30,
  maxDuration: 240,
  advanceDays: 14,
  bookingTimeRange: ['08:00', '22:00'] as string[],
  timeGranularity: 30,
  cancelLimitMin: 15,
  dailyLimit: 3,
  holidayAllowed: false,
  remindMin: 15,
  autoRelease: false,
  autoReleaseMin: 15
})

const configMap: Record<string, string> = {}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getBookingRules()
    
    res.data.forEach((item: ConfigInfo) => {
      configMap[item.configKey] = item.configValue
    })
    
    // 映射到表单
    formData.minDuration = Number(configMap['booking.min_duration']) || 30
    formData.maxDuration = Number(configMap['booking.max_duration']) || 240
    formData.advanceDays = Number(configMap['booking.advance_days']) || 14
    formData.bookingTimeRange = [
      configMap['booking.start_time'] || '08:00',
      configMap['booking.end_time'] || '22:00'
    ]
    formData.timeGranularity = Number(configMap['booking.time_granularity']) || 30
    formData.cancelLimitMin = Number(configMap['booking.cancel_limit_min']) || 15
    formData.dailyLimit = Number(configMap['booking.daily_limit']) || 3
    formData.holidayAllowed = configMap['booking.holiday_allowed'] === 'true'
    formData.remindMin = Number(configMap['booking.remind_min']) || 15
    formData.autoRelease = configMap['booking.auto_release'] === 'true'
    formData.autoReleaseMin = Number(configMap['booking.auto_release_min']) || 15
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const configs: ConfigInfo[] = [
      { id: 0, configKey: 'booking.min_duration', configValue: String(formData.minDuration), configName: '最小预约时长' },
      { id: 0, configKey: 'booking.max_duration', configValue: String(formData.maxDuration), configName: '最大预约时长' },
      { id: 0, configKey: 'booking.advance_days', configValue: String(formData.advanceDays), configName: '可预约天数' },
      { id: 0, configKey: 'booking.start_time', configValue: formData.bookingTimeRange[0], configName: '可预约开始时间' },
      { id: 0, configKey: 'booking.end_time', configValue: formData.bookingTimeRange[1], configName: '可预约结束时间' },
      { id: 0, configKey: 'booking.time_granularity', configValue: String(formData.timeGranularity), configName: '时间粒度' },
      { id: 0, configKey: 'booking.cancel_limit_min', configValue: String(formData.cancelLimitMin), configName: '提前取消时限' },
      { id: 0, configKey: 'booking.daily_limit', configValue: String(formData.dailyLimit), configName: '每人每日预约上限' },
      { id: 0, configKey: 'booking.holiday_allowed', configValue: String(formData.holidayAllowed), configName: '节假日是否可预约' },
      { id: 0, configKey: 'booking.remind_min', configValue: String(formData.remindMin), configName: '开始前提醒' },
      { id: 0, configKey: 'booking.auto_release', configValue: String(formData.autoRelease), configName: '是否开启自动释放' },
      { id: 0, configKey: 'booking.auto_release_min', configValue: String(formData.autoReleaseMin), configName: '自动释放超时' }
    ]
    
    await updateBookingRules(configs)
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  margin-left: 15px;
  color: #999;
  font-size: 12px;
}
</style>
