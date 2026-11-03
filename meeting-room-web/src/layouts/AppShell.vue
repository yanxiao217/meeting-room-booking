<template>
  <div class="shell">
    <aside class="rail" :class="{ open: drawerOpen }">
      <div class="brand">
        <span class="brand-mark" aria-hidden="true"></span>
        <span class="brand-name">会议室预约</span>
      </div>

      <nav class="nav">
        <template v-for="group in navGroups" :key="group.label">
          <p v-if="group.label" class="nav-label">{{ group.label }}</p>
          <router-link
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
            @click="drawerOpen = false"
          >
            <span>{{ item.title }}</span>
            <span v-if="item.badge" class="nav-badge num">{{ item.badge }}</span>
          </router-link>
        </template>
      </nav>

      <div class="rail-foot">
        <router-link to="/profile" class="user-chip" @click="drawerOpen = false">
          <span class="avatar">{{ initial }}</span>
          <span class="user-meta">
            <span class="user-name">{{ displayName }}</span>
            <span class="user-sub">{{ user?.deptName || roleLabel }}</span>
          </span>
        </router-link>
        <button class="logout" type="button" @click="handleLogout">退出登录</button>
      </div>
    </aside>

    <div v-if="drawerOpen" class="scrim" @click="drawerOpen = false"></div>

    <div class="body">
      <header class="topbar">
        <button class="menu-btn" type="button" aria-label="打开菜单" @click="drawerOpen = true">
          <el-icon><Menu /></el-icon>
        </button>
        <div class="titles">
          <h1 class="page-title">{{ route.meta.title }}</h1>
          <p class="page-sub">{{ todayLabel }}</p>
        </div>
        <div class="spacer"></div>
        <div class="clock num" aria-label="当前时间">{{ clock }}</div>
        <router-link to="/notifications" class="bell" aria-label="通知">
          <el-icon><Bell /></el-icon>
          <span v-if="notify.unread" class="bell-dot num">{{ notify.unread > 99 ? '99+' : notify.unread }}</span>
        </router-link>
        <el-button class="el-button--marker" @click="ui.openBooking()">新建预约</el-button>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>

    <BookingDialog />
    <ChatBot />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { useNotifyStore } from '@/stores/notify'
import BookingDialog from '@/components/BookingDialog.vue'
import ChatBot from '@/components/ChatBot.vue'
import { todayISO, weekdayCN, shortDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const ui = useUiStore()
const notify = useNotifyStore()

const drawerOpen = ref(false)
const clock = ref('')

const user = computed(() => auth.user)
const displayName = computed(() => user.value?.realName || user.value?.username || '未登录')
const initial = computed(() => displayName.value.charAt(0))
const roleLabel = computed(() => (user.value?.role === 'admin' ? '管理员' : '用户'))

const todayLabel = computed(() => {
  const iso = todayISO()
  return `${shortDate(iso)} ${weekdayCN(iso)}`
})

const navGroups = computed(() => [
  {
    label: '',
    items: [
      { path: '/board', title: '今日空闲', badge: 0 },
      { path: '/week', title: '本周排期', badge: 0 }
    ]
  },
  {
    label: '我的',
    items: [
      { path: '/bookings', title: '我的预约', badge: 0 },
      { path: '/waitlist', title: '我的候补', badge: 0 },
      { path: '/minutes', title: '会议纪要', badge: 0 },
      { path: '/templates', title: '会议模板', badge: 0 }
    ]
  },
  {
    label: '其他',
    items: [
      { path: '/credit', title: '信用分', badge: 0 },
      { path: '/repairs', title: '设备报修', badge: 0 },
      { path: '/notifications', title: '通知', badge: notify.unread },
      { path: '/profile', title: '个人中心', badge: 0 }
    ]
  }
])

function isActive(path: string) {
  return route.path === path || route.path.startsWith(path + '/')
}

function tick() {
  const d = new Date()
  clock.value = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}

let timer: number | undefined

watch(
  () => route.path,
  () => {
    notify.refresh()
  }
)

onMounted(async () => {
  tick()
  timer = window.setInterval(tick, 20000)
  if (auth.token && !auth.user) {
    try {
      await auth.fetchUserInfo()
    } catch {
      // request interceptor already redirected on 401
    }
  }
  notify.refresh()
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped lang="scss">
.shell {
  display: flex;
  min-height: 100vh;
}

/* ---------- rail ---------- */
.rail {
  width: var(--rail);
  flex: none;
  background: var(--surface);
  border-right: 1px solid var(--line);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 22px 22px 18px;
}

.brand-mark {
  width: 15px;
  height: 15px;
  background: var(--marker);
  border: 1px solid var(--ink);
  flex: none;
}

.brand-name {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 16px;
  letter-spacing: -0.02em;
}

.nav {
  flex: 1;
  padding: 4px 12px;
  overflow-y: auto;
}

.nav-label {
  font-size: 12px;
  color: var(--ink-3);
  margin: 16px 10px 6px;
}

.nav-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 9px 10px;
  border-radius: var(--radius-sm);
  text-decoration: none;
  color: var(--ink-2);
  font-weight: 500;
  margin-bottom: 2px;

  &:hover {
    background: var(--surface-2);
    color: var(--ink);
  }

  &.active {
    background: var(--ink);
    color: #fff;
  }
}

.nav-badge {
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--marker);
  color: var(--marker-ink);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.rail-foot {
  border-top: 1px solid var(--line);
  padding: 14px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: inherit;
  padding: 6px;
  border-radius: var(--radius-sm);

  &:hover {
    background: var(--surface-2);
  }
}

.avatar {
  width: 32px;
  height: 32px;
  flex: none;
  border-radius: 50%;
  background: var(--ink);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-weight: 700;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
  min-width: 0;
}

.user-name {
  font-weight: 600;
  font-size: 13px;
}

.user-sub {
  font-size: 12px;
  color: var(--ink-3);
}

.logout {
  width: 100%;
  margin-top: 8px;
  padding: 7px;
  background: none;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  color: var(--ink-2);
  font: inherit;
  font-size: 12px;
  cursor: pointer;

  &:hover {
    border-color: var(--line-strong);
    color: var(--ink);
  }
}

/* ---------- body ---------- */
.body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: var(--topbar);
  flex: none;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 24px;
  background: var(--surface);
  border-bottom: 1px solid var(--line);
  position: sticky;
  top: 0;
  z-index: 20;
}

.menu-btn {
  display: none;
  background: none;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  width: 36px;
  height: 36px;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--ink);
}

.titles {
  min-width: 0;
}

.page-title {
  font-size: 19px;
}

.page-sub {
  margin: 0;
  font-size: 12px;
  color: var(--ink-3);
}

.clock {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 17px;
  letter-spacing: -0.01em;
}

.bell {
  position: relative;
  width: 38px;
  height: 38px;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--ink);
  text-decoration: none;

  &:hover {
    background: var(--surface-2);
  }
}

.bell-dot {
  position: absolute;
  top: -7px;
  right: -7px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border-radius: 999px;
  background: var(--danger);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--surface);
}

.content {
  flex: 1;
  padding: 24px;
  min-width: 0;
}

.scrim {
  display: none;
}

/* ---------- mobile ---------- */
@media (max-width: 900px) {
  .rail {
    position: fixed;
    z-index: 60;
    left: 0;
    top: 0;
    transform: translateX(-100%);
    transition: transform 0.22s ease;
    box-shadow: 0 0 0 1px var(--line);

    &.open {
      transform: translateX(0);
    }
  }

  .scrim {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(22, 26, 23, 0.35);
    z-index: 50;
  }

  .menu-btn {
    display: inline-flex;
  }

  .clock {
    display: none;
  }

  .topbar {
    padding: 0 16px;
    gap: 12px;
  }

  .content {
    padding: 16px;
  }
}
</style>
