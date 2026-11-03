package com.example.meeting.common.constant;

/**
 * 通用常量
 */
public class CommonConstant {

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Token请求头
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * 用户角色：普通用户
     */
    public static final String ROLE_USER = "user";

    /**
     * 用户角色：管理员
     */
    public static final String ROLE_ADMIN = "admin";

    /**
     * 用户状态：正常
     */
    public static final Integer USER_STATUS_NORMAL = 1;

    /**
     * 用户状态：禁用
     */
    public static final Integer USER_STATUS_DISABLED = 0;

    private CommonConstant() {
    }
}
