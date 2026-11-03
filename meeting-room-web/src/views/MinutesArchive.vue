<template>
  <div class="minutes-page">
    <div class="toolbar panel">
      <el-input
        v-model="keyword"
        placeholder="按主题、会议室或纪要内容搜索"
        clearable
        style="max-width: 360px"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <div class="spacer"></div>
      <span class="count num">{{ filtered.length }} 条纪要</span>
    </div>

    <div v-loading="loading" class="list">
      <article v-for="entry in filtered" :key="entry.booking.id" class="entry panel" @click="open(entry.booking.id)">
        <div class="entry-head">
          <span class="entry-topic">{{ entry.booking.topic }}</span>
          <span class="entry-when num">
            {{ entry.booking.bookingDate }} · {{ entry.booking.roomName }}
          </span>
        </div>
        <p class="entry-body">{{ excerpt(entry.minutes.content) }}</p>
        <div class="entry-foot">
          <span class="num">{{ entry.minutes.submitTime?.replace('T', ' ').slice(0, 16) }}</span>
          <span class="open">查看详情</span>
        </div>
      </article>

      <div v-if="!loading && !filtered.length" class="empty">
        <p class="empty-title">{{ keyword ? '没有匹配的纪要' : '还没有归档的纪要' }}</p>
        <p>{{ keyword ? '换个关键词试试。' : '会议结束后，在预约详情里写下纪要，它会出现在这里。' }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMyBookings, getMinutes } from '@/api'
import type { BookingInfo, MeetingMinutes } from '@/types'

interface Entry {
  booking: BookingInfo
  minutes: MeetingMinutes
}

const router = useRouter()
const loading = ref(false)
const keyword = ref('')
const entries = ref<Entry[]>([])

const filtered = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return entries.value
  return entries.value.filter((e) => {
    const hay = `${e.booking.topic} ${e.booking.roomName || ''} ${e.minutes.content || ''}`.toLowerCase()
    return hay.includes(k)
  })
})

function excerpt(text?: string) {
  if (!text) return '（无内容）'
  return text.length > 120 ? text.slice(0, 120) + '…' : text
}

function open(id: number) {
  router.push(`/bookings/${id}`)
}

async function load() {
  loading.value = true
  try {
    const res = await getMyBookings({ pageSize: 100 })
    const completed = res.data.list.filter((b) => b.status === 'completed')
    const results = await Promise.allSettled(completed.map((b) => getMinutes(b.id)))
    const list: Entry[] = []
    results.forEach((r, i) => {
      if (r.status === 'fulfilled' && r.value.data) {
        list.push({ booking: completed[i], minutes: r.value.data })
      }
    })
    list.sort((a, b) => (b.booking.bookingDate || '').localeCompare(a.booking.bookingDate || ''))
    entries.value = list
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.minutes-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}

.count {
  font-size: 13px;
  color: var(--ink-2);
}

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 160px;
}

.entry {
  padding: 16px 20px;
  cursor: pointer;
  transition: border-color 0.15s ease;

  &:hover {
    border-color: var(--line-strong);
  }
}

.entry-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 14px;
}

.entry-topic {
  font-weight: 600;
  font-size: 15px;
}

.entry-when {
  font-size: 12px;
  color: var(--ink-3);
  white-space: nowrap;
}

.entry-body {
  margin: 8px 0 0;
  color: var(--ink-2);
  font-size: 13px;
  line-height: 1.6;
}

.entry-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 12px;
  color: var(--ink-3);
}

.open {
  color: var(--ink);
  text-decoration: underline;
}
</style>
