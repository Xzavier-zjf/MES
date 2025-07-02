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

 Date: 02/07/2025 11:11:06
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
