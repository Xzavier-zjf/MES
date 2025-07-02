package com.shoujike.process.service;

import com.shoujike.common.exception.BusinessException;
import com.shoujike.common.exception.EntityNotFoundException;
import com.shoujike.process.model.dto.InjectionParamCreateDTO;
import com.shoujike.process.model.dto.InjectionParamDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InjectionParamService {
    InjectionParamDTO createInjectionParam(InjectionParamCreateDTO createDTO) throws BusinessException;
    InjectionParamDTO updateInjectionParam(Integer id, InjectionParamCreateDTO updateDTO) throws EntityNotFoundException;
    InjectionParamDTO getInjectionParamById(Integer id) throws EntityNotFoundException;
    InjectionParamDTO getInjectionParamByTaskId(String taskId) throws EntityNotFoundException;
    void deleteInjectionParam(Integer id) throws EntityNotFoundException;
    Page<InjectionParamDTO> getAllInjectionParams(Pageable pageable);



}