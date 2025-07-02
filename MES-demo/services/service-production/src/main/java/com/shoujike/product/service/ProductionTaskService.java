package com.shoujike.product.service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shoujike.common.client.EquipmentClient;
import com.shoujike.common.dto.DeviceDTO;
import com.shoujike.common.exception.EntityNotFoundException;
import com.shoujike.product.mapper.ProductionPlanMapper;
import com.shoujike.product.model.DTO.TaskCreateDTO;
import com.shoujike.product.model.DTO.TaskUpdateDTO;
import com.shoujike.product.model.entity.ProductionPlan;
import com.shoujike.product.model.entity.ProductionTask;
import com.shoujike.product.mapper.ProductionTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class ProductionTaskService extends ServiceImpl<ProductionTaskMapper, ProductionTask> {

    @Autowired
    private EquipmentClient equipmentClient;
    @Autowired
    private ProductionTaskMapper productionTaskMapper;
    @Autowired
    private ProductionPlanMapper planMapper;

    @Transactional
    public ProductionTask createTask(TaskCreateDTO createDTO, Integer planId) {
        ProductionTask task = new ProductionTask();

        // 设置必填字段
        task.setTaskCode(createDTO.getTaskCode());
        task.setPlanId(planId); // ❗从方法参数传入
        task.setDeviceId(createDTO.getDeviceId());
        task.setProcessType(createDTO.getProcessType());
        task.setQuantity(createDTO.getQuantity());
        task.setStartTime(createDTO.getStartTime());
        task.setEndTime(createDTO.getEndTime());

        // 默认状态
        task.setStatus("待下发");

        // 保存到数据库（MyBatis-Plus 的 save 方法）
        save(task);

        return task;
    }

    public void updateTaskStatus(Integer id, String status, LocalDateTime endTime) throws EntityNotFoundException {
        ProductionTask task = getById(id);
        if (task == null) {
            throw new EntityNotFoundException("生产任务不存在");
        }

        // 状态流转校验...
        task.setStatus(status);
        if (endTime != null) {
            task.setEndTime(endTime);
        }
        updateById(task);
    }

    public void updateTask(Integer id, TaskUpdateDTO dto) throws EntityNotFoundException {
        ProductionTask task = getById(id);
        if (task == null) {
            throw new EntityNotFoundException("生产任务不存在");
        }
            task.setTaskCode(dto.getTaskCode());
            task.setDeviceId(dto.getDeviceId());
            task.setQuantity(dto.getQuantity());
            task.setStatus(dto.getStatus());
        updateById(task);
    }
    public List<ProductionTask> getTasksByPlan(Integer planId) {
        return productionTaskMapper.selectByPlanId(Long.valueOf(planId));
    }

    // 实现类

    public void deleteTaskById(Long id) {
        productionTaskMapper.deleteById(id);
    }

    public List<ProductionTask> getTasksByDevice(Integer deviceId, String status) {
        return productionTaskMapper.selectByDeviceAndStatus(deviceId, status);
    }

    public Page<ProductionTask> getTasksByConditions(String planCode, String processType, String status, Pageable pageable) {
        LambdaQueryWrapper<ProductionTask> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(planCode)) {
            QueryWrapper<ProductionPlan> planWrapper = new QueryWrapper<>();
            planWrapper.eq("plan_code", planCode);

            List<ProductionPlan> plans = planMapper.selectList(planWrapper);
            if (plans == null || plans.isEmpty()) {
                return Page.empty(pageable);
            }

            List<Long> planIds = plans.stream()
                    .map(plan -> plan.getId().longValue())  // 把 Integer 转成 Long
                    .collect(Collectors.toList());


            wrapper.in(ProductionTask::getPlanId, planIds);
        }

        if (StringUtils.isNotBlank(processType)) {
            wrapper.eq(ProductionTask::getProcessType, processType);
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(ProductionTask::getStatus, status);
        }

        long current = pageable.getPageNumber() + 1;
        long size = pageable.getPageSize();

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProductionTask> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);

        var result = this.page(mpPage, wrapper);

        return new PageImpl<>(result.getRecords(), pageable, result.getTotal());
    }



    public ProductionTask getTaskById(Integer id) throws EntityNotFoundException {
        ProductionTask task = productionTaskMapper.selectById(id);
        if (task == null) {
            throw new EntityNotFoundException("任务ID不存在: " + id);
        }
        return task;
    }
    public void reportTaskProgress(Integer taskId, Integer completedQuantity) throws EntityNotFoundException {
        ProductionTask task = getById(taskId);
        if (task == null) {
            throw new EntityNotFoundException("生产任务不存在");
        }

        // 进度上报逻辑...
    }

    public Integer calculateCompletedQuantity(Integer planId) {
        return baseMapper.sumCompletedQuantityByPlan(planId);
    }

    public String getDeviceName(Integer deviceId) {
        DeviceDTO device = equipmentClient.getDeviceById(deviceId).getBody();
        return device != null ? device.getName() : "未知设备";
    }

    // 报表功能
    public BigDecimal calculateDeviceOEE(Integer deviceId, String period) {
        // OEE计算逻辑...
        return null;
    }
}
