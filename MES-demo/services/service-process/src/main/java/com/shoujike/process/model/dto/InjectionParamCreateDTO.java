package com.shoujike.process.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InjectionParamCreateDTO {

    private String taskId;


    private String planId;


    private String deviceId;


    private Float pressure;

    private Integer quantity;

    private Float injectionSpeed;

    private Float holdTime;


    private Float coolingTime;

    private Float moldTemperature;
    private Float materialTemperature;
}
