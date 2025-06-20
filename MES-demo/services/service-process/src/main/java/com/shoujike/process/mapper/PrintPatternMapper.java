package com.shoujike.process.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoujike.process.model.entity.PrintPattern;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PrintPatternMapper extends BaseMapper<PrintPattern> {

    @Select("SELECT * FROM print_pattern WHERE machine_model = #{machineModel}")
    List<PrintPattern> findByMachineModel(@Param("machineModel") String machineModel);

    @Select("SELECT * FROM print_pattern WHERE pattern_code = #{patternCode}")
    PrintPattern findByPatternCode(@Param("patternCode") String patternCode);
}
