<template>
  <div class="waitlist-page">
    <p class="intro panel">
      目标时段被别人订走时可以排队候补。有人取消时，系统会按顺序通知你，限时确认即可拿到时段。
    </p>

    <div v-loading="loading" class="list">
      <article v-for="w in items" :key="w.id" class="wl panel" :class="{ urgent: isUrgent(w) }">
        <div class="wl-main">
          <div class="wl-head">
            <span class="wl-room">{{ roomName(w.roomId) }}</span>
            <span class="rank num">第 {{ w.rankNo }} 位</span>
          </div>
          <p class="wl-when num">
            {{ w.bookingDate }} {{ weekdayCN(w.bookingDate) }} · {{ hhmm(w.startTime) }}–{{ hhmm(w.endTime) }}
          </p>
          <p v-if="w.status === 'notified' && w.confirmDeadline" class="wl-deadline">
            请在 <span class="num">{{ remaining(w.confirmDeadline) }}</span> 内确认，超时会顺延给下一位
          </p>
        </div>

        <div class="wl-side">
          <span class="pill" :class="statusClass(w.status)">{{ statusLabel(w.status) }}</span>
          <div class="wl-actions">
            <el-button
              v-if="w.status === 'notified'"
              class="el-button--marker"
              size="small"
              :loading="busy === w.id"
              @click="confirm(w)"
            >
              确认预约
            </el-button>
            <button
              v-if="w.status === 'waiting' || w.status === 'notified'"
              type="button"
              class="link danger"
              @click="leave(w)"
            >
              退出候补
            </button>
          </div>
        </div>
      </article>

      <div v-if="!loading && !items.length" class="empty">
        <p class="empty-title">还没有候补记录</p>
        <p>在「今日空闲」里点到已被占用的时段，就能加入候补队列。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyWaitlist, leaveWaitlist, confirmWaitlist, getRoomList } from '@/api'
import type { RoomInfo, WaitlistItem } from '@/types'
import { useUiStore } from '@/stores/ui'
import { hhmm, weekdayCN } from '@/utils/format'

const ui = useUiStore()

const loading = ref(false)
const raw = ref<WaitlistItem[]>([])
const rooms = ref<RoomInfo[]>([])
const busy = ref<number | null>(null)
const now = ref(Date.now())

/** 候补时段已经结束（时间已过），不再展示 */
function hasPast(w: WaitlistItem): boolean {
  const end = new Date(`${w.bookingDate}T${w.endTime}`).getTime()
  return now.value > end
}

const items = computed(() => raw.value.filter((w) => !hasPast(w)))

const STATUS_LABELS: Record<string, string> = {
  waiting: '等待中',
  notified: '待确认',
  confirmed: '已确认',
  cancelled: '已放弃',
  expired: '已超时'
}

function statusLabel(s: string) {
  return STATUS_LABELS[s] || s
}

function statusClass(s: string) {
  if (s === 'notified') return 'pending'
  if (s === 'confirmed') return 'confirmed'
  if (s === 'waiting') return 'plain'
  return 'cancelled'
}

function roomName(id: number) {
  return rooms.value.find((r) => r.id === id)?.roomName || `会议室 #${id}`
}

function isUrgent(w: WaitlistItem) {
  return w.status === 'notified'
}

function remaining(deadline: string) {
  const ms = new Date(deadline).getTime() - now.value
  if (ms <= 0) return '0 分钟'
  const min = Math.ceil(ms / 60000)
  return `${min} 分钟`
}

async function load() {
  loading.value = true
  try {
    const [wl, roomRes] = await Promise.all([getMyWaitlist(), getRoomList()])
    raw.value = wl.data
    rooms.value = roomRes.data
  } finally {
    loading.value = false
  }
}

async function confirm(w: WaitlistItem) {
  busy.value = w.id
  try {
    await confirmWaitlist(w.id)
    ElMessage.success('已确认，预约成功')
    ui.bumpVersion()
    await load()
  } finally {
    busy.value = null
  }
}

async function leave(w: WaitlistItem) {
  await ElMessageBox.confirm('退出后这个时段释放给后面排队的人，确定吗？', '退出候补', { type: 'warning' })
  await leaveWaitlist(w.id)
  ElMessage.success('已退出候补')
  await load()
}

let timer: number | undefined

watch(
  () => ui.version,
  () => load()
)

onMounted(() => {
  load()
  timer = window.setInterval(() => (now.value = Date.now()), 30000)
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped lang="scss">
.waitlist-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.intro {
  margin: 0;
  padding: 14px 18px;
  color: var(--ink-2);
  font-size: 13px;
  line-height: 1.6;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 160px;
}

.wl {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 16px 20px;

  &.urgent {
    border-color: var(--warn);
    box-shadow: inset 3px 0 0 var(--warn);
  }
}

.wl-room {
  font-weight: 600;
  font-size: 15px;
}

.rank {
  font-size: 12px;
  color: var(--ink-2);
  margin-left: 10px;
}

.wl-when {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--ink-2);
}

.wl-deadline {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--warn);
}

.wl-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.wl-actions {
  display: flex;
  align-items: center;
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

  &.danger {
    color: var(--danger);
  }
}

@media (max-width: 640px) {
  .wl {
    flex-direction: column;
    align-items: flex-start;
  }

  .wl-side {
    align-items: flex-start;
    flex-direction: row;
    width: 100%;
    justify-content: space-between;
  }
}
</style>
