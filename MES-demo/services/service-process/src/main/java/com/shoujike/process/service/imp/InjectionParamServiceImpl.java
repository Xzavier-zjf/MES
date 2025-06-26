package com.shoujike.process.service.imp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoujike.common.exception.BusinessException;
import com.shoujike.common.exception.EntityNotFoundException;

import com.shoujike.process.model.client.ProductionTaskClient;
import com.shoujike.product.model.DTO.TaskDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.shoujike.process.mapper.InjectionParamMapper;
import com.shoujike.process.model.dto.InjectionParamCreateDTO;
import com.shoujike.process.model.dto.InjectionParamDTO;
import com.shoujike.process.model.entity.InjectionParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.shoujike.process.service.InjectionParamService;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InjectionParamServiceImpl implements InjectionParamService {

    private final InjectionParamMapper injectionParamMapper;
    private final ObjectMapper objectMapper;
    private final ProductionTaskClient productionTaskClient;

    @Override
    @Transactional
    public InjectionParamDTO createInjectionParam(InjectionParamCreateDTO createDTO) throws BusinessException {
        // 验证任务是否存在
        verifyTaskExists(createDTO.getTaskId());

        // 检查是否已为该任务创建参数
        if (injectionParamMapper.findByTaskId(createDTO.getTaskId()) != null) {
            throw new BusinessException("该任务已存在工艺参数");
        }

        InjectionParam param = new InjectionParam();
        param.setTaskId(createDTO.getTaskId());
        param.setPressure(createDTO.getPressure());
        param.setInjectionSpeed(createDTO.getInjectionSpeed());
        param.setHoldTime(createDTO.getHoldTime());
        param.setCoolingTime(createDTO.getCoolingTime());
        param.setMoldTemperature(createDTO.getMoldTemperature());
        param.setMaterialTemperature(createDTO.getMaterialTemperature());

        injectionParamMapper.insert(param);
        return convertToDTO(param);
    }

    @Override
    @Transactional
    public InjectionParamDTO updateInjectionParam(Integer id, InjectionParamCreateDTO updateDTO) throws EntityNotFoundException {
        InjectionParam param = injectionParamMapper.selectById(id);
        if (param == null) {
            throw new EntityNotFoundException("注塑工艺参数不存在");
        }

        // 更新参数
        param.setPressure(updateDTO.getPressure());
        param.setInjectionSpeed(updateDTO.getInjectionSpeed());
        param.setHoldTime(updateDTO.getHoldTime());
        param.setCoolingTime(updateDTO.getCoolingTime());
        param.setMoldTemperature(updateDTO.getMoldTemperature());
        param.setMaterialTemperature(updateDTO.getMaterialTemperature());

        injectionParamMapper.updateById(param);
        return convertToDTO(param);
    }

    @Override
    public InjectionParamDTO getInjectionParamById(Integer id) throws EntityNotFoundException {
        InjectionParam param = injectionParamMapper.selectById(id);
        if (param == null) {
            throw new EntityNotFoundException("注塑工艺参数不存在");
        }
        return convertToDTO(param);
    }

    @Override
    public InjectionParamDTO getInjectionParamByTaskId(Integer taskId) throws EntityNotFoundException {
        InjectionParam param = injectionParamMapper.findByTaskId(taskId);
        if (param == null) {
            throw new EntityNotFoundException("该任务未设置工艺参数");
        }
        return convertToDTO(param);
    }

    @Override
    @Transactional
    public void deleteInjectionParam(Integer id) throws EntityNotFoundException {
        if (injectionParamMapper.deleteById(id) == 0) {
            throw new EntityNotFoundException("注塑工艺参数不存在");
        }
    }

    private InjectionParamDTO convertToDTO(InjectionParam param) {
        return objectMapper.convertValue(param, InjectionParamDTO.class);
    }

    private void verifyTaskExists(Integer taskId) throws BusinessException {
        try {
            ResponseEntity<TaskDTO> response = productionTaskClient.getTaskById(taskId);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException("关联的任务不存在");
            }
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            throw new BusinessException("验证任务时出错: " + msg);
        }
    }

}
