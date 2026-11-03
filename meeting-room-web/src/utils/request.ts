import { ElMessage } from 'element-plus'
import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import type { Result } from '@/types'
import { useAuthStore } from '@/stores/auth'

const service: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 30000
})

service.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers['Authorization'] = `Bearer ${authStore.token}`
  }
  return config
})

service.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        const authStore = useAuthStore()
        authStore.logout()
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    let message = '网络异常，请稍后重试'
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        message = '登录已过期，请重新登录'
        const authStore = useAuthStore()
        authStore.logout()
        window.location.href = '/login'
      } else if (status === 403) {
        message = '没有权限访问'
      } else if (status === 404) {
        message = '请求的资源不存在'
      } else if (status === 500) {
        message = '服务器异常'
      } else {
        message = error.response.data?.message || message
      }
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
