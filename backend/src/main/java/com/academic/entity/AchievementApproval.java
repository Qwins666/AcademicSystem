package com.academic.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = false)
@TableName("achievement_approval")
public class AchievementApproval {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 成果ID
     */
    @TableField("achievement_id")
    private Long achievementId;

    /**
     * 审核者ID
     */
    @TableField("approver_id")
    private Long approverId;

    /**
     * 审核级别：FIRST-初审，FINAL-终审
     */
    @TableField("approval_level")
    private String approvalLevel;

    /**
     * 审核状态：APPROVED-通过，REJECTED-驳回
     */
    @TableField("status")
    private String status;

    /**
     * 审核意见
     */
    @TableField("comment")
    private String comment;

    /**
     * 审核时间
     */
    @TableField(value = "approval_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalTime;
}
