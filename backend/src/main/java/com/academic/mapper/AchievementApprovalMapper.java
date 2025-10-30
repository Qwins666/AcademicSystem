package com.academic.mapper;

import com.academic.entity.AchievementApproval;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface AchievementApprovalMapper extends BaseMapper<AchievementApproval> {

    /**
     * 根据成果ID获取审核记录列表
     */
    List<AchievementApproval> getByAchievementId(@Param("achievementId") Long achievementId);

    /**
     * 根据成果ID和审核级别获取审核记录
     */
    AchievementApproval getByAchievementIdAndLevel(@Param("achievementId") Long achievementId, @Param("approvalLevel") String approvalLevel);
}
