package com.academic.controller;

import com.academic.common.Result;
import com.academic.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
@CrossOrigin
public class StatisticsController {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取成果趋势数据
     */
    @GetMapping("/achievements/trend")
    public Result<List<StatisticsService.TrendData>> getAchievementTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "month") String period) {
        try {
            // 如果未提供日期范围，默认使用最近6个月
            if (startDate == null || endDate == null) {
                LocalDate now = LocalDate.now();
                endDate = now.format(DateTimeFormatter.ISO_DATE);
                startDate = now.minusMonths(6).format(DateTimeFormatter.ISO_DATE);
            }

            List<StatisticsService.TrendData> trendData = statisticsService.getAchievementTrend(startDate, endDate, type, period);
            return Result.success(trendData);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取活动参与率统计
     */
    @GetMapping("/activity-participation")
    public Result<List<StatisticsService.ParticipationData>> getActivityParticipation(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String activityType) {
        try {
            // 如果未提供日期范围，默认使用最近6个月
            if (startDate == null || endDate == null) {
                LocalDate now = LocalDate.now();
                endDate = now.format(DateTimeFormatter.ISO_DATE);
                startDate = now.minusMonths(6).format(DateTimeFormatter.ISO_DATE);
            }

            List<StatisticsService.ParticipationData> participationData = statisticsService.getActivityParticipation(startDate, endDate, activityType);
            LOG.info("活动参与率后端数据："+participationData);
            return Result.success(participationData);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取成果分布数据
     */
    @GetMapping("/achievement-distribution")
    public Result<Map<String, List<StatisticsService.DistributionData>>> getAchievementDistribution(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String dimension) {
        try {
            // 如果未提供日期范围，默认使用最近6个月
            if (startDate == null || endDate == null) {
                LocalDate now = LocalDate.now();
                endDate = now.format(DateTimeFormatter.ISO_DATE);
                startDate = now.minusMonths(6).format(DateTimeFormatter.ISO_DATE);
            }

            // 使用我们之前添加的方法来获取分布数据
            Map<String, List<StatisticsService.DistributionData>> distributionData = statisticsService.getAchievementDistribution(startDate, endDate, dimension);

            return Result.success(distributionData);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取成果审核状态分布
     */
    @GetMapping("/approval-status")
    public Result<List<StatisticsService.StatusData>> getApprovalStatusDistribution(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String type) {
        try {
            // 如果未提供日期范围，默认使用最近6个月
            if (startDate == null || endDate == null) {
                LocalDate now = LocalDate.now();
                endDate = now.format(DateTimeFormatter.ISO_DATE);
                startDate = now.minusMonths(6).format(DateTimeFormatter.ISO_DATE);
            }

            List<StatisticsService.StatusData> statusData = statisticsService.getApprovalStatusDistribution(startDate, endDate, type);
            LOG.info("审核状态分布图表的后端数据："+statusData.get(1).getStatus());
            return Result.success(statusData);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户活跃度数据
     */
    @GetMapping("/user-activity")
    public Result<List<StatisticsService.UserActivityData>> getUserActivity(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String role) {
        try {
            // 如果未提供日期范围，默认使用最近30天
            if (startDate == null || endDate == null) {
                LocalDate now = LocalDate.now();
                endDate = now.format(DateTimeFormatter.ISO_DATE);
                startDate = now.minusDays(30).format(DateTimeFormatter.ISO_DATE);
            }

            List<StatisticsService.UserActivityData> userActivityData = statisticsService.getUserActivity(startDate, endDate, role);
            return Result.success(userActivityData);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取系统概览数据
     */
    @GetMapping("/overview")
    public Result<StatisticsService.SystemOverview> getSystemOverview() {
        try {
            StatisticsService.SystemOverview overview = statisticsService.getSystemOverview();
            return Result.success(overview);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取活动总数
     */
    @GetMapping("/activities/total")
    public Result getTotalActivities() {
        try {
            return Result.success(statisticsService.getTotalActivities());
        } catch (Exception e) {
            return Result.error("获取活动总数失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目总数
     */
    @GetMapping("/projects/total")
    public Result getTotalProjects() {
        try {
            return Result.success(statisticsService.getTotalProjects());
        } catch (Exception e) {
            return Result.error("获取项目总数失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户总数
     */
    @GetMapping("/users/total")
    public Result getTotalUsers() {
        try {
            return Result.success(statisticsService.getTotalUsers());
        } catch (Exception e) {
            return Result.error("获取用户总数失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目状态分布
     */
    @GetMapping("/projects/status")
    public Result getProjectsByStatus() {
        try {
            return Result.success(statisticsService.getProjectsByStatus());
        } catch (Exception e) {
            return Result.error("获取项目状态分布失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目类型分布
     */
    @GetMapping("/projects/type")
    public Result getProjectsByType() {
        try {
            return Result.success(statisticsService.getProjectsByType());
        } catch (Exception e) {
            return Result.error("获取项目类型分布失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目趋势数据
     */
    @GetMapping("/projects/trend")
    public Result getProjectTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "month") String period) {
        try {
            // 如果未提供日期范围，默认使用最近6个月
            if (startDate == null || endDate == null) {
                LocalDate now = LocalDate.now();
                endDate = now.format(DateTimeFormatter.ISO_DATE);
                startDate = now.minusMonths(6).format(DateTimeFormatter.ISO_DATE);
            }

            return Result.success(statisticsService.getProjectTrend(startDate, endDate, period));
        } catch (Exception e) {
            return Result.error("获取项目趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取活动类型分布
     */
    @GetMapping("/activities/type")
    public Result getActivitiesByType() {
        try {
            return Result.success(statisticsService.getActivitiesByType());
        } catch (Exception e) {
            return Result.error("获取活动类型分布失败: " + e.getMessage());
        }
    }

    /**
     * 获取活动参与总人数
     */
    @GetMapping("/activities/participants")
    public Result getTotalParticipants() {
        try {
            return Result.success(statisticsService.getTotalParticipants());
        } catch (Exception e) {
            return Result.error("获取活动参与总人数失败: " + e.getMessage());
        }
    }

    /**
     * 获取活动趋势数据
     */
    @GetMapping("/activities/trend")
    public Result getActivityTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "month") String period) {
        try {
            // 如果未提供日期范围，默认使用最近6个月
            if (startDate == null || endDate == null) {
                LocalDate now = LocalDate.now();
                endDate = now.format(DateTimeFormatter.ISO_DATE);
                startDate = now.minusMonths(6).format(DateTimeFormatter.ISO_DATE);
            }

            return Result.success(statisticsService.getActivityTrend(startDate, endDate, period));
        } catch (Exception e) {
            return Result.error("获取活动趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目统计数据
     */
    @GetMapping("/projects")
    public Result<Map<String, Object>> getProjectStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            // 如果未提供日期范围，默认使用最近6个月
            if (startDate == null || endDate == null) {
                LocalDate now = LocalDate.now();
                endDate = now.format(DateTimeFormatter.ISO_DATE);
                startDate = now.minusMonths(6).format(DateTimeFormatter.ISO_DATE);
            }

            Map<String, Object> projectStats = new HashMap<>();

            // 项目总数
            projectStats.put("totalProjects", statisticsService.getTotalProjects());

            // 按状态统计项目
            projectStats.put("projectsByStatus", statisticsService.getProjectsByStatus());

            // 按类型统计项目
            projectStats.put("projectsByType", statisticsService.getProjectsByType());

            // 项目趋势
            projectStats.put("projectTrend", statisticsService.getProjectTrend(startDate, endDate, "month"));

            return Result.success(projectStats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取活动统计数据
     */
    @GetMapping("/activities")
    public Result<Map<String, Object>> getActivityStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            // 如果未提供日期范围，默认使用最近6个月
            if (startDate == null || endDate == null) {
                LocalDate now = LocalDate.now();
                endDate = now.format(DateTimeFormatter.ISO_DATE);
                startDate = now.minusMonths(6).format(DateTimeFormatter.ISO_DATE);
            }

            Map<String, Object> activityStats = new HashMap<>();

            // 活动总数
            activityStats.put("totalActivities", statisticsService.getTotalActivities());

            // 按类型统计活动
            activityStats.put("activitiesByType", statisticsService.getActivitiesByType());

            // 活动参与总人数
            activityStats.put("totalParticipants", statisticsService.getTotalParticipants());

            // 活动趋势
            activityStats.put("activityTrend", statisticsService.getActivityTrend(startDate, endDate, "month"));

            return Result.success(activityStats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


}
