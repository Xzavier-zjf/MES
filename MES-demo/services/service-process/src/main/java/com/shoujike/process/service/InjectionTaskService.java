package com.shoujike.process.service;

import com.shoujike.product.model.DTO.InjectionTaskInfoDTO;

import java.util.List;

public interface InjectionTaskService {
    List<InjectionTaskInfoDTO> getInjectionTasks();
}
