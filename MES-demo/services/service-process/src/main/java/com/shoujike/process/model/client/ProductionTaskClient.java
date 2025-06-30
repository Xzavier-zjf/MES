package com.shoujike.process.model.client;

import com.shoujike.product.model.DTO.InjectionTaskInfoDTO;
import com.shoujike.product.model.DTO.TaskDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "service-production")
public interface ProductionTaskClient {

    @GetMapping("/api/v1/production/tasks/{taskId}")
    ResponseEntity<TaskDTO> getTaskById(@PathVariable("taskId") String taskId);
    @GetMapping("/api/v1/production/tasks/injection")
    List<InjectionTaskInfoDTO> getInjectionTasks();
}

