package com.academic.service.impl;

import com.academic.mapper.ActivityMapper;
import com.academic.mapper.ActivityParticipantMapper;
import com.academic.mapper.AchievementMapper;
import com.academic.mapper.ProjectMapper;
import com.academic.mapper.UserMapper;
import com.academic.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityParticipantMapper activityParticipantMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> getAchievementTrendByGrade() {
        // 获取各类成果数量趋势图数据（按年级）
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("开始查询不同年级的各类成果数量...");

            // 联表查询不同年级的各类成果数量
            String sql = "SELECT " +
                         "u.grade, " +
                         "a.type, " +
                         "COUNT(*) as count " +
                         "FROM achievement a " +
                         "JOIN user u ON a.submitter_id = u.id " +
                         "WHERE u.grade IS NOT NULL " +
                         "GROUP BY u.grade, a.type " +
                         "ORDER BY u.grade ASC, a.type ASC";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

            // 初始化年级列表和各类成果数量映射
            List<String> grades = new ArrayList<>();
            Map<String, Map<String, Integer>> gradeAchievementMap = new HashMap<>();

            // 收集所有年级
            for (Map<String, Object> row : rows) {
                String grade = (String) row.get("grade");
                if (!grades.contains(grade)) {
                    grades.add(grade);
                    gradeAchievementMap.put(grade, new HashMap<>());
                }
            }

            // 如果没有数据，设置默认值
            if (grades.isEmpty()) {
                grades = Arrays.asList("2021级", "2022级", "2023级", "2024级", "2025级");
                for (String grade : grades) {
                    gradeAchievementMap.put(grade, new HashMap<>());
                }
            }

            // 填充各类成果数量
            for (Map<String, Object> row : rows) {
                String grade = (String) row.get("grade");
                String type = (String) row.get("type");
                int count = ((Number) row.get("count")).intValue();

                gradeAchievementMap.get(grade).put(type, count);
            }

            // 准备返回数据
            int[] paperCounts = new int[grades.size()];
            int[] patentCounts = new int[grades.size()];
            int[] certificateCounts = new int[grades.size()];
            int[] projectCounts = new int[grades.size()];

            for (int i = 0; i < grades.size(); i++) {
                String grade = grades.get(i);
                Map<String, Integer> achievementCounts = gradeAchievementMap.get(grade);

                paperCounts[i] = achievementCounts.getOrDefault("PAPER", 0);
                patentCounts[i] = achievementCounts.getOrDefault("PATENT", 0);
                certificateCounts[i] = achievementCounts.getOrDefault("CERTIFICATE", 0);
                projectCounts[i] = achievementCounts.getOrDefault("PROJECT", 0);
            }

            // 设置返回结果
            result.put("grades", grades.toArray(new String[0]));
            result.put("paperCount", paperCounts);
            result.put("patentCount", patentCounts);
            result.put("certificateCount", certificateCounts);
            result.put("projectCount", projectCounts);

            System.out.println("不同年级的各类成果数量查询成功");

        } catch (Exception e) {
            System.out.println("获取不同年级的各类成果数量失败：" + e);
            // 异常情况下设置默认值
            result.put("grades", new String[] {"2019级", "2020级", "2021级", "2022级", "2023级"});
            result.put("paperCount", new int[] {0, 0, 0, 0, 0});
            result.put("patentCount", new int[] {0, 0, 0, 0, 0});
            result.put("certificateCount", new int[] {0, 0, 0, 0, 0});
            result.put("projectCount", new int[] {0, 0, 0, 0, 0});
        }

        return result;
    }

    @Override
    public Map<String, Object> getAchievementTrendByType() {
        // 获取各类成果数量趋势图数据（按类型）
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("开始查询成果类型趋势数据...");

            // 查询不同类型成果的数量
            String sql = "SELECT type, COUNT(*) as count FROM achievement GROUP BY type";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            // 准备返回数据
            List<String> types = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();

            // 定义类型映射，将数据库中的类型代码映射为中文名称
            Map<String, String> typeNameMap = new HashMap<>();
            typeNameMap.put("PAPER", "论文");
            typeNameMap.put("PATENT", "专利");
            typeNameMap.put("CERTIFICATE", "证书");
            typeNameMap.put("PROJECT", "项目");

            // 填充实际统计数据
            for (Map<String, Object> row : results) {
                String type = (String) row.get("type");
                Integer count = ((Number) row.get("count")).intValue();

                // 添加到结果中（使用中文名称）
                String typeName = typeNameMap.getOrDefault(type, type);
                types.add(typeName);
                counts.add(count);
            }

            // 设置返回结果
            result.put("types", types.toArray(new String[0]));
            result.put("counts", counts.stream().mapToInt(Integer::intValue).toArray());

            System.out.println("成果类型趋势数据查询成功，共" + types.size() + "种类型");

        } catch (Exception e) {
            System.out.println("获取成果类型趋势失败：" + e);
            // 异常情况下返回默认数据，避免前端报错
            result.put("types", new String[] {"论文", "专利", "证书", "项目"});
            result.put("counts", new int[] {0, 0, 0, 0});
        }
        return result;
    }

    @Override
    public Map<String, Object> getAchievementTrendByTimeRange(String startDate, String endDate) {
        // 获取各类成果数量趋势图数据（按时间段）
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("开始查询成果时间趋势数据...");

            // 构建查询SQL
            StringBuilder sql = new StringBuilder();
            List<Object> params = new ArrayList<>();

            sql.append("SELECT DATE_FORMAT(create_time, '%Y-%m') as month, ");
            sql.append("COUNT(*) as count ");
            sql.append("FROM achievement ");
            sql.append("WHERE 1=1 ");

            // 添加日期筛选条件
            if (startDate != null && !startDate.isEmpty()) {
                sql.append("AND create_time >= ? ");
                params.add(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql.append("AND create_time <= ? ");
                params.add(endDate);
            }

            sql.append("GROUP BY month ");
            sql.append("ORDER BY month ASC");

            // 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString(), params.toArray());

            // 准备返回数据
            List<String> months = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();

            // 填充实际统计数据
            for (Map<String, Object> row : results) {
                String monthStr = (String) row.get("month");
                Integer count = ((Number) row.get("count")).intValue();

                // 转换日期格式为月显示（例如 "2024-01" -> "1月"）
                String month = monthStr.substring(5) + "月";
                months.add(month);
                counts.add(count);
            }

            // 设置返回结果
            result.put("months", months.toArray(new String[0]));
            result.put("counts", counts.stream().mapToInt(Integer::intValue).toArray());

            System.out.println("成果时间趋势数据查询成功，共" + months.size() + "个月份");

        } catch (Exception e) {
            System.out.println("获取成果时间趋势失败：" + e);
            // 异常情况下返回默认数据，避免前端报错
            result.put("months", new String[] {"1月", "2月", "3月", "4月", "5月", "6月"});
            result.put("counts", new int[] {0, 0, 0, 0, 0, 0});
        }
        return result;
    }

    @Override
    public Map<String, Object> getActivityParticipationRateBarChart() {
        // 获取活动参与率统计数据（柱状图）
        Map<String, Object> result = new HashMap<>();
        List<String> activityNames = new ArrayList<>();
        List<Double> participationRates = new ArrayList<>();

        try {
            System.out.println("开始查询活动参与率柱状图数据...");

            // 查询最近几个活动的参与率数据
            String sql = "SELECT " +
                         "a.title as activity_name, " +
                         "a.max_participants as total_slots, " +
                         "a.start_time, " +
                         "COUNT(ap.id) as participant_count " +
                         "FROM activity a " +
                         "LEFT JOIN activity_participant ap ON a.id = ap.activity_id " +
                         "WHERE a.status = 'COMPLETED' OR a.status = 'PUBLISHED' " +
                         "GROUP BY a.id, a.title, a.max_participants, a.start_time " +
                         "ORDER BY a.start_time DESC " +
                         "LIMIT 10";

            List<Map<String, Object>> activities = jdbcTemplate.queryForList(sql);

            for (Map<String, Object> activity : activities) {
                String name = (String) activity.get("activity_name");
                // 限制活动名称长度，避免图表显示问题
                if (name.length() > 15) {
                    name = name.substring(0, 15) + "...";
                }
                activityNames.add(name);

                Integer totalSlots = (Integer) activity.get("total_slots");
                Long participantCount = activity.get("participant_count") != null ?
                                       ((Number) activity.get("participant_count")).longValue() : 0L;

                // 计算参与率
                double rate = (totalSlots != null && totalSlots > 0) ?
                             Math.round((participantCount * 100.0 / totalSlots) * 10) / 10.0 : 0.0;
                participationRates.add(rate);
            }

            // 转换为数组格式
            result.put("activities", activityNames.toArray(new String[0]));
            result.put("participationRates", participationRates.stream().mapToDouble(Double::doubleValue).toArray());

            System.out.println("活动参与率柱状图数据查询成功，共" + activities.size() + "条记录");

        } catch (Exception e) {
            System.out.println("获取活动参与率柱状图数据失败：" + e);
            // 异常情况下返回默认数据，避免前端报错
            result.put("activities", new String[] {});
            result.put("participationRates", new double[] {});
        }

        return result;
    }

    @Override
    public Map<String, Object> getActivityParticipationRatePieChart() {
        // 获取活动参与率统计数据（饼图）
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("开始查询活动参与率饼图数据...");

            // 查询已发布的活动总数和相关参与信息
            String sql = "SELECT " +
                         "SUM(CASE WHEN ap.status = 'ATTENDED' THEN 1 ELSE 0 END) as attended, " +
                         "SUM(CASE WHEN ap.status = 'REGISTERED' THEN 1 ELSE 0 END) as registered, " +
                         "SUM(CASE WHEN ap.status = 'ABSENT' THEN 1 ELSE 0 END) as absent " +
                         "FROM activity a " +
                         "LEFT JOIN activity_participant ap ON a.id = ap.activity_id " +
                         "WHERE (a.status = 'COMPLETED' OR a.status = 'PUBLISHED') " +
                         "AND a.start_time <= NOW()";


            Map<String, Object> counts = jdbcTemplate.queryForMap(sql);

            // 设置返回结果
            result.put("labels", new String[] {"已参与", "已报名", "缺席"});

            Long attended = counts.get("attended") != null ? ((Number) counts.get("attended")).longValue() : 0L;
            Long registered = counts.get("registered") != null ? ((Number) counts.get("registered")).longValue() : 0L;
            Long absent = counts.get("absent") != null ? ((Number) counts.get("absent")).longValue() : 0L;

            result.put("values", new long[] {attended, registered, absent});

            System.out.println("活动参与率饼图数据查询成功");

        } catch (Exception e) {
            System.out.println("获取活动参与率饼图数据失败：" + e);
            // 异常情况下返回默认数据，避免前端报错
            result.put("labels", new String[] {"已参与", "已报名", "缺席"});
            result.put("values", new long[] {0, 0, 0});
        }

        return result;
    }

    @Override
    public Map<String, Object> getAchievementDistributionByMajor() {
        // 获取成果分布数据（按专业）
        Map<String, Object> result = new HashMap<>();
        List<String> majors = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        try {
            System.out.println("开始查询成果按专业分布数据...");

            // 联表查询成果和用户信息，按专业分组统计
            String sql = "SELECT u.major as major_name, COUNT(*) as achievement_count " +
                         "FROM achievement a JOIN user u ON a.submitter_id = u.id " +
                         "GROUP BY u.major " +
                         "ORDER BY achievement_count DESC";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            for (Map<String, Object> row : results) {
                String majorName = (String) row.get("major_name");
                if (majorName != null && !majorName.isEmpty()) {
                    majors.add(majorName);
                    counts.add(((Number) row.get("achievement_count")).intValue());
                }
            }

            System.out.println("成果按专业分布查询成功，共" + majors.size() + "个专业");

        } catch (Exception e) {
            System.out.println("获取成果按专业分布失败：" + e);
        }

        result.put("majors", majors.toArray(new String[0]));
        result.put("counts", counts.stream().mapToInt(Integer::intValue).toArray());
        return result;
    }

    @Override
    public Map<String, Object> getAchievementDistributionByDepartment() {
        // 获取成果分布数据（按学院）
        Map<String, Object> result = new HashMap<>();
        List<String> departments = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        try {
            System.out.println("开始查询成果按学院分布数据...");

            // 联表查询成果和用户信息，按学院分组统计
            String sql = "SELECT u.department as department_name, COUNT(*) as achievement_count " +
                         "FROM achievement a JOIN user u ON a.submitter_id = u.id " +
                         "GROUP BY u.department " +
                         "ORDER BY achievement_count DESC";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            for (Map<String, Object> row : results) {
                String deptName = (String) row.get("department_name");
                if (deptName != null && !deptName.isEmpty()) {
                    departments.add(deptName);
                    counts.add(((Number) row.get("achievement_count")).intValue());
                }
            }

            System.out.println("成果按学院分布查询成功，共" + departments.size() + "个学院");

        } catch (Exception e) {
            System.out.println("获取成果按学院分布失败：" + e);
        }

        result.put("departments", departments.toArray(new String[0]));
        result.put("counts", counts.stream().mapToInt(Integer::intValue).toArray());
        return result;
    }

    @Override
    public Map<String, Object> getAchievementDistributionByGender() {
        // 获取成果分布数据（按性别）
        Map<String, Object> result = new HashMap<>();
        List<String> genders = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        try {
            System.out.println("开始查询成果按性别分布数据...");

            // 联表查询成果和用户信息，按性别分组统计
            String sql = "SELECT u.gender, COUNT(*) as achievement_count " +
                         "FROM achievement a JOIN user u ON a.submitter_id = u.id " +
                         "GROUP BY u.gender";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            // 创建性别到数量的映射，方便处理
            Map<String, Integer> genderCountMap = new HashMap<>();
            genderCountMap.put("男", 0);
            genderCountMap.put("女", 0);
            genderCountMap.put("未知", 0);

            // 填充实际统计数据
            for (Map<String, Object> row : results) {
                String gender = (String) row.get("gender");
                if (gender != null) {
                    if (gender.equals("男") || gender.equals("女")) {
                        genderCountMap.put(gender, ((Number) row.get("achievement_count")).intValue());
                    } else {
                        genderCountMap.put("未知", genderCountMap.get("未知") +
                                          ((Number) row.get("achievement_count")).intValue());
                    }
                } else {
                    genderCountMap.put("未知", genderCountMap.get("未知") +
                                      ((Number) row.get("achievement_count")).intValue());
                }
            }

            // 按固定顺序添加数据，确保前端显示一致
            genders.add("男");
            genders.add("女");
            genders.add("未知");

            counts.add(genderCountMap.get("男"));
            counts.add(genderCountMap.get("女"));
            counts.add(genderCountMap.get("未知"));

            System.out.println("成果按性别分布查询成功");

        } catch (Exception e) {
            System.out.println("获取成果按性别分布失败：" + e);
        }

        result.put("genders", genders.toArray(new String[0]));
        result.put("counts", counts.stream().mapToInt(Integer::intValue).toArray());
        return result;
    }

    @Override
    public Map<String, Object> getAchievementApprovalStatusDistribution() {
        // 获取成果审核状态分布数据（仪表盘展示）
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("开始查询成果审核状态分布数据...");

            // 查询不同状态的成果数量
            String sql = "SELECT status, COUNT(*) as count FROM achievement GROUP BY status";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            // 初始化各状态计数
            long pending = 0;
            long approved = 0;
            long rejected = 0;

            // 统计各状态数量
            for (Map<String, Object> row : results) {
                String status = (String) row.get("status");
                long count = ((Number) row.get("count")).longValue();

                switch (status) {
                    case "PENDING":
                        pending = count;
                        break;
                    case "APPROVED":
                        approved = count;
                        break;
                    case "REJECTED":
                        rejected = count;
                        break;
                }
            }

            // 计算总数
            long total = pending + approved + rejected;

            // 设置返回结果
            result.put("pending", pending);
            result.put("approved", approved);
            result.put("rejected", rejected);
            result.put("total", total);

            System.out.println("成果审核状态分布查询成功，待审核：" + pending + ", 已通过：" + approved + ", 已拒绝：" + rejected);

        } catch (Exception e) {
            System.out.println("获取成果审核状态分布失败：" + e);
            // 异常情况下设置默认值
            result.put("pending", 0);
            result.put("approved", 0);
            result.put("rejected", 0);
            result.put("total", 0);
        }

        return result;
    }

    @Override
    public Map<String, Object> getUserActivityStatistics() {
        // 获取用户活跃度统计数据
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("开始查询用户活跃度统计数据...");

            // 查询总用户数
            String totalUsersSql = "SELECT COUNT(*) FROM user";
            long totalUsers = jdbcTemplate.queryForObject(totalUsersSql, Long.class);

            // 查询活跃用户数（假设一个月内有活动的用户为活跃用户）
            String activeUsersSql = "SELECT COUNT(DISTINCT a.submitter_id) " +
                                  "FROM achievement a " +
                                  "WHERE a.create_time > DATE_SUB(NOW(), INTERVAL 1 MONTH)";
            long activeUsers = jdbcTemplate.queryForObject(activeUsersSql, Long.class);

            // 查询活跃学生数
            String activeStudentsSql = "SELECT COUNT(DISTINCT a.submitter_id) " +
                                     "FROM achievement a JOIN user u ON a.submitter_id = u.id " +
                                     "WHERE a.create_time > DATE_SUB(NOW(), INTERVAL 1 MONTH) " +
                                     "AND u.role = 'STUDENT'";
            long activeStudents = jdbcTemplate.queryForObject(activeStudentsSql, Long.class);

            // 查询活跃教师数
            String activeTeachersSql = "SELECT COUNT(DISTINCT a.submitter_id) " +
                                     "FROM achievement a JOIN user u ON a.submitter_id = u.id " +
                                     "WHERE a.create_time > DATE_SUB(NOW(), INTERVAL 1 MONTH) " +
                                     "AND u.role = 'TEACHER'";
            long activeTeachers = jdbcTemplate.queryForObject(activeTeachersSql, Long.class);

            // 计算活跃度
            double activityRate = totalUsers > 0 ? Math.round((activeUsers * 100.0 / totalUsers) * 10) / 10.0 : 0.0;

            // 设置返回结果
            result.put("activeUsers", activeUsers);
            result.put("totalUsers", totalUsers);
            result.put("activityRate", activityRate);
            result.put("activeStudents", activeStudents);
            result.put("activeTeachers", activeTeachers);

            System.out.println("用户活跃度统计数据查询成功，活跃用户数：" + activeUsers + ", 总用户数：" + totalUsers);

        } catch (Exception e) {
            System.out.println("获取用户活跃度统计数据失败：" + e);
            // 异常情况下设置默认值
            result.put("activeUsers", 0);
            result.put("totalUsers", 0);
            result.put("activityRate", 0.0);
            result.put("activeStudents", 0);
            result.put("activeTeachers", 0);
        }

        return result;
    }

    @Override
    public Map<String, Object> getSystemOverviewData() {
        // 获取系统整体运营数据概览
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("开始查询系统概览数据...");

            // 查询总用户数
            String totalUsersSql = "SELECT COUNT(*) FROM user";
            long totalUsers = jdbcTemplate.queryForObject(totalUsersSql, Long.class);

            // 查询总活动数
            String totalActivitiesSql = "SELECT COUNT(*) FROM activity";
            long totalActivities = jdbcTemplate.queryForObject(totalActivitiesSql, Long.class);

            // 查询总成果数
            String totalAchievementsSql = "SELECT COUNT(*) FROM achievement";
            long totalAchievements = jdbcTemplate.queryForObject(totalAchievementsSql, Long.class);

            // 查询总项目数
            String totalProjectsSql = "SELECT COUNT(*) FROM project";
            long totalProjects = jdbcTemplate.queryForObject(totalProjectsSql, Long.class);

            // 查询待审核数量
            String pendingApprovalsSql = "SELECT COUNT(*) FROM achievement WHERE status = 'PENDING'";
            long pendingApprovals = jdbcTemplate.queryForObject(pendingApprovalsSql, Long.class);

            // 查询今日活跃用户数（假设今日有登录记录的为活跃用户）
            String activeTodaySql = "SELECT COUNT(DISTINCT user_id) FROM login_log WHERE login_time >= CURDATE()";
            long activeToday = jdbcTemplate.queryForObject(activeTodaySql, Long.class);

            // 查询本周新增用户数
            String weeklyNewUsersSql = "SELECT COUNT(*) FROM user WHERE create_time > DATE_SUB(NOW(), INTERVAL 7 DAY)";
            long weeklyNewUsers = jdbcTemplate.queryForObject(weeklyNewUsersSql, Long.class);

            // 查询本周新增项目数
            String weeklyNewProjectsSql = "SELECT COUNT(*) FROM project WHERE create_time > DATE_SUB(NOW(), INTERVAL 7 DAY)";
            long weeklyNewProjects = jdbcTemplate.queryForObject(weeklyNewProjectsSql, Long.class);

            // 查询本周新增成果数
            String weeklyNewAchievementsSql = "SELECT COUNT(*) FROM achievement WHERE create_time > DATE_SUB(NOW(), INTERVAL 7 DAY)";
            long weeklyNewAchievements = jdbcTemplate.queryForObject(weeklyNewAchievementsSql, Long.class);

            // 设置返回结果
            result.put("totalUsers", totalUsers);
            result.put("totalActivities", totalActivities);
            result.put("totalAchievements", totalAchievements);
            result.put("totalProjects", totalProjects);
            result.put("pendingApprovals", pendingApprovals);
            result.put("activeToday", activeToday);
            result.put("weeklyNewUsers", weeklyNewUsers);
            result.put("weeklyNewProjects", weeklyNewProjects);
            result.put("weeklyNewAchievements", weeklyNewAchievements);

            System.out.println("系统概览数据查询成功，总用户数：" + totalUsers + ", 总活动数：" + totalActivities + ", 总成果数：" + totalAchievements);

        } catch (Exception e) {
            System.out.println("获取系统概览数据失败：" + e);
            // 异常情况下设置默认值
            result.put("totalUsers", 0);
            result.put("totalActivities", 0);
            result.put("totalAchievements", 0);
            result.put("totalProjects", 0);
            result.put("pendingApprovals", 0);
            result.put("activeToday", 0);
            result.put("weeklyNewUsers", 0);
            result.put("weeklyNewProjects", 0);
            result.put("weeklyNewAchievements", 0);
        }

        return result;
    }

    @Override
    public StatisticsService.SystemOverview getSystemOverview() {
        StatisticsService.SystemOverview overview = new StatisticsService.SystemOverview();

        try {
            // 1. 查询总用户数
            String totalUsersSql = "SELECT COUNT(*) FROM user";
            Long totalUsers = jdbcTemplate.queryForObject(totalUsersSql, Long.class);
            overview.setTotalUsers(totalUsers);
            System.out.println("总用户数：" + totalUsers);

            // 2. 查询总项目数
            String totalProjectsSql = "SELECT COUNT(*) FROM project";
            Long totalProjects = jdbcTemplate.queryForObject(totalProjectsSql, Long.class);
            overview.setTotalProjects(totalProjects);
            System.out.println("总项目数：" + totalProjects);

            // 3. 查询总活动数
            String totalActivitiesSql = "SELECT COUNT(*) FROM activity";
            Long totalActivities = jdbcTemplate.queryForObject(totalActivitiesSql, Long.class);
            overview.setTotalActivities(totalActivities);
            System.out.println("总活动数：" + totalActivities);

            // 4. 查询总成果数
            String totalAchievementsSql = "SELECT COUNT(*) FROM achievement";
            Long totalAchievements = jdbcTemplate.queryForObject(totalAchievementsSql, Long.class);
            overview.setTotalAchievements(totalAchievements);
            System.out.println("总成果数：" + totalAchievements);

            // 5. 查询待审核数量
            String pendingApprovalsSql = "SELECT COUNT(*) FROM achievement WHERE status = 'PENDING'";
            Long pendingApprovals = jdbcTemplate.queryForObject(pendingApprovalsSql, Long.class);
            overview.setPendingApprovals(pendingApprovals);
            System.out.println("待审核数量：" + pendingApprovals);

            // 7-9. 其他统计数据仅查询并打印，不设置到对象中
            String weeklyNewUsersSql = "SELECT COUNT(*) FROM user WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            Long weeklyNewUsers = jdbcTemplate.queryForObject(weeklyNewUsersSql, Long.class);
            System.out.println("本周新增用户数：" + weeklyNewUsers);

            String weeklyNewProjectsSql = "SELECT COUNT(*) FROM project WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            Long weeklyNewProjects = jdbcTemplate.queryForObject(weeklyNewProjectsSql, Long.class);
            System.out.println("本周新增项目数：" + weeklyNewProjects);

            String weeklyNewAchievementsSql = "SELECT COUNT(*) FROM achievement WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            Long weeklyNewAchievements = jdbcTemplate.queryForObject(weeklyNewAchievementsSql, Long.class);
            System.out.println("本周新增成果数：" + weeklyNewAchievements);

        } catch (Exception e) {
            System.out.println("获取系统概览数据失败：" + e);
            // 设置默认值，确保即使setter不存在也不会导致异常
            try {
                overview.setTotalUsers(0L);
                overview.setTotalProjects(0L);
                overview.setTotalActivities(0L);
                overview.setTotalAchievements(0L);
                overview.setPendingApprovals(0L);
                try {
                    overview.setActiveToday(0L);
                } catch (NoSuchMethodError ignored) {}
            } catch (Exception ex) {
                System.out.println("设置默认值失败：" + ex);
            }
        }

        return overview;
    }

    @Override
    public List<TrendData> getAchievementTrend(String startDate, String endDate, String type, String period) {
        // 获取成果趋势数据
        List<TrendData> trendDataList = new ArrayList<>();

        try {
            System.out.println("开始查询成果趋势数据...");

            // 构建查询SQL
            StringBuilder sql = new StringBuilder();
            List<Object> params = new ArrayList<>();

            // 根据period参数确定日期格式化方式
            String dateFormat;
            if ("month".equals(period)) {
                dateFormat = "DATE_FORMAT(create_time, '%Y-%m')";
            } else if ("week".equals(period)) {
                dateFormat = "DATE_FORMAT(create_time, '%Y-%U')";
            } else {
                dateFormat = "DATE_FORMAT(create_time, '%Y')";
            }

            sql.append("SELECT ").append(dateFormat).append(" as date, ");
            sql.append("type, COUNT(*) as count ");
            sql.append("FROM achievement ");
            sql.append("WHERE 1=1 ");

            // 添加日期筛选条件
            if (startDate != null && !startDate.isEmpty()) {
                sql.append("AND create_time >= ? ");
                params.add(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql.append("AND create_time <= ? ");
                params.add(endDate);
            }

            // 添加类型筛选条件
            if (type != null && !type.isEmpty()) {
                sql.append("AND type = ? ");
                params.add(type);
            }

            sql.append("GROUP BY date, type ");
            sql.append("ORDER BY date ASC, type ASC");

            // 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString(), params.toArray());

            // 转换结果
            for (Map<String, Object> row : results) {
                TrendData data = new TrendData();
                // 处理不同时间格式的显示
                String dateStr = (String) row.get("date");
                if ("week".equals(period)) {
                    // 将年-周格式转换为更友好的格式
                    String[] parts = dateStr.split("-");
                    data.setDate(parts[0] + "年第" + parts[1] + "周");
                } else {
                    data.setDate(dateStr);
                }
                data.setType((String) row.get("type"));
                data.setCount(((Number) row.get("count")).longValue());
                trendDataList.add(data);
            }

            System.out.println("成果趋势数据查询成功，共" + trendDataList.size() + "条记录");

        } catch (Exception e) {
            System.out.println("获取成果趋势数据失败：" + e);
        }

        return trendDataList;
    }

    @Override
    public List<TrendData> getAchievementTrendByGrade(String startDate, String endDate, String type, String period) {
        // 获取不同年级的成果趋势
        List<TrendData> trendDataList = new ArrayList<>();

        try {
            System.out.println("开始查询不同年级的成果趋势...");

            // 构建SQL查询
            StringBuilder sql = new StringBuilder();
            List<Object> params = new ArrayList<>();

            // 根据period参数确定日期格式化方式
            String dateFormat;
            if ("month".equals(period)) {
                dateFormat = "DATE_FORMAT(a.create_time, '%Y-%m')";
            } else if ("week".equals(period)) {
                dateFormat = "DATE_FORMAT(a.create_time, '%Y-%U')";
            } else {
                dateFormat = "DATE_FORMAT(a.create_time, '%Y')";
            }

            // 联表查询成果和用户年级信息
            sql.append("SELECT ").append(dateFormat).append(" as date, ");
            sql.append("a.type, u.grade, COUNT(*) as count ");
            sql.append("FROM achievement a ");
            sql.append("JOIN user u ON a.submitter_id = u.id ");
            sql.append("WHERE 1=1 ");

            // 添加日期筛选条件
            if (startDate != null && !startDate.isEmpty()) {
                sql.append("AND a.create_time >= ? ");
                params.add(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql.append("AND a.create_time <= ? ");
                params.add(endDate);
            }

            // 添加类型筛选条件
            if (type != null && !type.isEmpty()) {
                sql.append("AND a.type = ? ");
                params.add(type);
            }

            sql.append("GROUP BY date, a.type, u.grade ");
            sql.append("ORDER BY date ASC, a.type ASC, u.grade ASC");

            // 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString(), params.toArray());

            // 转换结果
            for (Map<String, Object> row : results) {
                TrendData data = new TrendData();

                // 处理日期格式
                String dateStr = (String) row.get("date");
                if ("week".equals(period)) {
                    String[] parts = dateStr.split("-");
                    data.setDate(parts[0] + "年第" + parts[1] + "周");
                } else {
                    data.setDate(dateStr);
                }

                data.setType((String) row.get("type"));
                data.setGrade((String) row.get("grade"));
                System.out.println("年级信息：" + row.get("grade") + "，类型：" + row.get("type") + "，数量：" + ((Number) row.get("count")).longValue());
                data.setCount(((Number) row.get("count")).longValue());
                trendDataList.add(data);
            }

            System.out.println("年级成果趋势查询成功，共" + trendDataList.size() + "条记录");

        } catch (Exception e) {
            System.out.println("获取年级成果趋势失败：" + e);
        }

        return trendDataList;
    }

    @Override
    public List<ParticipationData> getActivityParticipation(String startDate, String endDate, String activityType) {
        // 获取活动参与率统计数据
        List<ParticipationData> participationDataList = new ArrayList<>();

        try {
            log.info("开始查询活动参与率统计数据，参数: startDate={}, endDate={}, activityType={}",
                     startDate, endDate, activityType);

            // 使用联表查询优化性能，避免嵌套查询
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT a.id, a.title, a.type, a.max_participants, a.current_participants, a.start_time, ");
            sql.append("COUNT(ap.id) as participant_count ");
            sql.append("FROM activity a ");
            sql.append("LEFT JOIN activity_participant ap ON a.id = ap.activity_id ");
            sql.append("WHERE a.status = 'PUBLISHED' OR a.status = 'COMPLETED' ");

            List<Object> params = new ArrayList<>();

            // 按日期筛选
            if (startDate != null && !startDate.isEmpty()) {
                sql.append("AND a.start_time >= ? ");
                try {
                    // 尝试解析日期格式，确保兼容性
                    LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
                    params.add(startDateTime);
                } catch (Exception e) {
                    params.add(startDate);
                }
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql.append("AND a.start_time <= ? ");
                try {
                    // 尝试解析日期格式，确保兼容性
                    LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
                    params.add(endDateTime);
                } catch (Exception e) {
                    params.add(endDate);
                }
            }

            // 按活动类型筛选
            if (activityType != null && !activityType.isEmpty()) {
                sql.append("AND a.type = ? ");
                params.add(activityType);
            }

            // 分组并排序
            sql.append("GROUP BY a.id, a.title, a.type, a.max_participants, a.current_participants, a.start_time ");
            sql.append("ORDER BY a.start_time DESC LIMIT 20");

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString(), params.toArray());

            for (Map<String, Object> result : results) {
                ParticipationData data = new ParticipationData();
                data.setActivityName((String) result.get("title"));
                data.setActivityType((String) result.get("type"));

                // 获取活动总名额
                Integer maxParticipants = (Integer) result.get("max_participants");
                Long totalSlots = maxParticipants != null ? maxParticipants.longValue() : 0L;
                data.setTotalSlots(totalSlots);

                // 获取实际参与人数
                Long totalParticipants = ((Number) result.get("participant_count")).longValue();
                data.setTotalParticipants(totalParticipants);

                // 计算参与率
                double participationRate = totalSlots > 0 ?
                        Math.round((totalParticipants * 100.0 / totalSlots) * 10) / 10.0 : 0.0;
                data.setParticipationRate(participationRate);

                // 设置活动日期
                Object startTimeObj = result.get("start_time");
                if (startTimeObj instanceof LocalDateTime) {
                    data.setDate(((LocalDateTime) startTimeObj).toLocalDate().toString());
                } else if (startTimeObj != null) {
                    data.setDate(startTimeObj.toString().split(" ")[0]);
                } else {
                    data.setDate("-");
                }

                participationDataList.add(data);
            }

            log.info("活动参与率统计数据查询成功，共{}条记录", participationDataList.size());

        } catch (Exception e) {
            log.error("获取活动参与率统计数据失败", e);
        }

        return participationDataList;
    }

    @Override
    public Map<String, List<DistributionData>> getAchievementDistribution(String startDate, String endDate, String dimension) {
        System.out.println("开始获取成果分布数据，参数：startDate=" + startDate + ", endDate=" + endDate + ", dimension=" + dimension);
        Map<String, List<DistributionData>> result = new HashMap<>();

        // 根据dimension参数选择维度（默认为按类型）
        String actualDimension = (dimension != null && !dimension.isEmpty()) ? dimension : "type";
        List<DistributionData> distributionList = new ArrayList<>();

        try {
            StringBuilder sql = new StringBuilder();
            List<Object> params = new ArrayList<>();

            if ("type".equals(actualDimension)) {
                // 按类型分布
                sql.append("SELECT type as category, COUNT(*) as count ");
                sql.append("FROM achievement ");
                sql.append("WHERE create_time BETWEEN ? AND ? ");
                sql.append("GROUP BY type ");
                sql.append("ORDER BY count DESC");
                params.add(startDate);
                params.add(endDate);
            } else if ("department".equals(actualDimension)) {
                // 按学院分布 - 关联user表
                sql.append("SELECT u.department as category, COUNT(*) as count ");
                sql.append("FROM achievement a JOIN user u ON a.submitter_id = u.id ");
                sql.append("WHERE a.create_time BETWEEN ? AND ? ");
                sql.append("GROUP BY u.department ");
                sql.append("ORDER BY count DESC");
                params.add(startDate);
                params.add(endDate);
            } else if ("major".equals(actualDimension)) {
                // 按专业分布 - 关联user表
                sql.append("SELECT u.major as category, COUNT(*) as count ");
                sql.append("FROM achievement a JOIN user u ON a.submitter_id = u.id ");
                sql.append("WHERE a.create_time BETWEEN ? AND ? ");
                sql.append("GROUP BY u.major ");
                sql.append("ORDER BY count DESC");
                params.add(startDate);
                params.add(endDate);
            }

            if (sql.length() > 0) {
                // 执行查询
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());

                // 计算总数
                long total = 0;
                for (Map<String, Object> row : rows) {
                    total += (Long) row.get("count");
                }

                // 转换为DistributionData对象并计算百分比
                for (Map<String, Object> row : rows) {
                    DistributionData data = new DistributionData();
                    data.setCategory((String) row.get("category"));
                    data.setCount((Long) row.get("count"));
                    // 避免除以零的情况
                    double percentage = total > 0 ? (double) Math.round(((Long) row.get("count") * 100.0 / total) * 10) / 10 : 0;
                    data.setPercentage(percentage);
                    distributionList.add(data);
                }

                System.out.println("成果分布数据查询成功，共" + distributionList.size() + "条记录");
            }
        } catch (Exception e) {
            System.out.println("获取成果分布数据失败：" + e);
        }

        result.put(actualDimension, distributionList);
        return result;
    }

    @Override
    public List<StatusData> getApprovalStatusDistribution(String startDate, String endDate, String type) {
        System.out.println("开始获取成果审核状态分布数据，参数：startDate=" + startDate + ", endDate=" + endDate + ", type=" + type);
        List<StatusData> statusDataList = new ArrayList<>();

        try {
            StringBuilder sql = new StringBuilder();
            List<Object> params = new ArrayList<>();

            // 构建SQL查询
            sql.append("SELECT status, COUNT(*) as count ");
            sql.append("FROM achievement ");
            sql.append("WHERE create_time BETWEEN ? AND ? ");
            params.add(startDate);
            params.add(endDate);

            // 添加类型筛选条件
            if (type != null && !type.isEmpty()) {
                sql.append("AND type = ? ");
                params.add(type);
            }

            sql.append("GROUP BY status ");
            sql.append("ORDER BY FIELD(status, 'PENDING', 'APPROVED', 'REJECTED')");

            // 执行查询
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());

            // 计算总数
            long total = 0;
            for (Map<String, Object> row : rows) {
                total += (Long) row.get("count");
            }

            // 转换为StatusData对象并计算百分比
            for (Map<String, Object> row : rows) {
                StatusData data = new StatusData();
                // 转换状态码为中文显示
                String statusCode = (String) row.get("status");
                String statusName;
                switch (statusCode) {
                    case "PENDING":
                        statusName = "待审核";
                        break;
                    case "APPROVED":
                        statusName = "审核通过";
                        break;
                    case "REJECTED":
                        statusName = "审核拒绝";
                        break;
                    default:
                        statusName = statusCode;
                }
                data.setStatus(statusName);
                data.setCount((Long) row.get("count"));
                // 避免除以零的情况
                double percentage = total > 0 ? (double) Math.round(((Long) row.get("count") * 100.0 / total) * 10) / 10 : 0;
                data.setPercentage(percentage);
                statusDataList.add(data);
            }

            System.out.println("成果审核状态分布数据查询成功，共" + statusDataList.size() + "条记录");
        } catch (Exception e) {
            System.out.println("获取成果审核状态分布数据失败：" + e);
        }

        return statusDataList;
    }

    @Override
    public List<UserActivityData> getUserActivity(String startDate, String endDate, String role) {
        // 获取用户活跃度数据
        List<UserActivityData> userActivityDataList = new ArrayList<>();
        try {
            System.out.println("开始查询用户活跃度数据...");

            // 构建查询SQL
            StringBuilder sql = new StringBuilder();
            List<Object> params = new ArrayList<>();

            sql.append("SELECT u.id as userId, u.username, u.role, ");
            sql.append("COUNT(ap.id) as activityCount, ");
            sql.append("MAX(ap.join_time) as lastActiveDate ");
            sql.append("FROM user u ");
            sql.append("LEFT JOIN activity_participant ap ON u.id = ap.user_id ");
            sql.append("LEFT JOIN activity a ON ap.activity_id = a.id ");
            sql.append("WHERE 1=1 ");

            // 添加日期筛选条件
            if (startDate != null && !startDate.isEmpty()) {
                sql.append("AND ap.join_time >= ? ");
                params.add(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql.append("AND ap.join_time <= ? ");
                params.add(endDate);
            }

            // 添加角色筛选条件
            if (role != null && !role.isEmpty()) {
                sql.append("AND u.role = ? ");
                params.add(role);
            }

            sql.append("GROUP BY u.id, u.username, u.role ");
            sql.append("ORDER BY activityCount DESC ");
            sql.append("LIMIT 20"); // 限制返回前20个最活跃的用户

            // 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString(), params.toArray());

            // 填充实际统计数据
            for (Map<String, Object> row : results) {
                UserActivityData data = new UserActivityData();
                data.setUserId((String) row.get("userId"));
                data.setUsername((String) row.get("username"));
                data.setRole((String) row.get("role"));
                data.setActivityCount((Long) row.get("activityCount"));

                // 处理最近活动日期
                Object lastActiveObj = row.get("lastActiveDate");
                if (lastActiveObj != null) {
                    // 如果是Timestamp类型，转换为字符串
                    if (lastActiveObj instanceof java.sql.Timestamp) {
                        java.sql.Timestamp timestamp = (java.sql.Timestamp) lastActiveObj;
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        data.setLastActiveDate(sdf.format(timestamp));
                    } else {
                        data.setLastActiveDate(String.valueOf(lastActiveObj));
                    }
                } else {
                    data.setLastActiveDate("无活动记录");
                }

                userActivityDataList.add(data);
            }

            System.out.println("用户活跃度数据查询成功，共" + userActivityDataList.size() + "条记录");

        } catch (Exception e) {
            System.out.println("获取用户活跃度数据失败：" + e);
            // 异常情况下返回空列表
        }

        return userActivityDataList;
    }

    @Override
    public long getTotalUsers() {
        // 获取总用户数
        try {
            String sql = "SELECT COUNT(*) FROM user";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            System.out.println("获取总用户数失败：" + e);
            return 0;
        }
    }

    @Override
    public long getTotalProjects() {
        // 获取项目总数
        try {
            String sql = "SELECT COUNT(*) FROM project";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            System.out.println("获取项目总数失败：" + e);
            return 0;
        }
    }

    @Override
    public Map<String, Object> getProjectsByStatus() {
        // 按状态统计项目
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("开始查询项目状态分布数据...");

            // 查询不同状态的项目数量
            String sql = "SELECT status, COUNT(*) as count FROM project GROUP BY status";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            // 初始化各状态计数
            Map<String, Long> statusCountMap = new HashMap<>();
            statusCountMap.put("PLANNING", 0L);
            statusCountMap.put("ONGOING", 0L);
            statusCountMap.put("COMPLETED", 0L);
            statusCountMap.put("CANCELLED", 0L);

            // 填充实际统计数据
            for (Map<String, Object> row : results) {
                String status = (String) row.get("status");
                Long count = ((Number) row.get("count")).longValue();
                statusCountMap.put(status, count);
            }

            // 设置返回结果，按照前端需要的顺序和中文显示名称
            result.put("statuses", new String[] {"进行中", "已完成", "已暂停", "计划中"});
            result.put("counts", new long[] {
                statusCountMap.get("ONGOING"),  // 进行中
                statusCountMap.get("COMPLETED"), // 已完成
                statusCountMap.get("CANCELLED"), // 已暂停（映射为已取消）
                statusCountMap.get("PLANNING")  // 计划中（映射为规划中）
            });

            System.out.println("项目状态分布数据查询成功");

        } catch (Exception e) {
            System.out.println("获取项目状态分布失败：" + e);
            // 异常情况下返回默认数据，避免前端报错
            result.put("statuses", new String[] {"进行中", "已完成", "已暂停", "计划中"});
            result.put("counts", new long[] {0, 0, 0, 0});
        }
        return result;
    }

    @Override
    public Map<String, Object> getProjectsByType() {
        // 按类型统计项目
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("开始查询项目类型分布数据...");

            // 查询不同类型的项目数量
            String sql = "SELECT type, COUNT(*) as count FROM project GROUP BY type";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            // 初始化类型到中文名称的映射
            Map<String, String> typeNameMap = new HashMap<>();
            typeNameMap.put("RESEARCH", "科研项目");
            typeNameMap.put("INNOVATION", "创新项目");
            typeNameMap.put("PRACTICE", "实践项目");

            // 准备返回数据
            List<String> types = new ArrayList<>();
            List<Long> counts = new ArrayList<>();

            // 填充实际统计数据
            for (Map<String, Object> row : results) {
                String type = (String) row.get("type");
                Long count = ((Number) row.get("count")).longValue();

                // 只有在映射表中存在的类型才添加到结果中
                if (typeNameMap.containsKey(type)) {
                    types.add(typeNameMap.get(type));
                    counts.add(count);
                }
            }

            // 设置返回结果
            result.put("types", types.toArray(new String[0]));
            result.put("counts", counts.stream().mapToLong(Long::longValue).toArray());

            System.out.println("项目类型分布数据查询成功，共" + types.size() + "种类型");

        } catch (Exception e) {
            System.out.println("获取项目类型分布失败：" + e);
            // 异常情况下返回默认数据，避免前端报错
            result.put("types", new String[] {"科研项目", "教学改革", "竞赛项目", "校企合作"});
            result.put("counts", new long[] {0, 0, 0, 0});
        }
        return result;
    }

    @Override
    public List<TrendData> getProjectTrend(String startDate, String endDate, String period) {
        // 获取项目趋势数据
        List<TrendData> trendDataList = new ArrayList<>();

        try {
            System.out.println("开始查询项目趋势数据，参数: startDate=" + startDate + ", endDate=" + endDate + ", period=" + period);

            // 构建查询SQL
            StringBuilder sql = new StringBuilder();
            List<Object> params = new ArrayList<>();

            // 根据period参数确定日期格式化方式
            String dateFormat;
            if ("month".equals(period)) {
                dateFormat = "DATE_FORMAT(create_time, '%Y-%m')";
            } else if ("week".equals(period)) {
                dateFormat = "DATE_FORMAT(create_time, '%Y-%U')";
            } else {
                dateFormat = "DATE_FORMAT(create_time, '%Y')";
            }

            sql.append("SELECT ").append(dateFormat).append(" as date, ");
            sql.append("COUNT(*) as count ");
            sql.append("FROM project ");
            sql.append("WHERE 1=1 ");

            // 添加日期筛选条件
            if (startDate != null && !startDate.isEmpty()) {
                sql.append("AND create_time >= ? ");
                params.add(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql.append("AND create_time <= ? ");
                params.add(endDate);
            }

            sql.append("GROUP BY date ");
            sql.append("ORDER BY date ASC");

            // 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString(), params.toArray());

            // 转换结果
            for (Map<String, Object> row : results) {
                TrendData data = new TrendData();
                // 处理不同时间格式的显示
                String dateStr = (String) row.get("date");
                if ("week".equals(period)) {
                    // 将年-周格式转换为更友好的格式
                    String[] parts = dateStr.split("-");
                    data.setDate(parts[0] + "年第" + parts[1] + "周");
                } else {
                    data.setDate(dateStr);
                }
                data.setType("项目总数");
                data.setCount(((Number) row.get("count")).longValue());
                trendDataList.add(data);
            }

            System.out.println("项目趋势数据查询成功，共" + trendDataList.size() + "条记录");

        } catch (Exception e) {
            System.out.println("获取项目趋势数据失败：" + e);
            // 异常情况下返回空列表
        }

        return trendDataList;
    }

    @Override
    public long getTotalActivities() {
        // 获取活动总数
        try {
            String sql = "SELECT COUNT(*) FROM activity";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            System.out.println("获取活动总数失败：" + e);
            return 0;
        }
    }

    @Override
    public Map<String, Object> getActivitiesByType() {
        // 按类型统计活动
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("开始查询活动类型分布数据...");

            // 查询不同类型的活动数量
            String sql = "SELECT type, COUNT(*) as count FROM activity GROUP BY type";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            // 准备返回数据
            List<String> types = new ArrayList<>();
            List<Long> counts = new ArrayList<>();

            // 定义类型映射，将数据库中的类型代码映射为中文名称
            Map<String, String> typeNameMap = new HashMap<>();
            typeNameMap.put("TRAINING", "培训活动");
            typeNameMap.put("LECTURE", "学术讲座");
            typeNameMap.put("COMPETITION", "竞赛活动");
            typeNameMap.put("WORKSHOP", "工作坊");
            typeNameMap.put("EXHIBITION", "展览");
            typeNameMap.put("EXCHANGE", "交流活动");
            typeNameMap.put("MEETING", "会议");
            typeNameMap.put("EDITION", "编辑活动"); // 添加缺失的类型映射

            // 确保查询结果存在
            if (results != null && !results.isEmpty()) {
                // 填充实际统计数据
                for (Map<String, Object> row : results) {
                    String type = (String) row.get("type");
                    Long count = ((Number) row.get("count")).longValue();

                    System.out.println("查询到的活动类型: " + type + ", 数量: " + count);

                    // 添加到结果中（使用中文名称）
                    String typeName = typeNameMap.getOrDefault(type, type + "(未分类)");
                    types.add(typeName);
                    counts.add(count);
                }
            } else {
                // 如果没有数据，返回默认数据以确保前端能正常显示
                types.add("培训活动");
                types.add("学术讲座");
                types.add("竞赛活动");
                counts.add(1L);
                counts.add(1L);
                counts.add(1L);
                System.out.println("未查询到活动类型数据，使用默认数据");
            }

            // 设置返回结果
            result.put("types", types.toArray(new String[0]));
            result.put("counts", counts.stream().mapToLong(Long::longValue).toArray());

            System.out.println("活动类型分布数据查询成功，共" + types.size() + "种类型");
            System.out.println("返回的类型名称: " + Arrays.toString((String[])result.get("types")));
            System.out.println("返回的数量: " + Arrays.toString((long[])result.get("counts")));

        } catch (Exception e) {
            System.out.println("获取活动类型分布失败：" + e);
            // 异常情况下返回默认数据，避免前端报错
            result.put("types", new String[] {"学术讲座", "竞赛活动", "培训活动", "会议"});
            result.put("counts", new long[] {0, 0, 0, 0});
        }
        return result;
    }

    @Override
    public long getTotalParticipants() {
        // 获取参与活动总人数
        try {
            String sql = "SELECT COUNT(DISTINCT user_id) FROM activity_participant";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            System.out.println("获取参与活动总人数失败：" + e);
            return 0;
        }
    }

    @Override
    public List<TrendData> getActivityTrend(String startDate, String endDate, String period) {
        // 获取活动趋势数据
        List<TrendData> trendDataList = new ArrayList<>();

        try {
            System.out.println("开始查询活动趋势数据，参数: startDate=" + startDate + ", endDate=" + endDate + ", period=" + period);

            // 构建查询SQL
            StringBuilder sql = new StringBuilder();
            List<Object> params = new ArrayList<>();

            // 根据period参数确定日期格式化方式
            String dateFormat;
            if ("month".equals(period)) {
                dateFormat = "DATE_FORMAT(create_time, '%Y-%m')";
            } else if ("week".equals(period)) {
                dateFormat = "DATE_FORMAT(create_time, '%Y-%U')";
            } else {
                dateFormat = "DATE_FORMAT(create_time, '%Y')";
            }

            sql.append("SELECT ").append(dateFormat).append(" as date, ");
            sql.append("COUNT(*) as count ");
            sql.append("FROM activity ");
            sql.append("WHERE 1=1 ");

            // 添加日期筛选条件
            if (startDate != null && !startDate.isEmpty()) {
                sql.append("AND create_time >= ? ");
                params.add(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql.append("AND create_time <= ? ");
                params.add(endDate);
            }

            sql.append("GROUP BY date ");
            sql.append("ORDER BY date ASC");

            // 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString(), params.toArray());

            // 转换结果
            for (Map<String, Object> row : results) {
                TrendData data = new TrendData();
                // 处理不同时间格式的显示
                String dateStr = (String) row.get("date");
                if ("week".equals(period)) {
                    // 将年-周格式转换为更友好的格式
                    String[] parts = dateStr.split("-");
                    data.setDate(parts[0] + "年第" + parts[1] + "周");
                } else {
                    data.setDate(dateStr);
                }
                data.setType("活动总数");
                data.setCount(((Number) row.get("count")).longValue());
                trendDataList.add(data);
            }

            System.out.println("活动趋势数据查询成功，共" + trendDataList.size() + "条记录");

        } catch (Exception e) {
            System.out.println("获取活动趋势数据失败：" + e);
            // 异常情况下返回空列表
        }

        return trendDataList;
    }
}
