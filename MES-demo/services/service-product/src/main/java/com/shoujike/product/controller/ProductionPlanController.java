package com.shoujike.product.controller;

import com.shoujike.product.client.ProcessClient;
import com.shoujike.product.entity.ProductionPlan;
import com.shoujike.product.service.ProductionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plans")
public class ProductionPlanController {
    private final ProductionPlanService planService;
    private final ProcessClient processClient;

    @PostMapping
    public ResponseEntity<ProductionPlan> create(@RequestBody ProductionPlan plan) {
        return ResponseEntity.ok(planService.createPlan(plan));
    }

    @PostMapping("/{id}/issue")
    public ResponseEntity<String> issuePlan(@PathVariable Long id) {
        planService.issuePlan(id);
        return ResponseEntity.ok("计划已下发");
    }
}