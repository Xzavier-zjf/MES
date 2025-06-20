package com.shoujike.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shoujike.order.model.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
    // 自定义SQL可以在此处添加
    // 例如：按时间范围统计运行时间
    @Select("SELECT SUM(runtime_minutes) FROM device WHERE id = #{deviceId} AND updated_at BETWEEN #{start} AND #{end}")
    Long sumRuntimeByPeriod(@Param("deviceId") Integer deviceId,
                            @Param("start") Date start,
                            @Param("end") Date end);
}
