import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/room',
    children: [
      {
        path: 'room',
        name: 'RoomManage',
        component: () => import('@/views/room/index.vue'),
        meta: { title: '会议室管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'booking',
        name: 'BookingManage',
        component: () => import('@/views/booking/index.vue'),
        meta: { title: '预约管理', icon: 'Calendar' }
      },
      {
        path: 'waitlist',
        name: 'WaitlistManage',
        component: () => import('@/views/booking/WaitlistManage.vue'),
        meta: { title: '候补管理', icon: 'List' }
      },
      {
        path: 'repair',
        name: 'RepairManage',
        component: () => import('@/views/repair/index.vue'),
        meta: { title: '报修管理', icon: 'Tools' }
      },
      {
        path: 'rating',
        name: 'RatingManage',
        component: () => import('@/views/rating/RatingManage.vue'),
        meta: { title: '评价管理', icon: 'Star' }
      },
      {
        path: 'stats',
        name: 'StatsDashboard',
        component: () => import('@/views/stats/index.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis' }
      },
      {
        path: 'config/rules',
        name: 'BookingRules',
        component: () => import('@/views/config/BookingRules.vue'),
        meta: { title: '预约规则', icon: 'Setting' }
      },
      {
        path: 'config/waitlist',
        name: 'WaitlistRules',
        component: () => import('@/views/config/WaitlistRules.vue'),
        meta: { title: '候补/周期/审批规则', icon: 'Setting' }
      },
      {
        path: 'config/quota',
        name: 'QuotaConfig',
        component: () => import('@/views/config/QuotaConfig.vue'),
        meta: { title: '部门配额', icon: 'Setting' }
      },
      {
        path: 'config/notification',
        name: 'NotificationConfig',
        component: () => import('@/views/config/NotificationConfig.vue'),
        meta: { title: '通知配置', icon: 'Bell' }
      },
      {
        path: 'config/credit',
        name: 'CreditRuleConfig',
        component: () => import('@/views/config/CreditRuleConfig.vue'),
        meta: { title: '信用分规则', icon: 'Star' }
      },
      {
        path: 'credit/records',
        name: 'CreditRecords',
        component: () => import('@/views/credit/CreditRecords.vue'),
        meta: { title: '信用记录', icon: 'Document' }
      },
      {
        path: 'dept',
        name: 'DeptManage',
        component: () => import('@/views/dept/index.vue'),
        meta: { title: '部门管理', icon: 'Collection' }
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'log',
        name: 'OperationLog',
        component: () => import('@/views/log/index.vue'),
        meta: { title: '操作日志', icon: 'Document' }
      },
      {
        path: 'chatbot-logs',
        name: 'ChatbotLogs',
        component: () => import('@/views/chatbot/ChatbotLogs.vue'),
        meta: { title: '问答日志', icon: 'ChatDotRound' }
      },
      {
        path: 'ai-models',
        name: 'AiModelConfig',
        component: () => import('@/views/ai/AiModelConfig.vue'),
        meta: { title: 'AI模型配置', icon: 'Cpu' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  document.title = `${to.meta.title || ''} - 会议室预约管理平台`
  
  // 不需要认证的页面直接放行
  if (to.meta.requiresAuth === false) {
    next()
    return
  }
  
  // 检查是否已登录
  if (!authStore.token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  
  next()
})

export default router
