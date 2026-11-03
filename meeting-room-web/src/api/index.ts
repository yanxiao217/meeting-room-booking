import request from '@/utils/request'
import type {
  Result,
  PageResult,
  LoginResult,
  UserInfo,
  RoomInfo,
  CalendarData,
  Availability,
  BookingInfo,
  BookingCreatePayload,
  NotificationInfo,
  WaitlistItem,
  RepairOrder,
  MeetingMinutes,
  BookingTemplate,
  CreditRecord,
  RoomRating,
  ChatBotAnswer
} from '@/types'

// ---------------- auth ----------------
export function login(data: { username: string; password: string }) {
  return request.post<Result<LoginResult>>('/auth/login', data) as Promise<Result<LoginResult>>
}

export function register(data: { username: string; password: string; phone?: string; realName?: string }) {
  return request.post<Result<UserInfo>>('/auth/register', data) as Promise<Result<UserInfo>>
}

export function getUserInfo() {
  return request.get<Result<UserInfo>>('/auth/userinfo') as Promise<Result<UserInfo>>
}

export function updateProfile(data: { realName?: string; phone?: string; email?: string }) {
  return request.put<Result<UserInfo>>('/auth/profile', data) as Promise<Result<UserInfo>>
}

export function logout() {
  return request.post<Result<null>>('/auth/logout') as Promise<Result<null>>
}

export function getUnreadCount() {
  return request.get<Result<number>>('/auth/unread-count') as Promise<Result<number>>
}

// ---------------- rooms ----------------
export function getRoomList(params?: { floor?: string; capacity?: number; status?: number }) {
  return request.get<Result<RoomInfo[]>>('/rooms', { params }) as Promise<Result<RoomInfo[]>>
}

export function getRoomDetail(id: number) {
  return request.get<Result<RoomInfo>>(`/rooms/${id}`) as Promise<Result<RoomInfo>>
}

export function getAvailability(id: number, date: string) {
  return request.get<Result<Availability>>(`/rooms/${id}/availability`, { params: { date } }) as Promise<Result<Availability>>
}

export function getCalendar(params: { startDate: string; endDate: string; floor?: string; capacity?: number }) {
  return request.get<Result<CalendarData>>('/rooms/calendar', { params }) as Promise<Result<CalendarData>>
}

export function addFavorite(id: number) {
  return request.post<Result<null>>(`/rooms/${id}/favorite`) as Promise<Result<null>>
}

export function removeFavorite(id: number) {
  return request.delete<Result<null>>(`/rooms/${id}/favorite`) as Promise<Result<null>>
}

// ---------------- bookings ----------------
export function createBooking(data: BookingCreatePayload) {
  return request.post<Result<BookingInfo>>('/bookings', data) as Promise<Result<BookingInfo>>
}

export function updateBooking(id: number, data: Partial<BookingCreatePayload>) {
  return request.put<Result<BookingInfo>>(`/bookings/${id}`, data) as Promise<Result<BookingInfo>>
}

export function cancelBooking(id: number, reason: string) {
  return request.put<Result<null>>(`/bookings/${id}/cancel`, { reason }) as Promise<Result<null>>
}

export function getMyBookings(params?: { status?: string; pageNum?: number; pageSize?: number }) {
  return request.get<Result<PageResult<BookingInfo>>>('/bookings/mine', { params }) as Promise<Result<PageResult<BookingInfo>>>
}

export function getBookingDetail(id: number) {
  return request.get<Result<BookingInfo>>(`/bookings/${id}`) as Promise<Result<BookingInfo>>
}

export function signBooking(id: number) {
  return request.post<Result<null>>(`/bookings/${id}/sign`) as Promise<Result<null>>
}

export function getBookingQrCode(id: number) {
  return request.get<Result<{ token: string }>>(`/bookings/${id}/qrcode`) as Promise<Result<{ token: string }>>
}

export function scanBookingQrCode(token: string) {
  return request.get<Result<BookingInfo>>(`/bookings/scan/${token}`) as Promise<Result<BookingInfo>>
}

export function signByQrCode(token: string) {
  return request.post<Result<null>>('/bookings/sign-by-qrcode', { token }) as Promise<Result<null>>
}

// ---------------- waitlist ----------------
export function joinWaitlist(data: { roomId: number; bookingDate: string; startTime: string; endTime: string }) {
  return request.post<Result<null>>('/waitlist', {
    roomId: String(data.roomId),
    bookingDate: data.bookingDate,
    startTime: data.startTime,
    endTime: data.endTime
  }) as Promise<Result<null>>
}

export function getMyWaitlist() {
  return request.get<Result<WaitlistItem[]>>('/waitlist/mine') as Promise<Result<WaitlistItem[]>>
}

export function leaveWaitlist(id: number) {
  return request.delete<Result<null>>(`/waitlist/${id}`) as Promise<Result<null>>
}

export function confirmWaitlist(id: number) {
  return request.post<Result<null>>(`/waitlist/${id}/confirm`) as Promise<Result<null>>
}

// ---------------- minutes ----------------
export function submitMinutes(bookingId: number, data: MeetingMinutes) {
  return request.post<Result<MeetingMinutes>>(`/bookings/${bookingId}/minutes`, data) as Promise<Result<MeetingMinutes>>
}

export function getMinutes(bookingId: number) {
  return request.get<Result<MeetingMinutes>>(`/bookings/${bookingId}/minutes`) as Promise<Result<MeetingMinutes>>
}

// ---------------- repairs ----------------
export function createRepair(data: Partial<RepairOrder>) {
  return request.post<Result<RepairOrder>>('/repairs', data) as Promise<Result<RepairOrder>>
}

export function getMyRepairs() {
  return request.get<Result<RepairOrder[]>>('/repairs/mine') as Promise<Result<RepairOrder[]>>
}

export function getRepairDetail(id: number) {
  return request.get<Result<RepairOrder>>(`/repairs/${id}`) as Promise<Result<RepairOrder>>
}

// ---------------- templates ----------------
export function getTemplates() {
  return request.get<Result<BookingTemplate[]>>('/templates') as Promise<Result<BookingTemplate[]>>
}

export function createTemplate(data: Partial<BookingTemplate>) {
  return request.post<Result<BookingTemplate>>('/templates', data) as Promise<Result<BookingTemplate>>
}

export function updateTemplate(id: number, data: Partial<BookingTemplate>) {
  return request.put<Result<null>>(`/templates/${id}`, data) as Promise<Result<null>>
}

export function deleteTemplate(id: number) {
  return request.delete<Result<null>>(`/templates/${id}`) as Promise<Result<null>>
}

// ---------------- notifications ----------------
export function getNotifications(params?: { type?: string; pageNum?: number; pageSize?: number }) {
  return request.get<Result<PageResult<NotificationInfo>>>('/notifications', { params }) as Promise<Result<PageResult<NotificationInfo>>>
}

export function markNotificationRead(id: number) {
  return request.put<Result<null>>(`/notifications/${id}/read`) as Promise<Result<null>>
}

export function markAllNotificationsRead() {
  return request.put<Result<null>>('/notifications/read-all') as Promise<Result<null>>
}

// ---------------- credit ----------------
export function getMyCreditScore() {
  return request.get<Result<{ score: number; threshold: number; restrictDays: number }>>('/credit/my-score') as Promise<Result<{ score: number; threshold: number; restrictDays: number }>>
}

export function getMyCreditRecords(params?: { pageNum?: number; pageSize?: number }) {
  return request.get<Result<PageResult<CreditRecord>>>('/credit/my-records', { params }) as Promise<Result<PageResult<CreditRecord>>>
}

// ---------------- ratings ----------------
export function submitRating(data: { bookingId: number; rating: number; tags?: string; comment?: string }) {
  return request.post<Result<null>>('/ratings', data) as Promise<Result<null>>
}

export function getRoomRatings(roomId: number, limit = 10) {
  return request.get<Result<RoomRating[]>>(`/ratings/room/${roomId}`, { params: { limit } }) as Promise<Result<RoomRating[]>>
}

export function getRoomAvgRating(roomId: number) {
  return request.get<Result<number>>(`/ratings/room/${roomId}/avg`) as Promise<Result<number>>
}

// ---------------- chatbot ----------------
export function askChatBot(question: string) {
  return request.post<Result<ChatBotAnswer>>('/chatbot/ask', { question }) as Promise<Result<ChatBotAnswer>>
}
