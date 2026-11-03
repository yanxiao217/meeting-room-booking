<template>
  <div class="waitlist-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">候补管理</h1>
        <p class="page-description">查看候补队列，手动补位调整顺序</p>
      </div>
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabChange" class="waitlist-tabs">
      <el-tab-pane label="进行中候补" name="active" />
      <el-tab-pane label="历史候补" name="historical" />
    </el-tabs>

    <div class="filter-card">
      <div class="filter-row">
        <el-select v-model="queryParams.roomId" placeholder="按会议室筛选" clearable filterable class="filter-item">
          <el-option v-for="room in roomList" :key="room.id" :label="room.roomName" :value="room.id" />
        </el-select>
        <el-date-picker
          v-model="queryParams.bookingDate"
          type="date"
          placeholder="按日期筛选"
          value-format="YYYY-MM-DD"
          clearable
          class="filter-item"
        />
        <el-select 
          v-model="queryParams.status" 
          placeholder="按状态筛选" 
          clearable 
          class="filter-item"
        >
          <el-option 
            v-for="opt in statusOptions" 
            :key="opt.value" 
            :label="opt.label" 
            :value="opt.value" 
          />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="roomName" label="会议室" min-width="120" />
        <el-table-column prop="bookingDate" label="日期" width="120" />
        <el-table-column label="时间段" width="150">
          <template #default="{ row }">
            <span class="time-range">{{ row.startTime }} - {{ row.endTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="候补人" width="100" />
        <el-table-column prop="rankNo" label="候补顺序" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">第 {{ row.rankNo }} 位</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-tag', row.status]">{{ row.statusName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="加入时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'waiting'" type="primary" link @click="handlePromote(row)">
              手动补位
            </el-button>
            <span v-else class="no-action">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWaitlistList, getRoomList, getUserList, promoteWaitlist } from '@/api'
import type { WaitlistInfo, RoomInfo, UserInfo } from '@/types'

const loading = ref(false)
const tableData = ref<WaitlistInfo[]>([])
const roomList = ref<RoomInfo[]>([])
const userList = ref<UserInfo[]>([])
const total = ref(0)

// 当前选中的 tab：active=进行中, historical=历史
const activeTab = ref<'active' | 'historical'>('active')

const queryParams = reactive({
  roomId: undefined as number | undefined,
  bookingDate: '',
  status: '',
  activeOnly: true,
  pageNum: 1,
  pageSize: 10
})

// 根据 tab 显示不同的状态选项
const statusOptions = computed(() => {
  if (activeTab.value === 'active') {
    return [
      { label: '等待中', value: 'waiting' },
      { label: '已通知', value: 'notified' },
      { label: '已确认', value: 'confirmed' }
    ]
  } else {
    return [
      { label: '已放弃', value: 'cancelled' },
      { label: '已超时', value: 'expired' }
    ]
  }
})

const getUsername = (id?: number) => {
  if (!id) return ''
  const user = userList.value.find((u) => u.id === id)
  return user ? (user.realName || user.username) : String(id)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getWaitlistList(queryParams)
    tableData.value = res.data.list.map((item) => ({
      ...item,
      roomName: getRoomName(item.roomId),
      userName: getUsername(item.userId)
    }))
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const getRoomName = (id: number) => {
  const room = roomList.value.find((r) => r.id === id)
  return room ? room.roomName : String(id)
}

const handleTabChange = (pane: any) => {
  const name = pane?.paneName ?? activeTab.value
  activeTab.value = name
  queryParams.status = ''
  queryParams.activeOnly = name === 'active'
  queryParams.pageNum = 1
  loadData()
}

const resetQuery = () => {
  queryParams.roomId = undefined
  queryParams.bookingDate = ''
  queryParams.status = ''
  queryParams.activeOnly = activeTab.value === 'active'
  queryParams.pageNum = 1
  loadData()
}

const handlePromote = async (row: WaitlistInfo) => {
  await ElMessageBox.confirm(
    `确定要补位候补人「${row.userName}」吗？系统将通知其限时确认。`,
    '手动补位',
    { type: 'warning' }
  )
  await promoteWaitlist(row.id)
  ElMessage.success('已通知候补人')
  loadData()
}

const loadRoomList = async () => {
  const res = await getRoomList()
  roomList.value = res.data
}

const loadUserList = async () => {
  const res = await getUserList({ pageSize: 1000 })
  userList.value = res.data.list
}

onMounted(() => {
  loadData()
  loadRoomList()
  loadUserList()
})
</script>

<style lang="scss" scoped>
.waitlist-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #2d3748;
    margin-bottom: 4px;
  }

  .page-description {
    font-size: 14px;
    color: #718096;
  }
}

.waitlist-tabs {
  margin-bottom: 16px;
  
  :deep(.el-tabs__header) {
    border-bottom: 1px solid #f0ede8;
  }
  
  :deep(.el-tabs__item) {
    color: #4a5568;
    font-weight: 500;
  }
  
  :deep(.el-tabs__item.is-active) {
    color: #161a17;
  }
  
  :deep(.el-tabs__underline) {
    background: #f3e24a;
    height: 3px;
  }
}

.filter-card {
  background: #ffffff;
  border: 1px solid #f0ede8;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;

  .filter-row {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;

    .filter-item {
      width: 240px;
    }
  }
}

.table-card {
  background: #ffffff;
  border: 1px solid #f0ede8;
  border-radius: 12px;
  overflow: hidden;

  :deep(.el-table) {
    --el-table-border-color: #f0ede8;
    --el-table-header-bg-color: #fafaf9;
  }
}

.time-range {
  font-family: 'SF Mono', 'Monaco', monospace;
  font-size: 13px;
  color: #4a5568;
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &.waiting {
    background: #fdf6ec;
    color: #d4845a;
  }

  &.notified {
    background: #eaf3fd;
    color: #5a8cd4;
  }
  
  &.confirmed {
    background: #f0f9eb;
    color: #7c9a72;
  }

  &.cancelled {
    background: #fef0f0;
    color: #c47070;
  }
  
  &.expired {
    background: #f7f6f3;
    color: #a0aec0;
  }
}

.no-action {
  color: #a0aec0;
}

.pagination-container {
  padding: 16px 20px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #f0ede8;
}
</style>