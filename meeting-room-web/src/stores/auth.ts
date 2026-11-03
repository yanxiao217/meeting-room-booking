import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo } from '@/types'
import { login as loginApi, register as registerApi, getUserInfo as getUserInfoApi, logout as logoutApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<UserInfo | null>(null)

  async function login(username: string, password: string) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    return res.data
  }

  async function register(payload: { username: string; password: string; phone?: string; realName?: string }) {
    const res = await registerApi(payload)
    return res.data
  }

  async function fetchUserInfo() {
    const res = await getUserInfoApi()
    user.value = res.data
    return res.data
  }

  async function logout() {
    try {
      if (token.value) await logoutApi()
    } catch {
      // ignore network errors on logout
    }
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  return { token, user, login, register, fetchUserInfo, logout }
})
