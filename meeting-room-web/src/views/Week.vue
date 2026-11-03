<template>
  <div class="week-page">
    <div class="week-bar panel">
      <div>
        <p class="week-range">{{ rangeLabel }}</p>
        <p class="week-sub">点任意格子进入那一天的排期</p>
      </div>
      <div class="spacer"></div>
      <el-select v-model="floor" placeholder="全部楼层" clearable size="small" style="width: 130px" @change="loadWeek">
        <el-option v-for="f in floors" :key="f" :label="f" :value="f" />
      </el-select>
    </div>

    <div v-loading="loading" class="week panel">
      <div class="grid" :style="{ gridTemplateColumns: `168px repeat(${days.length}, minmax(96px, 1fr))` }">
        <div class="cell head corner">会议室</div>
        <button
          v-for="d in days"
          :key="d"
          type="button"
          class="cell head day"
          :class="{ today: d === todayISO() }"
          @click="goDay(d)"
        >
          <span class="dow">{{ weekdayCN(d) }}</span>
          <span class="dnum num">{{ shortDate(d) }}</span>
        </button>

        <template v-for="room in rooms" :key="room.roomId">
          <div class="cell room">
            <span class="room-name">{{ room.roomName }}</span>
            <span class="room-meta num">{{ room.capacity }}人</span>
          </div>
          <button
            v-for="d in days"
            :key="d + room.roomId"
            type="button"
            class="cell day-cell"
            :class="{ today: d === todayISO() }"
            @click="goDay(d)"
          >
            <template v-if="cellOf(d, room.roomId)">
              <span class="cell-count num">{{ cellOf(d, room.roomId)!.count }}</span>
              <span class="cell-unit">场</span>
              <span class="cell-bar">
                <span class="cell-fill" :style="{ width: barWidth(d, room.roomId) }"></span>
              </span>
            </template>
            <span v-else class="cell-empty">—</span>
          </button>
        </template>
      </div>

      <div v-if="!loading && !rooms.length" class="empty">
        <p class="empty-title">没有符合条件的会议室</p>
        <p>试试清空楼层筛选。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCalendar, getRoomList } from '@/api'
import type { RoomCalendar, TimeSlot } from '@/types'
import { addDays, minutesOf, shortDate, todayISO, weekdayCN } from '@/utils/format'

const router = useRouter()

const DAY_MIN = 14 * 60

const start = todayISO()
const days = Array.from({ length: 7 }, (_, i) => addDays(start, i))

const floor = ref<string | undefined>(undefined)
const floors = ref<string[]>([])
const rooms = ref<RoomCalendar[]>([])
const weekData = ref<Record<string, Record<number, { count: number; minutes: number }>>>({})
const loading = ref(false)

const rangeLabel = computed(() => `${shortDate(days[0])} – ${shortDate(days[6])}`)

function cellOf(day: string, roomId: number) {
  return weekData.value[day]?.[roomId]
}

function barWidth(day: string, roomId: number) {
  const cell = cellOf(day, roomId)
  if (!cell) return '0%'
  return `${Math.min(100, (cell.minutes / DAY_MIN) * 100)}%`
}

function merge(slots: TimeSlot[]) {
  let count = 0
  let minutes = 0
  let curBooking: number | undefined
  let curEnd = -1
  for (const s of slots) {
    if (!s.booked) {
      curBooking = undefined
      continue
    }
    const sm = minutesOf(s.startTime)
    const em = minutesOf(s.endTime)
    if (s.bookingId !== curBooking || sm !== curEnd) {
      count += 1
    }
    minutes += em - sm
    curBooking = s.bookingId
    curEnd = em
  }
  return { count, minutes }
}

async function loadFloors() {
  const res = await getRoomList({ status: 1 })
  floors.value = Array.from(new Set(res.data.map((r) => r.floor).filter((f): f is string => !!f)))
}

async function loadWeek() {
  loading.value = true
  try {
    const results = await Promise.all(
      days.map((d) => getCalendar({ startDate: d, endDate: d, floor: floor.value }))
    )
    rooms.value = results[0]?.data.rooms || []
    const map: Record<string, Record<number, { count: number; minutes: number }>> = {}
    results.forEach((res, i) => {
      const d = days[i]
      map[d] = {}
      for (const room of res.data.rooms || []) {
        map[d][room.roomId] = merge(room.bookedSlots || [])
      }
    })
    weekData.value = map
  } finally {
    loading.value = false
  }
}

function goDay(d: string) {
  router.push({ path: '/board', query: { date: d, floor: floor.value } })
}

onMounted(() => {
  loadFloors()
  loadWeek()
})
</script>

<style scoped lang="scss">
.week-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.week-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
}

.week-range {
  margin: 0;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 17px;
}

.week-sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--ink-3);
}

.week {
  overflow-x: auto;
}

.grid {
  display: grid;
  min-width: 760px;
}

.cell {
  border-bottom: 1px solid var(--line);
  border-right: 1px solid var(--line);
  padding: 10px 12px;
  min-height: 52px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.corner {
  font-size: 12px;
  color: var(--ink-3);
  background: var(--surface);
}

.head {
  position: sticky;
  top: 0;
  z-index: 2;
  background: var(--surface);
  flex-direction: column;
  align-items: flex-start;
  gap: 0;
  border: none;
  border-bottom: 1px solid var(--line);
  border-right: 1px solid var(--line);
  text-align: left;
  cursor: pointer;
  font: inherit;

  &.today {
    background: var(--surface-2);
  }
}

.dow {
  font-size: 12px;
  color: var(--ink-3);
}

.dnum {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 14px;
}

.room {
  justify-content: space-between;
  background: var(--surface);
  position: sticky;
  left: 0;
  z-index: 1;
}

.room-name {
  font-weight: 600;
  font-size: 13px;
}

.room-meta {
  font-size: 12px;
  color: var(--ink-3);
}

.day-cell {
  border: none;
  border-bottom: 1px solid var(--line);
  border-right: 1px solid var(--line);
  background: var(--surface);
  cursor: pointer;
  font: inherit;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  gap: 3px;
  text-align: left;

  &:hover {
    background: var(--surface-2);
  }

  &.today {
    background: #fafaf4;
  }
}

.cell-count {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 15px;
  line-height: 1;
}

.cell-unit {
  font-size: 11px;
  color: var(--ink-3);
}

.cell-bar {
  width: 100%;
  height: 4px;
  background: var(--surface-3);
  border-radius: 2px;
  overflow: hidden;
}

.cell-fill {
  display: block;
  height: 100%;
  background: var(--busy);
}

.cell-empty {
  color: var(--line-strong);
}
</style>
