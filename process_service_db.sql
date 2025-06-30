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

 Date: 28/06/2025 22:23:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for injection_param
-- ----------------------------
DROP TABLE IF EXISTS `injection_param`;
CREATE TABLE `injection_param`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `plan_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计划编号',
  `task_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对应任务ID',
  `device_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备编号',
  `pressure` float NULL DEFAULT NULL COMMENT '注塑压力(MPa)',
  `injection_speed` float NULL DEFAULT NULL COMMENT '注塑速度(mm/s)',
  `hold_time` float NULL DEFAULT NULL COMMENT '保压时间(s)',
  `cooling_time` float NULL DEFAULT NULL COMMENT '冷却时间(s)',
  `mold_temperature` float NULL DEFAULT NULL COMMENT '模具温度(℃)',
  `material_temperature` float NULL DEFAULT NULL COMMENT '料筒温度(℃)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '注塑工艺参数表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of injection_param
-- ----------------------------
INSERT INTO `injection_param` VALUES (1, 'PLAN001', '1001', 'SJ001', 85.5, 120.3, 15.2, 20, 180.5, 220);
INSERT INTO `injection_param` VALUES (2, 'PLAN002', '1002', 'SJ002', 80, 150.5, 12.8, 18, 175, 215);
INSERT INTO `injection_param` VALUES (3, 'PLAN003', '1003', 'SJ003', 90.2, 130.7, 10.5, 22, 185, 225);
INSERT INTO `injection_param` VALUES (4, 'PLAN004', '1004', 'SJ004', 88, 140.2, 14.5, 19, 178, 218);
INSERT INTO `injection_param` VALUES (5, 'PLAN005', '1005', 'SJ005', 82.5, 125.8, 13.2, 21, 172, 212);
INSERT INTO `injection_param` VALUES (6, 'PLAN006', '1006', 'SJ006', 86.3, 135, 11.8, 23, 182, 222);
INSERT INTO `injection_param` VALUES (7, 'PLAN007', '1007', 'SJ007', 84.7, 145.5, 16, 17, 177, 217);
INSERT INTO `injection_param` VALUES (8, 'PLAN008', '1008', 'SJ008', 92, 115.2, 12.5, 20, 188, 228);
INSERT INTO `injection_param` VALUES (9, 'PLAN009', '1009', 'SJ009', 81, 160, 10, 24, 170, 210);
INSERT INTO `injection_param` VALUES (10, 'PLAN010', '1010', 'SJ010', 89.5, 120.8, 18.2, 18, 183, 223);

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
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '印刷图案信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of print_pattern
-- ----------------------------
INSERT INTO `print_pattern` VALUES (1, 'PC001', '图案1', 'ModelX', 'http://example.com/image1.jpg', 800, 5);
INSERT INTO `print_pattern` VALUES (2, 'PC002', '图案2', 'ModelY', 'http://example.com/image2.jpg', 750, 4.5);
INSERT INTO `print_pattern` VALUES (3, 'PC003', '图案3', 'ModelZ', 'http://example.com/image3.jpg', 850, 5.5);
INSERT INTO `print_pattern` VALUES (4, 'PC004', '图案4', 'ModelW', 'http://example.com/image4.jpg', 780, 4.8);
INSERT INTO `print_pattern` VALUES (5, 'PC005', '图案5', 'ModelV', 'http://example.com/image5.jpg', 860, 6);
INSERT INTO `print_pattern` VALUES (6, 'PC006', '图案6', 'ModelU', 'http://example.com/image6.jpg', 790, 4.7);
INSERT INTO `print_pattern` VALUES (7, 'PC007', '图案7', 'ModelT', 'http://example.com/image7.jpg', 870, 5.8);
INSERT INTO `print_pattern` VALUES (8, 'PC008', '图案8', 'ModelS', 'http://example.com/image8.jpg', 760, 4.3);
INSERT INTO `print_pattern` VALUES (9, 'PC009', '图案9', 'ModelR', 'http://example.com/image9.jpg', 840, 5.2);
INSERT INTO `print_pattern` VALUES (10, 'PC010', '图案10', 'ModelQ', 'http://example.com/image10.jpg', 820, 4.9);
INSERT INTO `print_pattern` VALUES (11, '123333', '123333', 'A', NULL, 3, 3);

SET FOREIGN_KEY_CHECKS = 1;
