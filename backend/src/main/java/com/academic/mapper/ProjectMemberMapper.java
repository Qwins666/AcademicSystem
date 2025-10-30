package com.academic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;
import java.util.Map;


@Mapper
public interface ProjectMemberMapper extends BaseMapper<Object> {

    /**
     * 根据项目ID查询所有成员
     * 查询结果包含成员信息和用户信息
     */
    @Select("SELECT pm.*, u.username AS user_name FROM project_member pm LEFT JOIN user u ON pm.user_id = u.id WHERE pm.project_id = #{projectId}")
    List<Map<String, Object>> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目ID和用户ID查询成员
     */
    @Select("SELECT * FROM project_member WHERE project_id = #{projectId} AND user_id = #{userId}")
    Map<String, Object> selectByProjectAndUser(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 根据用户ID查询所有参与的项目
     */
    @Select("SELECT * FROM project_member WHERE user_id = #{userId}")
    List<Map<String, Object>> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据项目ID和用户ID删除项目成员
     */
    @Delete("DELETE FROM project_member WHERE project_id = #{projectId} AND user_id = #{userId}")
    int deleteByProjectAndUser(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
