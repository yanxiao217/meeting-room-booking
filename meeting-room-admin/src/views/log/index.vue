<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="queryParams.username" placeholder="操作人" clearable style="width: 150px" />
      <el-select v-model="queryParams.module" placeholder="操作模块" clearable style="width: 150px">
        <el-option label="会议室管理" value="会议室管理" />
        <el-option label="预约管理" value="预约管理" />
        <el-option label="用户管理" value="用户管理" />
        <el-option label="系统设置" value="系统设置" />
        <el-option label="预约" value="预约" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>
    
    <!-- 表格 -->
    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="module" label="操作模块" width="120" />
        <el-table-column prop="operation" label="操作内容" width="120" />
        <el-table-column prop="method" label="请求方法" width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="100" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getOperationLogs } from '@/api'
import type { OperationLogInfo } from '@/types'

const loading = ref(false)
const tableData = ref<OperationLogInfo[]>([])

const queryParams = reactive({
  username: '',
  module: '',
  pageNum: 1,
  pageSize: 20
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getOperationLogs(queryParams)
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.username = ''
  queryParams.module = ''
  loadData()
}

onMounted(() => {
  loadData()
})
</script>
