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

 Date: 28/06/2025 22:23:44
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
  `runtime_minutes` int(0) NOT NULL DEFAULT 0 COMMENT '运行时长(分钟)',
  `open_close_times` int(0) NOT NULL DEFAULT 0 COMMENT '开合模次数',
  `last_maintenance_time` datetime(0) NULL DEFAULT NULL COMMENT '最后维护时间',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `injection_time` float NULL DEFAULT NULL COMMENT '注塑时间',
  `injection_pressure` float NULL DEFAULT NULL COMMENT '注塑压力',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of device
-- ----------------------------
INSERT INTO `device` VALUES (1, 'SJ001', '注塑机', '运行中', 45, 100, '2024-12-05 08:00:00', '2024-12-05 09:00:00', '2024-12-05 10:00:00', 30.5, 50.25);
INSERT INTO `device` VALUES (2, 'SJ002', '注塑机', '空闲', 0, 0, '2024-12-05 08:30:00', '2024-12-05 09:00:00', '2024-12-05 09:00:00', 25.8, 45.6);
INSERT INTO `device` VALUES (3, 'SJ003', '压机', '空闲', 60, 150, '2024-12-05 07:45:00', '2024-12-05 08:00:00', '2024-12-05 10:30:00', 35.2, 55.4);
INSERT INTO `device` VALUES (4, 'SJ004', '注塑机', '运行中', 90, 200, '2024-12-05 07:00:00', '2024-12-05 08:00:00', '2024-12-05 11:00:00', 40.7, 60.1);
INSERT INTO `device` VALUES (5, 'SJ005', '压机', '空闲', 0, 0, '2024-12-05 08:15:00', '2024-12-05 08:30:00', '2024-12-05 08:30:00', 20.3, 40.2);
INSERT INTO `device` VALUES (6, 'SJ006', '注塑机', '故障', 30, 75, '2024-12-05 09:15:00', '2024-12-05 09:30:00', '2024-12-05 12:00:00', 28.5, 52.8);
INSERT INTO `device` VALUES (7, 'SJ007', '压机', '运行中', 75, 180, '2024-12-05 06:45:00', '2024-12-05 07:00:00', '2024-12-05 11:30:00', 38.2, 58.9);
INSERT INTO `device` VALUES (8, 'SJ008', '注塑机', '空闲', 0, 0, '2024-12-05 08:45:00', '2024-12-05 09:00:00', '2024-12-05 09:00:00', 15.6, 35.4);

SET FOREIGN_KEY_CHECKS = 1;
