<template>
  <div class="page-container">
    <!-- 工具栏 -->
    <div class="search-bar">
      <el-button type="primary" @click="handleCreate">新增部门</el-button>
      <span class="toolbar-tip">部门用于组织架构与配额管理，用户可在「用户管理」中分配部门</span>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="deptName" label="部门名称" min-width="140" />
        <el-table-column label="父部门" width="140">
          <template #default="{ row }">
            {{ row.parentId && row.parentId !== 0 ? getDeptName(row.parentId) : '顶级部门' }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              @click="handleToggleStatus(row, 0)"
            >
              停用
            </el-button>
            <el-button v-else type="success" link @click="handleToggleStatus(row, 1)">
              启用
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="editVisible" :title="editForm.id ? '编辑部门' : '新增部门'" width="480px" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="editForm" label-width="90px" :rules="editRules" ref="editFormRef">
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="editForm.deptName" placeholder="请输入部门名称" maxlength="30" />
        </el-form-item>
        <el-form-item label="父部门" prop="parentId">
          <el-select v-model="editForm.parentId" placeholder="选择父部门（默认顶级）" clearable style="width: 100%">
            <el-option label="顶级部门" :value="0" />
            <el-option
              v-for="dept in parentOptions"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="editForm.sort" :min="1" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeptList, createDept, updateDept, deleteDept } from '@/api'
import type { DeptInfo } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const tableData = ref<DeptInfo[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDeptList()
    tableData.value = res.data
    total.value = res.data.length
  } finally {
    loading.value = false
  }
}

const getDeptName = (id: number) => {
  const dept = tableData.value.find((d) => d.id === id)
  return dept ? dept.deptName : `#${id}`
}

// 父部门下拉：编辑时排除自己，避免把自己设为自己的父部门
const parentOptions = computed(() => tableData.value.filter((d) => d.id !== editForm.id))

// ========== 新增/编辑弹窗 ==========
const editVisible = ref(false)
const editSaving = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive({
  id: 0,
  deptName: '',
  parentId: 0,
  sort: 1,
  status: 1
})
const editRules: FormRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const handleCreate = () => {
  editForm.id = 0
  editForm.deptName = ''
  editForm.parentId = 0
  editForm.sort = 1
  editForm.status = 1
  editVisible.value = true
}

const handleEdit = (row: DeptInfo) => {
  editForm.id = row.id
  editForm.deptName = row.deptName
  editForm.parentId = row.parentId || 0
  editForm.sort = row.sort || 1
  editForm.status = row.status ?? 1
  editVisible.value = true
}

const handleSaveEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate()
  editSaving.value = true
  try {
    const payload = {
      deptName: editForm.deptName,
      parentId: editForm.parentId || 0,
      sort: editForm.sort,
      status: editForm.status
    }
    if (editForm.id) {
      await updateDept(editForm.id, payload)
    } else {
      await createDept(payload)
    }
    ElMessage.success('保存成功')
    editVisible.value = false
    loadData()
  } finally {
    editSaving.value = false
  }
}

// ========== 停用/启用、删除 ==========
const handleToggleStatus = async (row: DeptInfo, status: number) => {
  const action = status === 1 ? '启用' : '停用'
  await ElMessageBox.confirm(`确定要${action}部门「${row.deptName}」吗？`, '提示', { type: 'warning' })
  await updateDept(row.id, { status })
  ElMessage.success(`${action}成功`)
  loadData()
}

const handleDelete = async (row: DeptInfo) => {
  await ElMessageBox.confirm(
    `确定要删除部门「${row.deptName}」吗？删除后该部门下的用户将失去部门归属，请谨慎操作。`,
    '删除确认',
    { type: 'warning', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger' }
  )
  await deleteDept(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.toolbar-tip {
  margin-left: 12px;
  font-size: 13px;
  color: #a0aec0;
}
</style>
