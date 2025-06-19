package com.shoujike.product.service;

import com.shoujike.product.entity.ProductionPlan;
import com.shoujike.product.mapper.ProductionPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductionPlanService {
    private final ProductionPlanMapper planMapper;


    public ProductionPlan createPlan(ProductionPlan plan) {
        plan.setStatus("draft");
        planMapper.insert(plan);
        return plan;
    }

    public void issuePlan(Long planId) {
        planMapper.updateStatus(planId, "issued");
    }
}