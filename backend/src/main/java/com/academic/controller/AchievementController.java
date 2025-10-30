package com.academic.controller;

import com.academic.common.Result;
import com.academic.entity.Achievement;
import com.academic.service.AchievementService;
import com.academic.util.FileUtil;
import com.academic.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/achievements")
@CrossOrigin
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取所有成果列表（支持搜索和分页）
     */
    @GetMapping
    public Result<List<Achievement>> getAllAchievements(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        try {
            // 构建搜索参数
            Map<String, Object> searchParams = new HashMap<>();
            if (title != null && !title.trim().isEmpty()) {
                searchParams.put("title", title);
            }
            if (type != null && !type.trim().isEmpty()) {
                searchParams.put("type", type);
            }
            if (status != null && !status.trim().isEmpty()) {
                searchParams.put("status", status);
            }

            // 根据搜索参数获取成果列表
            List<Achievement> achievements = new ArrayList<>();

            if (searchParams.isEmpty()) {
                // 如果没有搜索参数，获取所有成果
                achievements = achievementService.getAllAchievements();
            } else {
                // 如果有搜索参数，需要使用带条件的查询
                // 注意：这里直接使用lambdaQueryWrapper进行查询，因为getAllAchievementsWithSearch方法可能不存在
                LambdaQueryWrapper<Achievement> queryWrapper = new LambdaQueryWrapper<>();

                if (searchParams.containsKey("title")) {
                    queryWrapper.like(Achievement::getTitle, searchParams.get("title"));
                }
                if (searchParams.containsKey("type")) {
                    queryWrapper.eq(Achievement::getType, searchParams.get("type"));
                }
                if (searchParams.containsKey("status")) {
                    queryWrapper.eq(Achievement::getStatus, searchParams.get("status"));
                }

                achievements = achievementService.list(queryWrapper);
            }

            return Result.success(achievements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取成果详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getAchievementById(@PathVariable Long id) {
        try {
            Map<String, Object> achievementDetail = achievementService.getAchievementById(id);
            if (achievementDetail != null) {
                return Result.success(achievementDetail);
            } else {
                return Result.notFound("成果不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建成果
     */
    @PostMapping
    public Result<String> createAchievement(@RequestBody Achievement achievement, HttpServletRequest request) {
        try {
            // 从JWT中获取当前用户ID作为提交者ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                // 从token中解析出用户ID
                String actualToken = token.substring(7);
                Long submitterId = jwtUtil.getUserIdFromToken(actualToken);
                achievement.setSubmitterId(submitterId);
                achievement.setStatus("PENDING"); // 默认状态为待审核

                if (achievementService.saveAchievement(achievement)) {
                    return Result.success("成果创建成功，等待审核");
                } else {
                    return Result.error("成果创建失败");
                }
            }
            return Result.unauthorized("未登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新成果
     */
    @PutMapping("/{id}")
    public Result<String> updateAchievement(@PathVariable Long id, @RequestBody Achievement achievement) {
        try {
            achievement.setId(id);
            // 已提交审核的成果不允许修改
            Map<String, Object> existingAchievementDetail = achievementService.getAchievementById(id);
            if (existingAchievementDetail != null && !"PENDING".equals(existingAchievementDetail.get("status"))) {
                return Result.error("已提交审核的成果不允许修改");
            }

            if (achievementService.saveAchievement(achievement)) {
                return Result.success("成果更新成功");
            } else {
                return Result.error("成果更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除成果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteAchievement(@PathVariable Long id) {
        try {
            // 已提交审核的成果不允许删除
            Map<String, Object> achievementDetail = achievementService.getAchievementById(id);
            if (achievementDetail != null && !"PENDING".equals(achievementDetail.get("status"))) {
                return Result.error("已提交审核的成果不允许删除");
            }

            if (achievementService.deleteAchievement(id)) {
                return Result.success("成果删除成功");
            } else {
                return Result.error("成果删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据提交者ID获取成果列表
     */
    @GetMapping("/submitter/{submitterId}")
    public Result<List<Achievement>> getAchievementsBySubmitterId(@PathVariable Long submitterId) {
        try {
            List<Achievement> achievements = achievementService.getAchievementsBySubmitterId(submitterId);
            return Result.success(achievements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据提交者ID和搜索条件获取成果列表（支持分页）
     */
    @GetMapping("/submitter/{submitterId}/search")
    public Result<Map<String, Object>> getAchievementsBySubmitterIdWithSearch(
            @PathVariable Long submitterId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // 构建搜索参数，只添加非null且非空的值
            Map<String, Object> searchParams = new HashMap<>();
            if (title != null && !title.trim().isEmpty()) {
                searchParams.put("title", title);
            }
            if (type != null && !type.trim().isEmpty()) {
                searchParams.put("type", type);
            }
            if (status != null && !status.trim().isEmpty()) {
                searchParams.put("status", status);
            }

            // 获取搜索结果和总数
            List<Achievement> achievements = achievementService.getAchievementsBySubmitterIdWithSearch(
                    submitterId, searchParams, pageNum, pageSize);
            long total = achievementService.getAchievementsCountBySubmitterIdWithSearch(submitterId, searchParams);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", achievements);
            result.put("total", total);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("pages", (total + pageSize - 1) / pageSize); // 计算总页数

            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据类型获取成果列表
     */
    @GetMapping("/type/{type}")
    public Result<List<Achievement>> getAchievementsByType(@PathVariable String type) {
        try {
            List<Achievement> achievements = achievementService.getAchievementsByType(type);
            return Result.success(achievements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据状态获取成果列表
     */
    @GetMapping("/status/{status}")
    public Result<List<Achievement>> getAchievementsByStatus(@PathVariable String status) {
        try {
            List<Achievement> achievements = achievementService.getAchievementsByStatus(status);
            return Result.success(achievements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 初审成果
     */
    @PostMapping("/{id}/first-approve")
    public Result<String> firstApproveAchievement(@PathVariable Long id, @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        try {
            // 从JWT中获取当前用户ID作为审核者ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                // 从token中解析出用户ID
                String actualToken = token.substring(7);
                Long approverId = jwtUtil.getUserIdFromToken(actualToken);
                boolean approved = Boolean.TRUE.equals(requestBody.get("approved"));
                String comment = (String) requestBody.get("comment");

                if (achievementService.firstApproveAchievement(id, approverId, approved, comment)) {
                    return Result.success("初审" + (approved ? "通过" : "驳回") + "成功");
                } else {
                    return Result.error("初审操作失败");
                }
            }
            return Result.unauthorized("未登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 终审成果
     */
    @PostMapping("/{id}/final-approve")
    public Result<String> finalApproveAchievement(@PathVariable Long id, @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        try {
            // 从JWT中获取当前用户ID作为审核者ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                // 从token中解析出用户ID
                String actualToken = token.substring(7);
                Long approverId = jwtUtil.getUserIdFromToken(actualToken);
                boolean approved = Boolean.TRUE.equals(requestBody.get("approved"));
                String comment = (String) requestBody.get("comment");

                if (achievementService.finalApproveAchievement(id, approverId, approved, comment)) {
                    return Result.success("终审" + (approved ? "通过" : "驳回") + "成功");
                } else {
                    return Result.error("终审操作失败");
                }
            }
            return Result.unauthorized("未登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取待初审的成果列表
     */
    @GetMapping("/pending/first")
    public Result<List<Achievement>> getPendingFirstApprovalAchievements() {
        try {
            List<Achievement> achievements = achievementService.getPendingFirstApprovalAchievements();
            return Result.success(achievements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取待终审的成果列表
     */
    @GetMapping("/pending/final")
    public Result<List<Achievement>> getPendingFinalApprovalAchievements() {
        try {
            List<Achievement> achievements = achievementService.getPendingFinalApprovalAchievements();
            return Result.success(achievements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传成果文件
     */
    @PostMapping("/upload-file")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("achievementId") Long achievementId,
                                                 HttpServletRequest request) {
        try {
            // 从JWT中获取当前用户ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                // 使用FileUtil实现文件上传，子目录为achievements
                String fileUrl = fileUtil.uploadFile(file, "achievements");

                // 更新成果记录的文件信息
                Achievement achievement = achievementService.getById(achievementId);
                if (achievement != null) {
                    achievement.setFileUrl(fileUrl);
                    achievement.setFileName(file.getOriginalFilename());
                    achievement.setFileSize(file.getSize());
                    achievementService.updateById(achievement);
                } else {
                    return Result.error("未找到对应的成果记录");
                }

                // 构建返回结果
                Map<String, String> result = new HashMap<>();
                result.put("fileUrl", fileUrl);
                result.put("fileName", file.getOriginalFilename());
                result.put("fileSize", String.valueOf(file.getSize()));

                return Result.success("文件上传成功", result);
            }
            return Result.unauthorized("未登录");
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取成果审核历史
     */
    @GetMapping("/{id}/approval-history")
    public Result<List<AchievementService.AchievementApprovalHistory>> getAchievementApprovalHistory(@PathVariable Long id) {
        try {
            List<AchievementService.AchievementApprovalHistory> history = achievementService.getAchievementApprovalHistory(id);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
