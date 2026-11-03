import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/AppShell.vue'),
    redirect: '/board',
    children: [
      {
        path: 'board',
        name: 'Board',
        component: () => import('@/views/Board.vue'),
        meta: { title: '今日空闲' }
      },
      {
        path: 'week',
        name: 'Week',
        component: () => import('@/views/Week.vue'),
        meta: { title: '本周排期' }
      },
      {
        path: 'bookings',
        name: 'MyBookings',
        component: () => import('@/views/MyBookings.vue'),
        meta: { title: '我的预约' }
      },
      {
        path: 'bookings/:id',
        name: 'BookingDetail',
        component: () => import('@/views/BookingDetail.vue'),
        meta: { title: '预约详情' }
      },
      {
        path: 'waitlist',
        name: 'Waitlist',
        component: () => import('@/views/Waitlist.vue'),
        meta: { title: '我的候补' }
      },
      {
        path: 'minutes',
        name: 'MinutesArchive',
        component: () => import('@/views/MinutesArchive.vue'),
        meta: { title: '会议纪要' }
      },
      {
        path: 'repairs',
        name: 'Repairs',
        component: () => import('@/views/Repairs.vue'),
        meta: { title: '设备报修' }
      },
      {
        path: 'templates',
        name: 'Templates',
        component: () => import('@/views/Templates.vue'),
        meta: { title: '会议模板' }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/Notifications.vue'),
        meta: { title: '通知' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'credit',
        name: 'Credit',
        component: () => import('@/views/Credit.vue'),
        meta: { title: '信用分' }
      }
    ]
  },
  {
    path: '/scan/:token',
    name: 'ScanConfirm',
    component: () => import('@/views/ScanConfirm.vue'),
    meta: { title: '扫码签到' }
  },
  { path: '/:pathMatch(.*)*', redirect: '/board' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 会议室预约` : '会议室预约'
  const authStore = useAuthStore()
  if (to.meta.public) {
    if (authStore.token && to.name === 'Login') return { path: '/board' }
    return true
  }
  if (!authStore.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
