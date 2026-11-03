<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>部门配额配置</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon class="mr-1"><Plus /></el-icon>
            新增配额
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="deptName" label="部门" min-width="120" />
        <el-table-column label="配额类型" width="150">
          <template #default="{ row }">
            <el-tag :type="row.quotaType === 'weekly' ? 'primary' : 'success'" size="small">
              {{ row.quotaTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quotaValue" label="配额值" width="100" />
        <el-table-column label="周期" width="100">
          <template #default="{ row }">{{ row.periodName }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">
              {{ row.enabled === 1 ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑配额' : '新增配额'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="部门" prop="deptId">
          <el-select v-model="formData.deptId" placeholder="请选择部门" filterable class="w-full">
            <el-option v-for="dept in deptList" :key="dept.id" :label="dept.deptName" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="配额类型" prop="quotaType">
          <el-select v-model="formData.quotaType" placeholder="请选择类型" class="w-full">
            <el-option label="每周预约次数" value="weekly" />
            <el-option label="每月预约时长" value="monthly" />
          </el-select>
        </el-form-item>
        <el-form-item label="配额值" prop="quotaValue">
          <el-input-number v-model="formData.quotaValue" :min="1" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="formData.enabled" :active-value="1" :inactive-value="0" />
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
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getDeptQuotaList, createDeptQuota, updateDeptQuota, deleteDeptQuota } from '@/api'
import { getDeptList } from '@/api'
import type { DeptQuotaInfo, DeptInfo } from '@/types'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<DeptQuotaInfo[]>([])
const deptList = ref<DeptInfo[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const formData = reactive({
  deptId: undefined as number | undefined,
  quotaType: 'weekly',
  quotaValue: 10,
  enabled: 1
})

const formRules: FormRules = {
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  quotaType: [{ required: true, message: '请选择配额类型', trigger: 'change' }],
  quotaValue: [{ required: true, message: '请输入配额值', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDeptQuotaList()
    tableData.value = res.data.map((item) => ({
      ...item,
      deptName: getDeptName(item.deptId),
      quotaTypeName: item.quotaType === 'weekly' ? '每周预约次数' : '每月预约时长',
      periodName: item.period === 'month' ? '月' : '周'
    }))
  } finally {
    loading.value = false
  }
}

const getDeptName = (id: number) => {
  const dept = deptList.value.find((d) => d.id === id)
  return dept ? dept.deptName : String(id)
}

const loadDeptList = async () => {
  const res = await getDeptList()
  deptList.value = res.data
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(formData, { deptId: undefined, quotaType: 'weekly', quotaValue: 10, enabled: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: DeptQuotaInfo) => {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(formData, {
    deptId: row.deptId,
    quotaType: row.quotaType,
    quotaValue: row.quotaValue,
    enabled: row.enabled
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value && currentId.value) {
        await updateDeptQuota(currentId.value, formData)
      } else {
        await createDeptQuota(formData)
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = async (row: DeptQuotaInfo) => {
  await ElMessageBox.confirm(`确定要删除「${row.deptName}」的配额吗？`, '提示', { type: 'warning' })
  await deleteDeptQuota(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(async () => {
  await loadDeptList().catch(() => {})
  loadData()
})
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .mr-1 {
    margin-right: 6px;
  }
}

.w-full {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
