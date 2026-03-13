package com.shoujike.process.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrintPatternDTO {
    private Integer id;
    private String planId;
    private String taskId;
    private int quantity;
    private String deviceId;
    private String patternCode;
    private String patternName;
    private String machineModel;
    private String imageUrl;
    private Integer defaultPrintSpeed;
    private Float defaultPressure;
}
