<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <span>AI模型配置</span>
            <span style="margin-left:12px;font-size:12px;color:#909399">
              配置默认且启用的模型后，智能问答助手将自动使用它；未配置时降级为规则解析
            </span>
          </div>
          <el-button type="primary" @click="openCreate">新增模型</el-button>
        </div>
      </template>

      <el-table :data="list" style="width:100%" v-loading="loading">
        <el-table-column prop="name" label="配置名称" width="140">
          <template #default="{ row }">
            <span>{{ row.name }}</span>
            <el-tag v-if="row.isDefault === 1" type="success" size="small" style="margin-left:6px">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="provider" label="提供方" width="110" />
        <el-table-column prop="model" label="模型" width="150" />
        <el-table-column prop="baseUrl" label="接口地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="密钥" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.apiKey" type="info" size="small">已配置</el-tag>
            <el-tag v-else type="danger" size="small">未配置</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled === 1"
              @change="(v: any) => toggle(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="test(row)">测试</el-button>
            <el-button link type="primary" :disabled="row.isDefault === 1" @click="makeDefault(row)">设默认</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑模型配置' : '新增模型配置'" width="560px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="配置名称" required>
          <el-input v-model="form.name" placeholder="如 DeepSeek" />
        </el-form-item>
        <el-form-item label="提供方">
          <el-select v-model="form.provider" style="width:100%" @change="onProviderChange">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="通义千问" value="qwen" />
            <el-option label="智谱GLM" value="zhipu" />
            <el-option label="OpenAI" value="openai" />
            <el-option label="Ollama本地" value="ollama" />
            <el-option label="自定义(OpenAI兼容)" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="接口地址" required>
          <el-input v-model="form.baseUrl" placeholder="https://api.deepseek.com/v1" />
        </el-form-item>
        <el-form-item label="API Key">
          <el-input v-model="form.apiKey" type="password" show-password :placeholder="form.id ? '留空则不修改' : '填写密钥'" />
        </el-form-item>
        <el-form-item label="模型名称" required>
          <el-input v-model="form.model" placeholder="deepseek-chat" />
        </el-form-item>
        <el-form-item label="采样温度">
          <el-input-number v-model="form.temperature" :min="0" :max="2" :step="0.1" />
        </el-form-item>
        <el-form-item label="最大tokens">
          <el-input-number v-model="form.maxTokens" :min="128" :max="32768" :step="256" />
        </el-form-item>
        <el-form-item label="超时(秒)">
          <el-input-number v-model="form.timeoutSeconds" :min="5" :max="120" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAiModelList, createAiModel, updateAiModel, deleteAiModel,
  setDefaultAiModel, toggleAiModelStatus, testAiModel
} from '@/api'
import type { AiModelConfigInfo } from '@/types'

const loading = ref(false)
const saving = ref(false)
const list = ref<AiModelConfigInfo[]>([])
const dialogVisible = ref(false)

const PRESETS: Record<string, { baseUrl: string; model: string }> = {
  deepseek: { baseUrl: 'https://api.deepseek.com/v1', model: 'deepseek-chat' },
  qwen: { baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1', model: 'qwen-plus' },
  zhipu: { baseUrl: 'https://open.bigmodel.cn/api/paas/v4', model: 'glm-4-flash' },
  openai: { baseUrl: 'https://api.openai.com/v1', model: 'gpt-4o-mini' },
  ollama: { baseUrl: 'http://localhost:11434/v1', model: 'qwen2.5' },
  custom: { baseUrl: '', model: '' }
}

const form = reactive<AiModelConfigInfo>({
  name: '',
  provider: 'deepseek',
  baseUrl: '',
  apiKey: '',
  model: '',
  temperature: 0.2,
  maxTokens: 2048,
  timeoutSeconds: 20,
  isDefault: 0,
  enabled: 1,
  remark: ''
})

function resetForm() {
  Object.assign(form, {
    id: undefined,
    name: '',
    provider: 'deepseek',
    baseUrl: PRESETS.deepseek.baseUrl,
    apiKey: '',
    model: PRESETS.deepseek.model,
    temperature: 0.2,
    maxTokens: 2048,
    timeoutSeconds: 20,
    isDefault: 0,
    enabled: 1,
    remark: ''
  })
}

function onProviderChange(v: string) {
  const p = PRESETS[v]
  if (p && !form.id) {
    form.baseUrl = p.baseUrl
    form.model = p.model
  }
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: AiModelConfigInfo) {
  Object.assign(form, { ...row, apiKey: '' })
  dialogVisible.value = true
}

async function load() {
  loading.value = true
  try {
    const res = await getAiModelList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!form.name || !form.baseUrl || !form.model) {
    ElMessage.warning('请填写名称、接口地址和模型名称')
    return
  }
  saving.value = true
  try {
    if (form.id) {
      await updateAiModel(form.id, { ...form })
    } else {
      await createAiModel({ ...form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function makeDefault(row: AiModelConfigInfo) {
  await setDefaultAiModel(row.id!)
  ElMessage.success('已设为默认模型')
  await load()
}

async function toggle(row: AiModelConfigInfo, v: boolean) {
  await toggleAiModelStatus(row.id!, v ? 1 : 0)
  ElMessage.success(v ? '已启用' : '已停用')
  await load()
}

async function remove(row: AiModelConfigInfo) {
  await ElMessageBox.confirm(`确认删除「${row.name}」？`, '提示', { type: 'warning' })
  await deleteAiModel(row.id!)
  ElMessage.success('已删除')
  await load()
}

async function test(row: AiModelConfigInfo) {
  const loadingMsg = ElMessage({ message: `正在测试「${row.name}」…`, duration: 0 })
  try {
    const res = await testAiModel(row.id!)
    loadingMsg.close()
    const d = res.data
    if (d.success) {
      ElMessage.success(`连接成功（${d.latencyMs}ms）${d.reply ? '：' + d.reply : ''}`)
    } else {
      ElMessage.error(d.message || '连接失败')
    }
  } catch {
    loadingMsg.close()
    ElMessage.error('测试失败')
  }
}

onMounted(load)
</script>
