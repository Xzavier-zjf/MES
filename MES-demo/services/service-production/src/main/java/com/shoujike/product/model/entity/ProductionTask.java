package com.shoujike.product.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("production_task")
public class ProductionTask {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String taskCode;
    private Integer planId;
    private Integer deviceId;
    private String processType;
    private Integer quantity;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}