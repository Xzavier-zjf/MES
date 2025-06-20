package com.shoujike.order.service;

import com.shoujike.common.exception.BusinessException;
import com.shoujike.common.exception.EntityNotFoundException;
import com.shoujike.order.model.DTO.DeviceCreateDTO;
import com.shoujike.order.model.DTO.DeviceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface DeviceService {
    DeviceDTO createDevice(DeviceCreateDTO createDTO);
    Page<DeviceDTO> listDevices(String status, Pageable pageable);
    DeviceDTO getDeviceById(Integer id) throws EntityNotFoundException;
    void updateDeviceStatus(Integer id, String status) throws EntityNotFoundException, BusinessException;
    void updateRuntimeData(Integer id, Integer addedMinutes, Integer addedCycles) throws EntityNotFoundException;
    void recordMaintenance(Integer id, String maintenanceInfo) throws EntityNotFoundException, BusinessException;
    BigDecimal calculateOEE(Integer deviceId, String period) throws EntityNotFoundException;
    BigDecimal calculateUtilization(Integer deviceId, String period) throws BusinessException;
}
