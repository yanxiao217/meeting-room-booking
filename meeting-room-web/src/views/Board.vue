<template>
  <div class="board-page">
    <div class="board-bar panel">
      <div class="date-nav">
        <button type="button" class="step" aria-label="前一天" @click="shift(-1)">‹</button>
        <button type="button" class="date-btn" @click="goToday">{{ dateLabel }}</button>
        <button type="button" class="step" aria-label="后一天" @click="shift(1)">›</button>
        <button v-if="date !== todayISO()" type="button" class="today-btn" @click="goToday">回到今天</button>
      </div>

      <div class="spacer"></div>

      <el-select v-model="floor" placeholder="全部楼层" clearable size="small" style="width: 130px" @change="loadGrid">
        <el-option v-for="f in floors" :key="f" :label="f" :value="f" />
      </el-select>
      <el-select v-model="capacity" placeholder="任意容量" clearable size="small" style="width: 120px" @change="loadGrid">
        <el-option v-for="c in CAPACITIES" :key="c" :label="`${c} 人以上`" :value="c" />
      </el-select>
    </div>

    <div class="board-summary">
      <p class="summary-line">
        <span class="num big">{{ rooms.length }}</span> 间会议室
        <template v-if="isToday">· 现在空闲 <span class="num big">{{ freeNow }}</span> 间</template>
      </p>
      <div class="legend">
        <span class="key free">空闲</span>
        <span class="key mine">我的预约</span>
        <span class="key busy">已占用</span>
        <span v-if="showNowLine" class="key now">当前时间</span>
      </div>
    </div>

    <div v-loading="loading" class="board panel">
      <div class="board-scroll">
        <div class="board-grid">
          <div class="corner">会议室</div>
          <div class="axis">
            <span
              v-for="(h, i) in HOURS"
              :key="h"
              class="tick num"
              :style="tickStyle(i, h)"
            >{{ String(h).padStart(2, '0') }}:00</span>
          </div>

          <template v-for="group in groupedRooms" :key="group.floor">
            <div class="floor-row">{{ group.floor || '其他' }}</div>
            <template v-for="room in group.rooms" :key="room.roomId">
              <div class="room-cell">
                <span class="room-name" :title="room.roomName">{{ room.roomName }}</span>
                <span class="room-meta num">{{ room.capacity }}人</span>
              </div>
              <div class="track" @click="onTrackClick($event, room)">
                <div
                  v-for="b in blocksMap[room.roomId] || []"
                  :key="b.id"
                  class="block"
                  :class="{ mine: b.isMine }"
                  :style="blockStyle(b)"
                  :title="b.isMine ? b.topic : '已占用'"
                  @click.stop="onBlockClick(b)"
                >
                  <span class="block-label">{{ b.isMine ? b.topic : '已占用' }}</span>
                </div>
                <div v-if="showNowLine" class="now-line" :style="{ left: nowPct }"></div>
              </div>
            </template>
          </template>
        </div>

        <div v-if="!loading && !rooms.length" class="empty">
          <p class="empty-title">这里还没有可预约的会议室</p>
          <p>换一个楼层或容量条件试试。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCalendar, getRoomList, getMyBookings } from '@/api'
import type { BookingInfo, RoomCalendar, TimeSlot } from '@/types'
import { useUiStore } from '@/stores/ui'
import {
  addDays,
  minutesOf,
  minutesToHHmm,
  shortDate,
  todayISO,
  weekdayCN
} from '@/utils/format'

interface Block {
  id: number
  start: number
  end: number
  topic?: string
  isMine?: boolean
  bookingId?: number
}

const router = useRouter()
const route = useRoute()
const ui = useUiStore()

const HOURS = Array.from({ length: 15 }, (_, i) => 8 + i)
const CAPACITIES = [2, 4, 6, 8, 10, 15, 20, 30]
const DAY_START = 8 * 60
const DAY_MIN = 14 * 60

const date = ref(todayISO())
const floor = ref<string | undefined>(undefined)
const capacity = ref<number | undefined>(undefined)
const rooms = ref<RoomCalendar[]>([])
const mine = ref<Map<number, BookingInfo>>(new Map())
const floors = ref<string[]>([])
const loading = ref(false)

// ticking clock so the "now" line stays accurate without a reload
const now = ref(Date.now())
let clockTimer: number | undefined

const isToday = computed(() => date.value === todayISO())
const dateLabel = computed(() => `${shortDate(date.value)} ${weekdayCN(date.value)}`)

const nowMinutes = computed(() => {
  const d = new Date(now.value)
  return d.getHours() * 60 + d.getMinutes()
})

const showNowLine = computed(
  () => isToday.value && nowMinutes.value >= DAY_START && nowMinutes.value <= DAY_START + DAY_MIN
)

const nowPct = computed(() => pct(nowMinutes.value))

const groupedRooms = computed(() => {
  const groups: { floor: string; rooms: RoomCalendar[] }[] = []
  for (const room of rooms.value) {
    const key = room.floor || ''
    let g = groups.find((x) => x.floor === key)
    if (!g) {
      g = { floor: key, rooms: [] }
      groups.push(g)
    }
    g.rooms.push(room)
  }
  return groups
})

const blocksMap = computed(() => {
  const map: Record<number, Block[]> = {}
  for (const room of rooms.value) {
    map[room.roomId] = toBlocks(room.bookedSlots, mine.value)
  }
  return map
})

const freeNow = computed(() => {
  const m = nowMinutes.value
  return rooms.value.filter((room) => {
    const blocks = blocksMap.value[room.roomId] || []
    return !blocks.some((b) => b.start <= m && b.end > m)
  }).length
})

function pct(min: number) {
  const clamped = Math.max(0, Math.min(DAY_MIN, min - DAY_START))
  return `${(clamped / DAY_MIN) * 100}%`
}

// offset is minutes elapsed since 08:00 (0 .. 840)
function offsetPct(offset: number) {
  const clamped = Math.max(0, Math.min(DAY_MIN, offset))
  return `${(clamped / DAY_MIN) * 100}%`
}

function tickStyle(index: number, hour: number) {
  const left = offsetPct((hour - 8) * 60)
  let transform = 'translateX(-50%)'
  if (index === 0) transform = 'translateX(0)'
  else if (index === HOURS.length - 1) transform = 'translateX(-100%)'
  return { left, transform }
}

function toBlocks(slots: TimeSlot[], mineMap: Map<number, BookingInfo>): Block[] {
  const blocks: Block[] = []
  let cur: Block | null = null
  for (const s of slots) {
    if (s.booked) {
      const sm = minutesOf(s.startTime)
      const em = minutesOf(s.endTime)
      if (cur && cur.bookingId === s.bookingId && cur.end === sm) {
        cur.end = em
      } else {
        const info = s.bookingId != null ? mineMap.get(s.bookingId) : undefined
        cur = {
          id: blocks.length,
          start: sm,
          end: em,
          bookingId: s.bookingId,
          isMine: !!info,
          topic: info?.topic
        }
        blocks.push(cur)
      }
    } else {
      cur = null
    }
  }
  return blocks
}

function blockStyle(b: Block) {
  return {
    left: pct(b.start),
    width: `${((b.end - b.start) / DAY_MIN) * 100}%`
  }
}

function onBlockClick(b: Block) {
  if (b.isMine && b.bookingId) {
    router.push(`/bookings/${b.bookingId}`)
  }
}

function onTrackClick(e: MouseEvent, room: RoomCalendar) {
  const el = e.currentTarget as HTMLElement
  const rect = el.getBoundingClientRect()
  const ratio = (e.clientX - rect.left) / rect.width
  let mins = Math.floor((ratio * DAY_MIN) / 30) * 30
  mins = Math.max(0, Math.min(DAY_MIN - 60, mins))
  ui.openBooking({
    roomId: room.roomId,
    bookingDate: date.value,
    startTime: minutesToHHmm(DAY_START + mins),
    endTime: minutesToHHmm(DAY_START + mins + 60)
  })
}

async function loadFloors() {
  const res = await getRoomList({ status: 1 })
  floors.value = Array.from(new Set(res.data.map((r) => r.floor).filter((f): f is string => !!f)))
}

async function loadGrid() {
  loading.value = true
  try {
    const [cal, mineRes] = await Promise.all([
      getCalendar({
        startDate: date.value,
        endDate: date.value,
        floor: floor.value,
        capacity: capacity.value
      }),
      getMyBookings({ pageSize: 100 })
    ])
    rooms.value = cal.data.rooms || []
    const map = new Map<number, BookingInfo>()
    for (const b of mineRes.data.list) {
      if (b.bookingDate === date.value) map.set(b.id, b)
    }
    mine.value = map
  } finally {
    loading.value = false
  }
}

function shift(n: number) {
  date.value = addDays(date.value, n)
  loadGrid()
}

function goToday() {
  date.value = todayISO()
  loadGrid()
}

watch(
  () => ui.version,
  () => loadGrid()
)

onMounted(() => {
  if (typeof route.query.date === 'string') date.value = route.query.date
  if (typeof route.query.floor === 'string') floor.value = route.query.floor
  loadFloors()
  loadGrid()
  clockTimer = window.setInterval(() => (now.value = Date.now()), 30000)
})

onUnmounted(() => {
  if (clockTimer) window.clearInterval(clockTimer)
})
</script>

<style scoped lang="scss">
.board-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.board-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
}

.date-nav {
  display: flex;
  align-items: center;
  gap: 6px;
}

.step {
  width: 30px;
  height: 30px;
  border: 1px solid var(--line);
  background: var(--surface);
  border-radius: var(--radius-sm);
  font-size: 17px;
  line-height: 1;
  color: var(--ink);
  cursor: pointer;

  &:hover {
    background: var(--surface-2);
  }
}

.date-btn {
  border: none;
  background: none;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 16px;
  color: var(--ink);
  cursor: pointer;
  padding: 4px 8px;
}

.today-btn {
  border: none;
  background: none;
  color: var(--ink-2);
  font: inherit;
  font-size: 12px;
  text-decoration: underline;
  cursor: pointer;
  padding: 4px;
}

.board-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.summary-line {
  margin: 0;
  color: var(--ink-2);
  font-size: 13px;
}

.big {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 18px;
  color: var(--ink);
}

.legend {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: var(--ink-2);
}

.key {
  display: inline-flex;
  align-items: center;
  gap: 6px;

  &::before {
    content: '';
    width: 11px;
    height: 11px;
    border: 1px solid var(--line-strong);
    border-radius: 2px;
  }

  &.free::before {
    background: var(--surface);
  }
  &.mine::before {
    background: var(--marker);
    border-color: var(--ink);
  }
  &.busy::before {
    background: var(--busy-soft);
  }
  &.now::before {
    background: var(--danger);
    border-color: var(--danger);
  }
}

/* ---------- the board ---------- */
.board {
  overflow: hidden;
}

.board-scroll {
  overflow-x: auto;
}

/* one grid for the axis AND every room row, so columns can never drift apart */
.board-grid {
  display: grid;
  grid-template-columns: 168px minmax(0, 1fr);
  min-width: 940px;
}

.corner {
  padding: 10px 14px;
  font-size: 12px;
  color: var(--ink-3);
  background: var(--surface);
  border-right: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
  position: sticky;
  left: 0;
  z-index: 2;
}

.axis {
  position: relative;
  height: 36px;
  background: var(--surface);
  border-bottom: 1px solid var(--line);
}

.tick {
  position: absolute;
  top: 10px;
  font-size: 11px;
  color: var(--ink-3);
  white-space: nowrap;
}

.floor-row {
  grid-column: 1 / -1;
  padding: 6px 14px;
  background: var(--surface-2);
  border-bottom: 1px solid var(--line);
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 12px;
  color: var(--ink-2);
}

.room-cell {
  height: 46px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  background: var(--surface);
  border-right: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
  position: sticky;
  left: 0;
  z-index: 1;
}

.room-name {
  font-weight: 600;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.room-meta {
  font-size: 12px;
  color: var(--ink-3);
  flex: none;
}

.track {
  position: relative;
  height: 46px;
  border-bottom: 1px solid var(--line);
  cursor: pointer;
  background-image: repeating-linear-gradient(
    to right,
    var(--line) 0,
    var(--line) 1px,
    transparent 1px,
    transparent calc(100% / 14)
  );

  &:hover {
    background-color: #f8f9f5;
  }
}

.block {
  position: absolute;
  top: 6px;
  bottom: 6px;
  background: var(--busy-soft);
  border: 1px solid var(--line-strong);
  border-radius: 3px;
  padding: 0 8px;
  display: flex;
  align-items: center;
  overflow: hidden;
  cursor: default;

  &.mine {
    background: var(--marker);
    border-color: var(--ink);
    cursor: pointer;
  }
}

.block-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.now-line {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 2px;
  margin-left: -1px;
  background: var(--danger);
  pointer-events: none;
  z-index: 2;
}
</style>
