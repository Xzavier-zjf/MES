package com.shoujike.process.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InjectionParamDTO {
    private Integer id;
    private String planCode;
    private String taskId;
    private String deviceId;
    private Float pressure;
    private Float injectionSpeed;
    private Float holdTime;
    private Float coolingTime;
    private Float moldTemperature;
    private Float materialTemperature;
}
