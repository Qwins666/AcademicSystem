package com.academic.controller;

import com.academic.common.Result;
import com.academic.entity.Project;
import com.academic.entity.User;
import com.academic.service.ProjectService;
import com.academic.service.UserService;
import com.academic.util.FileUtil;
import com.academic.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
@CrossOrigin
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger LOG= LoggerFactory.getLogger(ProjectController.class);

    /**
     * 获取所有项目列表
     */
    @GetMapping
    public Result<List<Project>> getAllProjects() {
        try {
            List<Project> projects = projectService.getAllProjects();
            return Result.success(projects);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取项目详情
     */
    @GetMapping("/{id}")
    public Result<Project> getProjectById(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id);
            if (project != null) {
                return Result.success(project);
            } else {
                return Result.notFound("项目不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建科研项目
     */
    @PostMapping
    public Result<String> createProject(@RequestBody Project project, HttpServletRequest request) {
        try {
            // 从JWT中获取当前用户ID作为项目负责人ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                // 从token中解析出用户ID
                String tokenWithoutBearer = token.substring(7);
                Long userId = jwtUtil.getUserIdFromToken(tokenWithoutBearer);
                project.setLeaderId(userId);

                if (projectService.saveProject(project)) {
                    // 同时将创建者添加为项目成员
                    projectService.addProjectMember(project.getId(), project.getLeaderId(), "LEADER");
                    return Result.success("项目创建成功");
                } else {
                    return Result.error("项目创建失败");
                }
            }
            return Result.unauthorized("未登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新项目信息
     */
    @PutMapping("/{id}")
    public Result<String> updateProject(@PathVariable Long id, @RequestBody Project project) {
        try {
            project.setId(id);
            if (projectService.saveProject(project)) {
                return Result.success("项目更新成功");
            } else {
                return Result.error("项目更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteProject(@PathVariable Long id) {
        try {
            if (projectService.deleteProject(id)) {
                return Result.success("项目删除成功");
            } else {
                return Result.error("项目删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据负责人ID获取项目列表
     */
    @GetMapping("/leader/{leaderId}")
    public Result<List<Project>> getProjectsByLeaderId(@PathVariable Long leaderId) {
        try {
            List<Project> projects = projectService.getProjectsByLeaderId(leaderId);
            return Result.success(projects);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据成员ID获取项目列表
     */
    @GetMapping("/member/{memberId}")
    public Result<List<Project>> getProjectsByMemberId(@PathVariable Long memberId) {
        try {
            List<Project> projects = projectService.getProjectsByMemberId(memberId);
            return Result.success(projects);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据类型获取项目列表
     */
    @GetMapping("/type/{type}")
    public Result<List<Project>> getProjectsByType(@PathVariable String type) {
        try {
            List<Project> projects = projectService.getProjectsByType(type);
            return Result.success(projects);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据状态获取项目列表
     */
    @GetMapping("/status/{status}")
    public Result<List<Project>> getProjectsByStatus(@PathVariable String status) {
        try {
            List<Project> projects = projectService.getProjectsByStatus(status);
            return Result.success(projects);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加项目成员
     */
    @PostMapping("/{id}/members")
    public Result<String> addProjectMember(@PathVariable Long id, @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        try {
            // 权限检查：项目负责人可以添加成员
            Long currentUserId = getUserIdFromRequest(request);

            // 检查是否是项目负责人
            Project project = projectService.getProjectById(id);
            if (project == null || !project.getLeaderId().equals(currentUserId)) {
                return Result.forbidden("只有项目负责人才能添加成员");
            }

            Long memberId = Long.valueOf(requestBody.get("memberId").toString());
            String role = (String) requestBody.get("role");

            if (projectService.addProjectMember(id, memberId, role)) {
                return Result.success("成员添加成功");
            } else {
                return Result.error("成员添加失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 移除项目成员
     */
    @DeleteMapping("/{id}/members/{memberId}")
    public Result<String> removeProjectMember(@PathVariable Long id, @PathVariable Long memberId, HttpServletRequest request) {
        try {
            // 权限检查：项目负责人可以移除成员
            Long currentUserId = getUserIdFromRequest(request);

            // 检查是否是项目负责人
            Project project = projectService.getProjectById(id);
            if (project == null || !project.getLeaderId().equals(currentUserId)) {
                return Result.forbidden("只有项目负责人才能移除成员");
            }

            if (projectService.removeProjectMember(id, memberId)) {
                return Result.success("成员移除成功");
            } else {
                return Result.error("成员移除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取项目成员列表
     */
    @GetMapping("/{id}/members")
    public Result<List<ProjectService.ProjectMember>> getProjectMembers(@PathVariable Long id) {
        try {
            List<ProjectService.ProjectMember> members = projectService.getProjectMembers(id);
            return Result.success(members);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加项目阶段
     */
    @PostMapping("/{id}/milestones")
    public Result<String> addProjectMilestone(@PathVariable Long id, @RequestBody ProjectService.ProjectMilestone milestone) {
        try {
            if (projectService.addProjectMilestone(id, milestone)) {
                return Result.success("阶段添加成功");
            } else {
                return Result.error("阶段添加失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新项目阶段
     */
    @PutMapping("/{id}/milestones/{milestoneId}")
    public Result<String> updateProjectMilestone(@PathVariable Long id, @PathVariable Long milestoneId, @RequestBody ProjectService.ProjectMilestone milestone) {
        try {
            milestone.setId(milestoneId);
            // 检查阶段是否存在
            List<ProjectService.ProjectMilestone> milestones = projectService.getProjectMilestones(id);
            boolean milestoneExists = milestones.stream().anyMatch(m -> m.getId().equals(milestoneId));

            if (!milestoneExists) {
                return Result.error("阶段不存在");
            }

            // 检查是否只是更新状态
            boolean isStatusOnlyUpdate = milestone.getName() == null && milestone.getDescription() == null && milestone.getDueDate() == null;

            // 尝试更新阶段
            if (projectService.updateProjectMilestone(id, milestone)) {
                return Result.success(isStatusOnlyUpdate ? "阶段状态更新成功" : "阶段更新成功");
            } else {
                // 检查是否因为阶段已完成导致更新失败
                ProjectService.ProjectMilestone existingMilestone = milestones.stream()
                    .filter(m -> m.getId().equals(milestoneId))
                    .findFirst().orElse(null);

                if (existingMilestone != null && "COMPLETED".equals(existingMilestone.getStatus()) && !isStatusOnlyUpdate) {
                    return Result.error("阶段已完成，不能编辑内容");
                }

                return Result.error("阶段更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查并更新逾期项目阶段
     */
    @PostMapping("/milestones/check-overdue")
    public Result<String> checkAndUpdateOverdueMilestones() {
        try {
            projectService.checkProjectMilestonesAndSendReminders();
            return Result.success("逾期阶段检查完成");
        } catch (Exception e) {
            return Result.error("逾期阶段检查失败: " + e.getMessage());
        }
    }

    /**
     * 删除项目阶段
     */
    @DeleteMapping("/{id}/milestones/{milestoneId}")
    public Result<String> deleteProjectMilestone(@PathVariable Long id, @PathVariable Long milestoneId) {
        try {
            if (projectService.deleteProjectMilestone(id, milestoneId)) {
                return Result.success("阶段删除成功");
            } else {
                return Result.error("阶段删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取项目阶段列表
     */
    @GetMapping("/{id}/milestones")
    public Result<List<ProjectService.ProjectMilestone>> getProjectMilestones(@PathVariable Long id) {
        try {
            List<ProjectService.ProjectMilestone> milestones = projectService.getProjectMilestones(id);
            return Result.success(milestones);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传项目文件
     */
    @PostMapping("/{id}/files")
    public Result<String> uploadProjectFile(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                           @RequestParam("milestoneId") Long milestoneId,
                                           @RequestParam(value = "milestoneKey", required = false) String milestoneKey,
                                           @RequestParam("uploaderId") Long uploaderId, HttpServletRequest request) {
        if(milestoneId==null){
            milestoneId=0L;
        }
        try {
            // 验证权限：确保上传者是项目成员
            String token = request.getHeader("Authorization");
            Long currentUserId = null;
            if (token != null && token.startsWith("Bearer ")) {
                String tokenWithoutBearer = token.substring(7);
                currentUserId = jwtUtil.getUserIdFromToken(tokenWithoutBearer);
                System.out.println("Token值: " + tokenWithoutBearer);
                System.out.println("Current User ID值: " + currentUserId);
            }
            System.out.println("Project ID的值: " + id);
            System.out.println("Milestone ID的值: " + milestoneId);
            System.out.println("Milestone Key的值: " + milestoneKey);
            System.out.println("Uploader ID的值: " + uploaderId);
            boolean isMember = projectService.isProjectMember(id, currentUserId);
            System.out.println("Is Project Member的值: " + isMember);
            if (currentUserId == null || !projectService.isProjectMember(id, currentUserId)) {
                return Result.forbidden("没有权限上传文件");
            }

            // 检查文件是否为空
            if (file.isEmpty()) {
                return Result.badRequest("上传文件不能为空");
            }

            // 使用FileUtil上传文件到指定子目录
            String subDir = "projects/" + id;
            String fileUrl;
            try {
                fileUrl = fileUtil.uploadFile(file, subDir);
            } catch (IOException e) {
                return Result.error("文件上传失败: " + e.getMessage());
            }

            // 创建项目文件对象并添加到数据库
            ProjectService.ProjectFile projectFile = new ProjectService.ProjectFile();
            projectFile.setProjectId(id);
            projectFile.setMilestoneId(milestoneId);
            projectFile.setMilestoneKey(milestoneKey);
            projectFile.setFileName(file.getOriginalFilename());
            projectFile.setFileUrl(fileUrl);
            projectFile.setFileSize(file.getSize());
            projectFile.setUploaderId(uploaderId);

            // 获取文件类型
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.lastIndexOf(".") > 0) {
                String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
                projectFile.setFileType(fileType);
            }

            if (projectService.addProjectFile(id, projectFile)) {
                return Result.success("文件上传成功");
            }
            return Result.error("文件上传失败: 无法保存文件记录到数据库");
        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目文件列表
     */
    @GetMapping("/{id}/files")
    public Result<List<ProjectService.ProjectFile>> getProjectFiles(@PathVariable Long id) {
        try {
            List<ProjectService.ProjectFile> files = projectService.getProjectFiles(id);
            return Result.success(files);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除项目文件
     */
    @DeleteMapping("/{id}/files/{fileId}")
    public Result<String> deleteProjectFile(@PathVariable Long id, @PathVariable Long fileId) {
        try {
            if (projectService.deleteProjectFile(id, fileId)) {
                return Result.success("文件删除成功");
            } else {
                return Result.error("文件删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 下载项目文件
     */
    @GetMapping("/{id}/files/{fileId}/download")
    public ResponseEntity<Resource> downloadProjectFile(@PathVariable Long id, @PathVariable Long fileId, HttpServletRequest request) {
        try {
            // 获取当前用户ID
            Long currentUserId = userService.getCurrentUserId(request);

            // 验证用户是否有权限访问该项目
            if (currentUserId != null) {
                // 获取用户角色
                String userRole = userService.getUserRole(currentUserId);
                System.out.println("下载文件用户权限：" + userRole);

                // 如果用户是管理员或者是项目成员，则有权限访问
                if ("ADMIN".equals(userRole) || projectService.isProjectMember(id, currentUserId)) {
                    //管理员和项目成员有权限下载文件
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 获取文件信息
            ProjectService.ProjectFile file = projectService.getProjectFileById(fileId);

            // 验证文件是否存在且属于该项目
            if (file == null || !file.getProjectId().equals(id)) {
                return ResponseEntity.notFound().build();
            }

            // 使用fileUtil读取文件内容
            Resource resource = fileUtil.getFileResource(file.getFileUrl());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // 根据文件类型设置不同的响应头
            String contentType;
            String contentDisposition;

            // 检查是否为PDF文件
            if (file.getFileName().toLowerCase().endsWith(".pdf")) {
                contentType = "application/pdf";
                contentDisposition = "inline; filename=" + URLEncoder.encode(file.getFileName(), "UTF-8");
            } else {
                contentType = "application/octet-stream";
                contentDisposition = "attachment; filename=" + URLEncoder.encode(file.getFileName(), "UTF-8");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(resource);
        } catch (Exception e) {
            System.err.println("文件下载失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 更改项目状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateProjectStatus(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        try {
            String status = requestBody.get("status");
            if (projectService.updateProjectStatus(id, status)) {
                return Result.success("项目状态更新成功");
            } else {
                return Result.error("项目状态更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /*
      获取用户详细信息
     */
    @GetMapping("/leaderDetail/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getById(id);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取学生列表（教师可访问）
     */
    @GetMapping("/available-students")
    public Result<List<User>> getAvailableStudents() {
        try {
            // 调用UserService的getUsersByRole方法获取学生列表
            List<User> students = userService.getUsersByRole("STUDENT");
            return Result.success(students);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查是否为教师
     */
    private boolean isTeacher(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String role = jwtUtil.getRoleFromToken(token);
            return "TEACHER".equals(role);
        }
        return false;
    }

    /**
     * 从请求中获取当前用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }
}
