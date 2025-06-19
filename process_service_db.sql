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

 Date: 19/06/2025 21:55:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for injection_param
-- ----------------------------
DROP TABLE IF EXISTS `injection_param`;
CREATE TABLE `injection_param`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `task_id` int(0) NOT NULL COMMENT '对应任务ID',
  `pressure` float NULL DEFAULT NULL COMMENT '注塑压力(MPa)',
  `injection_speed` float NULL DEFAULT NULL COMMENT '注塑速度(mm/s)',
  `hold_time` float NULL DEFAULT NULL COMMENT '保压时间(s)',
  `cooling_time` float NULL DEFAULT NULL COMMENT '冷却时间(s)',
  `mold_temperature` float NULL DEFAULT NULL COMMENT '模具温度(℃)',
  `material_temperature` float NULL DEFAULT NULL COMMENT '料筒温度(℃)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '注塑工艺参数表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of injection_param
-- ----------------------------

-- ----------------------------
-- Table structure for print_pattern
-- ----------------------------
DROP TABLE IF EXISTS `print_pattern`;
CREATE TABLE `print_pattern`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `pattern_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图案编号',
  `pattern_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图案名称',
  `machine_model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '适用机型',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图案图像地址',
  `default_print_speed` int(0) NULL DEFAULT NULL COMMENT '默认印刷速度(次/小时)',
  `default_pressure` float NULL DEFAULT NULL COMMENT '默认印刷压力(kg/cm²)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pattern_code`(`pattern_code`) USING BTREE,
  INDEX `idx_machine_model`(`machine_model`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '印刷图案信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of print_pattern
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
