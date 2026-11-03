<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>智能问答日志</span>
          <div style="display:flex;gap:12px;align-items:center">
            <el-select v-model="query.intent" placeholder="意图" clearable style="width:170px">
              <el-option label="查询空闲会议室" value="find_available_rooms" />
              <el-option label="查询我的预约" value="my_bookings" />
              <el-option label="查询会议室详情" value="room_info" />
              <el-option label="未知意图" value="unknown" />
            </el-select>
            <el-select v-model="query.success" placeholder="结果" clearable style="width:120px">
              <el-option label="成功" :value="1" />
              <el-option label="失败" :value="0" />
            </el-select>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </div>
        </div>
      </template>

      <el-table :data="logs" style="width:100%" v-loading="loading">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand">
              <p><strong>问题：</strong>{{ row.question }}</p>
              <p><strong>回答：</strong>{{ row.answer }}</p>
              <p><strong>参数：</strong><code>{{ row.params || '{}' }}</code></p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="intent" label="意图" width="150">
          <template #default="{ row }">
            <el-tag size="small">{{ intentLabel(row.intent) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resultCount" label="结果数" width="80" />
        <el-table-column prop="success" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small">
              {{ row.success === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costMs" label="耗时(ms)" width="100" />
        <el-table-column prop="createTime" label="提问时间" width="170">
          <template #default="{ row }">
            {{ row.createTime ? row.createTime.replace('T', ' ').slice(0, 19) : '' }}
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:16px;text-align:right">
        <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :page-size="query.pageSize"
          v-model:current-page="query.pageNum"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { getChatbotLogs } from '@/api'
import type { ChatbotLogInfo } from '@/types'

const loading = ref(false)
const logs = ref<ChatbotLogInfo[]>([])
const total = ref(0)

const query = reactive<{ intent?: string; success?: number; pageNum: number; pageSize: number }>({
  intent: '',
  success: undefined,
  pageNum: 1,
  pageSize: 20
})

function intentLabel(t?: string) {
  const map: Record<string, string> = {
    find_available_rooms: '查空闲会议室',
    my_bookings: '查我的预约',
    room_info: '查会议室详情',
    unknown: '未知'
  }
  return t ? map[t] || t : '—'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getChatbotLogs({
      intent: query.intent || undefined,
      success: query.success,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    logs.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  loadData()
}

function resetQuery() {
  query.intent = ''
  query.success = undefined
  query.pageNum = 1
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.expand {
  padding: 8px 16px;
  line-height: 1.8;
}
.expand p {
  margin: 0;
}
.expand code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
