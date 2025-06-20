package com.shoujike.process.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InjectionParamCreateDTO {
    @NotNull(message = "任务ID不能为空")
    private Integer taskId;

    @NotNull(message = "注塑压力不能为空")
    private Float pressure;

    @NotNull(message = "注塑速度不能为空")
    private Float injectionSpeed;

    @NotNull(message = "保压时间不能为空")
    private Float holdTime;

    @NotNull(message = "冷却时间不能为空")
    private Float coolingTime;

    private Float moldTemperature;
    private Float materialTemperature;
}
