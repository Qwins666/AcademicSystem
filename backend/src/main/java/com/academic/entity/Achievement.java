package com.academic.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = false)
@TableName("achievement")
public class Achievement {

    /**
     * 成果ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 成果标题
     */
    @TableField("title")
    private String title;

    /**
     * 成果类型：PAPER-论文，PATENT-专利，CERTIFICATE-证书，PROJECT-项目
     */
    @TableField("type")
    private String type;

    /**
     * 成果描述
     */
    @TableField("description")
    private String description;

    /**
     * 成果文件URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 标签（JSON格式）
     */
    @TableField("tags")
    private String tags;

    /**
     * 级别：NATIONAL-国家级，PROVINCIAL-省级，SCHOOL-校级
     */
    @TableField("level")
    private String level;

    /**
     * 提交者ID
     */
    @TableField("submitter_id")
    private Long submitterId;

    /**
     * 状态：PENDING-待审核，APPROVED-已通过，REJECTED-已驳回
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
