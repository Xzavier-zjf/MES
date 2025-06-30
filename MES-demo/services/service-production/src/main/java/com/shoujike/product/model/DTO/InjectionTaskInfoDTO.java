package com.shoujike.product.model.DTO;


import lombok.Data;

@Data
public class InjectionTaskInfoDTO {

    private String taskCode;
    private String planCode;
    private String deviceCode;

    // 工艺参数字段
    private String id;
    private Float pressure;
    private Float injectionSpeed;
    private Float holdTime;
    private Float coolingTime;
    private Float moldTemperature;
    private Float materialTemperature;

    // ✅ 补充：是否已录入参数
    private Boolean hasParam;
}


