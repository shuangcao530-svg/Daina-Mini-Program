-- =============================================
-- 校速达 (XiaoSuDa) 数据库完整初始化脚本
-- Version: 2.0
-- 使用方法: 在 Navicat 中新建查询，复制本脚本全部内容，点击运行
-- =============================================

-- 1. 创建数据库
DROP DATABASE IF EXISTS `xiaosuda`;
CREATE DATABASE `xiaosuda`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `xiaosuda`;

-- =============================================
-- 2. 用户表
-- =============================================
CREATE TABLE `user` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT,
    `open_id`       VARCHAR(64)    DEFAULT NULL COMMENT '微信OpenID',
    `nickname`      VARCHAR(64)    DEFAULT '未登录用户' COMMENT '昵称',
    `avatar_url`    VARCHAR(512)   DEFAULT NULL COMMENT '头像URL',
    `phone`         VARCHAR(20)    DEFAULT NULL COMMENT '手机号',
    `school`        VARCHAR(128)   DEFAULT '校园跑腿用户' COMMENT '学校',
    `role`          VARCHAR(16)    DEFAULT 'user' COMMENT '角色: user/runner/admin',
    `balance`       DECIMAL(10,2)  DEFAULT 0.00 COMMENT '可提现余额',
    `frozen_balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '冻结余额(提现中)',
    `total_earned`  DECIMAL(10,2)  DEFAULT 0.00 COMMENT '累计收益',
    `total_orders`  INT            DEFAULT 0 COMMENT '完成订单数',
    `last_login_at` DATETIME       DEFAULT NULL COMMENT '最后登录时间',
    `created_at`    DATETIME       DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `version`       INT            DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_open_id` (`open_id`),
    KEY `idx_role` (`role`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 3. 地址表
-- =============================================
CREATE TABLE `address` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL COMMENT '用户ID',
    `tag`        VARCHAR(32)  DEFAULT NULL COMMENT '标签: 宿舍/教学楼/实验室/图书馆/其他',
    `building`   VARCHAR(64)  DEFAULT NULL COMMENT '楼栋名称',
    `room`       VARCHAR(32)  DEFAULT NULL COMMENT '房间号',
    `detail`     VARCHAR(256) DEFAULT NULL COMMENT '详细地址',
    `contact`    VARCHAR(32)  DEFAULT NULL COMMENT '联系人',
    `phone`      VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    `is_default` TINYINT      DEFAULT 0 COMMENT '是否默认地址',
    `is_deleted` TINYINT      DEFAULT 0 COMMENT '软删除标记',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地址表';

-- =============================================
-- 4. 订单表
-- =============================================
CREATE TABLE `delivery_order` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT,
    `order_no`          VARCHAR(20)   NOT NULL COMMENT '订单号 XD+日期+序号',
    `user_id`           BIGINT        NOT NULL COMMENT '下单用户ID',
    `runner_id`         BIGINT        DEFAULT NULL COMMENT '接单跑腿员ID',
    `type`              VARCHAR(16)   DEFAULT '其他' COMMENT '类型: 快递/外卖/文件/钥匙/代购/其他',
    `pickup_location`   VARCHAR(256)  DEFAULT NULL COMMENT '取件地点',
    `delivery_location` VARCHAR(256)  DEFAULT NULL COMMENT '送达地点',
    `pickup_code`       VARCHAR(32)   DEFAULT NULL COMMENT '取件码',
    `urgency`           VARCHAR(8)    DEFAULT '普通' COMMENT '紧急程度: 普通/加急/极速',
    `fee`               DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费(用户支付)',
    `platform_fee_rate` DECIMAL(4,3)  DEFAULT 0.100 COMMENT '平台抽成比例(默认10%%)',
    `runner_earned`     DECIMAL(10,2) DEFAULT 0.00 COMMENT '跑腿员实际收入',
    `note`              VARCHAR(512)  DEFAULT NULL COMMENT '订单备注',
    `contact_name`      VARCHAR(32)   DEFAULT NULL COMMENT '联系人姓名',
    `contact_phone`     VARCHAR(20)   DEFAULT NULL COMMENT '联系人电话',
    `status`            VARCHAR(16)   DEFAULT '待接单' COMMENT '订单状态',
    `images`            TEXT          DEFAULT NULL COMMENT '物品照片(JSON数组)',
    `pickup_images`     TEXT          DEFAULT NULL COMMENT '取件凭证照片(JSON数组)',
    `delivery_images`   TEXT          DEFAULT NULL COMMENT '送达凭证照片(JSON数组)',
    `cancel_reason`     VARCHAR(256)  DEFAULT NULL COMMENT '取消原因',
    `cancel_by`         BIGINT        DEFAULT NULL COMMENT '取消操作人ID',
    `completed_at`      DATETIME      DEFAULT NULL COMMENT '完成时间',
    `cancelled_at`      DATETIME      DEFAULT NULL COMMENT '取消时间',
    `created_at`        DATETIME      DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `version`           INT           DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_runner_id` (`runner_id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_runner_status` (`runner_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =============================================
-- 5. 订单时间线
-- =============================================
CREATE TABLE `order_timeline` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `order_id`    BIGINT       NOT NULL COMMENT '订单ID',
    `status`      VARCHAR(16)  DEFAULT NULL COMMENT '当前状态',
    `label`       VARCHAR(256) DEFAULT NULL COMMENT '事件描述',
    `operator_id` BIGINT       DEFAULT NULL COMMENT '操作人ID',
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单时间线';

-- =============================================
-- 6. 校园地点
-- =============================================
CREATE TABLE `campus_point` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `name`       VARCHAR(64) NOT NULL COMMENT '地点名称',
    `category`   VARCHAR(16) DEFAULT 'both' COMMENT '分类: pickup/delivery/both',
    `building`   VARCHAR(64) DEFAULT NULL COMMENT '所属楼栋',
    `sort_order` INT         DEFAULT 0 COMMENT '排序',
    `is_active`  TINYINT     DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园地点';

-- =============================================
-- 7. 钱包交易记录 (NEW)
-- =============================================
CREATE TABLE `wallet_transaction` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`        BIGINT        NOT NULL COMMENT '用户ID',
    `order_id`       BIGINT        DEFAULT NULL COMMENT '关联订单ID',
    `type`           VARCHAR(24)   NOT NULL COMMENT '类型: earn/withdraw/freeze/unfreeze/adjust',
    `amount`         DECIMAL(10,2) NOT NULL COMMENT '变动金额(正=收入,负=支出)',
    `balance_before` DECIMAL(10,2) DEFAULT 0.00 COMMENT '变动前余额',
    `balance_after`  DECIMAL(10,2) DEFAULT 0.00 COMMENT '变动后余额',
    `description`    VARCHAR(256)  DEFAULT NULL COMMENT '描述',
    `created_at`     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_type` (`type`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包交易记录';

-- =============================================
-- 8. 提现记录 (NEW)
-- =============================================
CREATE TABLE `withdraw_record` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT        NOT NULL COMMENT '用户ID',
    `amount`       DECIMAL(10,2) NOT NULL COMMENT '提现金额',
    `status`       VARCHAR(16)   DEFAULT 'pending' COMMENT '状态: pending/processing/completed/rejected',
    `account_type` VARCHAR(16)   DEFAULT 'wechat' COMMENT '到账方式: wechat/alipay/bank',
    `account_info` VARCHAR(128)  DEFAULT NULL COMMENT '收款账号信息',
    `remark`       VARCHAR(256)  DEFAULT NULL COMMENT '备注/驳回原因',
    `processed_at` DATETIME      DEFAULT NULL COMMENT '处理时间',
    `created_at`   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提现记录';

-- =============================================
-- 9. 订单评价 (NEW)
-- =============================================
CREATE TABLE `order_review` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `order_id`      BIGINT       NOT NULL COMMENT '订单ID',
    `reviewer_id`   BIGINT       NOT NULL COMMENT '评价人ID',
    `target_user_id` BIGINT      NOT NULL COMMENT '被评价人ID',
    `rating`        TINYINT      NOT NULL COMMENT '评分(1-5星)',
    `content`       VARCHAR(512) DEFAULT NULL COMMENT '评价内容',
    `tags`          VARCHAR(256) DEFAULT NULL COMMENT '评价标签(JSON数组): ["快速","友好"]',
    `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_reviewer` (`order_id`, `reviewer_id`),
    KEY `idx_target_user` (`target_user_id`),
    KEY `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单评价';

-- =============================================
-- 10. 系统配置 (NEW)
-- =============================================
CREATE TABLE `system_config` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `config_key`  VARCHAR(64)  NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(256) NOT NULL COMMENT '配置值',
    `description` VARCHAR(256) DEFAULT NULL COMMENT '说明',
    `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置';

-- =============================================
-- 11. 聊天消息 (NEW)
-- =============================================
CREATE TABLE `chat_message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `order_id`    BIGINT       NOT NULL COMMENT '订单ID',
    `sender_id`   BIGINT       NOT NULL COMMENT '发送人ID',
    `receiver_id` BIGINT       NOT NULL COMMENT '接收人ID',
    `content`     VARCHAR(1024) NOT NULL COMMENT '消息内容',
    `is_read`     TINYINT      DEFAULT 0 COMMENT '是否已读',
    `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息';

-- =============================================
-- 12. 种子数据
-- =============================================

-- 校园地点
INSERT INTO `campus_point` (`name`, `category`, `building`, `sort_order`) VALUES
('东门驿站',   'pickup',  '东门',  1),
('南门外卖架', 'pickup',  '南门',  2),
('图书馆',     'both',    '主楼',  3),
('明德楼',     'delivery','教学楼',4),
('实验楼',     'delivery','教学楼',5),
('1号宿舍',    'delivery','宿舍区',6),
('2号宿舍',    'delivery','宿舍区',7),
('3号宿舍',    'delivery','宿舍区',8),
('校医院',     'delivery','公共区',9),
('食堂',       'pickup',  '生活区',10),
('行政楼',     'delivery','办公区',11),
('体育馆',     'both',    '运动区',12);

-- 演示用户（密码均为 123456，仅供开发测试）
INSERT INTO `user` (`open_id`, `nickname`, `avatar_url`, `phone`, `school`, `role`, `balance`, `total_earned`, `total_orders`) VALUES
('mock_user_001',   '李同学',   'https://api.dicebear.com/9.x/avataaars/svg?seed=user1',  '139****5678', 'xx大学', 'user',   88.00, 0.00,  0),
('mock_runner_001', '跑腿小王', 'https://api.dicebear.com/9.x/avataaars/svg?seed=runner1', '158****9012', 'xx大学', 'runner', 25.00, 156.00, 12),
('mock_admin_001',  '管理员',   'https://api.dicebear.com/9.x/avataaars/svg?seed=admin1',  '177****0000', 'xx大学', 'admin',   0.00, 0.00,  0);

-- 系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('platform_fee_rate',       '0.10', '平台服务费比例(0.10=10%)'),
('min_withdraw_amount',     '10.00','最低提现金额'),
('max_withdraw_amount',     '5000.00','单笔最高提现金额'),
('order_auto_cancel_minutes','30',   '未接单自动取消时间(分钟)'),
('service_hotline',         '400-000-0000', '客服热线'),
('app_version',             '1.0.0','当前版本号');
