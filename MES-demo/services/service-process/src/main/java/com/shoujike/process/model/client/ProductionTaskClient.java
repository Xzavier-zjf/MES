package com.shoujike.process.model.client;

import com.shoujike.product.model.DTO.TaskDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "production-com.shoujike.process.service",
        path = "/api/v1/production/tasks",
        fallbackFactory = ProductionTaskClientFallbackFactory.class
)
public interface ProductionTaskClient {

    @GetMapping("/{id}")
    ResponseEntity<TaskDTO> getTaskById(@PathVariable("id") Integer id);
}

