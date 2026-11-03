<template>
  <span class="credit-badge" :class="level">
    <svg viewBox="0 0 16 16" width="14" height="14" fill="currentColor" aria-hidden="true">
      <path d="M8 1a7 7 0 1 0 0 14A7 7 0 0 0 8 1Zm-.5 3.5a.5.5 0 0 1 1 0v4a.5.5 0 0 1-1 0v-4ZM8 11a1 1 0 1 1 0-2 1 1 0 0 1 0 2Z"/>
    </svg>
    {{ score }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ score: number }>()

const level = computed(() => {
  if (props.score >= 80) return 'good'
  if (props.score >= 60) return 'warn'
  return 'danger'
})
</script>

<style scoped>
.credit-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 13px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 99px;
  line-height: 1;
  white-space: nowrap;
}
.credit-badge.good { background: #dcfce7; color: #15803d; }
.credit-badge.warn { background: #fed7aa; color: #c2410c; }
.credit-badge.danger { background: #fecaca; color: #dc2626; }
</style>
