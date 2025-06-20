package com.shoujike.product.model.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlanDTO {
    private Integer id;
    private String planCode;
    private String productName;
    private Integer totalQuantity;
    private Integer priority;
    private String status;
    private LocalDateTime createTime;

    public void setCompletedQuantity(Integer integer) {

    }
}
