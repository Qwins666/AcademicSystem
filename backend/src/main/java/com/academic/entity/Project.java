package com.academic.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project")
public class Project {

    /**
     * 项目ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目名称
     */
    @TableField("name")
    private String name;

    /**
     * 项目描述
     */
    @TableField("description")
    private String description;

    /**
     * 项目类型：RESEARCH-研究项目，INNOVATION-创新项目，PRACTICE-实践项目
     */
    @TableField("type")
    private String type;

    /**
     * 项目级别：NATIONAL-国家级，PROVINCIAL-省级，SCHOOL-校级
     */
    @TableField("level")
    private String level;

    /**
     * 项目经费
     */
    @TableField("funding")
    private BigDecimal funding;

    /**
     * 开始日期
     */
    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @TableField("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 状态：PLANNING-规划中，ONGOING-进行中，COMPLETED-已完成，CANCELLED-已取消
     */
    @TableField("status")
    private String status;

    /**
     * 项目负责人ID
     */
    @TableField("leader_id")
    private Long leaderId;

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
