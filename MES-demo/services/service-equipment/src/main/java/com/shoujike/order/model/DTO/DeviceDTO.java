package com.shoujike.order.model.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceDTO {
    private Integer id;
    private String deviceCode;
    private String name;
    private String status;
    private String type;
    private Integer runtimeMinutes;
    private Integer openCloseTimes;
    private LocalDateTime lastMaintenanceTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Float injectionTime;
    private Float injectionPressure;
    private Float printingSpeed;
    private Float printingPressure;

}
