package com.academic.service;

import com.academic.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户信息（包含角色）
     */
    User getUserWithRolesByUsername(String username);

    /**
     * 用户登录
     */
    String login(String username, String password);

    /**
     * 用户注册
     */
    boolean register(User user);

    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 更新用户信息
     */
    boolean updateUserInfo(User user);

    /**
     * 根据角色查询用户列表
     */
    List<User> getUsersByRole(String role);

    /**
     * 启用/禁用用户
     */
    boolean updateUserStatus(Long userId, Integer status);

    /**
     * 更新用户角色
     */
    boolean updateUserRole(Long userId, String role);

    String getUserRole(Long currentUserId);
    
    /**
     * 从请求中获取当前用户ID
     */
    Long getCurrentUserId(HttpServletRequest request);
}
