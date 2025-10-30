package com.academic.service;

import com.academic.entity.Project;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface ProjectService extends IService<Project> {

    /**
     * 获取所有项目列表
     */
    List<Project> getAllProjects();

    /**
     * 根据ID获取项目详情
     */
    Project getProjectById(Long id);

    /**
     * 创建或更新项目
     */
    boolean saveProject(Project project);

    /**
     * 删除项目
     */
    boolean deleteProject(Long id);

    /**
     * 根据负责人ID获取项目列表
     */
    List<Project> getProjectsByLeaderId(Long leaderId);

    /**
     * 根据类型获取项目列表
     */
    List<Project> getProjectsByType(String type);

    /**
     * 根据状态获取项目列表
     */
    List<Project> getProjectsByStatus(String status);

    /**
     * 根据级别获取项目列表
     */
    List<Project> getProjectsByLevel(String level);

    /**
     * 根据用户ID获取参与的项目列表
     */
    List<Project> getProjectsByUserId(Long userId);

    /**
     * 根据成员ID获取项目列表
     */
    List<Project> getProjectsByMemberId(Long memberId);

    /**
     * 添加项目成员
     */
    boolean addProjectMember(Long projectId, Long memberId, String role);

    /**
     * 邀请用户参与项目
     */
    boolean inviteUserToProject(Long projectId, Long userId, String role);

    /**
     * 移除项目成员
     */
    boolean removeProjectMember(Long projectId, Long userId);

    /**
     * 更新项目成员角色
     */
    boolean updateProjectMemberRole(Long projectId, Long userId, String role);

    /**
     * 创建项目阶段
     */
    boolean createProjectMilestone(Long projectId, String name, String description, String dueDate);

    /**
     * 添加项目阶段
     */
    boolean addProjectMilestone(Long projectId, ProjectMilestone milestone);

    /**
     * 更新项目阶段
     */
    boolean updateProjectMilestone(Long milestoneId, String name, String description, String dueDate, String status);

    /**
     * 更新项目阶段
     */
    boolean updateProjectMilestone(Long projectId, ProjectMilestone milestone);

    /**
     * 删除项目阶段
     */
    boolean deleteProjectMilestone(Long milestoneId);

    /**
     * 删除项目阶段
     */
    boolean deleteProjectMilestone(Long projectId, Long milestoneId);

    /**
     * 根据项目ID获取所有阶段
     */
    List<ProjectMilestone> getProjectMilestones(Long projectId);

    /**
     * 上传项目文件
     */
    boolean uploadProjectFile(Long projectId, Long milestoneId, String fileName, String fileUrl, Long fileSize, String fileType, Long uploaderId);

    /**
     * 添加项目文件
     */
    boolean addProjectFile(Long projectId, ProjectFile projectFile);

    /**
     * 删除项目文件
     */
    boolean deleteProjectFile(Long fileId);

    /**
     * 删除项目文件
     */
    boolean deleteProjectFile(Long projectId, Long fileId);

    /**
     * 根据项目ID获取所有文件
     */
    List<ProjectFile> getProjectFiles(Long projectId);
    
    /**
     * 根据文件ID获取单个文件
     */
    ProjectFile getProjectFileById(Long fileId);

    /**
     * 根据项目阶段ID获取文件
     */
    List<ProjectFile> getMilestoneFiles(Long milestoneId);

    /**
     * 检查项目时间节点并发送提醒
     */
    void checkProjectMilestonesAndSendReminders();

    /**
     * 更新项目状态
     */
    boolean updateProjectStatus(Long projectId, String status);

    /**
     * 获取项目成员列表
     */
    List<ProjectMember> getProjectMembers(Long projectId);

    /**
     * 检查用户是否是项目成员
     */
    boolean isProjectMember(Long projectId, Long userId);

    /**
     * 项目阶段类（内部使用）
     */
    class ProjectMilestone {
        private Long id;
        private Long projectId;
        private String name;
        private String description;
        private String dueDate;
        private String status;
        private String key; // 添加key字段用于识别预定义阶段
        private String createTime;
        private String updateTime;

        // getter和setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
        public String getUpdateTime() { return updateTime; }
        public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    }

    /**
     * 项目文件类（内部使用）
     */
    class ProjectFile {
        private Long id;
        private Long projectId;
        private Long milestoneId;
        private String milestoneKey; // 添加milestoneKey字段用于关联预定义阶段
        private String fileName;
        private String fileUrl;
        private Long fileSize;
        private String fileType;
        private Long uploaderId;
        private String uploadTime;

        // getter和setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public Long getMilestoneId() { return milestoneId; }
        public void setMilestoneId(Long milestoneId) { this.milestoneId = milestoneId; }
        public String getMilestoneKey() { return milestoneKey; }
        public void setMilestoneKey(String milestoneKey) { this.milestoneKey = milestoneKey; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public Long getUploaderId() { return uploaderId; }
        public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }
        public String getUploadTime() { return uploadTime; }
        public void setUploadTime(String uploadTime) { this.uploadTime = uploadTime; }
    }

    /**
     * 项目成员类（内部使用）
     */
    class ProjectMember {
        private Long id;
        private Long projectId;
        private Long userId;
        private String userName;
        private String role;
        private String joinTime;

        // getter和setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getJoinTime() { return joinTime; }
        public void setJoinTime(String joinTime) { this.joinTime = joinTime; }
    }
}
