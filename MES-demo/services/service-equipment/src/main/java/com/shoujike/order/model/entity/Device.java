package com.shoujike.order.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("device")
public class Device {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String deviceCode;
    private String name;
    private String status;
    private Integer runtimeMinutes;
    private Integer openCloseTimes;
    private LocalDateTime lastMaintenanceTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Float injectionTime;
    private Float injectionPressure;
}
