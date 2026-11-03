<template>
  <div class="chatbot">
    <!-- 展开面板 -->
    <transition name="pop">
      <section v-if="open" class="panel">
        <header class="panel-head">
          <div class="head-title">
            <span class="dot" aria-hidden="true"></span>
            <span>预约助手</span>
          </div>
          <button type="button" class="icon-btn" aria-label="收起" @click="open = false">
            <el-icon><Close /></el-icon>
          </button>
        </header>

        <div ref="scrollRef" class="stream">
          <div v-if="!messages.length" class="intro">
            <p class="intro-title">你好，我是会议室预约助手</p>
            <p class="intro-sub">试试这样问我：</p>
            <button
              v-for="s in SUGGESTIONS"
              :key="s"
              type="button"
              class="suggestion"
              @click="send(s)"
            >
              {{ s }}
            </button>
          </div>

          <div
            v-for="(m, i) in messages"
            :key="i"
            class="msg"
            :class="m.role"
          >
            <div class="bubble">
              <p class="bubble-text">{{ m.text }}</p>

              <!-- 会议室推荐卡片 -->
              <div v-if="m.rooms?.length" class="cards">
                <button
                  v-for="r in m.rooms"
                  :key="r.roomId"
                  type="button"
                  class="room-card"
                  @click="bookRoom(r)"
                >
                  <span class="rc-name">{{ r.roomName }}</span>
                  <span class="rc-meta num">{{ r.floor }} · {{ r.capacity }}人</span>
                  <span v-if="r.equipmentText" class="rc-equip">{{ r.equipmentText }}</span>
                  <span v-if="r.timeRange" class="rc-time num">{{ r.timeRange }}</span>
                </button>
              </div>

              <!-- 预约卡片 -->
              <div v-if="m.bookings?.length" class="cards">
                <button
                  v-for="b in m.bookings"
                  :key="b.id"
                  type="button"
                  class="booking-card"
                  @click="goBooking(b.id)"
                >
                  <span class="bc-topic">{{ b.topic }}</span>
                  <span class="bc-meta num">{{ b.bookingDate }} {{ shortTime(b.startTime) }}–{{ shortTime(b.endTime) }}</span>
                  <span class="bc-room">{{ b.roomName }} · {{ b.statusName }}</span>
                </button>
              </div>
            </div>
          </div>

          <div v-if="loading" class="msg bot">
            <div class="bubble loading">
              <span class="tdot"></span><span class="tdot"></span><span class="tdot"></span>
            </div>
          </div>
        </div>

        <footer class="composer">
          <textarea
            v-model="draft"
            class="input"
            rows="1"
            placeholder="问我空闲会议室、我的预约…"
            @keydown.enter.exact.prevent="send()"
          ></textarea>
          <button
            type="button"
            class="send"
            :disabled="loading || !draft.trim()"
            aria-label="发送"
            @click="send()"
          >
            <el-icon><Promotion /></el-icon>
          </button>
        </footer>
      </section>
    </transition>

    <!-- 悬浮按钮 -->
    <button
      v-if="!open"
      type="button"
      class="fab"
      aria-label="打开预约助手"
      @click="toggle"
    >
      <el-icon :size="22"><ChatDotRound /></el-icon>
      <span class="fab-tip">预约助手</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import { askChatBot } from '@/api'
import type { ChatBotAnswer, ChatRoomCard } from '@/types'
import { useUiStore } from '@/stores/ui'

interface Msg {
  role: 'user' | 'bot'
  text: string
  rooms?: ChatRoomCard[]
  bookings?: ChatBotAnswer['bookings']
}

const SUGGESTIONS = [
  '明天下午2点到4点，能坐10人、有投影仪的会议室有哪些',
  '我最近有哪些预约',
  '创新厅在哪，能坐多少人'
]

const router = useRouter()
const ui = useUiStore()

const open = ref(false)
const draft = ref('')
const loading = ref(false)
const messages = ref<Msg[]>([])
const scrollRef = ref<HTMLElement | null>(null)

function toggle() {
  open.value = true
  nextTick(scrollToBottom)
}

function scrollToBottom() {
  const el = scrollRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

async function send(text?: string) {
  const question = (text ?? draft.value).trim()
  if (!question || loading.value) return

  messages.value.push({ role: 'user', text: question })
  draft.value = ''
  loading.value = true
  await nextTick()
  scrollToBottom()

  try {
    const res = await askChatBot(question)
    const data = res.data
    messages.value.push({
      role: 'bot',
      text: data.answer,
      rooms: data.rooms,
      bookings: data.bookings
    })
  } catch (e: any) {
    messages.value.push({
      role: 'bot',
      text: e?.response?.data?.message || '抱歉，我暂时无法回答，请稍后再试。'
    })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

function bookRoom(r: ChatRoomCard) {
  const [startTime, endTime] = (r.timeRange || '').split('–')
  ui.openBooking({
    roomId: r.roomId,
    bookingDate: r.date,
    startTime: startTime || undefined,
    endTime: endTime || undefined
  })
  open.value = false
}

function goBooking(id: number) {
  router.push(`/bookings/${id}`)
  open.value = false
}

function shortTime(t?: string) {
  if (!t) return ''
  return t.length >= 5 ? t.slice(0, 5) : t
}
</script>

<style scoped lang="scss">
.chatbot {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 80;
}

/* ---------- FAB ---------- */
.fab {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 48px;
  padding: 0 18px;
  border: 1px solid var(--ink);
  border-radius: 999px;
  background: var(--marker);
  color: var(--marker-ink, #161a17);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(22, 26, 23, 0.18);
  transition: transform 0.15s ease;

  &:hover {
    transform: translateY(-2px);
  }
}

.fab-tip {
  font-size: 14px;
}

/* ---------- panel ---------- */
.panel {
  width: 380px;
  max-width: calc(100vw - 32px);
  height: 560px;
  max-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border: 1px solid var(--line-strong, var(--line));
  border-radius: var(--radius);
  box-shadow: 0 16px 48px rgba(22, 26, 23, 0.22);
  overflow: hidden;
}

.panel-head {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--line);
  background: var(--surface);
}

.head-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 15px;
}

.dot {
  width: 9px;
  height: 9px;
  background: var(--marker);
  border: 1px solid var(--ink);
}

.icon-btn {
  border: none;
  background: none;
  color: var(--ink-2);
  cursor: pointer;
  display: inline-flex;
  padding: 4px;
  border-radius: var(--radius-sm);

  &:hover {
    background: var(--surface-2);
    color: var(--ink);
  }
}

.stream {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: var(--surface-2, #f5f5f3);
}

/* ---------- intro ---------- */
.intro {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.intro-title {
  margin: 0;
  font-weight: 600;
}

.intro-sub {
  margin: 4px 0 2px;
  font-size: 13px;
  color: var(--ink-3);
}

.suggestion {
  text-align: left;
  border: 1px solid var(--line);
  background: var(--surface);
  border-radius: var(--radius-sm);
  padding: 10px 12px;
  font: inherit;
  font-size: 13px;
  color: var(--ink);
  cursor: pointer;

  &:hover {
    border-color: var(--line-strong, var(--ink-2));
    background: var(--surface);
  }
}

/* ---------- messages ---------- */
.msg {
  display: flex;

  &.user {
    justify-content: flex-end;
  }

  &.bot {
    justify-content: flex-start;
  }
}

.bubble {
  max-width: 86%;
  padding: 10px 13px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg.user .bubble {
  background: var(--ink);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg.bot .bubble {
  background: var(--surface);
  color: var(--ink);
  border: 1px solid var(--line);
  border-bottom-left-radius: 4px;
}

.bubble-text {
  margin: 0;
}

/* ---------- cards ---------- */
.cards {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.room-card,
.booking-card {
  display: flex;
  flex-direction: column;
  gap: 3px;
  text-align: left;
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  background: var(--surface-2, #f7f7f5);
  font: inherit;
  cursor: pointer;

  &:hover {
    border-color: var(--marker);
    background: var(--surface);
  }
}

.rc-name,
.bc-topic {
  font-weight: 600;
  font-size: 13px;
}

.rc-meta,
.rc-time,
.bc-meta,
.bc-room {
  font-size: 12px;
  color: var(--ink-2);
}

.rc-equip {
  font-size: 12px;
  color: var(--ink-3);
}

/* ---------- loading ---------- */
.loading {
  display: inline-flex;
  gap: 5px;
  align-items: center;
  padding: 12px 14px;
}

.tdot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--ink-3);
  animation: bounce 1.2s infinite ease-in-out;

  &:nth-child(2) { animation-delay: 0.15s; }
  &:nth-child(3) { animation-delay: 0.3s; }
}

@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); opacity: 0.4; }
  40% { transform: translateY(-5px); opacity: 1; }
}

/* ---------- composer ---------- */
.composer {
  flex: none;
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid var(--line);
  background: var(--surface);
}

.input {
  flex: 1;
  resize: none;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  padding: 9px 11px;
  font: inherit;
  font-size: 14px;
  line-height: 1.5;
  max-height: 96px;
  background: var(--surface);
  color: var(--ink);

  &:focus {
    outline: none;
    border-color: var(--ink-2);
  }
}

.send {
  flex: none;
  width: 38px;
  height: 38px;
  border: 1px solid var(--ink);
  border-radius: var(--radius-sm);
  background: var(--marker);
  color: var(--marker-ink, #161a17);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;

  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
  }
}

/* ---------- transition ---------- */
.pop-enter-active,
.pop-leave-active {
  transition: opacity 0.16s ease, transform 0.16s ease;
}

.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.98);
}

@media (max-width: 640px) {
  .chatbot {
    right: 16px;
    bottom: 16px;
  }

  .fab-tip {
    display: none;
  }

  .panel {
    width: calc(100vw - 32px);
    height: calc(100vh - 120px);
  }
}
</style>
