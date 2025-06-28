package com.shoujike.product.model.DTO;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlanUpdateRequest {
    @NotBlank
    private String productName;

    @NotNull
    private Integer totalQuantity;

    @NotBlank
    private String status;

    @NotNull
    private Integer priority;
}
