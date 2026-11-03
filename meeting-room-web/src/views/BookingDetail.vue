<template>
  <div class="detail-page" v-loading="loading">
    <button type="button" class="back" @click="router.back()">‹ 返回</button>

    <template v-if="booking">
      <header class="head panel">
        <div class="head-main">
          <h2 class="topic">{{ booking.topic }}</h2>
          <p class="sub">
            <span>{{ booking.roomName }}</span>
            <span v-if="booking.roomLocation">· {{ booking.roomLocation }}</span>
          </p>
        </div>
        <StatusPill :status="booking.status" :name="booking.statusName" />
      </header>

      <section class="facts panel">
        <div class="fact">
          <span class="fact-label">日期</span>
          <span class="fact-value num">{{ booking.bookingDate }} {{ weekdayCN(booking.bookingDate) }}</span>
        </div>
        <div class="fact">
          <span class="fact-label">时间</span>
          <span class="fact-value num">{{ hhmm(booking.startTime) }}–{{ hhmm(booking.endTime) }}</span>
        </div>
        <div class="fact">
          <span class="fact-label">时长</span>
          <span class="fact-value num">{{ durationLabel(booking.durationMin) }}</span>
        </div>
        <div class="fact">
          <span class="fact-label">参会人数</span>
          <span class="fact-value num">{{ booking.attendeeCount || 1 }} 人</span>
        </div>
        <div class="fact">
          <span class="fact-label">预约编号</span>
          <span class="fact-value num">{{ booking.bookingNo }}</span>
        </div>
        <div class="fact">
          <span class="fact-label">提交时间</span>
          <span class="fact-value num">{{ booking.createTime?.replace('T', ' ').slice(0, 16) }}</span>
        </div>
      </section>

      <section v-if="booking.equipmentNeed?.length || booking.remark" class="panel notes">
        <div v-if="booking.equipmentNeed?.length" class="note-row">
          <span class="fact-label">需要设备</span>
          <div class="chips">
            <span v-for="e in booking.equipmentNeed" :key="e" class="chip">{{ equipmentLabel(e) }}</span>
          </div>
        </div>
        <div v-if="booking.remark" class="note-row">
          <span class="fact-label">备注</span>
          <p class="note-text">{{ booking.remark }}</p>
        </div>
      </section>

      <section v-if="booking.status === 'cancelled' && booking.cancelReason" class="panel cancel-note">
        <span class="fact-label">取消原因</span>
        <p class="note-text">{{ booking.cancelReason }}</p>
      </section>

      <QrCodeCard v-if="booking" :booking-id="booking.id" :status="booking.status" />

      <section class="panel minutes">
        <div class="panel-head">
          <span class="panel-title">会议纪要</span>
          <el-button v-if="!minutes && canWriteMinutes" class="el-button--marker" size="small" @click="editorOpen = true">
            写纪要
          </el-button>
        </div>
        <div class="panel-body">
          <template v-if="minutes">
            <ul v-if="agendaItems.length" class="agenda">
              <li v-for="(a, i) in agendaItems" :key="i">{{ a }}</li>
            </ul>
            <p class="minutes-content">{{ minutes.content }}</p>
            <p class="minutes-foot num">提交于 {{ minutes.submitTime?.replace('T', ' ').slice(0, 16) }}</p>
          </template>
          <p v-else-if="canWriteMinutes" class="muted">
            这次会议还没有纪要。趁记忆还新，记下结论和待办。
          </p>
          <p v-else class="muted">会议结束后可以在这里补写纪要。</p>
        </div>
      </section>

      <section v-if="canRate" class="panel rating-section">
        <div class="panel-head">
          <span class="panel-title">评价会议室</span>
          <el-button class="el-button--marker" size="small" @click="ratingOpen = true">写评价</el-button>
        </div>
        <div class="panel-body">
          <p class="muted">会议结束后24小时内可评价，您的评价将帮助改善会议环境。</p>
        </div>
      </section>

      <div v-if="canCancel" class="actions">
        <el-button type="danger" plain @click="cancelVisible = true">取消预约</el-button>
      </div>
    </template>

    <el-dialog v-model="editorOpen" title="写会议纪要" width="560px">
      <el-form label-position="top">
        <el-form-item label="议题（一行一条）">
          <el-input v-model="agendaText" type="textarea" :rows="3" placeholder="例如：\n评审范围\n排期与分工" />
        </el-form-item>
        <el-form-item label="纪要内容">
          <el-input v-model="contentText" type="textarea" :rows="6" placeholder="结论、决定、待办事项…" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editorOpen = false">取消</el-button>
        <el-button class="el-button--marker" :loading="saving" @click="saveMinutes">保存纪要</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="cancelVisible" title="取消预约" width="460px">
      <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="说明一下取消的原因（可选）" />
      <template #footer>
        <el-button @click="cancelVisible = false">再想想</el-button>
        <el-button type="danger" :loading="saving" @click="doCancel">确认取消</el-button>
      </template>
    </el-dialog>

    <RatingForm v-if="booking" :booking-id="booking.id" v-model="ratingOpen" @done="load" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBookingDetail, getMinutes, submitMinutes, cancelBooking } from '@/api'
import type { BookingInfo, MeetingMinutes } from '@/types'
import StatusPill from '@/components/StatusPill.vue'
import RatingForm from '@/components/RatingForm.vue'
import QrCodeCard from '@/components/QrCodeCard.vue'
import { useUiStore } from '@/stores/ui'
import { durationLabel, equipmentLabel, hhmm, weekdayCN } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const ui = useUiStore()

const id = Number(route.params.id)
const loading = ref(false)
const booking = ref<BookingInfo | null>(null)
const minutes = ref<MeetingMinutes | null>(null)

const editorOpen = ref(false)
const agendaText = ref('')
const contentText = ref('')
const cancelVisible = ref(false)
const cancelReason = ref('')
const saving = ref(false)
const ratingOpen = ref(false)

const canWriteMinutes = computed(
  () => booking.value?.status === 'completed' || booking.value?.status === 'signed'
)

const canRate = computed(
  () => booking.value?.status === 'completed'
)

const canCancel = computed(() => {
  if (!booking.value) return false
  if (booking.value.status !== 'confirmed' && booking.value.status !== 'pending') return false
  const start = new Date(`${booking.value.bookingDate}T${booking.value.startTime}`)
  return start.getTime() - Date.now() > 15 * 60 * 1000
})

const agendaItems = computed<string[]>(() => {
  if (!minutes.value?.agenda) return []
  try {
    const parsed = JSON.parse(minutes.value.agenda)
    return Array.isArray(parsed) ? parsed : [minutes.value.agenda]
  } catch {
    return minutes.value.agenda ? [minutes.value.agenda] : []
  }
})

async function load() {
  loading.value = true
  try {
    const res = await getBookingDetail(id)
    booking.value = res.data
    try {
      const m = await getMinutes(id)
      minutes.value = m.data
    } catch {
      minutes.value = null
    }
  } finally {
    loading.value = false
  }
}

async function saveMinutes() {
  if (!contentText.value.trim()) {
    ElMessage.warning('写点内容再保存吧')
    return
  }
  saving.value = true
  try {
    const agenda = agendaText.value
      .split('\n')
      .map((s) => s.trim())
      .filter(Boolean)
    await submitMinutes(id, {
      bookingId: id,
      agenda: agenda.length ? JSON.stringify(agenda) : undefined,
      content: contentText.value
    })
    ElMessage.success('纪要已保存')
    editorOpen.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function doCancel() {
  saving.value = true
  try {
    await cancelBooking(id, cancelReason.value)
    ElMessage.success('预约已取消')
    cancelVisible.value = false
    ui.bumpVersion()
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.detail-page {
  max-width: 820px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.back {
  align-self: flex-start;
  border: none;
  background: none;
  font: inherit;
  color: var(--ink-2);
  cursor: pointer;
  padding: 0;
}

.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 22px;
}

.topic {
  font-size: 22px;
}

.sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--ink-2);
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.facts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
}

.fact {
  padding: 16px 22px;
  border-right: 1px solid var(--line);
  border-bottom: 1px solid var(--line);

  &:nth-child(3n) {
    border-right: none;
  }

  &:nth-last-child(-n + 3) {
    border-bottom: none;
  }
}

.fact-label {
  display: block;
  font-size: 12px;
  color: var(--ink-3);
  margin-bottom: 4px;
}

.fact-value {
  font-weight: 600;
}

.notes,
.cancel-note {
  padding: 18px 22px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.cancel-note {
  border-color: var(--danger);
  background: var(--danger-soft);
}

.note-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.chip {
  border: 1px solid var(--line-strong);
  border-radius: 999px;
  padding: 2px 10px;
  font-size: 12px;
}

.note-text {
  margin: 0;
  line-height: 1.6;
}

.agenda {
  margin: 0 0 12px;
  padding-left: 18px;
  color: var(--ink);
}

.minutes-content {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.65;
}

.minutes-foot {
  margin: 14px 0 0;
  font-size: 12px;
  color: var(--ink-3);
}

.actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 640px) {
  .facts {
    grid-template-columns: 1fr 1fr;
  }

  .fact:nth-child(3n) {
    border-right: 1px solid var(--line);
  }

  .fact:nth-child(2n) {
    border-right: none;
  }
}
</style>
