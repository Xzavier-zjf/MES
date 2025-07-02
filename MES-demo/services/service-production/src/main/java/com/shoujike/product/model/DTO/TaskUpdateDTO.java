package com.shoujike.product.model.DTO;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class TaskUpdateDTO {
    private Integer id;
    private String taskCode;
    private Integer planId;
    private Integer deviceId;
    private String processType;
    private Integer quantity;
    private String status;
}
