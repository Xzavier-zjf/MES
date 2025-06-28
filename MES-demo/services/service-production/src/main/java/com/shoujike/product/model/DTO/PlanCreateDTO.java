package com.shoujike.product.model.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.List;

@Data
public class PlanCreateDTO {
    @TableField("plan_code")
    private String planCode;
    private String productName;
    private Integer totalQuantity;
    private Integer priority;
//    private List<TaskCreateDTO> tasks;
    private String status;
}
