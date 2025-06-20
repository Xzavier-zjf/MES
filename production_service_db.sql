/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80036
 Source Host           : localhost:3306
 Source Schema         : production_service_db

 Target Server Type    : MySQL
 Target Server Version : 80036
 File Encoding         : 65001

 Date: 20/06/2025 16:11:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
  `status` enum('draft','issued','completed') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'draft' COMMENT '状态:草稿、已下发、已完成',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_code`(`plan_code`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产计划表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of production_plan
-- ----------------------------
INSERT INTO `production_plan` VALUES (1, 'PLAN-20230601-001', 'iPhone 14手机壳', 5000, 1, 'issued', '2023-06-01 09:00:00');
INSERT INTO `production_plan` VALUES (2, 'PLAN-20230601-002', '三星S23手机壳', 3000, 2, 'issued', '2023-06-01 10:30:00');
INSERT INTO `production_plan` VALUES (3, 'PLAN-20230602-001', '华为Mate50手机壳', 4000, 1, 'completed', '2023-06-02 08:45:00');
INSERT INTO `production_plan` VALUES (4, 'PLAN-20230603-001', '小米13手机壳', 3500, 3, 'draft', '2023-06-03 14:20:00');
INSERT INTO `production_plan` VALUES (5, 'PLAN-20230605-001', 'OPPO Reno8手机壳', 2500, 2, 'issued', '2023-06-05 11:10:00');
INSERT INTO `production_plan` VALUES (6, 'PLAN-20230605-002', 'vivo X90手机壳', 2800, 1, 'issued', '2023-06-05 13:45:00');
INSERT INTO `production_plan` VALUES (7, 'PLAN-20230606-001', '荣耀Magic5手机壳', 3200, 2, 'draft', '2023-06-06 09:30:00');
INSERT INTO `production_plan` VALUES (8, 'PLAN-20230607-001', '红米Note12手机壳', 4500, 1, 'issued', '2023-06-07 10:00:00');
INSERT INTO `production_plan` VALUES (9, 'PLAN-20230608-001', '一加11手机壳', 1800, 3, 'completed', '2023-06-08 15:20:00');
INSERT INTO `production_plan` VALUES (10, 'PLAN-20230609-001', 'realme GT3手机壳', 2200, 2, 'issued', '2023-06-09 11:45:00');
INSERT INTO `production_plan` VALUES (11, 'PLAN-2025-001', '高端数控机床', 1000, 1, 'draft', NULL);

-- ----------------------------
-- Table structure for production_task
-- ----------------------------
DROP TABLE IF EXISTS `production_task`;
CREATE TABLE `production_task`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `plan_id` int(0) NOT NULL COMMENT '所属计划ID',
  `device_id` int(0) NOT NULL COMMENT '分配设备ID',
  `process_type` enum('injection','printing') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工艺类型:注塑、印刷',
  `quantity` int(0) NOT NULL COMMENT '任务数量',
  `status` enum('pending','in_progress','completed') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态:未开始、进行中、已完成',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '计划开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '实际完成时间',
  `injection_param_id` int(0) NULL DEFAULT NULL COMMENT '注塑参数ID',
  `print_pattern_id` int(0) NULL DEFAULT NULL COMMENT '印刷图案ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_id`(`plan_id`) USING BTREE,
  INDEX `idx_device_id`(`device_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of production_task
-- ----------------------------
INSERT INTO `production_task` VALUES (1, 1, 101, 'injection', 5000, 'completed', '2023-06-01 10:00:00', '2023-06-01 18:30:00', 1001, NULL);
INSERT INTO `production_task` VALUES (2, 1, 201, 'printing', 5000, 'completed', '2023-06-02 09:00:00', '2023-06-02 17:45:00', NULL, 2001);
INSERT INTO `production_task` VALUES (3, 2, 102, 'injection', 3000, 'completed', '2023-06-01 11:30:00', '2023-06-01 20:15:00', 1002, NULL);
INSERT INTO `production_task` VALUES (4, 2, 202, 'printing', 3000, 'in_progress', '2023-06-03 08:30:00', NULL, NULL, 2002);
INSERT INTO `production_task` VALUES (5, 3, 103, 'injection', 4000, 'completed', '2023-06-02 09:45:00', '2023-06-02 19:20:00', 1003, NULL);
INSERT INTO `production_task` VALUES (6, 3, 203, 'printing', 4000, 'completed', '2023-06-03 09:00:00', '2023-06-03 16:30:00', NULL, 2003);
INSERT INTO `production_task` VALUES (7, 5, 104, 'injection', 2500, 'in_progress', '2023-06-05 12:10:00', NULL, 1004, NULL);
INSERT INTO `production_task` VALUES (8, 5, 204, 'printing', 2500, 'pending', NULL, NULL, NULL, 2004);
INSERT INTO `production_task` VALUES (9, 6, 105, 'injection', 2800, 'in_progress', '2023-06-05 14:45:00', NULL, 1005, NULL);
INSERT INTO `production_task` VALUES (10, 6, 205, 'printing', 2800, 'pending', NULL, NULL, NULL, 2005);
INSERT INTO `production_task` VALUES (11, 8, 101, 'injection', 4500, 'in_progress', '2023-06-07 11:00:00', NULL, 1006, NULL);
INSERT INTO `production_task` VALUES (12, 8, 201, 'printing', 4500, 'pending', NULL, NULL, NULL, 2006);
INSERT INTO `production_task` VALUES (13, 10, 102, 'injection', 2200, 'in_progress', '2023-06-09 12:45:00', NULL, 1007, NULL);
INSERT INTO `production_task` VALUES (14, 10, 202, 'printing', 2200, 'pending', NULL, NULL, NULL, 2007);

SET FOREIGN_KEY_CHECKS = 1;
