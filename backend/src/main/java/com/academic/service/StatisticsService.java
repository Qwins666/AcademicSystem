package com.academic.service;

import java.util.List;
import java.util.Map;


public interface StatisticsService {

    /**
     * 系统概览数据类（内部使用）
     */
    class SystemOverview {
        private Long totalUsers;
        private Long totalActivities;
        private Long totalAchievements;
        private Long totalProjects;
        private Long pendingApprovals;
        private Long activeToday;
        private Long weeklyNewUsers;
        private Long weeklyNewProjects;
        private Long weeklyNewAchievements;

        // getter和setter方法
        public Long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
        public Long getTotalActivities() { return totalActivities; }
        public void setTotalActivities(Long totalActivities) { this.totalActivities = totalActivities; }
        public Long getTotalAchievements() { return totalAchievements; }
        public void setTotalAchievements(Long totalAchievements) { this.totalAchievements = totalAchievements; }
        public Long getTotalProjects() { return totalProjects; }
        public void setTotalProjects(Long totalProjects) { this.totalProjects = totalProjects; }
        public Long getPendingApprovals() { return pendingApprovals; }
        public void setPendingApprovals(Long pendingApprovals) { this.pendingApprovals = pendingApprovals; }
        public Long getActiveToday() { return activeToday; }
        public void setActiveToday(Long activeToday) { this.activeToday = activeToday; }
        public Long getWeeklyNewUsers() { return weeklyNewUsers; }
        public void setWeeklyNewUsers(Long weeklyNewUsers) { this.weeklyNewUsers = weeklyNewUsers; }
        public Long getWeeklyNewProjects() { return weeklyNewProjects; }
        public void setWeeklyNewProjects(Long weeklyNewProjects) { this.weeklyNewProjects = weeklyNewProjects; }
        public Long getWeeklyNewAchievements() { return weeklyNewAchievements; }
        public void setWeeklyNewAchievements(Long weeklyNewAchievements) { this.weeklyNewAchievements = weeklyNewAchievements; }
    }

    /**
     * 获取系统概览数据
     */
    SystemOverview getSystemOverview();

    //用户相关统计方法
    long getTotalUsers();

    // 项目相关统计方法
    long getTotalProjects();
    Map<String, Object> getProjectsByStatus();
    Map<String, Object> getProjectsByType();
    List<TrendData> getProjectTrend(String startDate, String endDate, String period);

    // 活动相关统计方法
    long getTotalActivities();
    Map<String, Object> getActivitiesByType();
    long getTotalParticipants();
    List<TrendData> getActivityTrend(String startDate, String endDate, String period);


    Map<String, Object> getAchievementTrendByGrade();

    /**
     * 获取各类成果数量趋势图数据（按类型）
     */
    Map<String, Object> getAchievementTrendByType();

    /**
     * 获取各类成果数量趋势图数据（按时间段）
     */
    Map<String, Object> getAchievementTrendByTimeRange(String startDate, String endDate);

    /**
     * 获取各类成果数量趋势图数据（按年级）
     */
    List<TrendData> getAchievementTrendByGrade(String startDate, String endDate, String type, String period);

    /**
     * 获取活动参与率统计数据（柱状图）
     */
    Map<String, Object> getActivityParticipationRateBarChart();

    /**
     * 获取活动参与率统计数据（饼图）
     */
    Map<String, Object> getActivityParticipationRatePieChart();

    /**
     * 获取成果分布数据（按专业）
     */
    Map<String, Object> getAchievementDistributionByMajor();

    /**
     * 获取成果分布数据（按学院）
     */
    Map<String, Object> getAchievementDistributionByDepartment();

    /**
     * 获取成果分布数据（按性别）
     */
    Map<String, Object> getAchievementDistributionByGender();

    /**
     * 获取成果审核状态分布数据（仪表盘展示）
     */
    Map<String, Object> getAchievementApprovalStatusDistribution();

    /**
     * 获取用户活跃度统计数据
     */
    Map<String, Object> getUserActivityStatistics();

    /**
     * 获取系统整体运营数据概览
     */
    Map<String, Object> getSystemOverviewData();

    /**
     * 获取成果趋势数据
     */
    List<TrendData> getAchievementTrend(String startDate, String endDate, String type, String period);

    /**
     * 趋势数据类（内部使用）
     */
    class TrendData {
        private String date;
        private Long count;
        private String type;
        private String grade;

        // getter和setter方法
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
    }

    /**
     * 获取活动参与率统计
     */
    List<ParticipationData> getActivityParticipation(String startDate, String endDate, String activityType);

    /**
     * 参与数据类（内部使用）
     */
    class ParticipationData {
        private String activityName;
        private String activityType;
        private Long totalParticipants;
        private Long totalSlots;
        private Double participationRate;
        private String date;

        // getter和setter方法
        public String getActivityName() { return activityName; }
        public void setActivityName(String activityName) { this.activityName = activityName; }
        public String getActivityType() { return activityType; }
        public void setActivityType(String activityType) { this.activityType = activityType; }
        public Long getTotalParticipants() { return totalParticipants; }
        public void setTotalParticipants(Long totalParticipants) { this.totalParticipants = totalParticipants; }
        public Long getTotalSlots() { return totalSlots; }
        public void setTotalSlots(Long totalSlots) { this.totalSlots = totalSlots; }
        public Double getParticipationRate() { return participationRate; }
        public void setParticipationRate(Double participationRate) { this.participationRate = participationRate; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    /**
     * 获取成果分布数据
     */
    Map<String, List<DistributionData>> getAchievementDistribution(String startDate, String endDate, String dimension);

    /**
     * 分布数据类（内部使用）
     */
    class DistributionData {
        private String category;
        private Long count;
        private Double percentage;

        // getter和setter方法
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }

    /**
     * 状态数据类（内部使用）
     */
    class StatusData {
        private String status;
        private Long count;
        private Double percentage;

        // getter和setter方法
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }

    /**
     * 获取成果审核状态分布
     */
    List<StatusData> getApprovalStatusDistribution(String startDate, String endDate, String type);

    /**
     * 用户活跃度数据类（内部使用）
     */
    class UserActivityData {
        private String userId;
        private String username;
        private String role;
        private Long activityCount;
        private String lastActiveDate;

        // getter和setter方法
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Long getActivityCount() { return activityCount; }
        public void setActivityCount(Long activityCount) { this.activityCount = activityCount; }
        public String getLastActiveDate() { return lastActiveDate; }
        public void setLastActiveDate(String lastActiveDate) { this.lastActiveDate = lastActiveDate; }
    }

    /**
     * 获取用户活跃度数据
     */
    List<UserActivityData> getUserActivity(String startDate, String endDate, String role);
}
