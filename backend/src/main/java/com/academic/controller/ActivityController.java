package com.academic.controller;

import com.academic.common.Result;
import com.academic.entity.Activity;
import com.academic.service.ActivityService;
import com.academic.util.FileUtil;
import com.academic.util.JwtUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;


@RestController
@RequestMapping("/activities")
@CrossOrigin
public class ActivityController {

    private static Logger log= LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    private ActivityService activityService;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取所有活动列表（包含报名状态）
     */
    @GetMapping
    public Result<List<java.util.Map<String, Object>>> getAllActivities(HttpServletRequest request) {
        try {
            // 从JWT中获取当前用户ID
            Long userId = null;
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    // 从token中解析出用户ID
                    token = token.substring(7); // 去掉Bearer前缀
                    userId = jwtUtil.getUserIdFromToken(token);
                } catch (Exception e) {
                    // token解析失败，可以记录日志但不抛出异常
                    userId = null;
                }
            }

            // 获取包含报名状态的活动列表
            List<java.util.Map<String, Object>> activities = activityService.getActivityListWithRegistrationStatus(userId);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取活动详情
     */
    @GetMapping("/{id}")
    public Result<Object> getActivityById(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 从JWT中获取当前用户ID
            Long userId = null;
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    // 从token中解析出用户ID
                    token = token.substring(7); // 去掉Bearer前缀
                    userId = jwtUtil.getUserIdFromToken(token);
                } catch (Exception e) {
                    // token解析失败，可以记录日志但不抛出异常
                    userId = null;
                }
            }

            // 使用新方法获取包含报名状态的活动详情
            java.util.Map<String, Object> activityDetail = activityService.getActivityDetailWithRegistrationStatus(id, userId);
            if (activityDetail != null) {
                return Result.success(activityDetail);
            } else {
                return Result.notFound("活动不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建活动
     */
    @PostMapping
    public Result<String> createActivity(@RequestBody Activity activity, HttpServletRequest request) {
        try {
            // 从JWT中获取当前用户ID作为创建者ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    // 从token中解析出用户ID
                    token = token.substring(7); // 去掉Bearer前缀
                    activity.setCreatorId(jwtUtil.getUserIdFromToken(token));
                } catch (Exception e) {
                    return Result.error("获取用户信息失败");
                }

                if (activityService.saveActivity(activity)) {
                    return Result.success("活动创建成功");
                } else {
                    return Result.error("活动创建失败");
                }
            }
            return Result.unauthorized("未登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public Result<String> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        try {
            activity.setId(id);
            if (activityService.saveActivity(activity)) {
                return Result.success("活动更新成功");
            } else {
                return Result.error("活动更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteActivity(@PathVariable Long id) {
        try {
            if (activityService.deleteActivity(id)) {
                return Result.success("活动删除成功");
            } else {
                return Result.error("活动删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据创建者ID获取活动列表
     */
    @GetMapping("/creator/{creatorId}")
    public Result<List<Activity>> getActivitiesByCreatorId(@PathVariable Long creatorId) {
        try {
            List<Activity> activities = activityService.getActivitiesByCreatorId(creatorId);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据类型获取活动列表
     */
    @GetMapping("/type/{type}")
    public Result<List<Activity>> getActivitiesByType(@PathVariable String type) {
        try {
            List<Activity> activities = activityService.getActivitiesByType(type);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据状态获取活动列表
     */
    @GetMapping("/status/{status}")
    public Result<List<Activity>> getActivitiesByStatus(@PathVariable String status) {
        try {
            List<Activity> activities = activityService.getActivitiesByStatus(status);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 报名参加活动
     */
    @PostMapping("/{id}/register")
    public Result<String> registerActivity(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 从JWT中获取当前用户ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    // 从token中解析出用户ID
                    token = token.substring(7); // 去掉Bearer前缀
                    Long userId = jwtUtil.getUserIdFromToken(token);

                    if (activityService.registerActivity(id, userId)) {
                        return Result.success("报名成功");
                    } else {
                        return Result.error("报名失败");
                    }
                } catch (Exception e) {
                    return Result.error("获取用户信息失败");
                }
            }
            return Result.unauthorized("未登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消报名活动
     */
    @PostMapping("/{id}/cancel-register")
    public Result<String> cancelRegistration(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 从JWT中获取当前用户ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    // 从token中解析出用户ID
                    token = token.substring(7); // 去掉Bearer前缀
                    Long userId = jwtUtil.getUserIdFromToken(token);

                    if (activityService.cancelRegistration(id, userId)) {
                        return Result.success("取消报名成功");
                    } else {
                        return Result.error("取消报名失败");
                    }
                } catch (Exception e) {
                    return Result.error("获取用户信息失败");
                }
            }
            return Result.unauthorized("未登录");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 导出参与名单
     * 根据type参数决定导出Excel还是PDF格式
     */
    @GetMapping("/{id}/participants/export")
    public ResponseEntity<byte[]> exportParticipants(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "excel") String type) {
        try {
            // 验证活动存在且状态为COMPLETED
            Activity activity = activityService.getById(id);
            if (activity == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileData;
            HttpHeaders headers = new HttpHeaders();

            if ("pdf".equalsIgnoreCase(type)) {
                // 导出PDF格式
                fileData = activityService.exportParticipantListPdf(id);
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "participants_" + id + ".pdf");
            } else {
                // 导出Excel格式（默认）
                fileData = activityService.exportParticipantListExcel(id);

                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                headers.setContentDispositionFormData("attachment", "participants_" + id + ".xlsx");
            }

            headers.setContentLength(fileData.length);

            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("导出参与名单失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("导出失败".getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 生成活动参与证明
     */
    @GetMapping("/{id}/certificate")
    public Result<String> generateParticipationCertificate(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 从JWT中获取当前用户ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    // 从token中解析出用户ID
                    token = token.substring(7); // 去掉Bearer前缀
                    Long userId = jwtUtil.getUserIdFromToken(token);

                    // 生成参与证明文件（字节数组）
                    byte[] certificateBytes = activityService.generateParticipationCertificateBytes(id, userId);
                    if (certificateBytes == null || certificateBytes.length == 0) {
                        return Result.error("参与证明生成失败");
                    }

                    // 使用FileUtil保存证书文件
                    String subDir = "certificates/activities";
                    String fileName = "activity_certificate_" + id + "_user_" + userId + ".pdf";
                    String certificateUrl;
                    try {
                        certificateUrl = fileUtil.saveFile(certificateBytes, fileName, subDir);
                    } catch (IOException e) {
                        return Result.error("参与证明保存失败: " + e.getMessage());
                    }

                    // 更新参与者记录中的证书URL
                    try {
                        activityService.updateParticipantCertificateUrl(id, userId, certificateUrl);
                    } catch (Exception e) {
                        // 记录错误但不影响证书的生成和返回
                        log.warn("更新参与者证书URL失败，活动ID: {}, 用户ID: {}", id, userId, e);
                    }

                    return Result.success("参与证明生成成功", certificateUrl);
                } catch (Exception e) {
                    return Result.error("获取用户信息失败");
                }
            }
            return Result.unauthorized("未登录");
        } catch (Exception e) {
            return Result.error("参与证明生成失败: " + e.getMessage());
        }
    }
}
