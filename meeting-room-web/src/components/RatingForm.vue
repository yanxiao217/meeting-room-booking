<template>
  <el-dialog :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" title="评价会议室" width="480px" :close-on-click-modal="false" destroy-on-close>
    <div v-if="submitted" class="done">
      <p>感谢您的评价！</p>
    </div>
    <div v-if="!submitted" class="form-body">
      <div class="star-row">
        <span class="star" :class="{ on: form.rating >= 1 }" @click="form.rating = 1">★</span>
        <span class="star" :class="{ on: form.rating >= 2 }" @click="form.rating = 2">★</span>
        <span class="star" :class="{ on: form.rating >= 3 }" @click="form.rating = 3">★</span>
        <span class="star" :class="{ on: form.rating >= 4 }" @click="form.rating = 4">★</span>
        <span class="star" :class="{ on: form.rating >= 5 }" @click="form.rating = 5">★</span>
        <span class="rating-text">{{ ratingLabel(form.rating) }}</span>
      </div>

      <div class="tag-section">
        <p class="label">标签（可多选）</p>
        <div class="tags">
          <button type="button" class="tag-btn" :class="{ active: form.tags.includes('env_good') }" @click="toggleTag('env_good')">环境好</button>
          <button type="button" class="tag-btn" :class="{ active: form.tags.includes('equip_good') }" @click="toggleTag('equip_good')">设备好用</button>
          <button type="button" class="tag-btn" :class="{ active: form.tags.includes('quiet') }" @click="toggleTag('quiet')">安静</button>
          <button type="button" class="tag-btn" :class="{ active: form.tags.includes('spacious') }" @click="toggleTag('spacious')">宽敞</button>
          <button type="button" class="tag-btn" :class="{ active: form.tags.includes('ac_issue') }" @click="toggleTag('ac_issue')">空调问题</button>
          <button type="button" class="tag-btn" :class="{ active: form.tags.includes('noise') }" @click="toggleTag('noise')">隔音差</button>
          <button type="button" class="tag-btn" :class="{ active: form.tags.includes('projector_bad') }" @click="toggleTag('projector_bad')">投影仪故障</button>
          <button type="button" class="tag-btn" :class="{ active: form.tags.includes('dirty') }" @click="toggleTag('dirty')">卫生差</button>
        </div>
      </div>

      <el-input
        v-model="form.comment"
        type="textarea"
        :rows="3"
        maxlength="500"
        show-word-limit
        placeholder="说说您的感受（可选）"
      />
    </div>

    <template #footer>
      <div v-if="submitted">
        <el-button class="el-button--marker" @click="emit('update:modelValue', false)">完成</el-button>
      </div>
      <div v-if="!submitted">
        <el-button @click="emit('update:modelValue', false)">取消</el-button>
        <el-button class="el-button--marker" :loading="submitting" :disabled="!form.rating" @click="submit">
          提交评价
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { submitRating } from '@/api'

const props = defineProps({
  bookingId: { type: Number, required: true },
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'done'])

const submitting = ref(false)
const submitted = ref(false)

const form = reactive({
  rating: 0,
  tags: [] as string[],
  comment: ''
})

function ratingLabel(r: number) {
  if (r <= 0) return ''
  if (r === 1) return '很差'
  if (r === 2) return '较差'
  if (r === 3) return '一般'
  if (r === 4) return '不错'
  return '非常好'
}

function toggleTag(v: string) {
  const i = form.tags.indexOf(v)
  if (i >= 0) form.tags.splice(i, 1)
  else form.tags.push(v)
}

async function submit() {
  if (!form.rating) return
  submitting.value = true
  try {
    await submitRating({
      bookingId: props.bookingId,
      rating: form.rating,
      tags: form.tags.length ? JSON.stringify(form.tags) : undefined,
      comment: form.comment || undefined
    })
    submitted.value = true
    emit('done')
  } catch {
    // interceptor shows error
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.done { text-align: center; padding: 32px 0; }
.done p { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.star-row { display: flex; align-items: center; gap: 6px; margin-bottom: 20px; }
.star { font-size: 28px; color: #d1d5db; cursor: pointer; transition: color .15s; }
.star.on { color: #f59e0b; }
.star:hover { color: #fbbf24; }
.rating-text { font-size: 14px; color: var(--ink-3); margin-left: 8px; }
.tag-section { margin-bottom: 16px; }
.label { font-size: 13px; color: var(--ink-3); margin-bottom: 8px; }
.tags { display: flex; flex-wrap: wrap; gap: 8px; }
.tag-btn {
  border: 1px solid var(--line);
  background: var(--bg);
  border-radius: 6px;
  padding: 4px 12px;
  font-size: 13px;
  cursor: pointer;
  transition: all .15s;
}
.tag-btn.active { background: var(--primary); color: var(--ink); border-color: var(--primary); }
</style>
