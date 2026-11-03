<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>信用记录查询</span>
          <div style="display:flex;gap:12px;align-items:center">
            <el-input v-model="searchUserId" placeholder="输入用户ID" style="width:160px" clearable />
            <el-button type="primary" @click="handleSearch" :disabled="!searchUserId">查询</el-button>
          </div>
        </div>
      </template>
      <el-table :data="records" style="width:100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="changeType" label="变动类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="row.changeValue > 0 ? 'success' : 'danger'">
              {{ typeLabel(row.changeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeValue" label="变动值" width="90">
          <template #default="{ row }">
            <span :style="{ color: row.changeValue > 0 ? '#16a34a' : '#dc2626', fontWeight: 600 }">
              {{ row.changeValue > 0 ? '+' : '' }}{{ row.changeValue }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="分值变化" width="130">
          <template #default="{ row }">
            <span class="num">{{ row.beforeScore }} → {{ row.afterScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="170">
          <template #default="{ row }">
            {{ row.createTime ? row.createTime.replace('T', ' ').slice(0, 19) : '' }}
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:16px;text-align:right" v-if="total > 20">
        <el-pagination
          layout="prev, pager, next"
          :total="total"
          :page-size="20"
          v-model:current-page="pageNum"
          @current-change="loadRecords"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getCreditRecords } from '@/api'
import type { CreditRecord } from '@/types'

const searchUserId = ref('')
const records = ref<CreditRecord[]>([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)

function typeLabel(t: string) {
  const map: Record<string, string> = {
    no_show: '爽约扣分',
    complete: '完成加分',
    weekly_recover: '周恢复',
    cancel_late: '逾期取消',
    admin_adjust: '管理员调整'
  }
  return map[t] || t
}

async function loadRecords() {
  if (!searchUserId.value) return
  loading.value = true
  try {
    const res = await getCreditRecords({
      userId: Number(searchUserId.value),
      pageNum: pageNum.value,
      pageSize: 20
    })
    records.value = res.data.list || []
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadRecords()
}
</script>
