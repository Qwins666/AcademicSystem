package com.academic.scheduler;

import com.academic.service.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class ActivityScheduler {

    private static final Logger log = LoggerFactory.getLogger(ActivityScheduler.class);

    @Autowired
    private ActivityService activityService;

    /**
     * 每天凌晨2点执行活动归档
     * 将已结束的活动自动设置为COMPLETED状态
     */
    @Scheduled(cron = "0 54 19 * * ?")
    public void archiveCompletedActivities() {
        try {
            log.info("开始执行活动归档定时任务");
            activityService.archiveCompletedActivities();
            log.info("活动归档定时任务执行完成");
        } catch (Exception e) {
            log.error("活动归档定时任务执行失败", e);
        }
    }
}
