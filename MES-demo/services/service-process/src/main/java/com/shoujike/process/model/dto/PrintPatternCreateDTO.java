package com.shoujike.process.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import java.beans.Transient;

@Data
public class PrintPatternCreateDTO {
    private String planId;
    private String taskId;
    private String deviceId;
    private String patternCode;

    private String patternName;

    private String machineModel;

    private String imageUrl;  // 新增字段用于存储图片路径

//    @Transient  // 表示不持久化到数据库
    private MultipartFile imageFile;  // 新增文件接收字段

    private Integer defaultPrintSpeed;
    private Float defaultPressure;
}
