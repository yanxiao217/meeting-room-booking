<template>
  <span class="pill" :class="status">{{ label }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ status: string; name?: string }>()

const LABELS: Record<string, string> = {
  pending: '待审批',
  confirmed: '已确认',
  signed: '已签到',
  completed: '已完成',
  cancelled: '已取消',
  expired: '已过期'
}

const label = computed(() => props.name || LABELS[props.status] || props.status)
</script>
