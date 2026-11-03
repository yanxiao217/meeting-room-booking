-- Migration v7: 签到二维码 — sign_source 字段
ALTER TABLE booking ADD COLUMN sign_source VARCHAR(20) DEFAULT NULL COMMENT '签到来源: manual手动 qrcode二维码' AFTER sign_time;
