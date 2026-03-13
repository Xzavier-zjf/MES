/*
 MES统一数据库
 将所有微服务的表合并到一个MES数据库中
 
 Date: 08/08/2025
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建MES数据库
CREATE DATABASE IF NOT EXISTS `MES` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `MES`;

-- ========================================
-- 认证服务表 (原auth_service_db)
-- ========================================

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '123', '123');

-- ========================================
-- 设备服务表 (原equipment_service_db)
-- ========================================

-- ----------------------------
-- Table structure for device
-- ----------------------------
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `device_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备名称',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '闲置' COMMENT '设备状态',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备类型',
  `runtime_minutes` int(0) NOT NULL DEFAULT 0 COMMENT '运行时长(分钟)',
  `open_close_times` int(0) NOT NULL DEFAULT 0 COMMENT '开合模次数',
  `last_maintenance_time` datetime(0) NULL DEFAULT NULL COMMENT '最后维护时间',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `injection_time` float NULL DEFAULT NULL COMMENT '注塑时间',
  `injection_pressure` float NULL DEFAULT NULL COMMENT '注塑压力',
  `printing_speed` float NULL DEFAULT NULL COMMENT '印刷速度',
  `printing_pressure` float NULL DEFAULT NULL COMMENT '印刷压力',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of device
-- ----------------------------
INSERT INTO `device` VALUES (1, 'INJ-001', '注塑机A型', '空闲', '注塑', 125, 4800, '2025-06-20 10:00:00', '2025-07-02 11:00:58', '2025-07-02 16:50:50', 12.5, 85.2, NULL, NULL);
INSERT INTO `device` VALUES (2, 'INJ-002', '注塑机B型', '运行中', '注塑', 183, 7200, '2025-06-25 14:30:00', '2025-07-02 11:00:58', '2025-07-02 16:50:52', 10.8, 92.7, NULL, NULL);
INSERT INTO `device` VALUES (3, 'INJ-003', '注塑机C型', '故障', '注塑', 92, 3500, '2025-05-15 09:00:00', '2025-07-02 11:00:58', '2025-07-02 16:50:54', 11.2, 88.5, NULL, NULL);
INSERT INTO `device` VALUES (4, 'PRT-101', '印刷机A型', '空闲', '印刷', 83, 0, '2025-06-18 11:00:00', '2025-07-02 11:00:58', '2025-07-02 16:50:55', NULL, NULL, 1200, 3.2);
INSERT INTO `device` VALUES (5, 'PRT-102', '印刷机B型', '运行中', '印刷', 156, 0, '2025-06-28 16:45:00', '2025-07-02 11:00:58', '2025-07-02 16:50:57', NULL, NULL, 1100, 2.8);
INSERT INTO `device` VALUES (6, 'PRT-103', '印刷机C型', '运行中', '印刷', 112, 0, '2025-06-10 13:20:00', '2025-07-02 11:00:58', '2025-07-02 16:50:59', NULL, NULL, 1300, 3.2);

-- ========================================
-- 工艺服务表 (原process_service_db)
-- ========================================

-- ----------------------------
-- Table structure for injection_param
-- ----------------------------
DROP TABLE IF EXISTS `injection_param`;
CREATE TABLE `injection_param`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `plan_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计划编号',
  `task_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对应任务ID',
  `device_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备编号',
  `quantity` int(0) NOT NULL COMMENT '生产数量',
  `pressure` float NULL DEFAULT NULL COMMENT '注塑压力(MPa)',
  `injection_speed` float NULL DEFAULT NULL COMMENT '注塑速度(mm/s)',
  `hold_time` float NULL DEFAULT NULL COMMENT '保压时间(s)',
  `cooling_time` float NULL DEFAULT NULL COMMENT '冷却时间(s)',
  `mold_temperature` float NULL DEFAULT NULL COMMENT '模具温度(℃)',
  `material_temperature` float NULL DEFAULT NULL COMMENT '料筒温度(℃)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '注塑工艺参数表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of injection_param
-- ----------------------------
INSERT INTO `injection_param` VALUES (1, 'PLAN-20250701-001', '1', 'INJ-002', 100, 92.5, 45.2, 8.5, 15.2, 65, 205);
INSERT INTO `injection_param` VALUES (2, 'PLAN-20250701-002', '3', 'INJ-001', 100, 85, 42.8, 7.8, 14.5, 62, 200);
INSERT INTO `injection_param` VALUES (3, 'PLAN-20250625-001', '5', 'INJ-002', 100, 88.3, 43.5, 8, 14.8, 63.5, 202);

-- ----------------------------
-- Table structure for print_pattern
-- ----------------------------
DROP TABLE IF EXISTS `print_pattern`;
CREATE TABLE `print_pattern`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `plan_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `task_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `device_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `quantity` int(0) NOT NULL COMMENT '生产数量',
  `pattern_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图案编号',
  `pattern_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图案名称',
  `machine_model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适用机型',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图案图像地址',
  `default_print_speed` int(0) NULL DEFAULT NULL COMMENT '默认印刷速度(次/小时)',
  `default_pressure` float NULL DEFAULT NULL COMMENT '默认印刷压力(kg/cm²)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pattern_code`(`pattern_code`) USING BTREE,
  INDEX `idx_machine_model`(`machine_model`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '印刷图案信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of print_pattern
-- ----------------------------
INSERT INTO `print_pattern` VALUES (1, 'PLAN-20250701-001', '2', 'PRT-102', 100, 'PAT-001', '几何星空', 'B', '/uploads/patterns/4106e9db-40d5-4922-96d8-3240a6c99e3f_R-C (1).jpg', 1200, 3.2);
INSERT INTO `print_pattern` VALUES (2, 'PLAN-20250701-002', '4', 'PRT-103', 100, 'PAT-002', '大理石纹', 'A', '/uploads/patterns/deeaa61e-3742-40dc-9e41-3507caf50e5e_R-C.jpg', 1100, 2.8);
INSERT INTO `print_pattern` VALUES (3, 'PLAN-20250625-001', '6', 'PRT-102', 100, 'PAT-003', '中国风祥云', 'B', '/uploads/patterns/819aad2c-5506-4a54-9288-1836982876ed_2126.png_860.png', 1300, 3.5);

-- ========================================
-- 生产服务表 (原production_service_db)
-- ========================================

-- ----------------------------
-- Table structure for production_plan
-- ----------------------------
DROP TABLE IF EXISTS `production_plan`;
CREATE TABLE `production_plan`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `plan_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计划编号',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产品名称',
  `total_quantity` int(0) NOT NULL COMMENT '总生产数量',
  `priority` int(0) NULL DEFAULT 0 COMMENT '优先级',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'draft' COMMENT '状态:草稿、已下发、已完成',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_code`(`plan_code`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产计划表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of production_plan
-- ----------------------------
INSERT INTO `production_plan` VALUES (1, 'PLAN-20250701-001', 'iPhone 15 保护壳', 5000, 1, '进行中', '2025-07-01 09:00:00');
INSERT INTO `production_plan` VALUES (2, 'PLAN-20250701-002', '三星 Galaxy S24 保护壳', 3000, 2, '待下发', '2025-07-01 10:30:00');
INSERT INTO `production_plan` VALUES (3, 'PLAN-20250625-001', '华为 Mate 60 保护壳', 8000, 1, '已完成', '2025-06-25 14:00:00');

-- ----------------------------
-- Table structure for production_task
-- ----------------------------
DROP TABLE IF EXISTS `production_task`;
CREATE TABLE `production_task`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `task_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `plan_id` int(0) NOT NULL,
  `device_id` int(0) NOT NULL,
  `process_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `quantity` int(0) NOT NULL,
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `start_time` datetime(0) NULL DEFAULT NULL,
  `end_time` datetime(0) NULL DEFAULT NULL,
  `injection_param_id` int(0) NULL DEFAULT NULL,
  `print_pattern_id` int(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of production_task
-- ----------------------------
INSERT INTO `production_task` VALUES (1, 'TASK-P001-1', 1, 2, '注塑', 5000, '进行中', '2025-07-02 08:00:00', NULL, NULL, NULL);
INSERT INTO `production_task` VALUES (2, 'TASK-P001-2', 1, 5, '印刷', 5000, '待下发', NULL, NULL, NULL, NULL);
INSERT INTO `production_task` VALUES (3, 'TASK-P002-1', 2, 1, '注塑', 3000, '待下发', NULL, NULL, NULL, NULL);
INSERT INTO `production_task` VALUES (4, 'TASK-P002-2', 2, 6, '印刷', 3000, '待下发', NULL, NULL, NULL, NULL);
INSERT INTO `production_task` VALUES (5, 'TASK-P003-1', 3, 2, '注塑', 8000, '已完成', '2025-06-26 09:00:00', '2025-06-28 18:00:00', NULL, NULL);
INSERT INTO `production_task` VALUES (6, 'TASK-P003-2', 3, 5, '印刷', 8000, '已完成', '2025-06-29 08:30:00', '2025-07-01 17:00:00', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;