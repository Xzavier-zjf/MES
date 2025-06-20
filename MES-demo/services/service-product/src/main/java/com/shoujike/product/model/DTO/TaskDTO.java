package com.shoujike.product.model.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Integer id;
    private Integer planId;
    private Integer deviceId;
    private String deviceName;
    private String processType;
    private Integer quantity;
    private Integer completedQuantity;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}