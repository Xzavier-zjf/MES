package com.shoujike.process.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoujike.process.model.entity.InjectionParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InjectionParamMapper extends BaseMapper<InjectionParam> {

    @Select("SELECT * FROM injection_param WHERE task_id = #{taskId}")
    InjectionParam findByTaskId(@Param("taskId") String taskId);
}
