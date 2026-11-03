<template>
  <div class="repairs-page">
    <div class="toolbar">
      <p class="lead">发现会议室设备出故障？在这里报修，处理进度会同步给你。</p>
      <el-button class="el-button--marker" @click="openForm">发起报修</el-button>
    </div>

    <div v-loading="loading" class="list">
      <article v-for="r in items" :key="r.id" class="ticket panel">
        <div class="ticket-head">
          <span class="ticket-device">{{ deviceLabel(r.deviceType) }} · {{ roomName(r.roomId) }}</span>
          <span class="pill" :class="statusClass(r.status)">{{ statusLabel(r.status) }}</span>
        </div>
        <p class="ticket-desc">{{ r.faultDesc }}</p>

        <div class="track">
          <span v-for="(s, i) in STEPS" :key="s.key" class="step" :class="{ done: stepIndex(r.status) >= i }">
            <span class="dot"></span>
            <span class="step-label">{{ s.label }}</span>
          </span>
        </div>

        <div v-if="r.handleDesc" class="handle">
          <span class="handle-label">处理说明</span>
          <span>{{ r.handleDesc }}</span>
        </div>

        <div class="ticket-foot num">
          <span>单号 {{ r.orderNo }}</span>
          <span v-if="r.createTime">{{ r.createTime.replace('T', ' ').slice(0, 16) }}</span>
          <span v-if="r.finishedTime">完成于 {{ r.finishedTime.replace('T', ' ').slice(0, 16) }}</span>
        </div>
      </article>

      <div v-if="!loading && !items.length" class="empty">
        <p class="empty-title">还没有报修记录</p>
        <p>设备一切正常。真出问题时，从这里提交。</p>
      </div>
    </div>

    <el-dialog v-model="formVisible" title="发起报修" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="会议室" prop="roomId">
          <el-select v-model="form.roomId" placeholder="选择会议室" filterable style="width: 100%">
            <el-option
              v-for="room in rooms"
              :key="room.id"
              :label="room.floor ? `${room.roomName} · ${room.floor}` : room.roomName"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="form.deviceType" placeholder="选择设备" style="width: 100%">
            <el-option v-for="d in DEVICES" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDesc">
          <el-input v-model="form.faultDesc" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="描述一下现象，例如：投影仪无法开机，指示灯不亮" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button class="el-button--marker" :loading="saving" @click="submit">提交报修</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createRepair, getMyRepairs, getRoomList } from '@/api'
import type { RepairOrder, RoomInfo } from '@/types'
import { REPAIR_DEVICE_LABELS } from '@/utils/format'

const DEVICES = Object.entries(REPAIR_DEVICE_LABELS).map(([value, label]) => ({ value, label }))
const STEPS = [
  { key: 'pending', label: '待处理' },
  { key: 'processing', label: '处理中' },
  { key: 'finished', label: '已完成' }
]

const loading = ref(false)
const items = ref<RepairOrder[]>([])
const rooms = ref<RoomInfo[]>([])
const formVisible = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  roomId: undefined as number | undefined,
  deviceType: '',
  faultDesc: ''
})

const rules: FormRules = {
  roomId: [{ required: true, message: '请选择会议室', trigger: 'change' }],
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  faultDesc: [{ required: true, message: '请描述故障现象', trigger: 'blur' }]
}

function deviceLabel(type: string) {
  return REPAIR_DEVICE_LABELS[type] || type
}

function roomName(id: number) {
  return rooms.value.find((r) => r.id === id)?.roomName || `会议室 #${id}`
}

function statusLabel(s: string) {
  return { pending: '待处理', processing: '处理中', finished: '已完成' }[s] || s
}

function statusClass(s: string) {
  return { pending: 'pending', processing: 'confirmed', finished: 'completed' }[s] || 'plain'
}

function stepIndex(s: string) {
  return STEPS.findIndex((x) => x.key === s)
}

function openForm() {
  Object.assign(form, { roomId: undefined, deviceType: '', faultDesc: '' })
  formVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await createRepair({
        roomId: form.roomId!,
        deviceType: form.deviceType,
        faultDesc: form.faultDesc
      })
      ElMessage.success('报修已提交，处理进度会通知你')
      formVisible.value = false
      await load()
    } finally {
      saving.value = false
    }
  })
}

async function load() {
  loading.value = true
  try {
    const [repairs, roomRes] = await Promise.all([getMyRepairs(), getRoomList()])
    items.value = repairs.data
    rooms.value = roomRes.data
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.repairs-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.lead {
  margin: 0;
  color: var(--ink-2);
  font-size: 13px;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 160px;
}

.ticket {
  padding: 16px 20px;
}

.ticket-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.ticket-device {
  font-weight: 600;
  font-size: 15px;
}

.ticket-desc {
  margin: 8px 0 0;
  color: var(--ink-2);
  line-height: 1.6;
}

.track {
  display: flex;
  gap: 0;
  margin: 16px 0 0;
}

.step {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  color: var(--ink-3);
  flex: 1;

  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: var(--line);
    margin: 0 10px;
  }

  &:last-child::after {
    display: none;
  }

  &.done {
    color: var(--ink);
  }
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  border: 1px solid var(--line-strong);
  background: var(--surface);
  flex: none;
}

.step.done .dot {
  background: var(--free);
  border-color: var(--free);
}

.handle {
  margin-top: 14px;
  padding: 10px 12px;
  background: var(--surface-2);
  border-radius: var(--radius-sm);
  font-size: 13px;
  display: flex;
  gap: 10px;
}

.handle-label {
  color: var(--ink-3);
  flex: none;
}

.ticket-foot {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 14px;
  font-size: 12px;
  color: var(--ink-3);
}
</style>
