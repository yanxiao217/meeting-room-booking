<template>
  <div class="booking-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">预约管理</h1>
        <p class="page-description">查看和管理所有会议室预约</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdminBooking">
          <el-icon class="mr-1"><Plus /></el-icon>
          代客预约
        </el-button>
      </div>
    </div>
    
    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabChange" class="booking-tabs">
      <el-tab-pane label="进行中预约" name="active" />
      <el-tab-pane label="历史预约" name="historical" />
    </el-tabs>

    <!-- 筛选栏 -->
    <div class="filter-card">
      <div class="filter-row">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          class="filter-item"
        />
        <el-select 
          v-model="queryParams.roomId" 
          placeholder="按会议室筛选" 
          clearable 
          filterable 
          class="filter-item"
        >
          <el-option v-for="room in roomList" :key="room.id" :label="room.roomName" :value="room.id" />
        </el-select>
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
    
    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="bookingNo" label="预约编号" width="180" />
        <el-table-column prop="roomName" label="会议室" min-width="120">
          <template #default="{ row }">
            <div class="room-name">{{ row.roomName }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="预约人" width="100" />
        <el-table-column prop="bookingDate" label="预约日期" width="120" />
        <el-table-column label="预约时间" width="150">
          <template #default="{ row }">
            <span class="time-range">{{ row.startTime }} - {{ row.endTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="topic" label="会议主题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="attendeeCount" label="参会人数" width="90">
          <template #default="{ row }">
            <span class="attendee-count">{{ row.attendeeCount }}人</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-tag', row.status]">{{ row.statusName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'pending'">
              <el-button type="success" link @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" link @click="handleReject(row)">拒绝</el-button>
            </template>
            <el-button
              v-else-if="row.status === 'confirmed'"
              type="danger"
              link
              @click="handleForceCancel(row)"
            >
              强制取消
            </el-button>
            <span v-else class="no-action">-</span>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
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
    
    <!-- 强制取消对话框 -->
    <el-dialog v-model="cancelDialogVisible" title="强制取消预约" width="500px" class="cancel-dialog">
      <div class="cancel-content">
        <p class="cancel-tip">确定要取消以下预约吗？</p>
        <div v-if="currentBooking" class="booking-info">
          <div class="info-row">
            <span class="info-label">会议室</span>
            <span class="info-value">{{ currentBooking.roomName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">时间</span>
            <span class="info-value">{{ currentBooking.bookingDate }} {{ currentBooking.startTime }}-{{ currentBooking.endTime }}</span>
          </div>
        </div>
        <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="请输入取消原因" class="cancel-reason" />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="danger" :loading="submitLoading" @click="confirmForceCancel">确认取消</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 代客预约对话框 -->
    <el-dialog v-model="adminBookingVisible" title="代客预约" width="600px" destroy-on-close class="booking-dialog">
      <el-form
        ref="bookingFormRef"
        :model="bookingFormData"
        :rules="bookingFormRules"
        label-width="100px"
        class="booking-form"
      >
        <el-form-item label="预约人" prop="userId">
          <el-select v-model="bookingFormData.userId" placeholder="请选择预约人" filterable class="w-full">
            <el-option v-for="user in userList" :key="user.id" :label="user.realName || user.username" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="会议室" prop="roomId">
          <el-select v-model="bookingFormData.roomId" placeholder="请选择会议室" filterable class="w-full">
            <el-option v-for="room in roomList" :key="room.id" :label="room.roomName" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期" prop="bookingDate">
          <el-date-picker
            v-model="bookingFormData.bookingDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            :disabled-date="(date: Date) => date < new Date()"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="时间段" prop="timeRange">
          <el-time-picker
            v-model="bookingFormData.timeRange"
            is-range
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="HH:mm"
            value-format="HH:mm"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="会议主题" prop="topic">
          <el-input v-model="bookingFormData.topic" placeholder="请输入会议主题" />
        </el-form-item>
        <el-form-item label="参会人数">
          <el-input-number v-model="bookingFormData.attendeeCount" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="adminBookingVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitAdminBooking">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审批拒绝对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="审批拒绝" width="500px" class="cancel-dialog">
      <div class="cancel-content">
        <p class="cancel-tip">确定拒绝以下预约申请吗？</p>
        <div v-if="currentBooking" class="booking-info">
          <div class="info-row">
            <span class="info-label">会议室</span>
            <span class="info-value">{{ currentBooking.roomName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">时间</span>
            <span class="info-value">{{ currentBooking.bookingDate }} {{ currentBooking.startTime }}-{{ currentBooking.endTime }}</span>
          </div>
        </div>
        <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" class="cancel-reason" />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" :loading="submitLoading" @click="confirmReject">确认拒绝</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getBookingList, getRoomList, getUserList, forceCancelBooking, createBookingByAdmin, approveBooking, rejectBooking } from '@/api'
import type { BookingInfo, RoomInfo, UserInfo } from '@/types'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<BookingInfo[]>([])
const roomList = ref<RoomInfo[]>([])
const userList = ref<UserInfo[]>([])
const total = ref(0)

const dateRange = ref<[string, string] | null>(null)

// 当前选中的 tab：active=进行中, historical=历史
const activeTab = ref<'active' | 'historical'>('active')

const queryParams = reactive({
  startDate: '',
  endDate: '',
  roomId: undefined as number | undefined,
  status: '',
  activeOnly: true,
  pageNum: 1,
  pageSize: 10
})

// 根据 tab 显示不同的状态选项
const statusOptions = computed(() => {
  if (activeTab.value === 'active') {
    return [
      { label: '待审批', value: 'pending' },
      { label: '已确认', value: 'confirmed' },
      { label: '已签到', value: 'signed' }
    ]
  } else {
    return [
      { label: '已完成', value: 'completed' },
      { label: '已取消', value: 'cancelled' },
      { label: '已过期', value: 'expired' }
    ]
  }
})

// 强制取消相关
const cancelDialogVisible = ref(false)
const currentBooking = ref<BookingInfo | null>(null)
const cancelReason = ref('')

// 审批拒绝相关
const rejectDialogVisible = ref(false)
const rejectReason = ref('')

// 代客预约相关
const adminBookingVisible = ref(false)
const bookingFormRef = ref<FormInstance>()
const bookingFormData = reactive({
  userId: undefined as number | undefined,
  roomId: undefined as number | undefined,
  bookingDate: '',
  timeRange: [] as string[],
  topic: '',
  attendeeCount: 1
})

const bookingFormRules: FormRules = {
  userId: [{ required: true, message: '请选择预约人', trigger: 'change' }],
  roomId: [{ required: true, message: '请选择会议室', trigger: 'change' }],
  bookingDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  topic: [{ required: true, message: '请输入会议主题', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    if (dateRange.value) {
      queryParams.startDate = dateRange.value[0]
      queryParams.endDate = dateRange.value[1]
    } else {
      queryParams.startDate = ''
      queryParams.endDate = ''
    }
    
    const res = await getBookingList(queryParams)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleTabChange = (pane: any) => {
  const name = pane?.paneName ?? activeTab.value
  activeTab.value = name
  // 切换 tab 时重置状态筛选
  queryParams.status = ''
  queryParams.activeOnly = name === 'active'
  queryParams.pageNum = 1
  loadData()
}

const resetQuery = () => {
  dateRange.value = null
  queryParams.startDate = ''
  queryParams.endDate = ''
  queryParams.roomId = undefined
  queryParams.status = ''
  queryParams.activeOnly = activeTab.value === 'active'
  queryParams.pageNum = 1
  loadData()
}

const handleForceCancel = (row: BookingInfo) => {
  currentBooking.value = row
  cancelReason.value = ''
  cancelDialogVisible.value = true
}

const handleApprove = async (row: BookingInfo) => {
  await ElMessageBox.confirm(`确定通过「${row.topic}」的预约申请吗？`, '审批通过', { type: 'success' })
  submitLoading.value = true
  try {
    await approveBooking(row.id)
    ElMessage.success('已通过审批')
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleReject = (row: BookingInfo) => {
  currentBooking.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!currentBooking.value) return
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  submitLoading.value = true
  try {
    await rejectBooking(currentBooking.value.id, rejectReason.value)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const confirmForceCancel = async () => {
  if (!currentBooking.value) return
  if (!cancelReason.value.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }
  
  submitLoading.value = true
  try {
    await forceCancelBooking(currentBooking.value.id, cancelReason.value)
    ElMessage.success('取消成功')
    cancelDialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleAdminBooking = async () => {
  resetBookingForm()
  adminBookingVisible.value = true
}

const submitAdminBooking = async () => {
  if (!bookingFormRef.value) return
  
  await bookingFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      const [startTime, endTime] = bookingFormData.timeRange
      await createBookingByAdmin({
        userId: bookingFormData.userId,
        roomId: bookingFormData.roomId,
        bookingDate: bookingFormData.bookingDate,
        startTime,
        endTime,
        topic: bookingFormData.topic,
        attendeeCount: bookingFormData.attendeeCount
      })
      ElMessage.success('预约成功')
      adminBookingVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

const resetBookingForm = () => {
  Object.assign(bookingFormData, {
    userId: undefined,
    roomId: undefined,
    bookingDate: '',
    timeRange: [],
    topic: '',
    attendeeCount: 1
  })
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
.booking-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  
  .header-content {
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #2D3748;
      margin-bottom: 4px;
    }
    
    .page-description {
      font-size: 14px;
      color: #718096;
    }
  }
  
  .header-actions {
    .mr-1 {
      margin-right: 6px;
    }
  }
}

.booking-tabs {
  margin-bottom: 16px;
  
  :deep(.el-tabs__header) {
    border-bottom: 1px solid #F0EDE8;
  }
  
  :deep(.el-tabs__item) {
    color: #4A5568;
    font-weight: 500;
  }
  
  :deep(.el-tabs__item.is-active) {
    color: #161A17;
  }
  
  :deep(.el-tabs__underline) {
    background: #F3E24A;
    height: 3px;
  }
}

.filter-card {
  background: #FFFFFF;
  border: 1px solid #F0EDE8;
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
  background: #FFFFFF;
  border: 1px solid #F0EDE8;
  border-radius: 12px;
  overflow: hidden;
  
  :deep(.el-table) {
    --el-table-border-color: #F0EDE8;
    --el-table-header-bg-color: #FAFAF9;
    
    th.el-table__cell {
      font-weight: 600;
      color: #4A5568;
    }
  }
}

.room-name {
  font-weight: 500;
  color: #2D3748;
}

.time-range {
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Fira Mono', 'Droid Sans Mono', 'Source Code Pro', monospace;
  font-size: 13px;
  color: #4A5568;
}

.attendee-count {
  font-weight: 500;
  color: #2D3748;
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  
  &.pending {
    background: #FDF6EC;
    color: #D4845A;
  }
  
  &.confirmed {
    background: #F0F9EB;
    color: #7C9A72;
  }
  
  &.signed {
    background: #EAF3FD;
    color: #5A8CD4;
  }
  
  &.completed {
    background: #F7F6F3;
    color: #718096;
  }
  
  &.cancelled {
    background: #FEF0F0;
    color: #C47070;
  }
  
  &.expired {
    background: #F7F6F3;
    color: #A0AEC0;
  }
}

.no-action {
  color: #A0AEC0;
}

.pagination-container {
  padding: 16px 20px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #F0EDE8;
}

.cancel-dialog {
  .cancel-content {
    .cancel-tip {
      color: #4A5568;
      margin-bottom: 16px;
    }
    
    .booking-info {
      background: #FAFAF9;
      border-radius: 8px;
      padding: 12px 16px;
      margin-bottom: 16px;
      
      .info-row {
        display: flex;
        margin-bottom: 8px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .info-label {
          width: 60px;
          color: #718096;
        }
        
        .info-value {
          color: #2D3748;
          font-weight: 500;
        }
      }
    }
    
    .cancel-reason {
      margin-top: 16px;
    }
  }
}

.booking-dialog {
  .booking-form {
    .w-full {
      width: 100%;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>