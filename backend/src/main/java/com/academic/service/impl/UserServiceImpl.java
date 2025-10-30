package com.academic.service.impl;

import com.academic.entity.User;
import com.academic.mapper.UserMapper;
import com.academic.service.UserService;
import com.academic.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

import java.util.List;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User getUserWithRolesByUsername(String username) {
        // 先查询用户基本信息
        User user = userMapper.selectUserWithRolesByUsername(username);
        if (user != null) {
            // 再查询用户角色并设置到用户对象中
            List<String> roles = userMapper.selectRolesByUserId(user.getId());
            user.setRoles(roles);
        }
        return user;
    }

    @Override
    public String login(String username, String password) {
        User user = getUserWithRolesByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 获取用户角色
        List<String> roles = userMapper.selectRolesByUserId(user.getId());
        String role = roles.isEmpty() ? "STUDENT" : roles.get(0);

        return jwtUtil.generateToken(username, user.getId(), role);
    }

    @Override
    @Transactional
    public boolean register(User user) {
        // 检查用户名是否已存在
        if (getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(user.getEmail()) &&
            getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail())) != null) {
            throw new RuntimeException("邮箱已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(0); // 默认禁用，需要管理员审批

        // 保存用户
        boolean saveResult = save(user);
        if (saveResult && user.getId() != null) {
            // 根据前端传入的角色设置用户角色，默认为学生角色
            String role = user.getRole() != null && "TEACHER".equals(user.getRole()) ? "TEACHER" : "STUDENT";
            userMapper.insertUserRole(user.getId(), role);
        }

        return saveResult;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    public boolean resetPassword(Long userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    public boolean updateUserInfo(User user) {
        return updateById(user);
    }

    @Override
    public List<User> getUsersByRole(String role) {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .inSql(User::getId, "SELECT user_id FROM user_role WHERE role = '" + role + "'"));
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(status);
        return updateById(user);
    }

    @Override
    @Transactional
    public boolean updateUserRole(Long userId, String role) {
        // 检查用户是否存在
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查角色是否有效
        if (!StringUtils.hasText(role)) {
            throw new RuntimeException("角色不能为空");
        }

        // 删除用户所有角色
        userMapper.deleteUserRoles(userId);

        // 添加新角色
        int result = userMapper.insertUserRole(userId, role);

        return result > 0;
    }

    @Override
    public String getUserRole(Long currentUserId) {
        if (currentUserId == null) return "STUDENT"; // 默认角色
        List<String> roles = userMapper.selectRolesByUserId(currentUserId);
        return roles.isEmpty() ? "STUDENT" : roles.get(0);
    }
    
    @Override
    public Long getCurrentUserId(HttpServletRequest request) {
        // 首先尝试从请求头获取Authorization
        String authorizationHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            // 提取token
            String token = authorizationHeader.substring(7);
            try {
                // 使用JwtUtil解析token获取用户ID
                return jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                System.err.println("解析Authorization头中的token失败: " + e.getMessage());
                // 继续尝试从URL参数获取token
            }
        }
        
        // 如果请求头中没有token或解析失败，尝试从URL参数获取token
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            try {
                // 使用JwtUtil解析token获取用户ID
                return jwtUtil.getUserIdFromToken(tokenParam);
            } catch (Exception e) {
                System.err.println("解析URL参数中的token失败: " + e.getMessage());
            }
        }
        
        return null;
    }
}
