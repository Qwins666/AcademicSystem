package com.academic.service.impl;

import com.academic.entity.Project;
import com.academic.mapper.ProjectMapper;
import com.academic.mapper.ProjectMemberMapper;
import com.academic.service.ProjectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Project> getAllProjects() {
        return list();
    }

    @Override
    public Project getProjectById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveProject(Project project) {
        return saveOrUpdate(project);
    }

    @Override
    public boolean deleteProject(Long id) {
        return removeById(id);
    }

    @Override
    public List<Project> getProjectsByLeaderId(Long leaderId) {
        return list(new LambdaQueryWrapper<Project>().eq(Project::getLeaderId, leaderId));
    }

    @Override
    public List<Project> getProjectsByType(String type) {
        return list(new LambdaQueryWrapper<Project>().eq(Project::getType, type));
    }

    @Override
    public List<Project> getProjectsByStatus(String status) {
        return list(new LambdaQueryWrapper<Project>().eq(Project::getStatus, status));
    }

    @Override
    public List<Project> getProjectsByLevel(String level) {
        return list(new LambdaQueryWrapper<Project>().eq(Project::getLevel, level));
    }

    @Override
    public List<Project> getProjectsByUserId(Long userId) {
        // 这里应该查询project_member表，获取用户参与的项目ID，然后查询项目信息
        // 暂时返回空列表，后续需要实现
        return new ArrayList<>();
    }

    @Override
    public List<Project> getProjectsByMemberId(Long memberId) {
        // 这里应该查询project_member表，获取用户参与的项目ID，然后查询项目信息
        // 暂时返回模拟数据，后续需要实现
        return getProjectsByUserId(memberId);
    }

    @Override
    public boolean addProjectMember(Long projectId, Long memberId, String role) {
        try {
            // 检查用户是否已经是项目成员
            Map<String, Object> existingMember = projectMemberMapper.selectByProjectAndUser(projectId, memberId);
            if (existingMember != null) {
                // 用户已经是项目成员，返回false
                return false;
            }

            // 创建项目成员记录
            Map<String, Object> projectMember = new HashMap<>();
            projectMember.put("project_id", projectId);
            projectMember.put("user_id", memberId);
            projectMember.put("role", role);
            // 使用标准格式的时间戳
            projectMember.put("join_time", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // 插入数据
            int result = projectMemberMapper.insert(projectMember);

            // 返回插入是否成功
            return result > 0;
        } catch (Exception e) {
            // 记录异常详情
            System.err.println("添加项目成员失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean inviteUserToProject(Long projectId, Long userId, String role) {
        // 这里需要向project_member表插入数据
        // 暂时返回true，后续需要实现
        return true;
    }

    @Override
    public boolean removeProjectMember(Long projectId, Long userId) {
        try {
            // 调用mapper方法删除项目成员
            int result = projectMemberMapper.deleteByProjectAndUser(projectId, userId);

            // 返回删除是否成功（影响行数大于0表示删除成功）
            return result > 0;
        } catch (Exception e) {
            // 记录异常详情
            System.err.println("移除项目成员失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateProjectMemberRole(Long projectId, Long userId, String role) {
        // 这里需要更新project_member表中的角色
        // 暂时返回true，后续需要实现
        return true;
    }

    @Override
    public boolean createProjectMilestone(Long projectId, String name, String description, String dueDate) {
        // 这里需要向project_milestone表插入数据
        // 暂时返回true，后续需要实现
        return true;
    }

    @Override
    public boolean addProjectMilestone(Long projectId, ProjectMilestone milestone) {
        // 向project_milestone表插入数据
        if (milestone != null) {
            milestone.setProjectId(projectId);
            milestone.setStatus("PENDING");

            try {
                // 使用JDBC插入数据
                String sql = "INSERT INTO project_milestone (project_id, name, description, due_date, status) VALUES (?, ?, ?, ?, ?)";
                int result = jdbcTemplate.update(sql,
                    projectId,
                    milestone.getName(),
                    milestone.getDescription(),
                    milestone.getDueDate(),
                    milestone.getStatus());

                return result > 0;
            } catch (Exception e) {
                // 记录异常详情
                System.err.println("添加项目阶段失败: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean updateProjectMilestone(Long milestoneId, String name, String description, String dueDate, String status) {
        // 这里需要更新project_milestone表
        // 暂时返回true，后续需要实现
        return true;
    }

    @Override
    public boolean updateProjectMilestone(Long projectId, ProjectMilestone milestone) {
        // 更新项目阶段
        if (milestone != null && milestone.getId() != null) {
            try {
                // 检查是否只是更新状态
                if (milestone.getName() == null && milestone.getDescription() == null && milestone.getDueDate() == null) {
                    // 获取当前状态
                    String checkSql = "SELECT status FROM project_milestone WHERE id = ? AND project_id = ?";
                    String currentStatus = jdbcTemplate.queryForObject(checkSql, String.class, milestone.getId(), projectId);

                    // 限制：已逾期(OVERDUE)状态只能更新为完成(COMPLETED)，不能更新为待完成(PENDING)
                    if ("OVERDUE".equals(currentStatus) && "PENDING".equals(milestone.getStatus())) {
                        System.out.println("已逾期状态只能标记为完成，不能标记为待完成");
                        return false;
                    }

                    // 执行状态更新
                    String updateSql = "UPDATE project_milestone SET status = ? WHERE id = ? AND project_id = ?";
                    int result = jdbcTemplate.update(updateSql,
                        milestone.getStatus(),
                        milestone.getId(),
                        projectId);
                    return result > 0;
                }

                // 先查询当前阶段的状态
                String checkSql = "SELECT status FROM project_milestone WHERE id = ? AND project_id = ?";
                String currentStatus = jdbcTemplate.queryForObject(checkSql, String.class, milestone.getId(), projectId);

                // 如果阶段已完成，不允许编辑内容（但可以更改状态）
                if ("COMPLETED".equals(currentStatus)) {
                    System.out.println("项目阶段已完成，不能编辑内容");
                    return false;
                }

                // 使用JDBC更新数据
                String updateSql = "UPDATE project_milestone SET name = ?, description = ?, due_date = ?, status = ? WHERE id = ? AND project_id = ?";
                if(milestone.getStatus()==null){
                    // 如果没有指定新状态，保持原状态，但如果原状态是OVERDUE且新截止日期在未来，则改为PENDING
                    if ("OVERDUE".equals(currentStatus) && milestone.getDueDate() != null) {
                        // 检查新的截止日期是否在未来
                        String checkFutureDateSql = "SELECT CASE WHEN ? > CURRENT_DATE() THEN 1 ELSE 0 END";
                        Boolean isFutureDate = jdbcTemplate.queryForObject(checkFutureDateSql, Boolean.class, milestone.getDueDate());
                        if (isFutureDate) {
                            milestone.setStatus("PENDING");
                        } else {
                            milestone.setStatus("OVERDUE");
                        }
                    } else {
                        milestone.setStatus(currentStatus);
                    }
                }
                int result = jdbcTemplate.update(updateSql,
                    milestone.getName(),
                    milestone.getDescription(),
                    milestone.getDueDate(),
                    milestone.getStatus(),
                    milestone.getId(),
                    projectId);

                return result > 0;
            } catch (Exception e) {
                // 记录异常详情
                System.err.println("更新项目阶段失败: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean deleteProjectMilestone(Long milestoneId) {
        // 这里需要从project_milestone表删除数据
        // 暂时返回true，后续需要实现
        return true;
    }

    @Override
    public boolean deleteProjectMilestone(Long projectId, Long milestoneId) {
        // 这里需要从project_milestone表删除数据
        // 暂时返回true，后续需要实现
        return deleteProjectMilestone(milestoneId);
    }

    @Override
    public List<ProjectMilestone> getProjectMilestones(Long projectId) {
        try {
            // 查询project_milestone表，获取项目的所有阶段
            String sql = "SELECT id, project_id, name, description, due_date, status, create_time, update_time FROM project_milestone WHERE project_id = ? ORDER BY id";
            return jdbcTemplate.query(sql, new Object[]{projectId}, (rs, rowNum) -> {
                ProjectMilestone milestone = new ProjectMilestone();
                milestone.setId(rs.getLong("id"));
                milestone.setProjectId(rs.getLong("project_id"));
                milestone.setName(rs.getString("name"));
                milestone.setDescription(rs.getString("description"));
                milestone.setDueDate(rs.getString("due_date"));
                milestone.setStatus(rs.getString("status"));
                milestone.setCreateTime(rs.getString("create_time"));
                milestone.setUpdateTime(rs.getString("update_time"));
                return milestone;
            });
        } catch (Exception e) {
            // 记录异常详情
            System.err.println("获取项目阶段失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean uploadProjectFile(Long projectId, Long milestoneId, String fileName, String fileUrl, Long fileSize, String fileType, Long uploaderId) {
        // 这里需要向project_file表插入数据
        // 暂时返回true，后续需要实现
        return true;
    }

    @Override
    public boolean addProjectFile(Long projectId, ProjectFile projectFile) {
        try{
            // 使用JDBC插入数据到 project_file 表
            String sql = "INSERT INTO project_file (project_id, milestone_id,  file_name, file_url, file_size, file_type, uploader_id, upload_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            int result = jdbcTemplate.update(sql,
                projectFile.getProjectId(),
                projectFile.getMilestoneId(),
                projectFile.getFileName(),
                projectFile.getFileUrl(),
                projectFile.getFileSize(),
                projectFile.getFileType(),
                projectFile.getUploaderId(),
                java.time.LocalDateTime.now()
            );
            return result > 0;
    } catch (Exception e) {
        System.err.println("添加项目文件失败: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
    }

    @Override
    public boolean deleteProjectFile(Long fileId) {
        // 这里需要从project_file表删除数据
        // 暂时返回true，后续需要实现
        return true;
    }

    @Override
    public boolean deleteProjectFile(Long projectId, Long fileId) {
        // 这里需要从project_file表删除数据
        // 暂时返回true，后续需要实现
        return deleteProjectFile(fileId);
    }

    @Override
    public List<ProjectFile> getProjectFiles(Long projectId) {
        try {
            // 使用JDBC查询project_file表，根据projectId获取所有相关文件
            String sql = "SELECT id, project_id, milestone_id, file_name, file_url, file_size, file_type, uploader_id, upload_time FROM project_file WHERE project_id = ? ORDER BY upload_time DESC";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, projectId);
            List<ProjectFile> projectFiles = new ArrayList<>();

            // 将数据库查询结果转换为ProjectFile对象
            for (Map<String, Object> result : results) {
                ProjectFile projectFile = new ProjectFile();

                // 设置ProjectFile对象的属性
                projectFile.setId(result.get("id") != null ? Long.valueOf(result.get("id").toString()) : null);
                projectFile.setProjectId(result.get("project_id") != null ? Long.valueOf(result.get("project_id").toString()) : null);
                projectFile.setMilestoneId(result.get("milestone_id") != null ? Long.valueOf(result.get("milestone_id").toString()) : null);
                projectFile.setFileName(result.get("file_name") != null ? result.get("file_name").toString() : null);
                projectFile.setFileUrl(result.get("file_url") != null ? result.get("file_url").toString() : null);
                projectFile.setFileSize(result.get("file_size") != null ? Long.valueOf(result.get("file_size").toString()) : null);
                projectFile.setFileType(result.get("file_type") != null ? result.get("file_type").toString() : null);
                projectFile.setUploaderId(result.get("uploader_id") != null ? Long.valueOf(result.get("uploader_id").toString()) : null);
                projectFile.setUploadTime(result.get("upload_time") != null ? result.get("upload_time").toString() : null);
                projectFiles.add(projectFile);
            }

            return projectFiles;
        } catch (Exception e) {
            System.err.println("查询项目文件失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public ProjectFile getProjectFileById(Long fileId) {
        try {
            // 使用JDBC查询project_file表，根据fileId获取单个文件
            String sql = "SELECT id, project_id, milestone_id, file_name, file_url, file_size, file_type, uploader_id, upload_time FROM project_file WHERE id = ?";

            Map<String, Object> result = jdbcTemplate.queryForMap(sql, fileId);
            ProjectFile projectFile = new ProjectFile();

            // 设置ProjectFile对象的属性
            projectFile.setId(result.get("id") != null ? Long.valueOf(result.get("id").toString()) : null);
            projectFile.setProjectId(result.get("project_id") != null ? Long.valueOf(result.get("project_id").toString()) : null);
            projectFile.setMilestoneId(result.get("milestone_id") != null ? Long.valueOf(result.get("milestone_id").toString()) : null);
            projectFile.setFileName(result.get("file_name") != null ? result.get("file_name").toString() : null);
            projectFile.setFileUrl(result.get("file_url") != null ? result.get("file_url").toString() : null);
            projectFile.setFileSize(result.get("file_size") != null ? Long.valueOf(result.get("file_size").toString()) : null);
            projectFile.setFileType(result.get("file_type") != null ? result.get("file_type").toString() : null);
            projectFile.setUploaderId(result.get("uploader_id") != null ? Long.valueOf(result.get("uploader_id").toString()) : null);
            projectFile.setUploadTime(result.get("upload_time") != null ? result.get("upload_time").toString() : null);

            return projectFile;
        } catch (Exception e) {
            System.err.println("查询单个项目文件失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<ProjectFile> getMilestoneFiles(Long milestoneId) {
        try {
            // 使用JDBC查询project_file表，根据milestoneId获取所有相关文件
            String sql = "SELECT id, project_id, milestone_id, file_name, file_url, file_size, file_type, uploader_id, upload_time FROM project_file WHERE milestone_id = ? ORDER BY upload_time DESC";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, milestoneId);
            List<ProjectFile> projectFiles = new ArrayList<>();

            // 将数据库查询结果转换为ProjectFile对象
            for (Map<String, Object> result : results) {
                ProjectFile projectFile = new ProjectFile();

                // 设置ProjectFile对象的属性
                projectFile.setId(result.get("id") != null ? Long.valueOf(result.get("id").toString()) : null);
                projectFile.setProjectId(result.get("project_id") != null ? Long.valueOf(result.get("project_id").toString()) : null);
                projectFile.setMilestoneId(result.get("milestone_id") != null ? Long.valueOf(result.get("milestone_id").toString()) : null);
                projectFile.setFileName(result.get("file_name") != null ? result.get("file_name").toString() : null);
                projectFile.setFileUrl(result.get("file_url") != null ? result.get("file_url").toString() : null);
                projectFile.setFileSize(result.get("file_size") != null ? Long.valueOf(result.get("file_size").toString()) : null);
                projectFile.setFileType(result.get("file_type") != null ? result.get("file_type").toString() : null);
                projectFile.setUploaderId(result.get("uploader_id") != null ? Long.valueOf(result.get("uploader_id").toString()) : null);
                projectFile.setUploadTime(result.get("upload_time") != null ? result.get("upload_time").toString() : null);

                projectFiles.add(projectFile);
            }

            return projectFiles;
        } catch (Exception e) {
            System.err.println("查询阶段文件失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void checkProjectMilestonesAndSendReminders() {
        try {
            // 查找所有已过截止日期但状态仍为PENDING的阶段
            String sql = "UPDATE project_milestone SET status = 'OVERDUE' WHERE status = 'PENDING' AND due_date < CURRENT_DATE()";
            int updatedCount = jdbcTemplate.update(sql);

            if (updatedCount > 0) {
                System.out.println("成功更新 " + updatedCount + " 个逾期阶段为OVERDUE状态");
            }
        } catch (Exception e) {
            // 记录异常详情
            System.err.println("检查和更新逾期项目阶段失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean updateProjectStatus(Long projectId, String status) {
        // 更新项目状态
        // 暂时返回true，后续需要实现
        Project project = getById(projectId);
        if (project != null) {
            project.setStatus(status);
            return saveOrUpdate(project);
        }
        return false;
    }

    @Override
    public List<ProjectMember> getProjectMembers(Long projectId) {
        // 查询project_member表，获取项目的所有成员
        List<Map<String, Object>> dbMembers = projectMemberMapper.selectByProjectId(projectId);
        List<ProjectMember> members = new ArrayList<>();

        // 将数据库查询结果转换为ProjectMember对象
        for (Map<String, Object> dbMember : dbMembers) {
            ProjectMember member = new ProjectMember();

            // 设置基本属性
            member.setId(dbMember.get("id") != null ? Long.valueOf(dbMember.get("id").toString()) : null);
            member.setProjectId(dbMember.get("project_id") != null ? Long.valueOf(dbMember.get("project_id").toString()) : null);
            member.setUserId(dbMember.get("user_id") != null ? Long.valueOf(dbMember.get("user_id").toString()) : null);

            // 设置用户名（从数据库查询结果或用户表关联获取）
            member.setUserName(dbMember.get("user_name") != null ? dbMember.get("user_name").toString() : "");

            // 设置角色
            member.setRole(dbMember.get("role") != null ? dbMember.get("role").toString() : "");

            // 设置加入时间
            member.setJoinTime(dbMember.get("join_time") != null ? dbMember.get("join_time").toString() : "");

            members.add(member);
        }

        return members;
    }

    @Override
    public boolean isProjectMember(Long projectId, Long userId) {
        List<ProjectMember> members = getProjectMembers(projectId);
        return members.stream().anyMatch(member -> member.getUserId().equals(userId));
    }
}
