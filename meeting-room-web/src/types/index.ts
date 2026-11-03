// ---- transport ----
export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T = unknown> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

// ---- auth ----
export interface UserInfo {
  id: number
  username: string
  phone?: string
  email?: string
  realName?: string
  avatar?: string
  deptId?: number
  deptName?: string
  role: string
  status: number
  createTime?: string
}

export interface LoginResult {
  token: string
  tokenType: string
  expiresIn: number
  user: UserInfo
}

// ---- rooms ----
export interface RoomInfo {
  id: number
  roomName: string
  floor?: string
  location?: string
  capacity: number
  roomType?: string
  roomTypeName?: string
  equipment?: string[]
  images?: string[]
  description?: string
  status: number
  statusName?: string
  needApproval?: number
  sort?: number
  todayBookingCount?: number
  createTime?: string
}

export interface TimeSlot {
  startTime: string
  endTime: string
  booked: boolean
  bookingId?: number
  topic?: string
  isMine?: boolean
}

export interface RoomCalendar {
  roomId: number
  roomName: string
  floor?: string
  capacity: number
  status: number
  bookedSlots: TimeSlot[]
}

export interface CalendarData {
  dateRange: string
  rooms: RoomCalendar[]
}

export interface AvailabilitySlot {
  startTime: string
  endTime: string
  available: boolean
}

export interface Availability {
  roomId: number
  date: string
  availableSlots: AvailabilitySlot[]
  suggestedSlots?: AvailabilitySlot[]
}

// ---- bookings ----
export interface BookingInfo {
  id: number
  bookingNo: string
  roomId: number
  roomName?: string
  roomLocation?: string
  userId: number
  userName?: string
  bookingDate: string
  startTime: string
  endTime: string
  durationMin: number
  topic: string
  attendeeCount?: number
  equipmentNeed?: string[]
  remark?: string
  status: string
  statusName?: string
  cancelReason?: string
  createTime?: string
}

export interface BookingCreatePayload {
  roomId: number
  bookingDate: string
  startTime: string
  endTime: string
  topic: string
  attendeeCount?: number
  equipmentNeed?: string[]
  remark?: string
}

// ---- notifications ----
export interface NotificationInfo {
  id: number
  type: string
  typeName?: string
  title: string
  content?: string
  bizType?: string
  bizId?: number
  isRead: number
  createTime?: string
}

// ---- waitlist (raw entity) ----
export interface WaitlistItem {
  id: number
  roomId: number
  bookingDate: string
  startTime: string
  endTime: string
  userId: number
  rankNo: number
  status: string
  notifyTime?: string
  confirmDeadline?: string
  createTime?: string
}

// ---- repairs (raw entity) ----
export interface RepairOrder {
  id: number
  orderNo: string
  roomId: number
  reporterId: number
  deviceType: string
  faultDesc: string
  images?: string
  status: string
  assigneeId?: number
  handleDesc?: string
  finishedTime?: string
  createTime?: string
}

// ---- minutes ----
export interface MeetingMinutes {
  id?: number
  bookingId: number
  authorId?: number
  agenda?: string
  content?: string
  attachmentList?: string
  submitTime?: string
  createTime?: string
  updateTime?: string
}

// ---- templates (raw entity) ----
export interface BookingTemplate {
  id?: number
  userId?: number
  templateName: string
  roomId?: number
  startTime?: string
  endTime?: string
  topic?: string
  attendeeCount?: number
  equipmentNeed?: string
  remark?: string
  createTime?: string
}

// ---- credit ----
export interface CreditRecord {
  id: number
  userId: number
  changeType: string
  changeValue: number
  beforeScore: number
  afterScore: number
  bizType?: string
  bizId?: number
  remark?: string
  createTime?: string
}

// ---- rating ----
export interface RoomRating {
  id: number
  bookingId: number
  roomId: number
  userId: number
  rating: number
  tags?: string
  comment?: string
  userName?: string
  createTime?: string
}

// ---- chatbot ----
export interface ChatRoomCard {
  roomId: number
  roomName: string
  floor?: string
  location?: string
  capacity: number
  equipment?: string[]
  equipmentText?: string
  date?: string
  timeRange?: string
  avgRating?: number
}

export interface ChatBookingCard {
  id: number
  bookingNo?: string
  roomName?: string
  bookingDate?: string
  startTime?: string
  endTime?: string
  topic?: string
  status?: string
  statusName?: string
}

export interface ChatBotAnswer {
  answer: string
  intent?: string
  intentName?: string
  success: boolean
  fallback?: string
  rooms?: ChatRoomCard[]
  bookings?: ChatBookingCard[]
}
