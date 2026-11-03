<template>
  <div class="notif-page">
    <div class="toolbar panel">
      <div class="filters">
        <button
          v-for="f in FILTERS"
          :key="f.value"
          type="button"
          class="filter"
          :class="{ on: type === f.value }"
          @click="setType(f.value)"
        >
          {{ f.label }}
        </button>
      </div>
      <div class="spacer"></div>
      <span class="unread num">{{ unreadCount }} 条未读</span>
      <el-button size="small" :disabled="!unreadCount" @click="readAll">全部已读</el-button>
    </div>

    <div v-loading="loading" class="list">
      <article
        v-for="n in items"
        :key="n.id"
        class="notif panel"
        :class="{ unread: !n.isRead }"
        @click="openItem(n)"
      >
        <span class="marker-dot" :class="{ on: !n.isRead }"></span>
        <div class="notif-main">
          <div class="notif-top">
            <span class="notif-title">{{ n.title }}</span>
            <span class="notif-time num">{{ n.createTime?.replace('T', ' ').slice(0, 16) }}</span>
          </div>
          <p class="notif-content">{{ n.content }}</p>
          <span class="notif-type">{{ n.typeName }}</span>
        </div>
      </article>

      <div v-if="!loading && !items.length" class="empty">
        <p class="empty-title">这里很安静</p>
        <p>预约、提醒、审批和报修的通知都会出现在这里。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, markNotificationRead, markAllNotificationsRead } from '@/api'
import type { NotificationInfo } from '@/types'
import { useNotifyStore } from '@/stores/notify'

const FILTERS = [
  { label: '全部', value: '' },
  { label: '预约', value: 'success' },
  { label: '提醒', value: 'remind' },
  { label: '审批', value: 'approve' },
  { label: '系统', value: 'system' }
]

const router = useRouter()
const notify = useNotifyStore()

const loading = ref(false)
const items = ref<NotificationInfo[]>([])
const type = ref('')

const unreadCount = computed(() => items.value.filter((n) => !n.isRead).length)

function setType(v: string) {
  type.value = v
  load()
}

async function load() {
  loading.value = true
  try {
    const res = await getNotifications({ type: type.value || undefined, pageSize: 50 })
    items.value = res.data.list
  } finally {
    loading.value = false
  }
}

async function openItem(n: NotificationInfo) {
  if (!n.isRead) {
    n.isRead = 1
    try {
      await markNotificationRead(n.id)
    } catch {
      // ignore
    }
    notify.refresh()
  }
  if (n.bizType === 'booking' && n.bizId) {
    router.push(`/bookings/${n.bizId}`)
  }
}

async function readAll() {
  await markAllNotificationsRead()
  items.value = items.value.map((n) => ({ ...n, isRead: 1 }))
  notify.refresh()
}

onMounted(load)
</script>

<style scoped lang="scss">
.notif-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  flex-wrap: wrap;
}

.filters {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.filter {
  border: none;
  background: none;
  font: inherit;
  font-size: 13px;
  color: var(--ink-2);
  padding: 5px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;

  &:hover {
    background: var(--surface-2);
  }

  &.on {
    background: var(--ink);
    color: #fff;
  }
}

.unread {
  font-size: 13px;
  color: var(--ink-2);
}

.list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 160px;
}

.notif {
  display: flex;
  gap: 14px;
  padding: 15px 18px;
  cursor: pointer;

  &:hover {
    border-color: var(--line-strong);
  }

  &.unread {
    background: #fbfbf3;
  }
}

.marker-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex: none;
  margin-top: 7px;
  background: var(--line);

  &.on {
    background: var(--marker);
    box-shadow: 0 0 0 1px var(--ink);
  }
}

.notif-main {
  flex: 1;
  min-width: 0;
}

.notif-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 14px;
}

.notif-title {
  font-weight: 600;
}

.notif-time {
  font-size: 12px;
  color: var(--ink-3);
  white-space: nowrap;
}

.notif-content {
  margin: 5px 0 0;
  color: var(--ink-2);
  font-size: 13px;
  line-height: 1.55;
}

.notif-type {
  display: inline-block;
  margin-top: 8px;
  font-size: 11px;
  color: var(--ink-3);
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 1px 8px;
}
</style>
