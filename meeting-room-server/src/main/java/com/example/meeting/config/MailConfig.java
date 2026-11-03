package com.example.meeting.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.meeting.entity.SysConfig;
import com.example.meeting.mapper.SysConfigMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮件配置 - 从 sys_config 动态加载 SMTP 设置
 */
@Slf4j
@Configuration
public class MailConfig {

    private final SysConfigMapper sysConfigMapper;

    public MailConfig(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
    }

    /**
     * 根据 sys_config 动态创建 JavaMailSender
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        String host = getConfigValue("email.smtp.host");
        String port = getConfigValue("email.smtp.port");
        String username = getConfigValue("email.smtp.username");
        String password = getConfigValue("email.smtp.password");

        if (host == null || host.isEmpty()) {
            log.info("[MailConfig] SMTP host not configured, using dummy mail sender");
            mailSender.setHost("localhost");
            mailSender.setPort(25);
            return mailSender;
        }

        mailSender.setHost(host);
        mailSender.setPort(port != null && !port.isEmpty() ? Integer.parseInt(port) : 587);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        log.info("[MailConfig] SMTP configured: host={}, port={}, username={}", host, mailSender.getPort(), username);
        return mailSender;
    }

    /**
     * 判断邮件是否已配置
     */
    public boolean isConfigured() {
        String host = getConfigValue("email.smtp.host");
        String username = getConfigValue("email.smtp.username");
        return host != null && !host.isEmpty() && username != null && !username.isEmpty();
    }

    /**
     * 获取发件人名称
     */
    public String getFromName() {
        String name = getConfigValue("email.smtp.from-name");
        return (name != null && !name.isEmpty()) ? name : "会议室预约系统";
    }

    /**
     * 获取发件人邮箱
     */
    public String getFromAddress() {
        return getConfigValue("email.smtp.username");
    }

    private String getConfigValue(String key) {
        SysConfig config = sysConfigMapper.selectByConfigKey(key);
        return config != null ? config.getConfigValue() : null;
    }
}
