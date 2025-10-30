package com.academic.service.impl;

import com.academic.entity.Activity;
import com.academic.entity.ActivityParticipant;
import com.academic.entity.User;
import com.academic.mapper.ActivityMapper;
import com.academic.mapper.ActivityParticipantMapper;
import com.academic.mapper.UserMapper;
import com.academic.service.ActivityService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityServiceImpl.class);

    @Resource
    private ActivityParticipantMapper activityParticipantMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public List<Activity> getAllActivities() {
        return list();
    }

    @Override
    public Activity getActivityById(Long id) {
        return getById(id);
    }

    /**
     * 获取活动详情并检查用户是否已报名
     * @param id 活动ID
     * @param userId 用户ID
     * @return 包含报名状态的活动信息Map
     */
    public java.util.Map<String, Object> getActivityDetailWithRegistrationStatus(Long id, Long userId) {
        Activity activity = getById(id);
        if (activity == null) {
            return null;
        }

        // 创建Map来存储活动信息和报名状态
        java.util.Map<String, Object> result = new java.util.HashMap<>();

        // 将Activity对象的所有属性复制到Map中
        try {
            java.lang.reflect.Field[] fields = Activity.class.getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                result.put(field.getName(), field.get(activity));
            }
        } catch (Exception e) {
            log.error("复制活动属性失败", e);
        }

        // 检查用户是否已报名
        if (userId != null) {
            ActivityParticipant participant = activityParticipantMapper.selectByActivityAndUser(id, userId);
            result.put("hasRegistered", participant != null);
        } else {
            result.put("hasRegistered", false);
        }

        return result;
    }

    @Override
    public boolean saveActivity(Activity activity) {
        return saveOrUpdate(activity);
    }

    @Override
    public boolean deleteActivity(Long id) {
        return removeById(id);
    }

    @Override
    public List<Activity> getActivitiesByCreatorId(Long creatorId) {
        return list(new LambdaQueryWrapper<Activity>().eq(Activity::getCreatorId, creatorId));
    }

    @Override
    public List<java.util.Map<String, Object>> getActivityListWithRegistrationStatus(Long userId) {
        // 获取所有活动
        List<Activity> activities = list();
        List<java.util.Map<String, Object>> resultList = new ArrayList<>();

        // 为每个活动添加报名状态
        for (Activity activity : activities) {
            // 创建Map来存储活动信息和报名状态
            java.util.Map<String, Object> activityMap = new java.util.HashMap<>();

            // 将Activity对象的所有属性复制到Map中
            try {
                java.lang.reflect.Field[] fields = Activity.class.getDeclaredFields();
                for (java.lang.reflect.Field field : fields) {
                    field.setAccessible(true);
                    activityMap.put(field.getName(), field.get(activity));
                }
            } catch (Exception e) {
                log.error("复制活动属性失败", e);
            }

            // 检查用户是否已报名
            if (userId != null) {
                ActivityParticipant participant = activityParticipantMapper.selectByActivityAndUser(activity.getId(), userId);
                activityMap.put("hasRegistered", participant != null);
            } else {
                activityMap.put("hasRegistered", false);
            }

            resultList.add(activityMap);
        }

        return resultList;
    }

    @Override
    public List<Activity> getActivitiesByType(String type) {
        return list(new LambdaQueryWrapper<Activity>().eq(Activity::getType, type));
    }

    @Override
    public List<Activity> getActivitiesByStatus(String status) {
        return list(new LambdaQueryWrapper<Activity>().eq(Activity::getStatus, status));
    }

    @Override
    public List<Activity> getRegisteredActivitiesByUserId(Long userId) {
        try {
            log.info("查询用户已报名活动，用户ID: {}", userId);
            // 查询用户已报名的活动ID列表
            List<ActivityParticipant> participants = activityParticipantMapper.selectByUserId(userId);
            if (participants.isEmpty()) {
                return new ArrayList<>();
            }

            // 提取活动ID并查询活动信息
            List<Long> activityIds = participants.stream()
                    .map(ActivityParticipant::getActivityId)
                    .collect(Collectors.toList());

            return list(new LambdaQueryWrapper<Activity>()
                    .in(Activity::getId, activityIds));
        } catch (Exception e) {
            log.error("查询用户已报名活动失败，用户ID: {}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean registerActivity(Long activityId, Long userId) {
        try {
            log.info("用户报名活动，用户ID: {}, 活动ID: {}", userId, activityId);

            // 检查活动是否存在
            Activity activity = getById(activityId);
            if (activity == null) {
                log.error("活动不存在，活动ID: {}", activityId);
                return false;
            }

            // 检查是否已报名
            ActivityParticipant existing = activityParticipantMapper.selectByActivityAndUser(activityId, userId);
            if (existing != null) {
                log.warn("用户已报名该活动，用户ID: {}, 活动ID: {}", userId, activityId);
                return false;
            }

            // 检查活动是否已达到最大参与人数
            Integer currentCount = activityParticipantMapper.countByActivityId(activityId);
            if (activity.getMaxParticipants() != null && currentCount != null && currentCount >= activity.getMaxParticipants()) {
                log.warn("活动已达到最大参与人数，活动ID: {}", activityId);
                return false;
            }

            // 检查报名截止时间
            if (activity.getRegistrationDeadline().toInstant(ZoneOffset.of("+8")).atZone(ZoneId.systemDefault()).toLocalDateTime().isBefore(LocalDateTime.now())) {
                log.warn("报名已截止，活动ID: {}", activityId);
                return false;
            }

            // 创建报名记录
            ActivityParticipant participant = new ActivityParticipant();
            participant.setActivityId(activityId);
            participant.setUserId(userId);
            participant.setRegistrationTime(LocalDateTime.now());
            participant.setStatus("REGISTERED");

            // 插入记录
            int result = activityParticipantMapper.insert(participant);

            // 更新活动当前参与人数
            if (result > 0) {
                activity.setCurrentParticipants(currentCount != null ? currentCount + 1 : 1);
                updateById(activity);
                log.info("用户报名成功，用户ID: {}, 活动ID: {}", userId, activityId);
            }

            return result > 0;
        } catch (Exception e) {
            log.error("用户报名活动失败，用户ID: {}, 活动ID: {}", userId, activityId, e);
            return false;
        }
    }

    @Override
    public boolean cancelRegistration(Long activityId, Long userId) {
        try {
            log.info("用户取消活动报名，用户ID: {}, 活动ID: {}", userId, activityId);

            // 检查是否已报名
            ActivityParticipant existing = activityParticipantMapper.selectByActivityAndUser(activityId, userId);
            if (existing == null) {
                log.warn("用户未报名该活动，用户ID: {}, 活动ID: {}", userId, activityId);
                return false;
            }

            // 删除报名记录
            int result = activityParticipantMapper.deleteByActivityAndUser(activityId, userId);

            // 更新活动当前参与人数
            if (result > 0) {
                Activity activity = getById(activityId);
                if (activity != null && activity.getCurrentParticipants() > 0) {
                    activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
                    updateById(activity);
                }
                log.info("用户取消报名成功，用户ID: {}, 活动ID: {}", userId, activityId);
            }

            return result > 0;
        } catch (Exception e) {
            log.error("用户取消活动报名失败，用户ID: {}, 活动ID: {}", userId, activityId, e);
            return false;
        }
    }

    @Override
    public boolean updateParticipantStatus(Long activityId, Long userId, String status) {
        try {
            log.info("更新参与者状态，用户ID: {}, 活动ID: {}, 状态: {}", userId, activityId, status);

            // 检查是否已报名
            ActivityParticipant existing = activityParticipantMapper.selectByActivityAndUser(activityId, userId);
            if (existing == null) {
                log.warn("用户未报名该活动，无法更新状态，用户ID: {}, 活动ID: {}", userId, activityId);
                return false;
            }

            // 更新状态
            int result = activityParticipantMapper.updateStatusByActivityAndUser(activityId, userId, status);

            log.info("更新参与者状态{}，用户ID: {}, 活动ID: {}", result > 0 ? "成功" : "失败", userId, activityId);
            return result > 0;
        } catch (Exception e) {
            log.error("更新参与者状态失败，用户ID: {}, 活动ID: {}, 状态: {}", userId, activityId, status, e);
            return false;
        }
    }

    @Override
    public String generateParticipationCertificate(Long activityId, Long userId) {
        // 这个方法现在被控制器通过generateParticipationCertificateBytes和FileUtil替代
        // 但为了保持兼容性，我们仍然返回一个模拟的URL
        return "/certificates/activities" + activityId + "_user_" + userId + ".pdf";
    }

    @Override
    public byte[] generateParticipationCertificateBytes(Long activityId, Long userId) {
        try {
            log.info("生成参与证明，用户ID: {}, 活动ID: {}", userId, activityId);

            // 首先检查活动是否存在
            Activity activity = getById(activityId);
            if (activity == null) {
                log.error("活动不存在，活动ID: {}", activityId);
                return new byte[0];
            }

            // 检查用户是否参与了该活动
            ActivityParticipant participant = activityParticipantMapper.selectByActivityAndUser(activityId, userId);
            if (participant == null) {
                log.warn("用户未参与该活动，无法生成证明，用户ID: {}, 活动ID: {}", userId, activityId);
                return new byte[0];
            }

            // 检查活动是否已完成
            if (!"COMPLETED".equals(activity.getStatus())) {
                log.warn("活动尚未完成，无法生成证明，活动ID: {}", activityId);
                return new byte[0];
            }

            // 创建一个简单的模拟PDF内容
            String certificateContent = "这是一份电子参与证明\n\n" +
                                       "兹证明用户（ID: " + userId + "）\n" +
                                       "已成功参与\n" +
                                       "活动名称：" + activity.getTitle() + "\n" +
                                       "活动ID：" + activityId + "\n" +
                                       "活动时间：" + activity.getStartTime() + " 至 " + activity.getEndTime() + "\n" +
                                       "颁发日期：" + java.time.LocalDate.now() + "\n\n" +
                                       "此证明仅作电子存档使用。";

            log.info("参与证明生成成功，用户ID: {}, 活动ID: {}", userId, activityId);

            // 将内容转换为字节数组
            // 实际项目中，这里应该生成真实的PDF文件字节
            return certificateContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("生成参与证明失败，用户ID: {}, 活动ID: {}", userId, activityId, e);
            return new byte[0];
        }
    }

    @Override
    public boolean updateParticipantCertificateUrl(Long activityId, Long userId, String certificateUrl) {
        try {
            log.info("更新参与者证书URL，用户ID: {}, 活动ID: {}", userId, activityId);

            // 调用mapper更新证书URL
            int result = activityParticipantMapper.updateCertificateUrlByActivityAndUser(activityId, userId, certificateUrl);

            log.info("更新参与者证书URL{}，用户ID: {}, 活动ID: {}", result > 0 ? "成功" : "失败", userId, activityId);
            return result > 0;
        } catch (Exception e) {
            log.error("更新参与者证书URL失败，用户ID: {}, 活动ID: {}", userId, activityId, e);
            return false;
        }
    }

    @Override
    public byte[] exportParticipantListExcel(Long activityId) {
        try {
            log.info("导出活动参与者名单Excel，活动ID: {}", activityId);

            // 获取活动信息
            Activity activity = getById(activityId);
            if (activity == null) {
                log.error("活动不存在，活动ID: {}", activityId);
                throw new RuntimeException("活动不存在");
            }

            // 创建Excel工作簿
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("活动参与者名单");

            // 创建表头样式
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建表头
            XSSFRow headerRow = sheet.createRow(0);
            XSSFCell cell0 = headerRow.createCell(0);
            cell0.setCellValue("序号");
            cell0.setCellStyle(headerStyle);

            XSSFCell cell1 = headerRow.createCell(1);
            cell1.setCellValue("用户名");
            cell1.setCellStyle(headerStyle);

            XSSFCell cell2 = headerRow.createCell(2);
            cell2.setCellValue("姓名");
            cell2.setCellStyle(headerStyle);

            XSSFCell cell3 = headerRow.createCell(3);
            cell3.setCellValue("报名时间");
            cell3.setCellStyle(headerStyle);

            XSSFCell cell4 = headerRow.createCell(4);
            cell4.setCellValue("状态");
            cell4.setCellStyle(headerStyle);

            // 查询活动参与者信息
            List<ActivityParticipant> participants = activityParticipantMapper.selectByActivityId(activityId);

            log.info("查询到参与者数量: {}", participants.size());

            // 填充数据
            for (int i = 0; i < participants.size(); i++) {
                ActivityParticipant participant = participants.get(i);
                XSSFRow row = sheet.createRow(i + 1);

                // 序号
                XSSFCell indexCell = row.createCell(0);
                indexCell.setCellValue(i + 1);

                // 用户ID
                XSSFCell usernameCell = row.createCell(1);
                try {
                    User user = userMapper.selectById(participant.getUserId());
                    usernameCell.setCellValue(user != null && user.getUsername() != null ? user.getUsername() : "未知用户");
                } catch (Exception e) {
                    log.error("获取用户信息失败，用户ID: {}", participant.getUserId(), e);
                    usernameCell.setCellValue("未知用户");
                }

                // 用户名
                XSSFCell realnameCell = row.createCell(2);
                try {
                    User user = userMapper.selectById(participant.getUserId());
                    realnameCell.setCellValue(user != null && user.getRealName() != null ? user.getRealName() : "无姓名");
                } catch (Exception e) {
                    log.error("获取用户信息失败，用户ID: {}", participant.getUserId(), e);
                    realnameCell.setCellValue("无姓名");
                }

                // 报名时间
                XSSFCell timeCell = row.createCell(3);
                if (participant.getRegistrationTime() != null) {
                    timeCell.setCellValue(participant.getRegistrationTime().toString());
                } else {
                    timeCell.setCellValue("");
                }

                // 状态
                XSSFCell statusCell = row.createCell(4);
                if (participant.getStatus() != null) {
                    statusCell.setCellValue(participant.getStatus());
                } else {
                    statusCell.setCellValue("");
                }
            }

            // 自动调整列宽
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            log.info("导出活动参与者名单Excel成功，活动ID: {}, 导出人数: {}", activityId, participants.size());
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("导出活动参与者名单Excel失败，活动ID: {}", activityId, e);
            // 创建错误信息Excel
            try {
                XSSFWorkbook errorWorkbook = new XSSFWorkbook();
                XSSFSheet errorSheet = errorWorkbook.createSheet("错误信息");
                XSSFRow errorRow = errorSheet.createRow(0);
                errorRow.createCell(0).setCellValue("导出失败: " + e.getMessage());
                ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
                errorWorkbook.write(errorStream);
                errorWorkbook.close();
                return errorStream.toByteArray();
            } catch (Exception ex) {
                log.error("创建错误Excel失败", ex);
                throw new RuntimeException("导出Excel失败", e);
            }
        }
    }

    @Override
    public byte[] exportParticipantListPdf(Long activityId) {
        try {
            log.info("导出活动参与者名单PDF，活动ID: {}", activityId);

            // 创建文档对象
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);

            // 打开文档
            document.open();

            // 创建字体 - 支持中文
            Font titleFont;
            Font subtitleFont;
            Font normalFont;
            Font headerFont;

            try {
                // 尝试加载中文字体，如果失败则使用默认字体
                BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
                titleFont = new Font(baseFont, 18, Font.BOLD);
                subtitleFont = new Font(baseFont, 14, Font.BOLD);
                normalFont = new Font(baseFont, 12, Font.NORMAL);
                headerFont = new Font(baseFont, 12, Font.BOLD);
            } catch (Exception e) {
                log.warn("加载中文字体失败，使用默认字体", e);
                // 备选方案：使用默认字体
                titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                subtitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
                normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
                headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            }

            // 添加标题
            Paragraph title = new Paragraph("活动参与者名单", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // 获取活动信息
            Activity activity = getById(activityId);
            if (activity != null) {
                document.add(new Paragraph("活动名称: " + activity.getTitle(), subtitleFont));
                document.add(new Paragraph("活动时间: " + activity.getStartTime() + " - " + activity.getEndTime(), normalFont));
                String activityType = activity.getType();
                String activityTypeName = "未知";
                if(activityType != null){
                    if(activityType.equals("TRAINING")){
                        activityTypeName = "培训";
                    }
                    else if(activityType.equals("COMPETITION")){
                        activityTypeName = "赛事";
                    }
                    else if(activityType.equals("LECTURE")){
                        activityTypeName = "讲座";
                    }
                }
                document.add(new Paragraph("活动类型: " + activityTypeName, normalFont));
                String activityStatus = activity.getStatus();
                String activityStatusName = "未知";
                if(activityStatus != null){
                    if(activityStatus.equals("COMPLETED")){
                        activityStatusName = "已结束";
                    }
                }
                document.add(new Paragraph("活动状态: " + activityStatusName, normalFont));
                document.add(new Paragraph("\n"));
            }

            // 查询活动参与者信息
            List<ActivityParticipant> participants = activityParticipantMapper.selectByActivityId(activityId);

            // 构建用户ID到用户信息的映射
            Map<Long, User> userMap = new HashMap<>();
            for (ActivityParticipant participant : participants) {
                if (!userMap.containsKey(participant.getUserId())) {
                    User user = userMapper.selectById(participant.getUserId());
                    if (user != null) {
                        userMap.put(user.getId(), user);
                    }
                }
            }

            // 创建表格
            PdfPTable table = new PdfPTable(5); // 5列
            table.setWidthPercentage(100);
            table.setSpacingBefore(20f);
            table.setSpacingAfter(20f);

            // 设置列宽
            float[] columnWidths = {1f, 2f, 2f, 3f, 2f};
            table.setWidths(columnWidths);

            // 添加表头
            String[] headers = {"序号", "用户名", "姓名", "报名时间", "状态"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }

            // 添加数据行
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < participants.size(); i++) {
                ActivityParticipant participant = participants.get(i);
                User user = userMap.get(participant.getUserId());

                // 序号
                PdfPCell cell1 = new PdfPCell(new Phrase(String.valueOf(i + 1), normalFont));
                cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell1);

                // 用户名
                PdfPCell cell2 = new PdfPCell(new Phrase(user != null ? user.getUsername() : "-", normalFont));
                cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell2);

                // 姓名
                PdfPCell cell3 = new PdfPCell(new Phrase(user != null ? user.getRealName() : "-", normalFont));
                cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell3);

                // 报名时间
                PdfPCell cell4;
                if (participant.getRegistrationTime() != null) {
                    try {
                        String timeStr = sdf.format(participant.getRegistrationTime());
                        cell4 = new PdfPCell(new Phrase(timeStr, normalFont));
                    } catch (IllegalArgumentException e) {
                        // 处理日期格式化异常
                        log.warn("日期格式化失败，用户ID: {}, 报名时间: {}", participant.getUserId(), participant.getRegistrationTime());
                        cell4 = new PdfPCell(new Phrase(participant.getRegistrationTime().toString(), normalFont));
                    }
                } else {
                    cell4 = new PdfPCell(new Phrase("-", normalFont));
                }
                cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell4);

                // 状态
                PdfPCell cell5 = new PdfPCell(new Phrase(participant.getStatus(), normalFont));
                cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell5);
            }

            // 添加表格到文档
            document.add(table);

            // 添加统计信息
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("总参与人数: " + participants.size(), normalFont));
            document.add(new Paragraph("导出时间: " + sdf.format(new Date()), normalFont));

            // 关闭文档
            document.close();

            log.info("导出活动参与者名单PDF成功，活动ID: {}, 导出人数: {}", activityId, participants.size());
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            log.error("导出活动参与者名单PDF失败，活动ID: {}", activityId, e);
            return new byte[0];
        }
    }

    @Override
    public void archiveCompletedActivities() {
        // 查询已结束且状态为PUBLISHED的活动
        LocalDateTime now = LocalDateTime.now();
        Date nowDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

        List<Activity> activities = list(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, "PUBLISHED")
                .lt(Activity::getEndTime, nowDate));

        // 更新活动状态为COMPLETED
        for (Activity activity : activities) {
            activity.setStatus("COMPLETED");
            updateById(activity);
        }
    }
}
