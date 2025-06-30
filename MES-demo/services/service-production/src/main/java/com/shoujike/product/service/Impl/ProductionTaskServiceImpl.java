package com.shoujike.product.service.Impl;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shoujike.product.model.DTO.InjectionTaskInfoDTO;
import com.shoujike.product.model.entity.ProductionPlan;
import com.shoujike.product.model.entity.ProductionTask;
import com.shoujike.product.mapper.ProductionPlanMapper;
import com.shoujike.product.mapper.ProductionTaskMapper;
import com.shoujike.product.service.TaskinjectionService ;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductionTaskServiceImpl implements TaskinjectionService {

    private final ProductionTaskMapper productionTaskMapper;
    private final ProductionPlanMapper planMapper;

    public ProductionTaskServiceImpl(ProductionTaskMapper productionTaskMapper, ProductionPlanMapper  planMapper) {
        this.productionTaskMapper = productionTaskMapper;
        this.planMapper = planMapper;
    }

    @Override
    public List<InjectionTaskInfoDTO> getInjectionTasks() {
        List<ProductionTask> tasks = productionTaskMapper.selectList(
                new QueryWrapper<ProductionTask>().eq("process_type", "注塑")
        );

        return tasks.stream().map(task -> {
            InjectionTaskInfoDTO dto = new InjectionTaskInfoDTO();
            dto.setTaskCode(String.valueOf(task.getId()));

            ProductionPlan plan = planMapper.selectById(task.getPlanId());
            dto.setPlanCode(plan != null ? plan.getPlanCode() : "未知");

            // 通过设备ID查询设备编码，这里你可能需要调用设备服务或者用Mapper查
            String deviceCode = productionTaskMapper.findDeviceCodeById(Long.valueOf(task.getDeviceId()));
            dto.setDeviceCode(deviceCode != null ? deviceCode : "未知");

            return dto;
        }).collect(Collectors.toList());
    }
}
