package com.shoujike.product.controller;



import com.shoujike.product.model.DTO.InjectionTaskInfoDTO;
import com.shoujike.product.service.TaskinjectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/production/tasks")
public class ProductionTaskinjectionController {

    private final TaskinjectionService taskinjectionService;

    public ProductionTaskinjectionController(TaskinjectionService taskinjectionService) {
        this.taskinjectionService = taskinjectionService;
    }

    /**
     * 获取所有注塑工艺类型的任务列表（包括 taskCode, planCode, deviceCode）
     */
    @GetMapping("/injection")
    public List<InjectionTaskInfoDTO> getInjectionTasks() {
        return taskinjectionService.getInjectionTasks();
    }
}

