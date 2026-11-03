<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside class="aside" :class="{ collapsed: isCollapse }">
      <div class="logo">
        <div class="logo-icon">
          <el-icon :size="20"><Calendar /></el-icon>
        </div>
        <transition name="fade">
          <span v-show="!isCollapse" class="title">会议预约</span>
        </transition>
      </div>
      
      <nav class="nav-menu">
        <template v-for="item in menuList" :key="item.path">
          <router-link
            :to="item.path"
            class="nav-item"
            :class="{ active: route.path === item.path }"
          >
            <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
            <transition name="fade">
              <span v-show="!isCollapse" class="nav-text">{{ item.title }}</span>
            </transition>
          </router-link>
        </template>
      </nav>
      
      <div class="aside-footer">
        <div class="collapse-btn" @click="isCollapse = !isCollapse">
          <el-icon :size="16">
            <DArrowLeft v-if="!isCollapse" />
            <DArrowRight v-else />
          </el-icon>
        </div>
      </div>
    </aside>
    
    <!-- 主体内容 -->
    <div class="main-container">
      <!-- 顶栏 -->
      <header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRoute?.meta?.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="authStore.user?.avatar">
                {{ authStore.user?.realName?.charAt(0) || authStore.user?.username?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ authStore.user?.realName || authStore.user?.username }}</span>
              <el-icon class="user-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 主体内容 -->
      <main class="main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapse = ref(false)

const currentRoute = computed(() => route)

const menuList = [
  { path: '/room', title: '会议室管理', icon: 'OfficeBuilding' },
  { path: '/booking', title: '预约管理', icon: 'Calendar' },
  { path: '/waitlist', title: '候补管理', icon: 'List' },
  { path: '/repair', title: '报修管理', icon: 'Tools' },
  { path: '/stats', title: '数据看板', icon: 'DataAnalysis' },
  { path: '/config/rules', title: '预约规则', icon: 'Setting' },
  { path: '/config/waitlist', title: '候补/周期/审批', icon: 'Setting' },
  { path: '/config/quota', title: '部门配额', icon: 'Setting' },
  { path: '/config/notification', title: '通知配置', icon: 'Bell' },
  { path: '/dept', title: '部门管理', icon: 'Collection' },
  { path: '/user', title: '用户管理', icon: 'User' },
  { path: '/log', title: '操作日志', icon: 'Document' },
  { path: '/chatbot-logs', title: '问答日志', icon: 'ChatDotRound' },
  { path: '/ai-models', title: 'AI模型配置', icon: 'Cpu' }
]

const handleCommand = (command: string) => {
  if (command === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}

onMounted(async () => {
  if (authStore.token && !authStore.user) {
    try {
      await authStore.fetchUserInfo()
    } catch (error) {
      authStore.logout()
      router.push('/login')
    }
  }
})
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  height: 100vh;
  background-color: #FAFAF9;
}

.aside {
  width: 200px;
  background-color: #FFFFFF;
  border-right: 1px solid #E8E5E0;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
  
  &.collapsed {
    width: 64px;
  }
  
  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    gap: 12px;
    border-bottom: 1px solid #E8E5E0;
    
    .logo-icon {
      width: 32px;
      height: 32px;
      background: linear-gradient(135deg, #5B8FB9 0%, #4A7A9E 100%);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      flex-shrink: 0;
    }
    
    .title {
      font-size: 16px;
      font-weight: 600;
      color: #2D3748;
      white-space: nowrap;
    }
  }
  
  .nav-menu {
    flex: 1;
    padding: 12px 8px;
    overflow-y: auto;
    
    .nav-item {
      display: flex;
      align-items: center;
      padding: 10px 12px;
      margin-bottom: 4px;
      border-radius: 6px;
      color: #4A5568;
      text-decoration: none;
      transition: all 0.2s ease;
      
      &:hover {
        background-color: #F7F6F3;
        color: #2D3748;
      }
      
      &.active {
        background-color: #EBF5FF;
        color: #5B8FB9;
        
        .nav-icon {
          color: #5B8FB9;
        }
      }
      
      .nav-icon {
        font-size: 18px;
        margin-right: 12px;
        flex-shrink: 0;
      }
      
      .nav-text {
        font-size: 14px;
        white-space: nowrap;
      }
    }
  }
  
  .aside-footer {
    padding: 12px;
    border-top: 1px solid #E8E5E0;
    
    .collapse-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 32px;
      border-radius: 6px;
      cursor: pointer;
      color: #718096;
      transition: all 0.2s ease;
      
      &:hover {
        background-color: #F7F6F3;
        color: #2D3748;
      }
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 64px;
  background-color: #FFFFFF;
  border-bottom: 1px solid #E8E5E0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  
  .header-left {
    .el-breadcrumb {
      :deep(.el-breadcrumb__item) {
        &:last-child {
          .el-breadcrumb__inner {
            color: #2D3748;
            font-weight: 500;
          }
        }
      }
    }
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      padding: 6px 12px;
      border-radius: 8px;
      transition: background-color 0.2s ease;
      
      &:hover {
        background-color: #F7F6F3;
      }
      
      .username {
        font-size: 14px;
        color: #2D3748;
        font-weight: 500;
      }
      
      .user-arrow {
        font-size: 12px;
        color: #718096;
      }
    }
  }
}

.main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: #FAFAF9;
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
