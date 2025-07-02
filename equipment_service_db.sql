/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80036
 Source Host           : localhost:3306
 Source Schema         : equipment_service_db

 Target Server Type    : MySQL
 Target Server Version : 80036
 File Encoding         : 65001

 Date: 02/07/2025 17:48:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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

SET FOREIGN_KEY_CHECKS = 1;
