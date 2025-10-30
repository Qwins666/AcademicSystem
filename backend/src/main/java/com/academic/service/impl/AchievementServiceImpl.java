package com.academic.service.impl;

import com.academic.entity.Achievement;
import com.academic.entity.AchievementApproval;
import com.academic.mapper.AchievementMapper;
import com.academic.mapper.AchievementApprovalMapper;
import com.academic.service.AchievementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class AchievementServiceImpl extends ServiceImpl<AchievementMapper, Achievement> implements AchievementService {

    @Autowired
    private AchievementApprovalMapper achievementApprovalMapper;

    @Override
    public List<Achievement> getAllAchievements() {
        return list();
    }

    @Override
    public Map<String, Object> getAchievementById(Long id) {
        // 获取基本成果信息
        Achievement achievement = getById(id);
        if (achievement == null) {
            return null;
        }

        // 转换为Map以添加额外信息
        Map<String, Object> achievementDetail = new HashMap<>();
        achievementDetail.put("id", achievement.getId());
        achievementDetail.put("title", achievement.getTitle());
        achievementDetail.put("type", achievement.getType());
        achievementDetail.put("level", achievement.getLevel());
        achievementDetail.put("description", achievement.getDescription());
        achievementDetail.put("fileUrl", achievement.getFileUrl());
        achievementDetail.put("fileName", achievement.getFileName());
        achievementDetail.put("fileSize", achievement.getFileSize());
        achievementDetail.put("submitterId", achievement.getSubmitterId());
        achievementDetail.put("status", achievement.getStatus());
        achievementDetail.put("createTime", achievement.getCreateTime());
        achievementDetail.put("updateTime", achievement.getUpdateTime());

        // 获取审核历史
        List<AchievementApprovalHistory> approvalHistory = getAchievementApprovalHistory(id);

        // 分离初审和终审信息
        for (AchievementApprovalHistory history : approvalHistory) {
            Map<String, Object> approvalMap = new HashMap<>();
            approvalMap.put("id", history.getId());
            approvalMap.put("approverId", history.getApproverId());
            approvalMap.put("approverName", history.getApproverName());
            approvalMap.put("status", history.getStatus());
            approvalMap.put("comment", history.getComment());
            approvalMap.put("approvalTime", history.getApprovalTime());
            approvalMap.put("approved", "APPROVED".equals(history.getStatus()));

            if ("FIRST".equals(history.getApprovalLevel())) {
                achievementDetail.put("firstApproval", approvalMap);
            } else if ("FINAL".equals(history.getApprovalLevel())) {
                achievementDetail.put("finalApproval", approvalMap);
            }
        }

        return achievementDetail;
    }

    @Override
    public boolean saveAchievement(Achievement achievement) {
        return saveOrUpdate(achievement);
    }

    @Override
    public boolean deleteAchievement(Long id) {
        return removeById(id);
    }

    @Override
    public List<Achievement> getAchievementsBySubmitterId(Long submitterId) {
        return list(new LambdaQueryWrapper<Achievement>().eq(Achievement::getSubmitterId, submitterId));
    }

    @Override
    public List<Achievement> getAchievementsBySubmitterIdWithSearch(Long submitterId, Map<String, Object> searchParams, int pageNum, int pageSize) {
        LambdaQueryWrapper<Achievement> queryWrapper = new LambdaQueryWrapper<Achievement>()
            .eq(Achievement::getSubmitterId, submitterId);

        // 添加搜索条件
        if (searchParams != null) {
            // 标题搜索（模糊匹配）
            if (searchParams.containsKey("title") && searchParams.get("title") != null) {
                String title = (String) searchParams.get("title");
                if (!title.trim().isEmpty()) {
                    queryWrapper.like(Achievement::getTitle, title);
                }
            }

            // 类型搜索（精确匹配）
            if (searchParams.containsKey("type") && searchParams.get("type") != null) {
                String type = (String) searchParams.get("type");
                if (!type.trim().isEmpty()) {
                    queryWrapper.eq(Achievement::getType, type);
                }
            }

            // 状态搜索（精确匹配）
            if (searchParams.containsKey("status") && searchParams.get("status") != null) {
                String status = (String) searchParams.get("status");
                if (!status.trim().isEmpty()) {
                    queryWrapper.eq(Achievement::getStatus, status);
                }
            }
        }

        // 添加分页
        int startIndex = (pageNum - 1) * pageSize;
        queryWrapper.orderByDesc(Achievement::getCreateTime);

        return list(queryWrapper.last("LIMIT " + startIndex + ", " + pageSize));
    }

    @Override
    public long getAchievementsCountBySubmitterIdWithSearch(Long submitterId, Map<String, Object> searchParams) {
        LambdaQueryWrapper<Achievement> queryWrapper = new LambdaQueryWrapper<Achievement>()
            .eq(Achievement::getSubmitterId, submitterId);

        // 添加搜索条件
        if (searchParams != null) {
            // 标题搜索（模糊匹配）
            if (searchParams.containsKey("title") && searchParams.get("title") != null) {
                String title = (String) searchParams.get("title");
                if (!title.trim().isEmpty()) {
                    queryWrapper.like(Achievement::getTitle, title);
                }
            }

            // 类型搜索（精确匹配）
            if (searchParams.containsKey("type") && searchParams.get("type") != null) {
                String type = (String) searchParams.get("type");
                if (!type.trim().isEmpty()) {
                    queryWrapper.eq(Achievement::getType, type);
                }
            }

            // 状态搜索（精确匹配）
            if (searchParams.containsKey("status") && searchParams.get("status") != null) {
                String status = (String) searchParams.get("status");
                if (!status.trim().isEmpty()) {
                    queryWrapper.eq(Achievement::getStatus, status);
                }
            }
        }

        return count(queryWrapper);
    }

    @Override
    public List<Achievement> getAchievementsByType(String type) {
        return list(new LambdaQueryWrapper<Achievement>().eq(Achievement::getType, type));
    }

    @Override
    public List<Achievement> getAchievementsByStatus(String status) {
        return list(new LambdaQueryWrapper<Achievement>().eq(Achievement::getStatus, status));
    }

    @Override
    public List<Achievement> getAchievementsByLevel(String level) {
        return list(new LambdaQueryWrapper<Achievement>().eq(Achievement::getLevel, level));
    }

    @Override
    public boolean firstApproveAchievement(Long achievementId, Long approverId, boolean approved, String comment) {
        // 向achievement_approval表插入初审记录，并更新成果状态
        Achievement achievement = getById(achievementId);
        if (achievement == null) {
            return false;
        }

        // 创建审核记录
        AchievementApproval approval = new AchievementApproval();
        approval.setAchievementId(achievementId);
        approval.setApproverId(approverId);
        approval.setApprovalLevel("FIRST");
        approval.setStatus(approved ? "APPROVED" : "REJECTED");
        approval.setComment(comment);
        approval.setApprovalTime(LocalDateTime.now());

        // 插入审核记录
        achievementApprovalMapper.insert(approval);

        // 更新成果状态
        achievement.setStatus(approved ? "PENDING_FINAL" : "REJECTED");
        achievement.setUpdateTime(LocalDateTime.now());

        return updateById(achievement);
    }

    @Override
    public boolean finalApproveAchievement(Long achievementId, Long approverId, boolean approved, String comment) {
        // 向achievement_approval表插入终审记录，并更新成果状态
        Achievement achievement = getById(achievementId);
        if (achievement == null) {
            return false;
        }

        // 创建审核记录
        AchievementApproval approval = new AchievementApproval();
        approval.setAchievementId(achievementId);
        approval.setApproverId(approverId);
        approval.setApprovalLevel("FINAL");
        approval.setStatus(approved ? "APPROVED" : "REJECTED");
        approval.setComment(comment);
        approval.setApprovalTime(LocalDateTime.now());

        // 插入审核记录
        achievementApprovalMapper.insert(approval);

        // 更新成果状态
        achievement.setStatus(approved ? "APPROVED" : "REJECTED");
        achievement.setUpdateTime(LocalDateTime.now());

        return updateById(achievement);
    }

    @Override
    public List<Achievement> getPendingFirstApprovalAchievements() {
        // 查询待初审的成果
        // 暂时返回空列表，后续需要实现
        return list(new LambdaQueryWrapper<Achievement>().eq(Achievement::getStatus, "PENDING"));
    }

    @Override
    public List<Achievement> getPendingFinalApprovalAchievements() {
        // 查询待终审的成果
        // 暂时返回空列表，后续需要实现
        return list(new LambdaQueryWrapper<Achievement>().eq(Achievement::getStatus, "PENDING_FINAL"));
    }

    @Override
    public List<Achievement> getAchievementsByTags(List<String> tags) {
        // 根据标签筛选成果
        // 暂时返回空列表，后续需要实现
        return new ArrayList<>();
    }

    @Override
    public List<AchievementApprovalHistory> getAchievementApprovalHistory(Long achievementId) {
        // 获取成果审核历史，使用BaseMapper的selectList方法
        List<AchievementApproval> approvals = achievementApprovalMapper.selectList(
            new LambdaQueryWrapper<AchievementApproval>()
                .eq(AchievementApproval::getAchievementId, achievementId)
                .orderByAsc(AchievementApproval::getApprovalTime)
        );
        List<AchievementApprovalHistory> historyList = new ArrayList<>();

        for (AchievementApproval approval : approvals) {
            AchievementApprovalHistory history = new AchievementApprovalHistory();
            history.setId(approval.getId());
            history.setAchievementId(approval.getAchievementId());
            history.setApproverId(approval.getApproverId());
            // 这里可以根据approverId查询用户名称，暂时留空
            history.setApproverName("审核人" + approval.getApproverId());
            history.setApprovalLevel(approval.getApprovalLevel());
            history.setStatus(approval.getStatus());
            history.setComment(approval.getComment());
            history.setApprovalTime(approval.getApprovalTime().toString());
            historyList.add(history);
        }

        return historyList;
    }
}
