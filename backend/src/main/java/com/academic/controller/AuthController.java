package com.academic.controller;

import com.academic.common.Result;
import com.academic.entity.User;
import com.academic.service.UserService;
import com.academic.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;


@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            if (username == null || password == null) {
                return Result.badRequest("用户名和密码不能为空");
            }

            String token = userService.login(username, password);
            User user = userService.getUserWithRolesByUsername(username);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);

            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        try {
            if (userService.register(user)) {
                return Result.success("注册成功，等待管理员审批");
            } else {
                return Result.error("注册失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    public Result<User> getUserInfo(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String username = jwtUtil.getUsernameFromToken(token);
                User user = userService.getUserWithRolesByUsername(username);
                return Result.success(user);
            }
            return Result.unauthorized("未登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                Long userId = jwtUtil.getUserIdFromToken(token);
                String oldPassword = request.get("oldPassword");
                String newPassword = request.get("newPassword");

                if (userService.changePassword(userId, oldPassword, newPassword)) {
                    return Result.success("密码修改成功");
                } else {
                    return Result.error("密码修改失败");
                }
            }
            return Result.unauthorized("未登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update-profile")
    public Result<String> updateProfile(@RequestBody User user, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                Long userId = jwtUtil.getUserIdFromToken(token);
                user.setId(userId);

                if (userService.updateUserInfo(user)) {
                    return Result.success("信息更新成功");
                } else {
                    return Result.error("信息更新失败");
                }
            }
            return Result.unauthorized("未登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取待审批用户列表（仅管理员）
     */
    @GetMapping("/pending-users")
    public Result<List<User>> getPendingUsers(HttpServletRequest request) {
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return Result.forbidden("没有权限访问");
            }

            // 查询状态为禁用（0）的用户
            List<User> pendingUsers = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 0));

            return Result.success(pendingUsers);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审批用户（仅管理员）
     */
    @PostMapping("/approve-user/{userId}")
    public Result<String> approveUser(@PathVariable Long userId, HttpServletRequest request) {
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return Result.forbidden("没有权限访问");
            }

            // 启用用户（status=1）
            if (userService.updateUserStatus(userId, 1)) {
                return Result.success("用户审批通过");
            } else {
                return Result.error("用户审批失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 拒绝用户申请（仅管理员）
     */
    @PostMapping("/reject-user/{userId}")
    public Result<String> rejectUser(@PathVariable Long userId, HttpServletRequest request) {
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return Result.forbidden("没有权限访问");
            }

            // 删除用户
            if (userService.removeById(userId)) {
                return Result.success("已拒绝用户申请");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String role = jwtUtil.getRoleFromToken(token);
            return "ADMIN".equals(role);
        }
        return false;
    }
}
