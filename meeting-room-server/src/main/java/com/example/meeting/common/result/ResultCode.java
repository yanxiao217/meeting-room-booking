package com.example.meeting.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(400, "参数检验失败"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "预约冲突"),

    // 用户相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_EXISTS(1004, "用户名已存在"),

    // 会议室相关
    ROOM_NOT_FOUND(2001, "会议室不存在"),
    ROOM_NAME_EXISTS(2002, "会议室名称已存在"),
    ROOM_IN_USE(2003, "会议室有未完成的预约，无法删除"),
    ROOM_UNAVAILABLE(2004, "会议室不可用"),

    // 预约相关
    BOOKING_NOT_FOUND(3001, "预约不存在"),
    BOOKING_CONFLICT(3002, "预约时间冲突"),
    BOOKING_CANCEL_LIMIT(3003, "已超过取消时限"),
    BOOKING_RULE_FAILED(3004, "不满足预约规则"),
    BOOKING_MODIFY_NOT_ALLOWED(3005, "当前状态不允许修改");

    private final Integer code;
    private final String message;
}
