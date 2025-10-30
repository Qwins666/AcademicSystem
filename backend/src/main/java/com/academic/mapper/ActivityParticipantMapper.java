package com.academic.mapper;

import com.academic.entity.ActivityParticipant;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;


public interface ActivityParticipantMapper extends BaseMapper<ActivityParticipant> {

    /**
     * 根据活动ID和用户ID查询是否已报名
     */
    ActivityParticipant selectByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId);

    /**
     * 根据活动ID查询所有参与者
     */
    List<ActivityParticipant> selectByActivityId(@Param("activityId") Long activityId);

    /**
     * 根据用户ID查询所有已报名活动
     */
    List<ActivityParticipant> selectByUserId(@Param("userId") Long userId);

    /**
     * 删除活动参与者记录
     */
    int deleteByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId);

    /**
     * 更新参与者状态
     */
    int updateStatusByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId, @Param("status") String status);

    /**
     * 更新参与者证书URL
     */
    int updateCertificateUrlByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId, @Param("certificateUrl") String certificateUrl);

    /**
     * 查询活动当前参与人数
     */
    Integer countByActivityId(@Param("activityId") Long activityId);
}
