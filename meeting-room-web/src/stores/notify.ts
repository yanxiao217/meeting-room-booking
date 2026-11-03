import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '@/api'

export const useNotifyStore = defineStore('notify', () => {
  const unread = ref(0)

  async function refresh() {
    try {
      const res = await getUnreadCount()
      unread.value = res.data || 0
    } catch {
      // silent: unread count is non-critical chrome
    }
  }

  function set(n: number) {
    unread.value = n
  }

  return { unread, refresh, set }
})
