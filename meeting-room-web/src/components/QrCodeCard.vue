<template>
  <div class="qr-card panel">
    <div class="qr-head">
      <span class="panel-title">签到二维码</span>
      <span v-if="token" class="qr-hint">会议开始前30分钟内可扫码签到</span>
    </div>
    <div class="qr-body">
      <div v-if="loading" class="qr-loading">加载中…</div>
      <div v-else-if="token" class="qr-wrap">
        <canvas ref="canvasRef"></canvas>
        <p class="qr-no num">{{ bookingNo }}</p>
      </div>
      <div v-else class="qr-unavailable">
        <p>{{ unavailableReason }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, nextTick } from 'vue'
import QRCode from 'qrcode'
import { getBookingQrCode } from '@/api'

const props = defineProps<{
  bookingId: number
  status: string
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
const token = ref('')
const bookingNo = ref('')
const loading = ref(false)
const unavailableReason = ref('')

const canGenerate = ['confirmed']

async function generate() {
  if (!canGenerate.includes(props.status)) {
    unavailableReason.value = props.status === 'signed' ? '已签到，无需重复扫码' :
      props.status === 'cancelled' ? '预约已取消' :
      props.status === 'expired' ? '预约已过期' :
      '当前状态不可生成二维码'
    return
  }

  loading.value = true
  try {
    const res = await getBookingQrCode(props.bookingId)
    token.value = res.data.token
    bookingNo.value = res.data.token.slice(0, 12) + '…'
  } catch (e: any) {
    unavailableReason.value = e?.response?.data?.message || '获取二维码失败'
  } finally {
    loading.value = false
  }
  // 等 loading=false 模板渲染出 canvas 后再绘制二维码
  await nextTick()
  await renderQr()
}

async function renderQr() {
  if (!canvasRef.value || !token.value) return
  const url = `${location.origin}/scan/${token.value}`
  await QRCode.toCanvas(canvasRef.value, url, {
    width: 200,
    margin: 2,
    color: { dark: '#161A17', light: '#FFFFFF' }
  })
}

onMounted(generate)
watch(() => props.status, generate)
</script>

<style scoped lang="scss">
.qr-card {
  padding: 18px 22px;
}
.qr-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 14px;
}
.qr-hint {
  font-size: 12px;
  color: var(--ink-3);
}
.qr-body {
  display: flex;
  justify-content: center;
}
.qr-loading {
  padding: 40px;
  color: var(--ink-3);
}
.qr-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.qr-no {
  font-size: 12px;
  color: var(--ink-3);
  margin: 0;
}
.qr-unavailable {
  padding: 30px;
  text-align: center;
  color: var(--ink-3);
  font-size: 14px;
}
canvas {
  border: 1px solid var(--line);
  border-radius: 8px;
}
</style>
