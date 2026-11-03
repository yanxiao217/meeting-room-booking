<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-select v-model="queryParams.role" placeholder="角色" clearable style="width: 120px">
        <el-option label="管理员" value="admin" />
        <el-option label="普通用户" value="user" />
      </el-select>
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-select v-model="queryParams.deptId" placeholder="部门" clearable filterable style="width: 160px">
        <el-option v-for="dept in deptList" :key="dept.id" :label="dept.deptName" :value="dept.id" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>
    
    <!-- 表格 -->
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="110" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="deptName" label="部门" width="120">
          <template #default="{ row }">
            {{ row.deptName || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160">
          <template #default="{ row }">
            {{ row.email || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="90">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small">
              {{ row.role === 'admin' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              @click="handleDisable(row)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              link
              @click="handleEnable(row)"
            >
              启用
            </el-button>
            <el-button type="primary" link @click="handleResetPassword(row)">重置密码</el-button>
            <el-dropdown @command="(cmd: string) => handleRoleChange(row, cmd)">
              <el-button type="primary" link>角色</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="admin" :disabled="row.role === 'admin'">设为管理员</el-dropdown-item>
                  <el-dropdown-item command="user" :disabled="row.role === 'user'">设为普通用户</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑用户信息" width="480px" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="editForm" label-width="80px" :rules="editRules" ref="editFormRef">
        <el-form-item label="用户名">
          <el-input :model-value="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" maxlength="100" />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-select v-model="editForm.deptId" placeholder="请选择部门" clearable filterable style="width: 100%">
            <el-option v-for="dept in deptList" :key="dept.id" :label="dept.deptName" :value="dept.id" />
          </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, getDeptList, updateUserStatus, resetPassword, assignRole, updateUser } from '@/api'
import type { UserInfo, DeptInfo } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const tableData = ref<UserInfo[]>([])
const total = ref(0)
const deptList = ref<DeptInfo[]>([])

const queryParams = reactive({
  role: '',
  status: undefined as number | undefined,
  deptId: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.role = ''
  queryParams.status = undefined
  queryParams.deptId = undefined
  queryParams.pageNum = 1
  loadData()
}

// ========== 编辑弹窗 ==========
const editVisible = ref(false)
const editSaving = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive({
  id: 0,
  username: '',
  realName: '',
  phone: '',
  email: '',
  deptId: undefined as number | undefined
})
const editRules: FormRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const handleEdit = (row: UserInfo) => {
  editForm.id = row.id
  editForm.username = row.username
  editForm.realName = row.realName || ''
  editForm.phone = row.phone || ''
  editForm.email = row.email || ''
  editForm.deptId = row.deptId
  editVisible.value = true
}

const handleSaveEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate()
  editSaving.value = true
  try {
    await updateUser(editForm.id, {
      realName: editForm.realName,
      phone: editForm.phone,
      email: editForm.email,
      deptId: editForm.deptId
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    loadData()
  } finally {
    editSaving.value = false
  }
}

// ========== 其他操作 ==========
const handleDisable = async (row: UserInfo) => {
  await ElMessageBox.confirm('确定要禁用该用户吗？', '提示', { type: 'warning' })
  await updateUserStatus(row.id, 0)
  ElMessage.success('操作成功')
  loadData()
}

const handleEnable = async (row: UserInfo) => {
  await updateUserStatus(row.id, 1)
  ElMessage.success('操作成功')
  loadData()
}

const handleResetPassword = async (row: UserInfo) => {
  await ElMessageBox.confirm(`确定要重置用户 "${row.username}" 的密码为默认密码(123456)吗？`, '提示', { type: 'warning' })
  await resetPassword(row.id)
  ElMessage.success('密码已重置为: 123456')
}

const handleRoleChange = async (row: UserInfo, role: string) => {
  const roleName = role === 'admin' ? '管理员' : '普通用户'
  await ElMessageBox.confirm(`确定要将用户 "${row.username}" 设置为${roleName}吗？`, '提示', { type: 'warning' })
  await assignRole(row.id, role)
  ElMessage.success('操作成功')
  loadData()
}

onMounted(() => {
  loadData()
  getDeptList().then((res) => {
    deptList.value = res.data
  })
})
</script>
