package com.shoujike.process.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("print_pattern")
public class PrintPattern {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("plan_id")
    private String planId;
    @TableField("task_id")
    private String taskId;
    @TableField("device_id")
    private String deviceId;
    private int quantity;
    @TableField("pattern_code")
    private String patternCode;

    @TableField("pattern_name")
    private String patternName;

    @TableField("machine_model")
    private String machineModel;

    @TableField("image_url")
    private String imageUrl;

    @TableField("default_print_speed")
    private Integer defaultPrintSpeed;

    @TableField("default_pressure")
    private Float defaultPressure;
}