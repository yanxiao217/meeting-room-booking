package com.example.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.meeting.dto.RegisterDTO;
import com.example.meeting.entity.User;
import com.example.meeting.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 用户注册
     */
    UserVO register(RegisterDTO dto);

    /**
     * 获取用户信息
     */
    UserVO getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    void updateUserInfo(User user);
}
