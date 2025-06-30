package com.shoujike.process.service.imp;

import com.shoujike.process.mapper.InjectionParamMapper;

import com.shoujike.process.model.client.ProductionTaskClient;
import com.shoujike.process.model.entity.InjectionParam;
import com.shoujike.process.service.InjectionTaskService;

import com.shoujike.product.model.DTO.InjectionTaskInfoDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InjectionTaskServiceImpl implements InjectionTaskService {

    private final ProductionTaskClient productionTaskClient;
    private final InjectionParamMapper injectionParamMapper;

    @Override
    public List<InjectionTaskInfoDTO> getInjectionTasks() {
        List<InjectionTaskInfoDTO> tasks = productionTaskClient.getInjectionTasks();

        return tasks.stream().map(dto -> {
            InjectionParam param = injectionParamMapper.findByTaskId(dto.getTaskCode());
            if (param != null) {
                dto.setHasParam(true);
                dto.setPressure(param.getPressure());
                dto.setInjectionSpeed(param.getInjectionSpeed());
                dto.setHoldTime(param.getHoldTime());
                dto.setCoolingTime(param.getCoolingTime());
                dto.setMoldTemperature(param.getMoldTemperature());
                dto.setMaterialTemperature(param.getMaterialTemperature());
            } else {
                dto.setHasParam(false);
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
