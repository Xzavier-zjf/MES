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

 Date: 28/06/2025 23:12:32
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
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'draft' COMMENT '状态:草稿、已下发、已完成',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_code`(`plan_code`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产计划表' ROW_FORMAT = Dynamic;

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
INSERT INTO `production_plan` VALUES (10, 'PLAN-20230609-001', 'realme GT3手机壳', 2200, 2, 'draft', '2023-06-09 11:45:00');
INSERT INTO `production_plan` VALUES (11, 'PLAN-2025-001', '高端数控机床', 1000, 1, 'draft', NULL);
INSERT INTO `production_plan` VALUES (15, '1111', '智能手机X1', 5000, 0, 'draft', '2025-06-25 22:45:24');
INSERT INTO `production_plan` VALUES (17, '1111', '智能手机X1', 5000, 0, 'draft', '2025-06-25 22:47:35');
INSERT INTO `production_plan` VALUES (18, '1111', '智能手机X1', 5000, 0, 'draft', '2025-06-26 15:51:57');
INSERT INTO `production_plan` VALUES (19, '1111', '智能手机X1', 5000, 0, 'draft', '2025-06-26 17:56:58');
INSERT INTO `production_plan` VALUES (20, 'test', 'test', 5, 1, '待下发', '2025-06-28 18:02:13');
INSERT INTO `production_plan` VALUES (21, 'test111', '123', 1, 1, '草稿', '2025-06-28 22:46:07');

-- ----------------------------
-- Table structure for production_task
-- ----------------------------
DROP TABLE IF EXISTS `production_task`;
CREATE TABLE `production_task`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
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
INSERT INTO `production_task` VALUES (1, 1, 5, '注塑', 500, '待下发', NULL, NULL, 2, NULL);
INSERT INTO `production_task` VALUES (2, 3, 7, '印刷', 500, '进行中', '2025-06-28 09:00:00', NULL, NULL, 5);
INSERT INTO `production_task` VALUES (3, 5, 9, '组装', 500, '已完成', '2025-06-28 10:00:00', '2025-06-28 12:00:00', NULL, NULL);
INSERT INTO `production_task` VALUES (4, 2, 4, '注塑', 300, '待下发', NULL, NULL, 3, NULL);
INSERT INTO `production_task` VALUES (5, 6, 8, '印刷', 300, '进行中', '2025-06-28 09:30:00', NULL, NULL, 6);
INSERT INTO `production_task` VALUES (6, 8, 10, '组装', 300, '已完成', '2025-06-28 11:00:00', '2025-06-28 13:00:00', NULL, NULL);
INSERT INTO `production_task` VALUES (7, 4, 6, '注塑', 800, '待下发', NULL, NULL, 4, NULL);
INSERT INTO `production_task` VALUES (8, 7, 2, '印刷', 800, '进行中', '2025-06-28 10:00:00', NULL, NULL, 7);
INSERT INTO `production_task` VALUES (9, 9, 3, '组装', 800, '已完成', '2025-06-28 11:30:00', '2025-06-28 14:00:00', NULL, NULL);
INSERT INTO `production_task` VALUES (10, 10, 1, '注塑', 1000, '待下发', NULL, NULL, 1, NULL);

SET FOREIGN_KEY_CHECKS = 1;
