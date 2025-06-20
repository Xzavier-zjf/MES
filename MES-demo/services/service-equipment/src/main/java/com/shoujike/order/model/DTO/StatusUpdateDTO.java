package com.shoujike.order.model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StatusUpdateDTO {
    @NotBlank(message = "状态不能为空")
    private String status;
}
