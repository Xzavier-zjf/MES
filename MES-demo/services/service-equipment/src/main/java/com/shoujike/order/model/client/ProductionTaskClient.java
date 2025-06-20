package com.shoujike.order.model.client;

import com.shoujike.order.model.DTO.ProductionTaskDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "com.shoujike.process.service-production", path = "/api/v1/production")
public interface ProductionTaskClient {

    @GetMapping("/devices/{deviceId}/tasks")
    List<ProductionTaskDTO> getTasksByDevice(
            @PathVariable Integer deviceId,
            @RequestParam String period);

    ResponseEntity<com.shoujike.product.model.DTO.TaskDTO> getTaskById(Integer taskId);
}
