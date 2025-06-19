package com.shoujike.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoujike.product.entity.ProductionTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.shoujike.product.entity.ProductionPlan;

import java.util.List;

@Mapper
public interface ProductionTaskMapper extends BaseMapper<ProductionTask> {

    @Select("SELECT * FROM production_task WHERE plan_id = #{planId}")
    List<ProductionTask> selectByPlanId(@Param("planId") Long planId);

    @Select("SELECT * FROM production_task WHERE device_id = #{deviceId} AND status = #{status}")
    List<ProductionTask> selectByDeviceIdAndStatus(@Param("deviceId") Long deviceId,
                                                   @Param("status") String status);

    @Select("SELECT * FROM production_task WHERE end_time IS NULL AND status = 'in_progress'")
    List<ProductionTask> selectInProgressTasks();
}

