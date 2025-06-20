package com.shoujike.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoujike.product.model.entity.ProductionTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProductionTaskMapper extends BaseMapper<ProductionTask> {

    @Select("SELECT * FROM production_task WHERE plan_id = #{planId} ORDER BY create_time DESC")
    List<ProductionTask> selectByPlanId(@Param("planId") Long planId);

    @Update("UPDATE production_task SET status = #{status} WHERE id = #{taskId}")
    int updateStatus(@Param("taskId") Long taskId, @Param("status") String status);

    Integer sumCompletedQuantityByPlan(Integer planId);
}

