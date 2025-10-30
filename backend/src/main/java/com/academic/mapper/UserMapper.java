package com.academic.mapper;

import com.academic.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户信息（包含角色）
     * 注意：需要在Service层手动设置roles属性
     */
    @Select("SELECT u.* FROM user u WHERE u.username = #{username}")
    User selectUserWithRolesByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询用户角色列表
     */
    @Select("SELECT role FROM user_role WHERE user_id = #{userId}")
    List<String> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 删除用户所有角色
     */
    @Delete("DELETE FROM user_role WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);

    /**
     * 为用户添加角色
     */
    @Insert("INSERT INTO user_role(user_id, role) VALUES(#{userId}, #{role})")
    int insertUserRole(@Param("userId") Long userId, @Param("role") String role);
}
