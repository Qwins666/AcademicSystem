package com.academic.controller;

import com.academic.common.Result;
import com.academic.entity.User;
import com.academic.service.UserService;
import com.academic.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户列表
     * 支持模糊查询
     * 重置
     */
    @GetMapping
    public Result<List<User>> getUserList(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        try {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            if (username != null && !username.isEmpty()) {
                queryWrapper.like(User::getUsername, username);
            }
            if (realName != null && !realName.isEmpty()) {
                queryWrapper.like(User::getRealName, realName);
            }
            if (status != null) {
                queryWrapper.eq(User::getStatus, status);
            }
            // 按角色查询需要join user_role表
            if (role != null && !role.isEmpty()) {
                queryWrapper.inSql(User::getId, "SELECT user_id FROM user_role WHERE role = '" + role + "'");
            }

            List<User> users = userService.list(queryWrapper);
            // 为每个用户添加角色信息
            for (User user : users) {
                User userWithRoles = userService.getUserWithRolesByUsername(user.getUsername());
                user.setRoles(userWithRoles.getRoles());
            }
            return Result.success(users);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.notFound("用户不存在");
            }
            // 获取用户角色信息
            User userWithRoles = userService.getUserWithRolesByUsername(user.getUsername());
            user.setRoles(userWithRoles.getRoles());
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Result<String> createUser(@RequestBody User user, HttpServletRequest request) {
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return Result.forbidden("没有权限创建用户");
            }

            // 检查用户名是否已存在
            if (userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())) != null) {
                return Result.error("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (user.getEmail() != null && !user.getEmail().isEmpty() &&
                userService.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail())) != null) {
                return Result.error("邮箱已存在");
            }

            // 保存用户
            if (userService.save(user)) {
                return Result.success("用户创建成功");
            } else {
                return Result.error("用户创建失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     * 编辑操作
     * 禁用操作
     */
    @PutMapping("/{id}")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return Result.forbidden("没有权限更新用户信息");
            }

            user.setId(id);
            if (userService.updateById(user)) {
                // 如果用户提交了角色信息，同步更新用户角色
                if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                    String role = user.getRoles().get(0);
                    userService.updateUserRole(id, role);
                }
                return Result.success("用户信息更新成功");
            } else {
                return Result.error("用户信息更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return Result.forbidden("没有权限删除用户");
            }

            if (userService.removeById(id)) {
                return Result.success("用户删除成功");
            } else {
                return Result.error("用户删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> requestBody, HttpServletRequest request) {
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return Result.forbidden("没有权限更新用户状态");
            }

            Integer status = requestBody.get("status");
            if (status == null) {
                return Result.badRequest("状态参数不能为空");
            }

            if (userService.updateUserStatus(id, status)) {
                return Result.success("用户状态更新成功");
            } else {
                return Result.error("用户状态更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/{id}/role")
    public Result<String> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return Result.forbidden("没有权限更新用户角色");
            }

            String role = requestBody.get("role");
            if (role == null || role.isEmpty()) {
                return Result.badRequest("角色参数不能为空");
            }

            if (userService.updateUserRole(id, role)) {
                return Result.success("用户角色更新成功");
            } else {
                return Result.error("用户角色更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置用户密码
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    public Result<String> resetUserPassword(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return Result.forbidden("没有权限重置用户密码");
            }

            // 使用默认初始密码：123456
            String defaultPassword = "123456";

            if (userService.resetPassword(id, defaultPassword)) {
                return Result.success("密码已重置为默认密码: 123456");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户基本信息（仅包含非敏感信息，如姓名）
     * 此接口用于在成果列表中显示提交者姓名
     */
    @GetMapping("/basic/{id}")
    public Result<Map<String, String>> getUserBasicInfo(@PathVariable Long id) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.notFound("用户不存在");
            }

            // 只返回非敏感信息
            Map<String, String> basicInfo = new HashMap<>();
            basicInfo.put("realName", user.getRealName());
            basicInfo.put("username", user.getUsername());

            return Result.success(basicInfo);
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
