<template>
  <div class="bookings-page">
    <div class="tabs">
      <button
        v-for="t in TABS"
        :key="t.key"
        type="button"
        :class="{ on: tab === t.key }"
        @click="tab = t.key"
      >
        {{ t.label }}
        <span class="tab-count num">{{ bucketList(t.key).length }}</span>
      </button>
    </div>

    <div v-loading="loading" class="list">
      <article v-for="b in bucketList(tab)" :key="b.id" class="slip" :class="{ 'slip-ongoing': isOngoing(b) }">
        <div class="slip-date">
          <span class="d num">{{ dayNum(b.bookingDate) }}</span>
          <span class="m">{{ monthLabel(b.bookingDate) }} · {{ weekdayCN(b.bookingDate) }}</span>
        </div>

        <div class="slip-main">
          <p class="slip-topic">
            {{ b.topic }}
            <span v-if="isOngoing(b)" class="live-dot"></span>
          </p>
          <p class="slip-meta">
            <span>{{ b.roomName }}</span>
            <span class="num">{{ hhmm(b.startTime) }}–{{ hhmm(b.endTime) }}</span>
            <span class="num">{{ b.attendeeCount || 1 }}人</span>
          </p>
          <p v-if="b.status === 'signed'" class="slip-signed">已签到 {{ b.signTime ? b.signTime.replace('T', ' ').slice(11, 16) : '' }}</p>
          <p v-if="b.status === 'cancelled' && b.cancelReason" class="slip-reason">
            取消原因：{{ b.cancelReason }}
          </p>
        </div>

        <div class="slip-side">
          <StatusPill :status="b.status" :name="b.statusName" />
          <div class="slip-actions">
            <button type="button" class="link" @click="goDetail(b.id)">详情</button>
            <button v-if="canSign(b)" type="button" class="link primary" @click="doSign(b)">签到</button>
            <button v-if="canNoShow(b)" type="button" class="link danger" @click="openNoShow(b)">无法到场</button>
            <button v-if="canCancel(b)" type="button" class="link danger" @click="openCancel(b)">取消</button>
          </div>
        </div>
      </article>

      <div v-if="!loading && !bucketList(tab).length" class="empty">
        <p class="empty-title">{{ emptyTitle }}</p>
        <p>{{ emptyHint }}</p>
      </div>
    </div>

    <!-- 取消预约弹窗 -->
    <el-dialog v-model="cancelVisible" title="取消预约" width="460px">
      <p class="dialog-line" v-if="cancelTarget">
        {{ cancelTarget.roomName }} · {{ cancelTarget.bookingDate }} {{ hhmm(cancelTarget.startTime) }}–{{ hhmm(cancelTarget.endTime) }}
      </p>
      <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="说明一下取消的原因（可选）" />
      <template #footer>
        <el-button @click="cancelVisible = false">再想想</el-button>
        <el-button type="danger" :loading="submitting" @click="confirmCancel">确认取消</el-button>
      </template>
    </el-dialog>

    <!-- 无法到场弹窗（视为爽约，扣信用分） -->
    <el-dialog v-model="noShowVisible" title="无法到场" width="460px">
      <div class="no-show-warn">
        <p>无法到场将被视为 <strong>爽约</strong>，系统将自动扣除信用分并释放时段。</p>
        <p class="no-show-sub">如有特殊情况，请在原因中说明，管理员可酌情处理。</p>
      </div>
      <p class="dialog-line" v-if="noShowTarget">
        {{ noShowTarget.roomName }} · {{ noShowTarget.bookingDate }} {{ hhmm(noShowTarget.startTime) }}–{{ hhmm(noShowTarget.endTime) }}
      </p>
      <el-input v-model="noShowReason" type="textarea" :rows="3" placeholder="请输入无法到场的原因（必填）" />
      <template #footer>
        <el-button @click="noShowVisible = false">返回</el-button>
        <el-button type="danger" :loading="submitting" @click="confirmNoShow">确认无法到场</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyBookings, cancelBooking, signBooking } from '@/api'
import type { BookingInfo } from '@/types'
import StatusPill from '@/components/StatusPill.vue'
import { useUiStore } from '@/stores/ui'
import { hhmm, weekdayCN } from '@/utils/format'

type TabKey = 'ongoing' | 'upcoming' | 'done' | 'cancelled'

const TABS: { key: TabKey; label: string }[] = [
  { key: 'ongoing', label: '进行中' },
  { key: 'upcoming', label: '即将开始' },
  { key: 'done', label: '已完成' },
  { key: 'cancelled', label: '已取消' }
]

const router = useRouter()
const ui = useUiStore()

const loading = ref(false)
const all = ref<BookingInfo[]>([])
const tab = ref<TabKey>('ongoing')

const cancelVisible = ref(false)
const cancelTarget = ref<BookingInfo | null>(null)
const cancelReason = ref('')

const noShowVisible = ref(false)
const noShowTarget = ref<BookingInfo | null>(null)
const noShowReason = ref('')

const submitting = ref(false)

const now = ref(Date.now())
let _clock: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  _clock = setInterval(() => { now.value = Date.now() }, 30000)
})
onUnmounted(() => { if (_clock) clearInterval(_clock) })

function isOngoing(b: BookingInfo): boolean {
  if (b.status !== 'confirmed' && b.status !== 'signed') return false
  const start = new Date(`${b.bookingDate}T${b.startTime}`).getTime()
  const end = new Date(`${b.bookingDate}T${b.endTime}`).getTime()
  // 严格进行中：当前时间落在会议时间段内（已结束的不再计入）
  return now.value >= start && now.value <= end
}

/** 会议结束超过 15 分钟（签到/爽约窗口过后），视为时间已过 */
function hasEnded(b: BookingInfo): boolean {
  const end = new Date(`${b.bookingDate}T${b.endTime}`).getTime()
  return now.value > end + 15 * 60 * 1000
}

/** 会议开始后、结束 15 分钟内仍可签到/标记无法到场 */
function inSignWindow(b: BookingInfo): boolean {
  if (b.status !== 'confirmed') return false
  const start = new Date(`${b.bookingDate}T${b.startTime}`).getTime()
  const end = new Date(`${b.bookingDate}T${b.endTime}`).getTime()
  return now.value >= start && now.value <= end + 15 * 60 * 1000
}

const emptyTitle = computed(() => {
  const map: Record<TabKey, string> = {
    ongoing: '当前没有进行中的会议',
    upcoming: '没有即将开始的预约',
    done: '还没有已完成的会议',
    cancelled: '没有已取消的预约'
  }
  return map[tab.value]
})
const emptyHint = computed(() => {
  const map: Record<TabKey, string> = {
    ongoing: '会议开始后可在此签到。',
    upcoming: '在「今日空闲」里挑一间会议室，选中空格即可预约。',
    done: '开完的会议会归档到这里。',
    cancelled: '取消过的预约会留在这里备查。'
  }
  return map[tab.value]
})

function bucket(b: BookingInfo): TabKey {
  if (b.status === 'cancelled' || b.status === 'expired') return 'cancelled'
  if (b.status === 'completed') return 'done'
  // 时间已过的 confirmed / pending 预约直接归档到「已完成」，不再出现在「进行中」「即将开始」
  if (hasEnded(b)) return 'done'
  if (isOngoing(b)) return 'ongoing'
  if (b.status === 'signed') return 'done'
  return 'upcoming'
}

function bucketList(key: TabKey) {
  return all.value
    .filter((b) => bucket(b) === key)
    .sort((a, b) => {
      const da = `${a.bookingDate} ${a.startTime}`
      const db = `${b.bookingDate} ${b.startTime}`
      return key === 'ongoing' ? da.localeCompare(db) : key === 'upcoming' ? da.localeCompare(db) : db.localeCompare(da)
    })
}

function dayNum(iso: string) {
  return Number(iso.slice(8, 10))
}

function monthLabel(iso: string) {
  return `${Number(iso.slice(5, 7))}月`
}

function canSign(b: BookingInfo): boolean {
  return inSignWindow(b)
}

function canNoShow(b: BookingInfo): boolean {
  return inSignWindow(b)
}

function canCancel(b: BookingInfo) {
  if (b.status !== 'confirmed' && b.status !== 'pending') return false
  const start = new Date(`${b.bookingDate}T${b.startTime}`)
  return start.getTime() - Date.now() > 15 * 60 * 1000
}

function goDetail(id: number) {
  router.push(`/bookings/${id}`)
}

function openCancel(b: BookingInfo) {
  cancelTarget.value = b
  cancelReason.value = ''
  cancelVisible.value = true
}

function openNoShow(b: BookingInfo) {
  noShowTarget.value = b
  noShowReason.value = ''
  noShowVisible.value = true
}

async function confirmCancel() {
  if (!cancelTarget.value) return
  submitting.value = true
  try {
    await cancelBooking(cancelTarget.value.id, cancelReason.value)
    ElMessage.success('预约已取消')
    cancelVisible.value = false
    ui.bumpVersion()
    await load()
  } finally {
    submitting.value = false
  }
}

async function doSign(b: BookingInfo) {
  submitting.value = true
  try {
    await signBooking(b.id)
    ElMessage.success('签到成功')
    ui.bumpVersion()
    await load()
  } finally {
    submitting.value = false
  }
}

async function confirmNoShow() {
  if (!noShowTarget.value) return
  if (!noShowReason.value.trim()) {
    ElMessage.warning('请填写无法到场的原因')
    return
  }
  submitting.value = true
  try {
    // 取消预约并标记为爽约
    await cancelBooking(noShowTarget.value.id, '[爽约] ' + noShowReason.value)
    ElMessage.warning('已记录无法到场，信用分将被扣除')
    noShowVisible.value = false
    ui.bumpVersion()
    await load()
  } finally {
    submitting.value = false
  }
}

async function load() {
  loading.value = true
  try {
    const res = await getMyBookings({ pageSize: 100 })
    all.value = res.data.list
  } finally {
    loading.value = false
  }
}

watch(
  () => ui.version,
  () => load()
)

onMounted(load)
</script>

<style scoped lang="scss">
.bookings-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tabs {
  display: inline-flex;
  gap: 4px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 4px;
  align-self: flex-start;
}

.tabs button {
  border: none;
  background: none;
  font: inherit;
  font-weight: 500;
  color: var(--ink-2);
  padding: 7px 14px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 7px;

  &.on {
    background: var(--ink);
    color: #fff;
  }
}

.tab-count {
  font-size: 11px;
  background: var(--surface-3);
  color: var(--ink-2);
  border-radius: 999px;
  padding: 0 6px;
  min-width: 18px;
  height: 17px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.tabs button.on .tab-count {
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 200px;
}

.slip {
  display: grid;
  grid-template-columns: 96px 1fr auto;
  gap: 18px;
  align-items: center;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 16px 18px;
}

.slip-ongoing {
  border-color: var(--primary);
  background: color-mix(in srgb, var(--primary) 4%, var(--surface));
}

.slip-date {
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--line);
  padding-right: 18px;
}

.slip-date .d {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 30px;
  line-height: 1;
}

.slip-date .m {
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 4px;
}

.slip-topic {
  margin: 0;
  font-weight: 600;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.slip-meta {
  margin: 5px 0 0;
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  font-size: 13px;
  color: var(--ink-2);
}

.slip-signed {
  margin: 6px 0 0;
  font-size: 12px;
  color: #16a34a;
  font-weight: 500;
}

.slip-reason {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--danger);
}

.slip-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.slip-actions {
  display: flex;
  gap: 14px;
}

.link {
  border: none;
  background: none;
  font: inherit;
  font-size: 13px;
  color: var(--ink);
  text-decoration: underline;
  cursor: pointer;
  padding: 0;

  &.danger { color: var(--danger); }
  &.primary { color: var(--primary); font-weight: 600; text-decoration: none; }
}

.dialog-line {
  margin: 0 0 12px;
  font-weight: 600;
}

.no-show-warn {
  background: #fef3c7;
  border: 1px solid #fbbf24;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  p { margin: 0; font-size: 14px; color: #92400e; }
  .no-show-sub { margin-top: 6px; font-size: 12px; color: #b45309; }
}

@media (max-width: 640px) {
  .slip {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .slip-date {
    flex-direction: row;
    align-items: baseline;
    gap: 8px;
    border-right: none;
    border-bottom: 1px solid var(--line);
    padding: 0 0 10px;
  }

  .slip-side {
    align-items: flex-start;
    flex-direction: row;
    justify-content: space-between;
  }
}
</style>
