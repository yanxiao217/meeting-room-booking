<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>信用分规则配置</span>
          <el-button type="primary" @click="handleSave" :loading="saving">保存配置</el-button>
        </div>
      </template>
      <el-table :data="configs" style="width:100%" v-loading="loading">
        <el-table-column prop="configName" label="配置项" width="200" />
        <el-table-column prop="configValue" label="值" width="160">
          <template #default="{ row }">
            <el-input v-model="row.configValue" :placeholder="row.description" />
          </template>
        </el-table-column>
        <el-table-column prop="configKey" label="配置键" width="220" />
        <el-table-column prop="description" label="说明" />
      </el-table>
    </el-card>

    <el-card style="margin-top:24px">
      <template #header>
        <span>低分用户列表</span>
      </template>
      <el-table :data="lowUsers" style="width:100%" v-loading="loadingUsers">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="creditScore" label="信用分" width="100">
          <template #default="{ row }">
            <el-tag :type="row.creditScore >= 80 ? 'success' : row.creditScore >= 60 ? 'warning' : 'danger'">
              {{ row.creditScore }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openAdjust(row)">调整分数</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="adjustVisible" title="调整信用分" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span>{{ adjustForm.username }}（当前 {{ adjustForm.currentScore }} 分）</span>
        </el-form-item>
        <el-form-item label="调整值">
          <el-input-number v-model="adjustForm.value" :min="-50" :max="50" />
          <span style="margin-left:8px;color:#909399;font-size:12px">正数加分，负数扣分</span>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="2" placeholder="必填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdjust" :loading="adjusting">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCreditRules, updateCreditRules, getLowScoreUsers, adjustCredit } from '@/api'
import type { ConfigInfo, UserInfo } from '@/types'

const configs = ref<ConfigInfo[]>([])
const lowUsers = ref<UserInfo[]>([])
const loading = ref(false)
const saving = ref(false)
const loadingUsers = ref(false)
const adjustVisible = ref(false)
const adjusting = ref(false)

const adjustForm = ref({
  userId: 0,
  username: '',
  currentScore: 0,
  value: 0,
  reason: ''
})

async function loadConfigs() {
  loading.value = true
  try {
    const res = await getCreditRules()
    configs.value = res.data
  } finally {
    loading.value = false
  }
}

async function loadLowUsers() {
  loadingUsers.value = true
  try {
    const res = await getLowScoreUsers()
    lowUsers.value = res.data
  } finally {
    loadingUsers.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    await updateCreditRules(configs.value)
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

function openAdjust(user: UserInfo) {
  adjustForm.value = {
    userId: user.id,
    username: user.username,
    currentScore: (user as any).creditScore || 100,
    value: 0,
    reason: ''
  }
  adjustVisible.value = true
}

async function submitAdjust() {
  if (!adjustForm.value.reason.trim()) {
    ElMessage.warning('请填写调整原因')
    return
  }
  adjusting.value = true
  try {
    await adjustCredit({
      userId: adjustForm.value.userId,
      value: adjustForm.value.value,
      reason: adjustForm.value.reason
    })
    ElMessage.success('调整成功')
    adjustVisible.value = false
    loadLowUsers()
  } finally {
    adjusting.value = false
  }
}

onMounted(() => {
  loadConfigs()
  loadLowUsers()
})
</script>
