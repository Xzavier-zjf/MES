package com.shoujike.order.model.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OEECalculationDTO {
    @NotNull(message = "设备ID不能为空")
    private Integer deviceId;

    @NotBlank(message = "时间段不能为空")
    private String period; // daily, weekly, monthly
}