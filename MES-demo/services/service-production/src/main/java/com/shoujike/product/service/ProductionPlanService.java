package com.shoujike.product.service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shoujike.common.exception.EntityNotFoundException;
import com.shoujike.product.model.DTO.PlanCreateDTO;
import com.shoujike.product.model.DTO.TaskCreateDTO;
import com.shoujike.product.model.entity.ProductionPlan;
import com.shoujike.product.model.entity.ProductionTask;
import com.shoujike.product.mapper.ProductionPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionPlanService extends ServiceImpl<ProductionPlanMapper, ProductionPlan> {

    @Autowired
    private ProductionTaskService taskService;

    @Transactional
    public ProductionPlan createPlanWithTasks(PlanCreateDTO createDTO) {
        // 转换为主实体
        ProductionPlan plan = convertToEntity(createDTO);

        // 保存计划
        save(plan);

        // 创建关联任务
//        List<ProductionTask> tasks = createDTO.getTasks().stream()
//                .map(dto -> convertToTaskEntity(dto, plan.getId()))
//                .collect(Collectors.toList());
//
//        taskService.saveBatch(tasks);

        return plan;
    }

    public void updatePlanStatus(Integer id, String status) throws EntityNotFoundException {
        ProductionPlan plan = getById(id);
        if (plan == null) {
            throw new EntityNotFoundException("生产计划不存在");
        }

        // 状态流转校验...
        plan.setStatus(status);
        updateById(plan);
    }

    public Page<ProductionPlan> getPlansByStatus(String status, Pageable pageable) {
        LambdaQueryWrapper<ProductionPlan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(ProductionPlan::getStatus, status);
        }
        wrapper.orderByDesc(ProductionPlan::getPriority);

        // PageHelper 的页码从1开始，而Pageable从0开始
        long current = pageable.getPageNumber() + 1;
        long size = pageable.getPageSize();

        // 使用 MyBatis-Plus 提供的分页
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProductionPlan> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProductionPlan> result =
                this.page(mpPage, wrapper);

        // ✅ Null 安全处理
        if (result == null || result.getRecords() == null) {
            return Page.empty(pageable);
        }

        // ✅ 转为 Spring Page 并返回
        return new PageImpl<>(result.getRecords(), pageable, result.getTotal());
    }



    private ProductionPlan convertToEntity(PlanCreateDTO dto) {
        ProductionPlan plan = new ProductionPlan();



        plan.setPlanCode(dto.getPlanCode());  // ✅ 重点是这一行
        plan.setProductName(dto.getProductName());
        plan.setTotalQuantity(dto.getTotalQuantity());
        plan.setCreateTime(LocalDateTime.now());
        return plan;
    }

    private ProductionTask convertToTaskEntity(TaskCreateDTO dto, Integer planId) {
        ProductionTask task = new ProductionTask();
        task.setPlanId(planId);
        task.setDeviceId(dto.getDeviceId());              // ⭐ 必填：device_id
        task.setProcessType(dto.getProcessType());        // ⭐ 工序类型
        task.setQuantity(dto.getQuantity());              // ⭐ 数量
        task.setStartTime(dto.getStartTime()); // ⭐ 计划开始时间
        task.setEndTime(dto.getEndTime());     // ⭐ 计划结束时间
        return task;
    }


}