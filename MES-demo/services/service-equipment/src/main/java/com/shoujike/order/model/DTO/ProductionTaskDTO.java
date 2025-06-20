package com.shoujike.order.model.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductionTaskDTO {
    private Integer id;
    private String taskCode;
    private Integer quantity;
    private Integer goodQuantity;
    private Integer actualRuntime; // 分钟
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}