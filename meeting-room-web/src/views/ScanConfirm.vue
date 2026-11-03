<template>
  <div class="scan-page">
    <div v-if="loading" class="state">验证二维码中…</div>

    <div v-else-if="error" class="state error">
      <p class="state-icon">✗</p>
      <p>{{ error }}</p>
      <button type="button" class="link" @click="router.push('/board')">返回首页</button>
    </div>

    <div v-else-if="booking" class="confirm-card panel">
      <header class="card-head">
        <StatusPill :status="booking.status" :name="booking.statusName" />
        <h2>{{ booking.topic }}</h2>
        <p class="sub">{{ booking.roomName }}</p>
      </header>

      <section class="card-facts">
        <div class="fact">
          <span class="fact-label">日期</span>
          <span class="fact-value num">{{ booking.bookingDate }}</span>
        </div>
        <div class="fact">
          <span class="fact-label">时间</span>
          <span class="fact-value num">{{ hhmm(booking.startTime) }}–{{ hhmm(booking.endTime) }}</span>
        </div>
        <div class="fact">
          <span class="fact-label">预约人</span>
          <span class="fact-value">{{ booking.userName }}</span>
        </div>
      </section>

      <div v-if="canSign" class="card-action">
        <el-button type="primary" :loading="signing" @click="doSign">
          确认签到
        </el-button>
        <p class="hint">签到后状态将不可撤销</p>
      </div>

      <div v-else class="card-action">
        <p class="hint">{{ signHint }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { scanBookingQrCode, signByQrCode } from '@/api'
import type { BookingInfo } from '@/types'
import StatusPill from '@/components/StatusPill.vue'
import { hhmm } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const token = route.params.token as string

const loading = ref(true)
const error = ref('')
const booking = ref<BookingInfo | null>(null)
const signing = ref(false)

const canSign = computed(() => booking.value?.status === 'confirmed')
const signHint = computed(() => {
  if (!booking.value) return ''
  const s = booking.value.status
  if (s === 'signed') return '该预约已签到，无需重复操作'
  if (s === 'cancelled') return '该预约已取消'
  if (s === 'expired') return '该预约已过期'
  return '当前状态不可签到'
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await scanBookingQrCode(token)
    booking.value = res.data
  } catch (e: any) {
    error.value = e?.response?.data?.message || '二维码无效或已过期'
  } finally {
    loading.value = false
  }
}

async function doSign() {
  signing.value = true
  try {
    await signByQrCode(token)
    ElMessage.success('签到成功')
    if (booking.value) {
      booking.value.status = 'signed'
      booking.value.statusName = '已签到'
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '签到失败')
  } finally {
    signing.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.scan-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: var(--bg);
}

.state {
  text-align: center;
  color: var(--ink-2);
  font-size: 15px;
  padding: 60px 20px;
}

.state.error {
  color: var(--danger);
}

.state-icon {
  font-size: 48px;
  margin: 0 0 12px;
}

.confirm-card {
  width: 100%;
  max-width: 420px;
  padding: 28px 24px;
}

.card-head {
  text-align: center;
  margin-bottom: 20px;
}

.card-head h2 {
  font-size: 20px;
  margin: 12px 0 4px;
}

.sub {
  font-size: 14px;
  color: var(--ink-2);
  margin: 0;
}

.card-facts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
  margin-bottom: 20px;
}

.fact {
  padding: 12px;
  text-align: center;
  border-right: 1px solid var(--line);

  &:last-child { border-right: none; }
}

.fact-label {
  display: block;
  font-size: 11px;
  color: var(--ink-3);
  margin-bottom: 4px;
}

.fact-value {
  font-weight: 600;
  font-size: 14px;
}

.card-action {
  text-align: center;
}

.hint {
  font-size: 12px;
  color: var(--ink-3);
  margin: 8px 0 0;
}

.link {
  border: none;
  background: none;
  font: inherit;
  color: var(--primary);
  text-decoration: underline;
  cursor: pointer;
  margin-top: 16px;
}
</style>
