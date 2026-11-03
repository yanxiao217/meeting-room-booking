package com.example.meeting.interceptor;

import com.example.meeting.entity.OperationLog;
import com.example.meeting.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 操作日志拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogInterceptor implements HandlerInterceptor {

    private final OperationLogMapper operationLogMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute("startTime");
        if (startTime == null) return;

        long costTime = System.currentTimeMillis() - startTime;

        // 只记录写操作
        String method = request.getMethod();
        if ("GET".equals(method)) return;

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = null;
            String username = null;
            if (authentication != null && authentication.getPrincipal() instanceof Long) {
                userId = (Long) authentication.getPrincipal();
                username = "user_" + userId;
            }

            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(userId);
            operationLog.setUsername(username);
            operationLog.setModule(getModule(request.getRequestURI()));
            operationLog.setOperation(getOperation(method));
            operationLog.setMethod(method + " " + request.getRequestURI());
            operationLog.setIp(getClientIp(request));
            operationLog.setStatus(response.getStatus() < 400 ? 1 : 0);
            operationLog.setCostTime(costTime);

            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    private String getModule(String uri) {
        if (uri.contains("/admin/rooms")) return "会议室管理";
        if (uri.contains("/admin/bookings")) return "预约管理";
        if (uri.contains("/admin/users")) return "用户管理";
        if (uri.contains("/admin/config")) return "系统设置";
        if (uri.contains("/bookings")) return "预约";
        if (uri.contains("/rooms")) return "会议室";
        return "其他";
    }

    private String getOperation(String method) {
        return switch (method) {
            case "POST" -> "新增";
            case "PUT" -> "修改";
            case "DELETE" -> "删除";
            default -> "操作";
        };
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }
}
