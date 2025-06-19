package com.shoujike.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("production_task")
@NoArgsConstructor
@AllArgsConstructor
public class ProductionTask {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "plan_id")
    private Long planId;

    @TableField(value = "device_id")
    private Long deviceId;

    @TableField(value = "process_type")
    private String processType;

    @TableField(value = "quantity")
    private Integer quantity;

    @TableField(value = "status")
    private String status;

    @TableField(value = "start_time")
    private LocalDateTime startTime;

    @TableField(value = "end_time")
    private LocalDateTime endTime;

    @TableField(value = "injection_param_id")
    private Long injectionParamId;

    @TableField(value = "print_pattern_id")
    private Long printPatternId;

    // 工艺类型枚举
    public enum ProcessType {
        INJECTION("injection", "注塑"),
        PRINTING("printing", "印刷");

        private final String code;
        private final String desc;

        ProcessType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    // 任务状态枚举
    public enum TaskStatus {
        PENDING("pending", "未开始"),
        IN_PROGRESS("in_progress", "进行中"),
        COMPLETED("completed", "已完成");

        private final String code;
        private final String desc;

        TaskStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}