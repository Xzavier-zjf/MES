/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80036
 Source Host           : localhost:3306
 Source Schema         : process_service_db

 Target Server Type    : MySQL
 Target Server Version : 80036
 File Encoding         : 65001

 Date: 02/07/2025 17:48:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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

SET FOREIGN_KEY_CHECKS = 1;
