package com.academic.service;

import com.academic.entity.Achievement;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;


public interface AchievementService extends IService<Achievement> {

    /**
     * 获取所有成果列表
     */
    List<Achievement> getAllAchievements();

    /**
     * 根据ID获取成果详情
     */
    Map<String, Object> getAchievementById(Long id);

    /**
     * 创建或更新成果
     */
    boolean saveAchievement(Achievement achievement);

    /**
     * 删除成果
     */
    boolean deleteAchievement(Long id);

    /**
     * 根据提交者ID获取成果列表
     */
    List<Achievement> getAchievementsBySubmitterId(Long submitterId);

    /**
     * 根据提交者ID和搜索条件获取成果列表（支持分页）
     */
    List<Achievement> getAchievementsBySubmitterIdWithSearch(Long submitterId, Map<String, Object> searchParams, int pageNum, int pageSize);

    /**
     * 根据提交者ID和搜索条件获取成果总数
     */
    long getAchievementsCountBySubmitterIdWithSearch(Long submitterId, Map<String, Object> searchParams);

    /**
     * 根据类型获取成果列表
     */
    List<Achievement> getAchievementsByType(String type);

    /**
     * 根据状态获取成果列表
     */
    List<Achievement> getAchievementsByStatus(String status);

    /**
     * 根据级别获取成果列表
     */
    List<Achievement> getAchievementsByLevel(String level);

    /**
     * 初审成果
     */
    boolean firstApproveAchievement(Long achievementId, Long approverId, boolean approved, String comment);

    /**
     * 终审成果
     */
    boolean finalApproveAchievement(Long achievementId, Long approverId, boolean approved, String comment);

    /**
     * 获取待初审的成果列表
     */
    List<Achievement> getPendingFirstApprovalAchievements();

    /**
     * 获取待终审的成果列表
     */
    List<Achievement> getPendingFinalApprovalAchievements();

    /**
     * 根据标签筛选成果
     */
    List<Achievement> getAchievementsByTags(List<String> tags);

    /**
     * 获取成果审核历史
     */
    List<AchievementApprovalHistory> getAchievementApprovalHistory(Long achievementId);

    /**
     * 审核历史类（内部使用）
     */
    class AchievementApprovalHistory {
        private Long id;
        private Long achievementId;
        private Long approverId;
        private String approverName;
        private String approvalLevel;
        private String status;
        private String comment;
        private String approvalTime;

        // getter和setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getAchievementId() { return achievementId; }
        public void setAchievementId(Long achievementId) { this.achievementId = achievementId; }
        public Long getApproverId() { return approverId; }
        public void setApproverId(Long approverId) { this.approverId = approverId; }
        public String getApproverName() { return approverName; }
        public void setApproverName(String approverName) { this.approverName = approverName; }
        public String getApprovalLevel() { return approvalLevel; }
        public void setApprovalLevel(String approvalLevel) { this.approvalLevel = approvalLevel; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        public String getApprovalTime() { return approvalTime; }
        public void setApprovalTime(String approvalTime) { this.approvalTime = approvalTime; }
    }
}
