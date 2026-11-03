<template>
  <div class="repair-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">报修管理</h1>
        <p class="page-description">处理会议室设备报修工单，闭环管理</p>
      </div>
    </div>

    <div class="filter-card">
      <div class="filter-row">
        <el-select v-model="queryParams.status" placeholder="按状态筛选" clearable class="filter-item">
          <el-option label="待处理" value="pending" />
          <el-option label="处理中" value="processing" />
          <el-option label="已完成" value="finished" />
        </el-select>
        <el-select v-model="queryParams.roomId" placeholder="按会议室筛选" clearable filterable class="filter-item">
          <el-option v-for="room in roomList" :key="room.id" :label="room.roomName" :value="room.id" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="工单编号" width="180" />
        <el-table-column prop="roomName" label="会议室" min-width="120" />
        <el-table-column label="设备类型" width="100">
          <template #default="{ row }">{{ row.deviceTypeName || row.deviceType }}</template>
        </el-table-column>
        <el-table-column prop="faultDesc" label="故障描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="reporterName" label="报修人" width="100" />
        <el-table-column prop="createTime" label="报修时间" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-tag', row.status]">{{ row.statusName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'pending'">
              <el-button type="primary" link @click="handleAssign(row)">接单</el-button>
            </template>
            <template v-else-if="row.status === 'processing'">
              <el-button type="success" link @click="handleFinish(row)">完成</el-button>
            </template>
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

    <!-- 完成工单对话框 -->
    <el-dialog v-model="finishDialogVisible" title="完成工单" width="500px" class="finish-dialog">
      <el-input v-model="handleDesc" type="textarea" :rows="4" placeholder="请输入处理说明" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="finishDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="confirmFinish">确认完成</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRepairList, getRoomList, getUserList, assignRepair, finishRepair } from '@/api'
import type { RepairOrderInfo, RoomInfo, UserInfo } from '@/types'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<RepairOrderInfo[]>([])
const roomList = ref<RoomInfo[]>([])
const userList = ref<UserInfo[]>([])
const total = ref(0)

const queryParams = reactive({
  status: '',
  roomId: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

const finishDialogVisible = ref(false)
const currentRepair = ref<RepairOrderInfo | null>(null)
const handleDesc = ref('')

const deviceTypeMap: Record<string, string> = {
  projector: '投影仪',
  tv: '电视',
  whiteboard: '白板',
  aircon: '空调',
  light: '灯光',
  other: '其他'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRepairList(queryParams)
    tableData.value = res.data.list.map((item) => ({
      ...item,
      deviceTypeName: deviceTypeMap[item.deviceType] || item.deviceType,
      reporterName: getUsername(item.reporterId)
    }))
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const getUsername = (id?: number) => {
  if (!id) return ''
  const user = userList.value.find((u) => u.id === id)
  return user ? (user.realName || user.username) : String(id)
}

const resetQuery = () => {
  queryParams.status = ''
  queryParams.roomId = undefined
  queryParams.pageNum = 1
  loadData()
}

const handleAssign = async (row: RepairOrderInfo) => {
  submitLoading.value = true
  try {
    await assignRepair(row.id, 1)
    ElMessage.success('已接单')
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleFinish = (row: RepairOrderInfo) => {
  currentRepair.value = row
  handleDesc.value = ''
  finishDialogVisible.value = true
}

const confirmFinish = async () => {
  if (!currentRepair.value) return
  if (!handleDesc.value.trim()) {
    ElMessage.warning('请输入处理说明')
    return
  }
  submitLoading.value = true
  try {
    await finishRepair(currentRepair.value.id, handleDesc.value)
    ElMessage.success('工单已完成')
    finishDialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
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
.repair-page {
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

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &.pending {
    background: #fdf6ec;
    color: #d4845a;
  }

  &.processing {
    background: #eaf3fd;
    color: #5a8cd4;
  }

  &.finished {
    background: #f0f9eb;
    color: #7c9a72;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
