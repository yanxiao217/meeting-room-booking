import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface BookingPrefill {
  roomId?: number
  bookingDate?: string
  startTime?: string
  endTime?: string
  topic?: string
  attendeeCount?: number
  equipmentNeed?: string[]
  remark?: string
}

export const useUiStore = defineStore('ui', () => {
  const bookingVisible = ref(false)
  const prefill = ref<BookingPrefill>({})
  // bumped whenever a booking is created/cancelled, so list views can reload
  const version = ref(0)

  function openBooking(p?: BookingPrefill) {
    prefill.value = p ? { ...p } : {}
    bookingVisible.value = true
  }

  function closeBooking() {
    bookingVisible.value = false
  }

  function bumpVersion() {
    version.value += 1
  }

  return { bookingVisible, prefill, version, openBooking, closeBooking, bumpVersion }
})
