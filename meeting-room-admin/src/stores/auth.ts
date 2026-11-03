import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo } from '@/types'
import { login as loginApi, getUserInfo as getUserInfoApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<UserInfo | null>(null)

  // 登录
  async function login(username: string, password: string) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    return res.data
  }

  // 获取用户信息
  async function fetchUserInfo() {
    const res = await getUserInfoApi()
    user.value = res.data
    return res.data
  }

  // 登出
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  // 是否是管理员
  function isAdmin() {
    return user.value?.role === 'admin'
  }

  return {
    token,
    user,
    login,
    fetchUserInfo,
    logout,
    isAdmin
  }
})
