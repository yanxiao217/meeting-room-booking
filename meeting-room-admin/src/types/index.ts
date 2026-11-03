// 通用响应类型
export interface Result<T = any> {
  code: number
  message: string
  data: T
}

// 分页响应类型
export interface PageResult<T = any> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

// 用户类型
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

// 登录响应
export interface LoginResult {
  token: string
  tokenType: string
  expiresIn: number
  user: UserInfo
}

// 会议室类型
export interface RoomInfo {
  id: number
  roomName: string
  floor: string
  location?: string
  capacity: number
  roomType: string
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

// 预约类型
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

// 通知类型
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

// 系统配置类型
export interface ConfigInfo {
  id: number
  configKey: string
  configValue: string
  configName: string
  description?: string
}

// 信用记录类型
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

// 评价记录类型
export interface RatingRecord {
  id: number
  bookingId: number
  roomId: number
  userId: number
  rating: number
  tags?: string
  comment?: string
  userName?: string
  roomName?: string
  createTime?: string
}

// 低分检查记录类型
export interface RatingCheckRecord {
  id: number
  ratingId: number
  roomId: number
  status: string
  repairId?: number
  remark?: string
  rating?: number
  comment?: string
  tags?: string
  userName?: string
  roomName?: string
  createTime?: string
}

// 操作日志类型
export interface OperationLogInfo {
  id: number
  userId?: number
  username?: string
  module?: string
  operation?: string
  method?: string
  params?: string
  ip?: string
  status: number
  errorMsg?: string
  costTime?: number
  createTime?: string
}

// 统计数据类型
export interface StatsInfo {
  roomId?: number
  roomName?: string
  bookingCount?: number
  usageRate?: number
  avgDuration?: number
  totalDuration?: number
}

// 部门类型
export interface DeptInfo {
  id: number
  deptName: string
  parentId?: number
  sort?: number
  status?: number
}

// 报修工单类型
export interface RepairOrderInfo {
  id: number
  orderNo: string
  roomId: number
  roomName?: string
  reporterId: number
  reporterName?: string
  deviceType: string
  deviceTypeName?: string
  faultDesc: string
  images?: string[]
  status: string
  statusName?: string
  assigneeId?: number
  assigneeName?: string
  handleDesc?: string
  finishedTime?: string
  createTime?: string
}

// 候补队列类型
export interface WaitlistInfo {
  id: number
  roomId: number
  roomName?: string
  bookingDate: string
  startTime: string
  endTime: string
  userId: number
  userName?: string
  rankNo: number
  status: string
  statusName?: string
  notifyTime?: string
  confirmDeadline?: string
  createTime?: string
}

// 部门配额类型
export interface DeptQuotaInfo {
  id: number
  deptId: number
  deptName?: string
  quotaType: string
  quotaTypeName?: string
  quotaValue: number
  period: string
  periodName?: string
  enabled: number
  createTime?: string
}

// 智能问答日志类型
export interface ChatbotLogInfo {
  id: number
  userId?: number
  question: string
  answer?: string
  intent?: string
  params?: string
  resultCount?: number
  success: number
  costMs?: number
  createTime?: string
}

// AI模型配置类型
export interface AiModelConfigInfo {
  id?: number
  name: string
  provider?: string
  baseUrl: string
  apiKey?: string
  model: string
  temperature?: number
  maxTokens?: number
  timeoutSeconds?: number
  isDefault?: number
  enabled?: number
  remark?: string
  createTime?: string
  updateTime?: string
}
