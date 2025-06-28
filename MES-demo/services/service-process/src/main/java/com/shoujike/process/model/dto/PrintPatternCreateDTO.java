package com.shoujike.process.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import java.beans.Transient;

@Data
public class PrintPatternCreateDTO {
    @NotBlank(message = "图案编号不能为空")
    private String patternCode;

    @NotBlank(message = "图案名称不能为空")
    private String patternName;

    @NotBlank(message = "适用机型不能为空")
    private String machineModel;

    private String imageUrl;  // 新增字段用于存储图片路径

//    @Transient  // 表示不持久化到数据库
    private MultipartFile imageFile;  // 新增文件接收字段

    private Integer defaultPrintSpeed;
    private Float defaultPressure;
}
