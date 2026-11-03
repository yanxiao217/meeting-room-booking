<template>
  <div class="room-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">会议室管理</h1>
        <p class="page-description">管理所有会议室资源与配置</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon class="mr-1"><Plus /></el-icon>
          新增会议室
        </el-button>
      </div>
    </div>
    
    <!-- 筛选栏 -->
    <div class="filter-card">
      <div class="filter-row">
        <el-input 
          v-model="queryParams.floor" 
          placeholder="按楼层筛选" 
          clearable 
          prefix-icon="Location"
          class="filter-item"
        />
        <el-input 
          v-model.number="queryParams.capacity" 
          placeholder="最小容量" 
          clearable 
          prefix-icon="User"
          class="filter-item"
        />
        <el-select 
          v-model="queryParams.status" 
          placeholder="按状态筛选" 
          clearable 
          prefix-icon="Filter"
          class="filter-item"
        >
          <el-option label="可用" :value="1" />
          <el-option label="维护中" :value="0" />
          <el-option label="停用" :value="2" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
    </div>
    
    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="roomName" label="会议室名称" min-width="120">
          <template #default="{ row }">
            <div class="room-name">{{ row.roomName }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="floor" label="楼层" width="100" />
        <el-table-column prop="capacity" label="容量" width="80">
          <template #default="{ row }">
            <span class="capacity-badge">{{ row.capacity }}人</span>
          </template>
        </el-table-column>
        <el-table-column prop="roomTypeName" label="类型" width="100" />
        <el-table-column label="设备" min-width="150">
          <template #default="{ row }">
            <div class="equipment-tags">
              <el-tag v-for="item in (row.equipment || [])" :key="item" size="small" type="info">
                {{ getEquipmentName(item) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-dot', getStatusClass(row.status)]">
              {{ row.statusName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="审批" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.needApproval === 1" type="warning" size="small">需审批</el-tag>
            <span v-else class="no-action">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="todayBookingCount" label="今日预约" width="100">
          <template #default="{ row }">
            <span class="booking-count">{{ row.todayBookingCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-dropdown @command="(cmd: number) => handleStatusChange(row, cmd)">
                <el-button type="primary" link>状态</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="1" :disabled="row.status === 1">设为可用</el-dropdown-item>
                    <el-dropdown-item :command="0" :disabled="row.status === 0">设为维护中</el-dropdown-item>
                    <el-dropdown-item :command="2" :disabled="row.status === 2">设为停用</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-popconfirm title="确定删除该会议室吗？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button type="danger" link>删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
      class="room-dialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="room-form"
      >
        <el-form-item label="会议室名称" prop="roomName">
          <el-input v-model="formData.roomName" placeholder="请输入会议室名称" />
        </el-form-item>
        <el-form-item label="所在楼层" prop="floor">
          <el-input v-model="formData.floor" placeholder="如: 3楼" />
        </el-form-item>
        <el-form-item label="具体位置">
          <el-input v-model="formData.location" placeholder="如: 3楼东侧A区" />
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="formData.capacity" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="formData.roomType" class="w-full">
            <el-option label="会议室" value="meeting" />
            <el-option label="培训室" value="training" />
            <el-option label="电话亭" value="phone" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备">
          <el-checkbox-group v-model="formData.equipment" class="equipment-checkbox">
            <el-checkbox label="projector">投影仪</el-checkbox>
            <el-checkbox label="tv">电视/大屏</el-checkbox>
            <el-checkbox label="whiteboard">白板</el-checkbox>
            <el-checkbox label="video_conference">视频会议</el-checkbox>
            <el-checkbox label="音响">音响</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status" class="status-radio">
            <el-radio :label="1">可用</el-radio>
            <el-radio :label="0">维护中</el-radio>
            <el-radio :label="2">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="预约审批">
          <el-switch v-model="formData.needApproval" :active-value="1" :inactive-value="0" />
          <span class="form-tip">开启后该会议室的预约需管理员审批</span>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="会议室描述或备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getRoomList, createRoom, updateRoom, deleteRoom, updateRoomStatus } from '@/api'
import type { RoomInfo } from '@/types'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<RoomInfo[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref(0)

const formRef = ref<FormInstance>()

const queryParams = reactive({
  floor: '',
  capacity: undefined as number | undefined,
  status: undefined as number | undefined
})

const formData = reactive({
  roomName: '',
  floor: '',
  location: '',
  capacity: 6,
  roomType: 'meeting',
  equipment: [] as string[],
  images: [] as string[],
  status: 1,
  needApproval: 0,
  description: '',
  sort: 0
})

const formRules: FormRules = {
  roomName: [{ required: true, message: '请输入会议室名称', trigger: 'blur' }],
  floor: [{ required: true, message: '请输入所在楼层', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容纳人数', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoomList(queryParams)
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.floor = ''
  queryParams.capacity = undefined
  queryParams.status = undefined
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增会议室'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row: RoomInfo) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑会议室'
  Object.assign(formData, {
    roomName: row.roomName,
    floor: row.floor,
    location: row.location || '',
    capacity: row.capacity,
    roomType: row.roomType,
    equipment: row.equipment || [],
    images: row.images || [],
    status: row.status,
    needApproval: row.needApproval ?? 0,
    description: row.description || '',
    sort: row.sort || 0
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      const submitData = {
        ...formData,
        equipment: formData.equipment
      }
      
      if (isEdit.value) {
        await updateRoom(editId.value, submitData)
        ElMessage.success('编辑成功')
      } else {
        await createRoom(submitData)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = async (id: number) => {
  try {
    await deleteRoom(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

const handleStatusChange = async (row: RoomInfo, status: number) => {
  try {
    await updateRoomStatus(row.id, status)
    ElMessage.success('状态修改成功')
    loadData()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

const resetFormData = () => {
  Object.assign(formData, {
    roomName: '',
    floor: '',
    location: '',
    capacity: 6,
    roomType: 'meeting',
    equipment: [],
    images: [],
    status: 1,
    needApproval: 0,
    description: '',
    sort: 0
  })
}

const getStatusClass = (status: number) => {
  const map: Record<number, string> = {
    0: 'maintenance',
    1: 'available',
    2: 'disabled'
  }
  return map[status] || ''
}

const getEquipmentName = (key: string) => {
  const map: Record<string, string> = {
    projector: '投影仪',
    tv: '电视',
    whiteboard: '白板',
    video_conference: '视频会议',
    '音响': '音响',
    phone: '电话'
  }
  return map[key] || key
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.room-page {
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
      width: 180px;
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

.capacity-badge {
  display: inline-block;
  padding: 2px 8px;
  background: #EBF5FF;
  color: #5B8FB9;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.equipment-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.status-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  
  &::before {
    content: '';
    width: 6px;
    height: 6px;
    border-radius: 50%;
  }
  
  &.available {
    color: #7C9A72;
    &::before {
      background: #7C9A72;
    }
  }
  
  &.maintenance {
    color: #D4845A;
    &::before {
      background: #D4845A;
    }
  }
  
  &.disabled {
    color: #C47070;
    &::before {
      background: #C47070;
    }
  }
}

.booking-count {
  font-weight: 600;
  color: #2D3748;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.no-action {
  color: #A0AEC0;
}

.form-tip {
  margin-left: 12px;
  color: #A0AEC0;
  font-size: 12px;
}

.room-form {
  .equipment-checkbox {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .status-radio {
    display: flex;
    gap: 24px;
  }
  
  .w-full {
    width: 100%;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
