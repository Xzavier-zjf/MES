package com.shoujike.order.model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceCreateDTO {
    @NotBlank(message = "设备编号不能为空")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    private String name;

    private String status;
}
