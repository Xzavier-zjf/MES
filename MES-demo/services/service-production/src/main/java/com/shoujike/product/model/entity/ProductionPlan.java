package com.shoujike.product.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("production_plan")
public class ProductionPlan {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String planCode;
    private String productName;
    private Integer totalQuantity;
    private Integer priority;
    private String status; // draft, issued, completed

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


}