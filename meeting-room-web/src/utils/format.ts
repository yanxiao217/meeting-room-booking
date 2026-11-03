// Small formatting helpers shared across views.

export function todayISO(): string {
  return toISODate(new Date())
}

export function toISODate(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

export function addDays(iso: string, n: number): string {
  const d = new Date(`${iso}T00:00:00`)
  d.setDate(d.getDate() + n)
  return toISODate(d)
}

export function weekdayCN(iso: string): string {
  const names = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return names[new Date(`${iso}T00:00:00`).getDay()]
}

export function shortDate(iso: string): string {
  const [, m, d] = iso.split('-')
  return `${Number(m)}月${Number(d)}日`
}

// "09:00:00" -> "09:00"
export function hhmm(t?: string): string {
  if (!t) return ''
  return t.slice(0, 5)
}

// minutes since midnight for "HH:mm[:ss]"
export function minutesOf(t: string): number {
  const [h, m] = t.split(':')
  return Number(h) * 60 + Number(m)
}

// minutes since midnight -> "HH:mm"
export function minutesToHHmm(m: number): string {
  const h = Math.floor(m / 60)
  const mm = m % 60
  return `${String(h).padStart(2, '0')}:${String(mm).padStart(2, '0')}`
}

export function durationLabel(min?: number): string {
  if (!min) return ''
  const h = Math.floor(min / 60)
  const m = min % 60
  if (h && m) return `${h}小时${m}分`
  if (h) return `${h}小时`
  return `${m}分钟`
}

export function isPast(iso: string, endTime?: string): boolean {
  const now = new Date()
  const end = endTime ? `${iso}T${endTime}` : `${iso}T23:59:59`
  return new Date(end) < now
}

export const DEVICE_LABELS: Record<string, string> = {
  projector: '投影仪',
  tv: '电视大屏',
  whiteboard: '白板',
  video_conference: '视频会议',
  audio: '音响',
  音响: '音响',
  coffee_machine: '咖啡机',
  phone: '电话'
}

export function equipmentLabel(key: string): string {
  return DEVICE_LABELS[key] || key
}

export const REPAIR_DEVICE_LABELS: Record<string, string> = {
  projector: '投影仪',
  tv: '电视大屏',
  whiteboard: '白板',
  aircon: '空调',
  light: '灯光',
  other: '其他'
}

export const REPAIR_STATUS_LABELS: Record<string, string> = {
  pending: '待处理',
  processing: '处理中',
  finished: '已完成'
}
