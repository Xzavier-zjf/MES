package com.shoujike.product.model.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskCreateDTO {
    private Integer id;
    private String taskCode;
    private Integer planId;
    private Integer deviceId;
    private String processType;
    private Integer quantity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}