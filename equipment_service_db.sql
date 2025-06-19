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

 Date: 19/06/2025 21:55:34
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
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备名称',
  `status` enum('running','fault','idle') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'idle' COMMENT '状态:运行、故障、闲置',
  `runtime_minutes` int(0) NULL DEFAULT 0 COMMENT '运行时长(分钟)',
  `open_close_times` int(0) NULL DEFAULT 0 COMMENT '开合模次数',
  `last_maintenance_time` datetime(0) NULL DEFAULT NULL COMMENT '上次维护时间',
  `next_maintenance_time` datetime(0) NULL DEFAULT NULL COMMENT '下次维护时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_device_code`(`device_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of device
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
