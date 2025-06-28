package com.shoujike.order.controller;

import com.shoujike.common.exception.BusinessException;
import com.shoujike.common.exception.EntityNotFoundException;
import com.shoujike.order.model.DTO.*;
import com.shoujike.order.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/equipment")
@Validated
public class EquipmentController {

    @Autowired
    private DeviceService deviceService;

    // ------------------- 设备基础管理 -------------------
    @PostMapping("/devices")
    public ResponseEntity<DeviceDTO> createDevice(
            @Valid @RequestBody DeviceCreateDTO createDTO) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceService.createDevice(createDTO));
    }

    @GetMapping("/devices")
    public ResponseEntity<Page<DeviceDTO>> listDevices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String name, // 新增：接收 name 参数
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        // 将 name 传递给服务层
        return ResponseEntity.ok(deviceService.listDevices(status, name, pageable));
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable Integer id) throws EntityNotFoundException {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    // ------------------- 设备状态管理 -------------------
    @PutMapping("/devices/{id}/status")
    public ResponseEntity<Void> updateDeviceStatus(
            @PathVariable Integer id,
            @Valid @RequestBody StatusUpdateDTO updateDTO) throws BusinessException, EntityNotFoundException {

        deviceService.updateDeviceStatus(id, updateDTO.getStatus());
        return ResponseEntity.ok().build();
    }

    // ------------------- 设备运行数据管理 -------------------
    @PostMapping("/devices/{id}/runtime")
    public ResponseEntity<Void> updateRuntimeData(
            @PathVariable Integer id,
            @Valid @RequestBody RuntimeUpdateDTO updateDTO) throws EntityNotFoundException {

        deviceService.updateRuntimeData(id, updateDTO.getAddedMinutes(), updateDTO.getAddedCycles());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/devices/{id}/maintenance")
    public ResponseEntity<Void> recordMaintenance(
            @PathVariable Integer id,
            @RequestParam String maintenanceInfo) throws BusinessException, EntityNotFoundException {

        deviceService.recordMaintenance(id, maintenanceInfo);
        return ResponseEntity.ok().build();
    }

    // ------------------- 设备性能分析 -------------------
    @PostMapping("/oee")
    public ResponseEntity<BigDecimal> calculateOEE(
            @Valid @RequestBody OEECalculationDTO calculationDTO) throws EntityNotFoundException {

        return ResponseEntity.ok(deviceService.calculateOEE(
                calculationDTO.getDeviceId(), calculationDTO.getPeriod()
        ));
    }

    @GetMapping("/devices/{id}/utilization")
    public ResponseEntity<BigDecimal> getDeviceUtilization(
            @PathVariable Integer id,
            @RequestParam String period) throws BusinessException {

        return ResponseEntity.ok(deviceService.calculateUtilization(id, period));
    }
}
