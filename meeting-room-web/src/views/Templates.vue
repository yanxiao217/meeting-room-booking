<template>
  <div class="templates-page">
    <div class="toolbar">
      <p class="lead">把常用的预约存成模板，下次一键填好表单。</p>
      <el-button class="el-button--marker" @click="openForm()">新建模板</el-button>
    </div>

    <div v-loading="loading" class="grid">
      <article v-for="t in items" :key="t.id" class="tpl panel">
        <div class="tpl-head">
          <span class="tpl-name">{{ t.templateName }}</span>
          <div class="tpl-menu">
            <button type="button" class="link" @click="openForm(t)">编辑</button>
            <button type="button" class="link danger" @click="remove(t)">删除</button>
          </div>
        </div>
        <p class="tpl-topic">{{ t.topic || '未命名会议' }}</p>
        <ul class="tpl-meta">
          <li v-if="t.roomId">{{ roomName(t.roomId) }}</li>
          <li v-if="t.startTime && t.endTime" class="num">{{ hhmm(t.startTime) }}–{{ hhmm(t.endTime) }}</li>
          <li v-if="t.attendeeCount" class="num">{{ t.attendeeCount }} 人</li>
        </ul>
        <div v-if="equipmentOf(t).length" class="chips">
          <span v-for="e in equipmentOf(t)" :key="e" class="chip">{{ equipmentLabel(e) }}</span>
        </div>
        <el-button class="tpl-use" size="small" @click="use(t)">用这个模板预约</el-button>
      </article>

      <div v-if="!loading && !items.length" class="empty">
        <p class="empty-title">还没有模板</p>
        <p>把每周例会这类固定安排存成模板，省去重复填写。</p>
      </div>
    </div>

    <el-dialog v-model="formVisible" :title="editing ? '编辑模板' : '新建模板'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" maxlength="50" placeholder="例如：每周需求评审" />
        </el-form-item>
        <el-form-item label="会议主题">
          <el-input v-model="form.topic" maxlength="50" placeholder="例如：V2.0 需求评审" />
        </el-form-item>
        <el-form-item label="常用会议室">
          <el-select v-model="form.roomId" placeholder="可不选" clearable filterable style="width: 100%">
            <el-option
              v-for="room in rooms"
              :key="room.id"
              :label="room.floor ? `${room.roomName} · ${room.floor}` : room.roomName"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <div class="time-row">
          <el-form-item label="开始时间">
            <el-time-select v-model="form.startTime" start="08:00" step="00:30" end="22:00" placeholder="开始" />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-time-select v-model="form.endTime" start="08:00" step="00:30" end="22:00" placeholder="结束" />
          </el-form-item>
          <el-form-item label="参会人数">
            <el-input-number v-model="form.attendeeCount" :min="1" :max="100" />
          </el-form-item>
        </div>
        <el-form-item label="需要设备">
          <el-checkbox-group v-model="form.equipment">
            <el-checkbox v-for="item in EQUIPMENT" :key="item.value" :label="item.value">
              {{ item.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button class="el-button--marker" :loading="saving" @click="submit">保存模板</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getTemplates, createTemplate, updateTemplate, deleteTemplate, getRoomList } from '@/api'
import type { BookingTemplate, RoomInfo } from '@/types'
import { useUiStore } from '@/stores/ui'
import { equipmentLabel, hhmm } from '@/utils/format'

const EQUIPMENT = [
  { value: 'projector', label: '投影仪' },
  { value: 'tv', label: '电视大屏' },
  { value: 'whiteboard', label: '白板' },
  { value: 'video_conference', label: '视频会议' },
  { value: '音响', label: '音响' }
]

const ui = useUiStore()

const loading = ref(false)
const saving = ref(false)
const items = ref<BookingTemplate[]>([])
const rooms = ref<RoomInfo[]>([])
const formVisible = ref(false)
const editing = ref<BookingTemplate | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  templateName: '',
  topic: '',
  roomId: undefined as number | undefined,
  startTime: '',
  endTime: '',
  attendeeCount: 1,
  equipment: [] as string[],
  remark: ''
})

const rules: FormRules = {
  templateName: [{ required: true, message: '给模板起个名字', trigger: 'blur' }]
}

function roomName(id: number) {
  return rooms.value.find((r) => r.id === id)?.roomName || `会议室 #${id}`
}

function equipmentOf(t: BookingTemplate): string[] {
  if (!t.equipmentNeed) return []
  try {
    const parsed = JSON.parse(t.equipmentNeed)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

function openForm(t?: BookingTemplate) {
  editing.value = t || null
  Object.assign(form, {
    templateName: t?.templateName || '',
    topic: t?.topic || '',
    roomId: t?.roomId,
    startTime: t?.startTime ? hhmm(t.startTime) : '',
    endTime: t?.endTime ? hhmm(t.endTime) : '',
    attendeeCount: t?.attendeeCount || 1,
    equipment: t ? equipmentOf(t) : [],
    remark: t?.remark || ''
  })
  formVisible.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload: Partial<BookingTemplate> = {
        templateName: form.templateName,
        topic: form.topic || undefined,
        roomId: form.roomId,
        startTime: form.startTime || undefined,
        endTime: form.endTime || undefined,
        attendeeCount: form.attendeeCount,
        equipmentNeed: form.equipment.length ? JSON.stringify(form.equipment) : undefined,
        remark: form.remark || undefined
      }
      if (editing.value?.id) {
        await updateTemplate(editing.value.id, payload)
        ElMessage.success('模板已更新')
      } else {
        await createTemplate(payload)
        ElMessage.success('模板已保存')
      }
      formVisible.value = false
      await load()
    } finally {
      saving.value = false
    }
  })
}

async function remove(t: BookingTemplate) {
  if (!t.id) return
  await ElMessageBox.confirm(`删除模板「${t.templateName}」？`, '删除模板', { type: 'warning' })
  await deleteTemplate(t.id)
  ElMessage.success('已删除')
  await load()
}

function use(t: BookingTemplate) {
  ui.openBooking({
    roomId: t.roomId,
    startTime: t.startTime ? hhmm(t.startTime) : undefined,
    endTime: t.endTime ? hhmm(t.endTime) : undefined,
    topic: t.topic,
    attendeeCount: t.attendeeCount,
    equipmentNeed: equipmentOf(t),
    remark: t.remark
  })
}

async function load() {
  loading.value = true
  try {
    const [tpl, roomRes] = await Promise.all([getTemplates(), getRoomList()])
    items.value = tpl.data
    rooms.value = roomRes.data
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.templates-page {
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

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
  min-height: 160px;
}

.tpl {
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
}

.tpl-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.tpl-name {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 15px;
}

.tpl-menu {
  display: flex;
  gap: 12px;
}

.tpl-topic {
  margin: 6px 0 0;
  color: var(--ink-2);
  font-size: 13px;
}

.tpl-meta {
  list-style: none;
  padding: 0;
  margin: 12px 0 0;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: var(--ink-2);
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.chip {
  border: 1px solid var(--line-strong);
  border-radius: 999px;
  padding: 2px 9px;
  font-size: 11px;
}

.tpl-use {
  margin-top: 16px;
  align-self: flex-start;
}

.link {
  border: none;
  background: none;
  font: inherit;
  font-size: 12px;
  color: var(--ink);
  text-decoration: underline;
  cursor: pointer;
  padding: 0;

  &.danger {
    color: var(--danger);
  }
}

.time-row {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 12px;
}
</style>
