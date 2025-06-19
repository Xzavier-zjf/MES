package com.shoujike.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoujike.product.entity.ProductionPlan;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductionPlanMapper extends BaseMapper<ProductionPlan> {
    void updateStatus(Long planId, String issued);
}