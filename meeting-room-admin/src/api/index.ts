import request from '@/utils/request'
import type { Result, PageResult, LoginResult, UserInfo, RoomInfo, BookingInfo, ConfigInfo, StatsInfo, DeptInfo, OperationLogInfo, RepairOrderInfo, WaitlistInfo, DeptQuotaInfo, RatingCheckRecord, ChatbotLogInfo, AiModelConfigInfo } from '@/types'

// 登录
export function login(data: { username: string; password: string }) {
  return request.post<Result<LoginResult>>('/auth/login', data) as Promise<Result<LoginResult>>
}

// 获取用户信息
export function getUserInfo() {
  return request.get<Result<UserInfo>>('/auth/userinfo') as Promise<Result<UserInfo>>
}

// 用户列表
export function getUserList(params?: { role?: string; deptId?: number; status?: number; pageNum?: number; pageSize?: number }) {
  return request.get<Result<PageResult<UserInfo>>>('/admin/users', { params }) as Promise<Result<PageResult<UserInfo>>>
}

// 修改用户状态
export function updateUserStatus(id: number, status: number) {
  return request.put<Result<void>>(`/admin/users/${id}/status`, { status }) as Promise<Result<void>>
}

// 重置密码
export function resetPassword(id: number) {
  return request.put<Result<void>>(`/admin/users/${id}/reset-password`) as Promise<Result<void>>
}

// 更新用户信息
export function updateUser(id: number, data: { realName?: string; phone?: string; email?: string; deptId?: number }) {
  return request.put<Result<void>>(`/admin/users/${id}`, data) as Promise<Result<void>>
}

// 分配角色
export function assignRole(id: number, role: string) {
  return request.put<Result<void>>(`/admin/users/${id}/role`, { role }) as Promise<Result<void>>
}

// 会议室列表
export function getRoomList(params?: { floor?: string; capacity?: number; status?: number }) {
  return request.get<Result<RoomInfo[]>>('/admin/rooms', { params }) as Promise<Result<RoomInfo[]>>
}

// 新增会议室
export function createRoom(data: Partial<RoomInfo>) {
  return request.post<Result<RoomInfo>>('/admin/rooms', data) as Promise<Result<RoomInfo>>
}

// 编辑会议室
export function updateRoom(id: number, data: Partial<RoomInfo>) {
  return request.put<Result<RoomInfo>>(`/admin/rooms/${id}`, data) as Promise<Result<RoomInfo>>
}

// 删除会议室
export function deleteRoom(id: number) {
  return request.delete<Result<void>>(`/admin/rooms/${id}`) as Promise<Result<void>>
}

// 修改会议室状态
export function updateRoomStatus(id: number, status: number) {
  return request.put<Result<void>>(`/admin/rooms/${id}/status`, { status }) as Promise<Result<void>>
}

// 预约列表
export function getBookingList(params?: { startDate?: string; endDate?: string; roomId?: number; userId?: number; status?: string; activeOnly?: boolean; pageNum?: number; pageSize?: number }) {
  return request.get<Result<PageResult<BookingInfo>>>('/admin/bookings', { params }) as Promise<Result<PageResult<BookingInfo>>>
}

// 强制取消预约
export function forceCancelBooking(id: number, reason: string) {
  return request.put<Result<void>>(`/admin/bookings/${id}/force-cancel`, { reason }) as Promise<Result<void>>
}

// 审批通过
export function approveBooking(id: number) {
  return request.put<Result<BookingInfo>>(`/admin/bookings/${id}/approve`) as Promise<Result<BookingInfo>>
}

// 审批拒绝
export function rejectBooking(id: number, reason: string) {
  return request.put<Result<void>>(`/admin/bookings/${id}/reject`, { reason }) as Promise<Result<void>>
}

// 代客预约
export function createBookingByAdmin(data: any) {
  return request.post<Result<BookingInfo>>('/admin/bookings/create-by-admin', data) as Promise<Result<BookingInfo>>
}

// 获取预约规则配置
export function getBookingRules() {
  return request.get<Result<ConfigInfo[]>>('/admin/config/booking-rules') as Promise<Result<ConfigInfo[]>>
}

// 更新预约规则配置
export function updateBookingRules(data: ConfigInfo[]) {
  return request.put<Result<void>>('/admin/config/booking-rules', data) as Promise<Result<void>>
}

// 获取概览指标
export function getStatsOverview(params?: { startDate?: string; endDate?: string }) {
  return request.get<Result<{ totalRooms: number; totalBookings: number; activeUsers: number; avgUsageRate: number }>>('/admin/stats/overview', { params }) as Promise<Result<{ totalRooms: number; totalBookings: number; activeUsers: number; avgUsageRate: number }>>
}

// 获取使用率统计
export function getUsageStats(params?: { startDate?: string; endDate?: string }) {
  return request.get<Result<StatsInfo[]>>('/admin/stats/usage', { params }) as Promise<Result<StatsInfo[]>>
}

// 获取热门会议室
export function getHotRooms(params?: { startDate?: string; endDate?: string }) {
  return request.get<Result<StatsInfo[]>>('/admin/stats/hot-rooms', { params }) as Promise<Result<StatsInfo[]>>
}

// 获取预约趋势
export function getBookingTrend(params?: { startDate?: string; endDate?: string }) {
  return request.get<Result<any>>('/admin/stats/trend', { params }) as Promise<Result<any>>
}

// 获取操作日志
export function getOperationLogs(params?: { userId?: number; module?: string; pageNum?: number; pageSize?: number }) {
  return request.get<Result<OperationLogInfo[]>>('/admin/operation-logs', { params }) as Promise<Result<OperationLogInfo[]>>
}

// ==================== 部门管理 ====================

// 部门列表
export function getDeptList() {
  return request.get<Result<DeptInfo[]>>('/admin/depts') as Promise<Result<DeptInfo[]>>
}

export function createDept(data: Partial<DeptInfo>) {
  return request.post<Result<void>>('/admin/depts', data) as Promise<Result<void>>
}

export function updateDept(id: number, data: Partial<DeptInfo>) {
  return request.put<Result<void>>(`/admin/depts/${id}`, data) as Promise<Result<void>>
}

export function deleteDept(id: number) {
  return request.delete<Result<void>>(`/admin/depts/${id}`) as Promise<Result<void>>
}

// ==================== 报修管理 ====================

// 报修工单列表
export function getRepairList(params?: { status?: string; roomId?: number; pageNum?: number; pageSize?: number }) {
  return request.get<Result<PageResult<RepairOrderInfo>>>('/admin/repairs', { params }) as Promise<Result<PageResult<RepairOrderInfo>>>
}

// 转派/接单
export function assignRepair(id: number, assigneeId: number) {
  return request.put<Result<void>>(`/admin/repairs/${id}/assign`, { assigneeId }) as Promise<Result<void>>
}

// 完成工单
export function finishRepair(id: number, handleDesc: string) {
  return request.put<Result<void>>(`/admin/repairs/${id}/finish`, { handleDesc }) as Promise<Result<void>>
}

// ==================== 候补管理 ====================

// 候补队列列表
export function getWaitlistList(params?: { roomId?: number; bookingDate?: string; status?: string; activeOnly?: boolean; pageNum?: number; pageSize?: number }) {
  return request.get<Result<PageResult<WaitlistInfo>>>('/admin/waitlist', { params }) as Promise<Result<PageResult<WaitlistInfo>>>
}

// 手动补位
export function promoteWaitlist(id: number) {
  return request.post<Result<void>>(`/admin/waitlist/${id}/promote`) as Promise<Result<void>>
}

// ==================== 部门配额管理 ====================

// 部门配额列表
export function getDeptQuotaList() {
  return request.get<Result<DeptQuotaInfo[]>>('/admin/dept-quotas') as Promise<Result<DeptQuotaInfo[]>>
}

// 新增部门配额
export function createDeptQuota(data: Partial<DeptQuotaInfo>) {
  return request.post<Result<void>>('/admin/dept-quotas', data) as Promise<Result<void>>
}

// 更新部门配额
export function updateDeptQuota(id: number, data: Partial<DeptQuotaInfo>) {
  return request.put<Result<void>>(`/admin/dept-quotas/${id}`, data) as Promise<Result<void>>
}

// 删除部门配额
export function deleteDeptQuota(id: number) {
  return request.delete<Result<void>>(`/admin/dept-quotas/${id}`) as Promise<Result<void>>
}

// ==================== 规则配置 ====================

// 获取候补规则配置
export function getWaitlistRules() {
  return request.get<Result<ConfigInfo[]>>('/admin/config/waitlist') as Promise<Result<ConfigInfo[]>>
}

// 更新候补规则配置
export function updateWaitlistRules(data: ConfigInfo[]) {
  return request.put<Result<void>>('/admin/config/waitlist', data) as Promise<Result<void>>
}

// 获取周期预约配置
export function getRecurringRules() {
  return request.get<Result<ConfigInfo[]>>('/admin/config/recurring') as Promise<Result<ConfigInfo[]>>
}

// 更新周期预约配置
export function updateRecurringRules(data: ConfigInfo[]) {
  return request.put<Result<void>>('/admin/config/recurring', data) as Promise<Result<void>>
}

// 获取审批配置
export function getApprovalRules() {
  return request.get<Result<ConfigInfo[]>>('/admin/config/approval') as Promise<Result<ConfigInfo[]>>
}

// 更新审批配置
export function updateApprovalRules(data: ConfigInfo[]) {
  return request.put<Result<void>>('/admin/config/approval', data) as Promise<Result<void>>
}

// ==================== 信用分管理 ====================

// 获取信用分规则配置
export function getCreditRules() {
  return request.get<Result<ConfigInfo[]>>('/admin/credit/rules') as Promise<Result<ConfigInfo[]>>
}

// 更新信用分规则配置
export function updateCreditRules(data: ConfigInfo[]) {
  return request.put<Result<void>>('/admin/credit/rules', data) as Promise<Result<void>>
}

// 信用记录查询
export function getCreditRecords(params: { userId: number; pageNum?: number; pageSize?: number }) {
  return request.get<Result<PageResult<CreditRecord>>>('/admin/credit/records', { params }) as Promise<Result<PageResult<CreditRecord>>>
}

// 管理员调整信用分
export function adjustCredit(data: { userId: number; value: number; reason: string }) {
  return request.post<Result<void>>('/admin/credit/adjust', data) as Promise<Result<void>>
}

// 低分用户列表
export function getLowScoreUsers() {
  return request.get<Result<UserInfo[]>>('/admin/credit/low-score-users') as Promise<Result<UserInfo[]>>
}

// ==================== 评价管理 ====================

// 全部会议室评价统计
export function getRoomRatingStats() {
  return request.get<Result<any[]>>('/admin/ratings/stats') as Promise<Result<any[]>>
}

// 指定会议室评分分布
export function getRatingDistribution(roomId: number) {
  return request.get<Result<any[]>>(`/admin/ratings/distribution/${roomId}`) as Promise<Result<any[]>>
}

// 低分检查列表
export function getPendingRatingChecks() {
  return request.get<Result<any[]>>('/admin/ratings/pending-checks') as Promise<Result<any[]>>
}

// 低分记录转报修
export function convertToRepair(checkId: number) {
  return request.post<Result<void>>(`/admin/ratings/convert-to-repair/${checkId}`) as Promise<Result<void>>
}

// ==================== 智能问答日志 ====================

// 问答日志列表
export function getChatbotLogs(params?: { intent?: string; success?: number; pageNum?: number; pageSize?: number }) {
  return request.get<Result<PageResult<ChatbotLogInfo>>>('/admin/chatbot-logs', { params }) as Promise<Result<PageResult<ChatbotLogInfo>>>
}

// ==================== AI模型配置 ====================

// 模型配置列表
export function getAiModelList() {
  return request.get<Result<AiModelConfigInfo[]>>('/admin/ai-models') as Promise<Result<AiModelConfigInfo[]>>
}

// 新增模型配置
export function createAiModel(data: AiModelConfigInfo) {
  return request.post<Result<AiModelConfigInfo>>('/admin/ai-models', data) as Promise<Result<AiModelConfigInfo>>
}

// 更新模型配置
export function updateAiModel(id: number, data: AiModelConfigInfo) {
  return request.put<Result<AiModelConfigInfo>>(`/admin/ai-models/${id}`, data) as Promise<Result<AiModelConfigInfo>>
}

// 删除模型配置
export function deleteAiModel(id: number) {
  return request.delete<Result<void>>(`/admin/ai-models/${id}`) as Promise<Result<void>>
}

// 设为默认
export function setDefaultAiModel(id: number) {
  return request.put<Result<void>>(`/admin/ai-models/${id}/default`) as Promise<Result<void>>
}

// 启用/停用
export function toggleAiModelStatus(id: number, enabled: number) {
  return request.put<Result<void>>(`/admin/ai-models/${id}/status`, { enabled }) as Promise<Result<void>>
}

// 测试连通性
export function testAiModel(id: number) {
  return request.post<Result<{ success: boolean; message: string; reply?: string; latencyMs?: number }>>(`/admin/ai-models/${id}/test`) as Promise<Result<{ success: boolean; message: string; reply?: string; latencyMs?: number }>>
}

// ==================== 邮件配置 ====================

// 获取SMTP配置
export function getSmtpConfig() {
  return request.get<Result<Record<string, string>>>('/admin/email/smtp') as Promise<Result<Record<string, string>>>
}

// 保存SMTP配置
export function saveSmtpConfig(data: { host: string; port: string; username: string; password: string; fromName: string; testRecipient: string }) {
  return request.put<Result<void>>('/admin/email/smtp', data) as Promise<Result<void>>
}

// 发送测试邮件
export function sendTestEmail(email: string) {
  return request.post<Result<void>>('/admin/email/test', { email }) as Promise<Result<void>>
}
