package com.shoujike.process.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class PrintPatternCreateDTO {
    @NotBlank(message = "图案编号不能为空")
    private String patternCode;

    @NotBlank(message = "图案名称不能为空")
    private String patternName;

    @NotBlank(message = "适用机型不能为空")
    private String machineModel;

    @URL(message = "图片地址格式无效")
    private String imageUrl;

    private Integer defaultPrintSpeed;
    private Float defaultPressure;
}
