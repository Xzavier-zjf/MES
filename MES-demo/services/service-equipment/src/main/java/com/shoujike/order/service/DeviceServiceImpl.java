package com.shoujike.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoujike.common.exception.BusinessException;
import com.shoujike.common.exception.EntityNotFoundException;
import com.shoujike.order.mapper.DeviceMapper;
import com.shoujike.order.model.DTO.DeviceCreateDTO;
import com.shoujike.order.model.DTO.DeviceDTO;
import com.shoujike.order.model.DTO.ProductionTaskDTO;
import com.shoujike.order.model.client.ProductionTaskClient;
import com.shoujike.order.model.entity.Device;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductionTaskClient productionTaskClient; // Feign 客户端

    @Override
    @Transactional
    public DeviceDTO createDevice(DeviceCreateDTO createDTO) {
        Device device = new Device();
        device.setDeviceCode(createDTO.getDeviceCode());
        device.setName(createDTO.getName());
        device.setStatus(StringUtils.isNotBlank(createDTO.getStatus()) ? createDTO.getStatus() : "闲置");

        // 为所有字段赋值
        device.setType(createDTO.getType()!= null? createDTO.getType() : "");
        device.setInjectionTime(createDTO.getInjectionTime() != null ? createDTO.getInjectionTime() : 0);
        device.setInjectionPressure(createDTO.getInjectionPressure() != null ? createDTO.getInjectionPressure() : 0.0f);
        device.setRuntimeMinutes(createDTO.getRuntimeMinutes() != null ? createDTO.getRuntimeMinutes() : 0);
        device.setOpenCloseTimes(createDTO.getOpenCloseTimes() != null ? createDTO.getOpenCloseTimes() : 0);
        device.setLastMaintenanceTime(createDTO.getLastMaintenanceTime() != null ? createDTO.getLastMaintenanceTime() : null);
        device.setPrintingSpeed(createDTO.getPrintingSpeed() != null ? createDTO.getPrintingSpeed() : 0.0f);
        device.setPrintingPressure(createDTO.getPrintingPressure()!= null? createDTO.getPrintingPressure() : 0.0f);
        save(device);
        return convertToDTO(device);
    }

    @Override
    public Page<DeviceDTO> listDevices(String status, String name, Pageable pageable) {
        // 创建 MyBatis-Plus 的 Page 对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Device> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                        pageable.getPageNumber() + 1,  // MyBatis-Plus 页码从1开始
                        pageable.getPageSize()
                );

        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();

        // 状态筛选
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(Device::getStatus, status);
        }

        // 新增：名称模糊筛选
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(Device::getName, name);
        }

        // 修复：使用 Pageable 的排序而不是固定排序
        pageable.getSort().forEach(order -> {
            String property = order.getProperty();
            Sort.Direction direction = order.getDirection();

        });

        // 使用 MP 的 page 方法
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Device> devicePage = page(mpPage, wrapper);

        // 转换为 DTO 页面
        List<DeviceDTO> dtoList = devicePage.getRecords()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 创建 Spring Data Page 对象返回
        return new PageImpl<>(
                dtoList,
                PageRequest.of((int) (devicePage.getCurrent() - 1), (int) devicePage.getSize(), pageable.getSort()),
                devicePage.getTotal()
        );
    }

    @Override
    public DeviceDTO getDeviceById(Integer id) throws EntityNotFoundException {
        Device device = getById(id);
        if (device == null) {
            throw new EntityNotFoundException("设备不存在");
        }
        return convertToDTO(device);
    }

    @Override
    @Transactional
    public void updateDeviceStatus(Integer id, String status) throws EntityNotFoundException, BusinessException {
        Device device = getById(id);
        if (device == null) {
            throw new EntityNotFoundException("设备不存在");
        }

        // 状态流转验证
        if ("维护中".equals(status) && !"故障".equals(device.getStatus())) {
            throw new BusinessException("只有故障设备可进入维护状态");
        }

        device.setStatus(status);
        updateById(device);
    }

    @Override
    @Transactional
    public void updateRuntimeData(Integer id, Integer addedMinutes, Integer addedCycles) throws EntityNotFoundException {
        Device device = getById(id);
        if (device == null) {
            throw new EntityNotFoundException("设备不存在");
        }

        // 更新运行时数据
        device.setRuntimeMinutes(device.getRuntimeMinutes() + addedMinutes);
        device.setOpenCloseTimes(device.getOpenCloseTimes() + addedCycles);

        // 如果设备在故障状态但开始运行，自动更新为运行状态
        if ("故障".equals(device.getStatus()) && addedMinutes > 0) {
            device.setStatus("运行");
        }

        updateById(device);
    }

    @Override
    @Transactional
    public void recordMaintenance(Integer id, String maintenanceInfo) throws EntityNotFoundException, BusinessException {
        Device device = getById(id);
        if (device == null) {
            throw new EntityNotFoundException("设备不存在");
        }

        // 只能维护"维护中"的设备
        if (!"维护中".equals(device.getStatus())) {
            throw new BusinessException("只有维护中设备可记录维护信息");
        }

        // 实际项目中可能需要单独的表存储维护记录
        device.setStatus("闲置"); // 维护完成设为闲置状态
        device.setLastMaintenanceTime(LocalDateTime.now());
        updateById(device);
    }

    @Override
    public BigDecimal calculateOEE(Integer deviceId, String period) throws EntityNotFoundException {
        // 1. 获取设备在指定时间段内的运行数据
        Device device = getById(deviceId);
        if (device == null) {
            throw new EntityNotFoundException("设备不存在");
        }

        // 2. 通过Feign客户端获取生产任务数据
        List<ProductionTaskDTO> tasks = productionTaskClient.getTasksByDevice(deviceId, period);

        // 3. 计算OEE
        long totalRuntime = 0;
        int goodProducts = 0;
        int totalProducts = 0;

        for (ProductionTaskDTO task : tasks) {
            totalRuntime += task.getActualRuntime();
            goodProducts += task.getGoodQuantity();
            totalProducts += task.getQuantity();
        }

        // 防止除零错误
        if (totalRuntime == 0 || totalProducts == 0) {
            return BigDecimal.ZERO;
        }

        // OEE = (合格产品数 / 总产品数) * (总运行时 / 计划运行时)
        BigDecimal qualityRate = BigDecimal.valueOf(goodProducts)
                .divide(BigDecimal.valueOf(totalProducts), 4, RoundingMode.HALF_UP);

        // 假设每天计划运行8小时 (480分钟)
        BigDecimal plannedRuntime = "daily".equals(period) ?
                BigDecimal.valueOf(480) :
                "weekly".equals(period) ?
                        BigDecimal.valueOf(480 * 7) :
                        BigDecimal.valueOf(480 * 30);

        BigDecimal performanceRate = BigDecimal.valueOf(totalRuntime)
                .divide(plannedRuntime, 4, RoundingMode.HALF_UP);

        return qualityRate.multiply(performanceRate)
                .multiply(BigDecimal.valueOf(0.95)) // 加入可用率因子
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateUtilization(Integer deviceId, String period) throws BusinessException {
        // 获取设备在该时间段内的总运行时间
        long actualRuntime = deviceRuntimeSum(deviceId, period);

        // 计算该时间段的总时间（分钟）
        long totalMinutes = 0;
        switch (period) {
            case "daily":
                totalMinutes = 24 * 60;
                break;
            case "weekly":
                totalMinutes = 7 * 24 * 60;
                break;
            case "monthly":
                totalMinutes = 30 * 24 * 60; // 简化计算
                break;
            default:
                throw new BusinessException("不支持的周期类型");
        }

        return BigDecimal.valueOf(actualRuntime)
                .divide(BigDecimal.valueOf(totalMinutes), 4, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // DTO转换方法
    private DeviceDTO convertToDTO(Device device) {
        DeviceDTO dto = objectMapper.convertValue(device, DeviceDTO.class);
        dto.setLastMaintenanceTime(device.getLastMaintenanceTime());
        dto.setCreatedAt(device.getCreatedAt());
        dto.setUpdatedAt(device.getUpdatedAt());
        return dto;
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    // 获取设备在指定时间段内的运行时间总和（伪代码）
    private long deviceRuntimeSum(Integer deviceId, String period) {
        // 实际实现需要查询数据库或调用其他服务
        return 2400; // 40小时运行时间
    }
}