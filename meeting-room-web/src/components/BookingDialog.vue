<template>
  <el-dialog
    v-model="visible"
    title="预约会议室"
    width="560px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="76px" label-position="left">
      <el-form-item label="会议室" prop="roomId">
        <el-select
          v-model="form.roomId"
          placeholder="选择会议室"
          filterable
          style="width: 100%"
          @change="onRoomChange"
        >
          <el-option
            v-for="room in rooms"
            :key="room.id"
            :label="roomOptionLabel(room)"
            :value="room.id"
          />
        </el-select>
        <p v-if="selectedRoom?.needApproval === 1" class="hint hint-warn">
          该会议室需要管理员审批，提交后请等待审批结果。
        </p>
      </el-form-item>

      <el-form-item label="日期" prop="bookingDate">
        <el-date-picker
          v-model="form.bookingDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          :disabled-date="disabledDate"
          style="width: 100%"
          @change="clearConflict"
        />
      </el-form-item>

      <el-form-item label="时间" prop="timeRange">
        <el-time-picker
          v-model="form.timeRange"
          is-range
          range-separator="至"
          start-placeholder="开始"
          end-placeholder="结束"
          format="HH:mm"
          value-format="HH:mm"
          step="00:30"
          :disabled-hours="disabledHours"
          :disabled-minutes="disabledMinutes"
          style="width: 100%"
          @change="clearConflict"
        />
        <p class="hint">按 30 分钟为一格，可在 08:00–22:00 之间选择。</p>
      </el-form-item>

      <el-form-item label="主题" prop="topic">
        <el-input v-model="form.topic" maxlength="50" show-word-limit placeholder="例如：V2.0 需求评审" />
      </el-form-item>

      <el-form-item label="参会人数">
        <el-input-number v-model="form.attendeeCount" :min="1" :max="maxAttendee" />
        <span v-if="selectedRoom" class="hint inline">不超过 {{ selectedRoom.capacity }} 人</span>
      </el-form-item>

      <el-form-item label="需要设备">
        <el-checkbox-group v-model="form.equipment">
          <el-checkbox
            v-for="item in EQUIPMENT"
            :key="item.value"
            :label="item.value"
            :disabled="isEquipmentDisabled(item.value)"
            @change="onEquipmentChange(item.value, $event)"
          >
            {{ item.label }}
            <template #default v-if="isEquipmentDisabled(item.value)">
              <span style="color: var(--ink-3);">{{ item.label }} (该会议室无此设备)</span>
            </template>
          </el-checkbox>
        </el-checkbox-group>
        <p class="hint" v-if="hasUnavailableEquipment">⚠ 已自动移除该会议室不支持的设备</p>
        <p class="hint" v-else>仅作备注，方便你到现场核对。</p>
      </el-form-item>

      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" placeholder="可选" />
      </el-form-item>

      <div v-if="conflict" class="conflict">
        <p class="conflict-title">这个时段已经被预约了</p>
        <p class="conflict-hint">换一个空闲时段，或加入候补队列——有人取消时会第一时间通知你。</p>
        <div v-if="freeSlots.length" class="slots">
          <button
            v-for="slot in freeSlots"
            :key="slot.startTime"
            type="button"
            class="slot num"
            @click="pickSlot(slot)"
          >
            {{ hhmm(slot.startTime) }}
          </button>
        </div>
        <el-button size="small" @click="doJoinWaitlist">加入候补队列</el-button>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button class="el-button--marker" :loading="submitting" @click="submit">确认预约</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createBooking, getRoomList, getAvailability, joinWaitlist } from '@/api'
import type { AvailabilitySlot, RoomInfo } from '@/types'
import { useUiStore } from '@/stores/ui'
import { hhmm, todayISO } from '@/utils/format'

const ui = useUiStore()

const EQUIPMENT = [
  { value: 'projector', label: '投影仪' },
  { value: 'tv', label: '电视大屏' },
  { value: 'whiteboard', label: '白板' },
  { value: 'video_conference', label: '视频会议' },
  { value: '音响', label: '音响' }
]

const visible = computed({
  get: () => ui.bookingVisible,
  set: (v: boolean) => {
    if (!v) ui.closeBooking()
  }
})

const formRef = ref<FormInstance>()
const rooms = ref<RoomInfo[]>([])
const submitting = ref(false)
const conflict = ref(false)
const freeSlots = ref<AvailabilitySlot[]>([])

const form = reactive({
  roomId: undefined as number | undefined,
  bookingDate: todayISO(),
  timeRange: [] as string[],
  topic: '',
  attendeeCount: 1,
  equipment: [] as string[],
  remark: ''
})

const rules: FormRules = {
  roomId: [{ required: true, message: '请选择会议室', trigger: 'change' }],
  bookingDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  topic: [{ required: true, message: '请填写会议主题', trigger: 'blur' }]
}

const selectedRoom = computed(() => rooms.value.find((r) => r.id === form.roomId))
const maxAttendee = computed(() => selectedRoom.value?.capacity || 100)

// ===== Equipment availability logic =====
const selectedRoomEquipment = computed(() => selectedRoom.value?.equipment || [])

const isEquipmentDisabled = (value: string) => {
  return !selectedRoomEquipment.value.includes(value)
}

const hasUnavailableEquipment = computed(() => {
  return form.equipment.some((eq) => !selectedRoomEquipment.value.includes(eq))
})

function onEquipmentChange(value: string, checked: boolean) {
  if (checked && !selectedRoomEquipment.value.includes(value)) {
    ElMessage.warning(`该会议室没有"${EQUIPMENT.find(e => e.value === value)?.label}"设备，已取消选择`)
    // Remove from equipment array
    const idx = form.equipment.indexOf(value)
    if (idx >= 0) form.equipment.splice(idx, 1)
  }
}

function roomOptionLabel(room: RoomInfo) {
  const bits = [room.roomName]
  if (room.floor) bits.push(room.floor)
  bits.push(`${room.capacity}人`)
  return bits.join(' · ')
}

function disabledDate(d: Date) {
  return d.getTime() < new Date(todayISO() + 'T00:00:00').getTime()
}

function clearConflict() {
  conflict.value = false
}

const isToday = computed(() => form.bookingDate === todayISO())
const nowMinutes = ref(0)
let _timer: ReturnType<typeof setInterval> | null = null

function refreshNowMinutes() {
  const n = new Date()
  nowMinutes.value = n.getHours() * 60 + n.getMinutes()
}

onMounted(() => { refreshNowMinutes(); _timer = setInterval(refreshNowMinutes, 30000) })
onUnmounted(() => { if (_timer) clearInterval(_timer) })

function disabledHours() {
  if (!isToday.value) return []
  const h = Math.floor(nowMinutes.value / 60)
  return Array.from({ length: h }, (_, i) => i)
}

function disabledMinutes(hour: number) {
  if (!isToday.value) return []
  const curH = Math.floor(nowMinutes.value / 60)
  if (hour < curH) return Array.from({ length: 60 }, (_, i) => i)
  if (hour === curH) return Array.from({ length: nowMinutes.value % 60 + 1 }, (_, i) => i)
  return []
}

watch(() => form.bookingDate, () => {
  if (!isToday.value || form.timeRange.length !== 2) return
  const [sh] = form.timeRange[0].split(':').map(Number)
  const [eh] = form.timeRange[1].split(':').map(Number)
  if (sh < Math.floor(nowMinutes.value / 60) || eh <= Math.floor(nowMinutes.value / 60)) {
    form.timeRange = []
  }
})

function onRoomChange() {
  clearConflict()
  if (selectedRoom.value && form.attendeeCount > selectedRoom.value.capacity) {
    form.attendeeCount = selectedRoom.value.capacity
  }
  // Auto-remove equipment that the new room doesn't have
  const available = selectedRoom.value?.equipment || []
  form.equipment = form.equipment.filter((eq) => available.includes(eq))
}

function pickSlot(slot: AvailabilitySlot) {
  form.timeRange = [hhmm(slot.startTime), hhmm(slot.endTime)]
  conflict.value = false
}

async function loadRooms() {
  if (rooms.value.length) return
  const res = await getRoomList({ status: 1 })
  rooms.value = res.data
}

async function loadFreeSlots() {
  freeSlots.value = []
  if (!form.roomId || !form.bookingDate) return
  try {
    const res = await getAvailability(form.roomId, form.bookingDate)
    freeSlots.value = (res.data.availableSlots || []).filter((s) => s.available).slice(0, 8)
  } catch {
    // ignore
  }
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    conflict.value = false
    try {
      const [startTime, endTime] = form.timeRange
      const res = await createBooking({
        roomId: form.roomId!,
        bookingDate: form.bookingDate,
        startTime,
        endTime,
        topic: form.topic,
        attendeeCount: form.attendeeCount,
        equipmentNeed: form.equipment,
        remark: form.remark || undefined
      })
      const pending = res.data.status === 'pending'
      ElMessage.success(pending ? '已提交，等待管理员审批' : '预约成功')
      ui.bumpVersion()
      ui.closeBooking()
    } catch (e) {
      const msg = e instanceof Error ? e.message : ''
      if (msg.includes('冲突')) {
        conflict.value = true
        loadFreeSlots()
      }
    } finally {
      submitting.value = false
    }
  })
}

async function doJoinWaitlist() {
  if (!form.roomId || form.timeRange.length !== 2) return
  try {
    await joinWaitlist({
      roomId: form.roomId,
      bookingDate: form.bookingDate,
      startTime: form.timeRange[0],
      endTime: form.timeRange[1]
    })
    ElMessage.success('已加入候补队列')
    ui.bumpVersion()
    ui.closeBooking()
  } catch {
    // interceptor shows the reason
  }
}

watch(
  () => ui.bookingVisible,
  async (open) => {
    if (!open) return
    await loadRooms()
    conflict.value = false
    freeSlots.value = []
    const p = ui.prefill
    form.roomId = p.roomId
    form.bookingDate = p.bookingDate || todayISO()
    form.timeRange = p.startTime && p.endTime ? [p.startTime, p.endTime] : []
    form.topic = p.topic || ''
    form.attendeeCount = p.attendeeCount || 1
    form.equipment = p.equipmentNeed || []
    form.remark = p.remark || ''
  }
)
</script>

<style scoped lang="scss">
.hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--ink-3);
  line-height: 1.5;

  &.inline {
    margin: 0 0 0 12px;
  }

  &.hint-warn {
    color: var(--warn);
  }
}

.conflict {
  margin-top: 4px;
  padding: 14px 16px;
  border: 1px solid var(--warn);
  background: var(--warn-soft);
  border-radius: var(--radius);
}

.conflict-title {
  margin: 0;
  font-family: var(--font-display);
  font-weight: 700;
  color: var(--ink);
}

.conflict-hint {
  margin: 4px 0 12px;
  font-size: 12px;
  color: var(--ink-2);
  line-height: 1.5;
}

.slots {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.slot {
  border: 1px solid var(--line-strong);
  background: var(--surface);
  border-radius: var(--radius-sm);
  padding: 3px 9px;
  font: inherit;
  font-size: 12px;
  cursor: pointer;
  color: var(--ink);

  &:hover {
    background: var(--marker);
    border-color: var(--ink);
  }
}
</style>
