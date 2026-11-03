<template>
  <div class="credit-page">
    <div class="score-card">
      <div class="score-ring" :class="level">
        <span class="score-num num">{{ score }}</span>
        <span class="score-label">当前信用分</span>
      </div>
      <div class="score-meta">
        <p v-if="score < threshold" class="warn-text">
          信用分低于 {{ threshold }}，仅可预约未来 {{ restrictDays }} 天内时段
        </p>
        <p v-else class="ok-text">信用良好，可正常使用全部预约功能</p>
        <div class="rules">
          <div class="rule"><span class="rule-dot green"></span>≥ 80 正常</div>
          <div class="rule"><span class="rule-dot orange"></span>60–79 受限</div>
          <div class="rule"><span class="rule-dot red"></span>&lt; 60 严重受限</div>
        </div>
      </div>
    </div>

    <div class="section">
      <h3 class="section-title">变动记录</h3>
      <div v-if="records.length" class="record-list">
        <div v-for="r in records" :key="r.id" class="record-row">
          <div class="record-main">
            <span class="record-type">{{ typeLabel(r.changeType) }}</span>
            <span class="record-remark">{{ r.remark || '—' }}</span>
          </div>
          <div class="record-right">
            <span class="record-delta num" :class="r.changeValue > 0 ? 'up' : 'down'">
              {{ r.changeValue > 0 ? '+' : '' }}{{ r.changeValue }}
            </span>
            <span class="record-snap num">{{ r.beforeScore }} → {{ r.afterScore }}</span>
            <span class="record-time">{{ fmtTime(r.createTime) }}</span>
          </div>
        </div>
      </div>
      <p v-else class="empty">暂无信用记录</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyCreditScore, getMyCreditRecords } from '@/api'
import type { CreditRecord } from '@/types'

const score = ref(100)
const threshold = ref(60)
const restrictDays = ref(3)
const records = ref<CreditRecord[]>([])
const level = ref('good')

function typeLabel(t: string) {
  const map: Record<string, string> = {
    no_show: '爽约扣分',
    complete: '完成加分',
    weekly_recover: '周恢复',
    cancel_late: '逾期取消',
    admin_adjust: '管理员调整'
  }
  return map[t] || t
}

function fmtTime(v?: string) {
  if (!v) return ''
  return v.slice(0, 16).replace('T', ' ')
}

onMounted(async () => {
  try {
    const [scoreRes, recordsRes] = await Promise.all([
      getMyCreditScore(),
      getMyCreditRecords({ pageNum: 1, pageSize: 50 })
    ])
    score.value = scoreRes.data.score
    threshold.value = scoreRes.data.threshold
    restrictDays.value = scoreRes.data.restrictDays
    records.value = recordsRes.data.list || []
    if (score.value >= 80) level.value = 'good'
    else if (score.value >= 60) level.value = 'warn'
    else level.value = 'danger'
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.credit-page { padding: 0; }

.score-card {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 32px;
  background: var(--bg);
  border: 1px solid var(--line);
  border-radius: 14px;
}
.score-ring {
  width: 120px; height: 120px;
  border-radius: 50%;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  flex-shrink: 0;
}
.score-ring.good { background: #dcfce7; }
.score-ring.warn { background: #fed7aa; }
.score-ring.danger { background: #fecaca; }
.score-num { font-size: 36px; font-weight: 700; line-height: 1; }
.score-ring.good .score-num { color: #15803d; }
.score-ring.warn .score-num { color: #c2410c; }
.score-ring.danger .score-num { color: #dc2626; }
.score-label { font-size: 12px; color: var(--ink-3); margin-top: 4px; }
.score-meta { flex: 1; }
.warn-text { color: #dc2626; font-weight: 600; margin: 0 0 12px; }
.ok-text { color: var(--ink-2); margin: 0 0 12px; }
.rules { display: flex; gap: 16px; }
.rule { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--ink-3); }
.rule-dot { width: 8px; height: 8px; border-radius: 50%; }
.rule-dot.green { background: #22c55e; }
.rule-dot.orange { background: #f97316; }
.rule-dot.red { background: #dc2626; }

.section { margin-top: 32px; }
.section-title { font-size: 15px; font-weight: 600; margin: 0 0 16px; }

.record-list { display: flex; flex-direction: column; gap: 1px; background: var(--line); border-radius: 10px; overflow: hidden; }
.record-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; background: var(--bg);
}
.record-main { flex: 1; min-width: 0; }
.record-type { font-weight: 600; font-size: 14px; margin-right: 8px; }
.record-remark { font-size: 13px; color: var(--ink-3); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: inline-block; max-width: 200px; vertical-align: bottom; }
.record-right { display: flex; align-items: center; gap: 12px; flex-shrink: 0; margin-left: 12px; }
.record-delta { font-weight: 700; font-size: 15px; }
.record-delta.up { color: #16a34a; }
.record-delta.down { color: #dc2626; }
.record-snap { font-size: 12px; color: var(--ink-3); }
.record-time { font-size: 12px; color: var(--ink-3); white-space: nowrap; }
.empty { text-align: center; padding: 40px; color: var(--ink-3); }

@media (max-width: 640px) {
  .score-card { flex-direction: column; gap: 16px; text-align: center; }
  .rules { justify-content: center; }
  .record-row { flex-direction: column; align-items: flex-start; gap: 8px; }
  .record-right { margin-left: 0; }
}
</style>
