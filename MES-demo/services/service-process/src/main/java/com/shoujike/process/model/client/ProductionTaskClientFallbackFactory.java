package com.shoujike.process.model.client;

import com.shoujike.product.model.DTO.TaskDTO;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductionTaskClientFallbackFactory implements FallbackFactory<ProductionTaskClient> {

    private static final Logger log = LoggerFactory.getLogger(ProductionTaskClientFallbackFactory.class);

    @Override
    public ProductionTaskClient create(Throwable cause) {
        return new ProductionTaskClient() {
            @Override
            public ResponseEntity<TaskDTO> getTaskById(Integer id) {
                log.error(() ->"调用生产服务获取任务失败: {}", cause);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        };
    }
}
