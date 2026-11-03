<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>候补规则配置</span>
          <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="formData" label-width="180px" v-loading="loading">
        <el-form-item label="是否开启候补">
          <el-switch v-model="formData.enabled" />
          <span class="form-tip">冲突时可加入候补队列</span>
        </el-form-item>

        <el-form-item label="候补确认时限(分钟)">
          <el-input-number v-model="formData.confirmMin" :min="1" :step="1" />
          <span class="form-tip">通知后N分钟内确认，超时顺延</span>
        </el-form-item>

        <el-form-item label="候补队列上限">
          <el-input-number v-model="formData.maxRank" :min="1" :step="1" />
          <span class="form-tip">同一时段每会议室候补人数上限</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>周期预约配置</span>
        </div>
      </template>

      <el-form label-width="180px" v-loading="loading">
        <el-form-item label="周期预约最大次数">
          <el-input-number v-model="formData.recurringMaxCount" :min="1" :step="1" />
          <span class="form-tip">单次周期预约最多生成次数</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>审批配置</span>
        </div>
      </template>

      <el-form label-width="180px" v-loading="loading">
        <el-form-item label="是否启用审批">
          <el-switch v-model="formData.approvalEnabled" />
          <span class="form-tip">特定会议室预约需审批</span>
        </el-form-item>

        <el-form-item label="审批超时(小时)">
          <el-input-number v-model="formData.approvalTimeout" :min="1" :step="1" />
          <span class="form-tip">超时未审批自动拒绝</span>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWaitlistRules, updateWaitlistRules, getRecurringRules, updateRecurringRules, getApprovalRules, updateApprovalRules } from '@/api'
import type { ConfigInfo } from '@/types'

const loading = ref(false)
const saving = ref(false)

const formData = reactive({
  enabled: true,
  confirmMin: 10,
  maxRank: 5,
  recurringMaxCount: 4,
  approvalEnabled: false,
  approvalTimeout: 2
})

const waitlistMap: Record<string, ConfigInfo> = {}
const recurringMap: Record<string, ConfigInfo> = {}
const approvalMap: Record<string, ConfigInfo> = {}

const loadData = async () => {
  loading.value = true
  try {
    const [waitlistRes, recurringRes, approvalRes] = await Promise.all([
      getWaitlistRules(),
      getRecurringRules(),
      getApprovalRules()
    ])

    waitlistRes.data.forEach((item) => { waitlistMap[item.configKey] = item })
    recurringRes.data.forEach((item) => { recurringMap[item.configKey] = item })
    approvalRes.data.forEach((item) => { approvalMap[item.configKey] = item })

    formData.enabled = waitlistMap['waitlist.enabled']?.configValue === 'true'
    formData.confirmMin = Number(waitlistMap['waitlist.confirm_min']?.configValue) || 10
    formData.maxRank = Number(waitlistMap['waitlist.max_rank']?.configValue) || 5
    formData.recurringMaxCount = Number(recurringMap['recurring.max_count']?.configValue) || 4
    formData.approvalEnabled = approvalMap['approval.enabled']?.configValue === 'true'
    formData.approvalTimeout = Number(approvalMap['approval.timeout_hour']?.configValue) || 2
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const waitlistConfigs: ConfigInfo[] = [
      { id: 0, configKey: 'waitlist.enabled', configValue: String(formData.enabled), configName: '是否开启候补' },
      { id: 0, configKey: 'waitlist.confirm_min', configValue: String(formData.confirmMin), configName: '候补确认时限' },
      { id: 0, configKey: 'waitlist.max_rank', configValue: String(formData.maxRank), configName: '候补队列上限' }
    ]
    const recurringConfigs: ConfigInfo[] = [
      { id: 0, configKey: 'recurring.max_count', configValue: String(formData.recurringMaxCount), configName: '周期预约最大次数' }
    ]
    const approvalConfigs: ConfigInfo[] = [
      { id: 0, configKey: 'approval.enabled', configValue: String(formData.approvalEnabled), configName: '是否启用审批' },
      { id: 0, configKey: 'approval.timeout_hour', configValue: String(formData.approvalTimeout), configName: '审批超时' }
    ]

    await Promise.all([
      updateWaitlistRules(waitlistConfigs),
      updateRecurringRules(recurringConfigs),
      updateApprovalRules(approvalConfigs)
    ])
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

.mt-4 {
  margin-top: 16px;
}
</style>
