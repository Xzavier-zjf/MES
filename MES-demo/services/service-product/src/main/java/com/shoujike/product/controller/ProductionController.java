package com.shoujike.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoujike.common.exception.EntityNotFoundException;
import com.shoujike.product.model.DTO.*;
import com.shoujike.product.model.entity.ProductionPlan;
import com.shoujike.product.model.entity.ProductionTask;
import com.shoujike.product.service.ProductionPlanService;
import com.shoujike.product.service.ProductionTaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/production")
@Validated
public class ProductionController {

    @Autowired
    private ProductionPlanService planService;

    @Autowired
    private ProductionTaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    // ------------------- 生产计划管理 -------------------
    @PostMapping("/plans")
    public ResponseEntity<PlanDTO> createProductionPlan(
            @Valid @RequestBody PlanCreateDTO createDTO) {

        ProductionPlan plan = planService.createPlanWithTasks(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToPlanDTO(plan));
    }

    @PutMapping("/plans/{id}/status")
    public ResponseEntity<Void> updatePlanStatus(
            @PathVariable Integer id,
            @RequestParam @NotBlank String status) throws EntityNotFoundException {

        planService.updatePlanStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/plans")
    public ResponseEntity<Page<PlanDTO>> getPlansByStatus(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ProductionPlan> plans = planService.getPlansByStatus(status, pageable);
        Page<PlanDTO> dtoPage = plans.map(this::convertToPlanDTO);
        return ResponseEntity.ok(dtoPage);
    }

    // ------------------- 生产任务管理 -------------------
    @PostMapping("/tasks")
    public ResponseEntity<TaskDTO> createProductionTask(
            @Valid @RequestBody TaskCreateDTO createDTO,
            @RequestParam Integer planId) {

        ProductionTask task = taskService.createTask(createDTO, planId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToTaskDTO(task));
    }


    @PutMapping("/tasks/{id}/status")
    public ResponseEntity<Void> updateTaskStatus(
            @PathVariable Integer id,
            @RequestParam @NotBlank String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime actualEndTime) throws EntityNotFoundException {

        taskService.updateTaskStatus(id, status, actualEndTime);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/plans/{planId}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByPlan(@PathVariable Integer planId) {
        List<ProductionTask> tasks = taskService.getTasksByPlan(planId);
        return ResponseEntity.ok(tasks.stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/devices/{deviceId}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByDevice(
            @PathVariable Integer deviceId,
            @RequestParam(required = false) String status) {

        List<ProductionTask> tasks = taskService.getTasksByDevice(deviceId, status);
        return ResponseEntity.ok(tasks.stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList()));
    }

    // ------------------- 生产数据采集 -------------------
    @PostMapping("/tasks/{id}/progress")
    public ResponseEntity<Void> reportTaskProgress(
            @PathVariable Integer id,
            @Valid @RequestBody ProgressReportDTO reportDTO) throws EntityNotFoundException {

        taskService.reportTaskProgress(id, reportDTO.getCompletedQuantity());
        return ResponseEntity.ok().build();
    }

    // ------------------- DTO 转换方法 -------------------
    private PlanDTO convertToPlanDTO(ProductionPlan plan) {
        PlanDTO dto = objectMapper.convertValue(plan, PlanDTO.class);
        dto.setCompletedQuantity(taskService.calculateCompletedQuantity(plan.getId()));
        return dto;
    }

    private TaskDTO convertToTaskDTO(ProductionTask task) {
        TaskDTO dto = objectMapper.convertValue(task, TaskDTO.class);
        // 获取设备名称
        dto.setDeviceName(taskService.getDeviceName(task.getDeviceId()));
        return dto;
    }
}