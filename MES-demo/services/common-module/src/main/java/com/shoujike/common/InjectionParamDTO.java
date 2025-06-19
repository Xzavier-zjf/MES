package com.shoujike.common;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class InjectionParamDTO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private Float pressure;
    private Float injectionSpeed;
    private Float holdTime;
    private Float coolingTime;
    private Float moldTemperature;
    private Float materialTemperature;
}