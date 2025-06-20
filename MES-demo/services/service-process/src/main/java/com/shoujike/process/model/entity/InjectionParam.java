package com.shoujike.process.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("injection_param")
public class InjectionParam {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("task_id")
    private Integer taskId;

    private Float pressure;

    @TableField("injection_speed")
    private Float injectionSpeed;

    @TableField("hold_time")
    private Float holdTime;

    @TableField("cooling_time")
    private Float coolingTime;

    @TableField("mold_temperature")
    private Float moldTemperature;

    @TableField("material_temperature")
    private Float materialTemperature;
}