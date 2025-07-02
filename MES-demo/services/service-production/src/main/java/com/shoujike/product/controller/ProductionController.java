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
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private RestTemplate restTemplate;

    // ------------------- 生产计划管理 -------------------
    @PostMapping("/plans")
    public ResponseEntity<PlanDTO> createProductionPlan(
            @Valid @RequestBody PlanCreateDTO createDTO) {

        ProductionPlan plan = planService.createPlanWithTasks(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToPlanDTO(plan));
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<Void> updatePlan(
            @PathVariable Integer id,
            @RequestBody @Validated PlanUpdateRequest request) {
        try {
            planService.updatePlanFields(id, request);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();  // 返回 404
        }
    }
    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Integer id) {
        boolean removed = planService.removeById(id);
        if (removed) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/plans")
    public ResponseEntity<Page<PlanDTO>> getPlansByStatus(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ProductionPlan> plans = planService.getPlansByStatus(status, pageable);

        // 防止 plans 为 null 的极端情况
        if (plans == null || plans.isEmpty()) {
            return ResponseEntity.ok(Page.empty(pageable));
        }

        Page<PlanDTO> dtoPage = plans.map(this::convertToPlanDTO);
        return ResponseEntity.ok(dtoPage);
    }


    // ------------------- 生产任务管理 -------------------
    @PostMapping("/tasks")
    public ResponseEntity<TaskDTO> createProductionTask(
            @Valid @RequestBody TaskCreateDTO createDTO,
            @RequestParam Integer planId) {

        ProductionTask task = taskService.createTask(createDTO, planId);


        Map<String, Object> params = new HashMap<>();
        params.put("taskId", task.getId());
        params.put("planId", task.getPlanId());
        params.put("deviceId", task.getDeviceId());

        // 根据工序类型选择接口路径
        String processType = createDTO.getProcessType();
        if ("注塑".equals(processType)) {
            restTemplate.postForObject(
                    "http://service-process/api/v1/process/injection-params",
                    params,
                    Void.class
            );
        } else if ("印刷".equals(processType)) {
            restTemplate.postForObject(
                    "http://service-process/api/v1/process/print-patterns",
                    params,
                    Void.class
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToTaskDTO(task));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<Void> updateTask(
            @PathVariable Integer id,
            @Valid @RequestBody TaskUpdateDTO dto)throws EntityNotFoundException  {
        taskService.updateTask(id, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/plans/code-to-id")
    public ResponseEntity<Integer> getPlanIdByCode(@RequestParam String code) {
        ProductionPlan plan = planService.lambdaQuery()
                .eq(ProductionPlan::getPlanCode, code)
                .one();
        return plan != null ? ResponseEntity.ok(plan.getId()) : ResponseEntity.notFound().build();
    }
    @GetMapping("/tasks")
    public ResponseEntity<Page<TaskDTO>> getTasks(
            @RequestParam(required = false) String planCode,
            @RequestParam(required = false) String processType,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ProductionTask> taskPage = taskService.getTasksByConditions(planCode, processType, status, pageable);
        return ResponseEntity.ok(taskPage.map(this::convertToDTO));
    }



    private TaskDTO convertToDTO(ProductionTask task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTaskCode(task.getTaskCode());
        dto.setPlanId(task.getPlanId());
        dto.setDeviceId(task.getDeviceId());
        dto.setProcessType(task.getProcessType());
        dto.setQuantity(task.getQuantity());
        dto.setStatus(task.getStatus());
        return dto;
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
    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Integer id) throws EntityNotFoundException {
        ProductionTask task = taskService.getTaskById(id); // 你自己实现获取逻辑
        return ResponseEntity.ok(convertToTaskDTO(task));
    }
    @GetMapping("/plans/codes")
    public ResponseEntity<List<String>> getAllPlanCodes() {
        List<String> codes = planService.list().stream()
                .map(ProductionPlan::getPlanCode)
                .collect(Collectors.toList());
        return ResponseEntity.ok(codes);
    }

    @GetMapping("/plans/{planId}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByPlan(@PathVariable Integer planId) {
        List<ProductionTask> tasks = taskService.getTasksByPlan(planId);
        return ResponseEntity.ok(tasks.stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList()));
    }
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
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