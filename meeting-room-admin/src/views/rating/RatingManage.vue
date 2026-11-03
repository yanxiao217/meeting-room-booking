<template>
  <div>
    <el-card>
      <template #header>
        <span>会议室评价统计</span>
      </template>
      <el-table :data="roomStats" style="width:100%" v-loading="loadingStats" @row-click="selectRoom">
        <el-table-column prop="roomId" label="会议室ID" width="100" />
        <el-table-column prop="roomName" label="会议室" />
        <el-table-column prop="ratingCount" label="评价数" width="100" />
        <el-table-column prop="avgRating" label="平均分" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.avgRating >= 4 ? '#16a34a' : row.avgRating >= 3 ? '#f59e0b' : '#dc2626', fontWeight: 600 }">
              {{ row.avgRating || '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="评分分布" width="280">
          <template #default="{ row }">
            <div class="dist-bar" v-if="selectedRoomId === row.roomId && distribution.length">
              <span v-for="d in distribution" :key="d.rating" class="dist-item">
                {{ d.rating }}★ <span class="num">{{ d.cnt }}</span>
              </span>
            </div>
            <span v-else class="muted" style="font-size:12px">点击查看详情</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card style="margin-top:24px">
      <template #header>
        <span>低分检查列表</span>
      </template>
      <el-table :data="pendingChecks" style="width:100%" v-loading="loadingChecks">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="roomName" label="会议室" width="140" />
        <el-table-column prop="userName" label="评价人" width="100" />
        <el-table-column prop="rating" label="评分" width="80">
          <template #default="{ row }">
            <span :style="{ color: row.rating <= 2 ? '#dc2626' : '#f59e0b', fontWeight: 600 }">{{ row.rating }}★</span>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评价内容" show-overflow-tooltip />
        <el-table-column prop="remark" label="检查备注" show-overflow-tooltip />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" type="warning" @click="doConvert(row)" :loading="convertingId === row.id">
              转报修
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoomRatingStats, getRatingDistribution, getPendingRatingChecks, convertToRepair } from '@/api'

const roomStats = ref<any[]>([])
const pendingChecks = ref<any[]>([])
const distribution = ref<any[]>([])
const selectedRoomId = ref<number | null>(null)
const loadingStats = ref(false)
const loadingChecks = ref(false)
const convertingId = ref<number | null>(null)

async function loadStats() {
  loadingStats.value = true
  try {
    const res = await getRoomRatingStats()
    roomStats.value = res.data || []
  } finally {
    loadingStats.value = false
  }
}

async function loadChecks() {
  loadingChecks.value = true
  try {
    const res = await getPendingRatingChecks()
    pendingChecks.value = res.data || []
  } finally {
    loadingChecks.value = false
  }
}

async function selectRoom(row: any) {
  selectedRoomId.value = row.roomId
  try {
    const res = await getRatingDistribution(row.roomId)
    distribution.value = res.data || []
  } catch {
    distribution.value = []
  }
}

async function doConvert(row: any) {
  try {
    await ElMessageBox.confirm(
      `确认将该低分检查记录转为报修工单？`,
      '转报修确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  convertingId.value = row.id
  try {
    await convertToRepair(row.id)
    ElMessage.success('已转为报修工单')
    loadChecks()
  } finally {
    convertingId.value = null
  }
}

onMounted(() => {
  loadStats()
  loadChecks()
})
</script>

<style scoped>
.dist-bar { display: flex; gap: 12px; }
.dist-item { font-size: 13px; color: var(--el-text-color-secondary); }
.muted { color: var(--el-text-color-placeholder); }
</style>
