package com.shoujike.order.model.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RuntimeUpdateDTO {
    @NotNull(message = "增加时长不能为空")
    @Min(value = 0, message = "时长不能为负数")
    private Integer addedMinutes;

    @NotNull(message = "增加开合次数不能为空")
    @Min(value = 0, message = "开合次数不能为负数")
    private Integer addedCycles;
}