package com.academic.service;

import com.academic.entity.Activity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface ActivityService extends IService<Activity> {

    /**
     * 获取所有活动列表
     */
    List<Activity> getAllActivities();

    /**
     * 根据ID获取活动详情
     */
    Activity getActivityById(Long id);

    /**
     * 获取活动详情并检查用户是否已报名
     * @param id 活动ID
     * @param userId 用户ID
     * @return 包含报名状态的活动信息Map
     */
    java.util.Map<String, Object> getActivityDetailWithRegistrationStatus(Long id, Long userId);

    /**
     * 获取活动列表并检查用户是否已报名
     * @param userId 用户ID
     * @return 包含报名状态的活动信息列表
     */
    List<java.util.Map<String, Object>> getActivityListWithRegistrationStatus(Long userId);

    /**
     * 创建或更新活动
     */
    boolean saveActivity(Activity activity);

    /**
     * 删除活动
     */
    boolean deleteActivity(Long id);

    /**
     * 根据创建者ID获取活动列表
     */
    List<Activity> getActivitiesByCreatorId(Long creatorId);

    /**
     * 根据类型获取活动列表
     */
    List<Activity> getActivitiesByType(String type);

    /**
     * 根据状态获取活动列表
     */
    List<Activity> getActivitiesByStatus(String status);

    /**
     * 根据用户ID获取已报名的活动列表
     */
    List<Activity> getRegisteredActivitiesByUserId(Long userId);

    /**
     * 报名参加活动
     */
    boolean registerActivity(Long activityId, Long userId);

    /**
     * 取消报名活动
     */
    boolean cancelRegistration(Long activityId, Long userId);

    /**
     * 更新活动参与者状态
     */
    boolean updateParticipantStatus(Long activityId, Long userId, String status);

    /**
     * 生成活动参与证明
     */
    String generateParticipationCertificate(Long activityId, Long userId);

    /**
     * 生成活动参与证明字节数组
     */
    byte[] generateParticipationCertificateBytes(Long activityId, Long userId);

    /**
     * 更新参与者证书URL
     */
    boolean updateParticipantCertificateUrl(Long activityId, Long userId, String certificateUrl);

    /**
     * 导出活动参与名单（Excel）
     */
    byte[] exportParticipantListExcel(Long activityId);

    /**
     * 导出活动参与名单（PDF）
     */
    byte[] exportParticipantListPdf(Long activityId);

    /**
     * 自动归档已结束的活动
     */
    void archiveCompletedActivities();
}
