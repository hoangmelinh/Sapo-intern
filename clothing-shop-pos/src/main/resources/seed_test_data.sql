-- =============================================
-- Chạy SAU KHI đã tạo DB bằng script chính
-- Thêm cột refresh_token + tạo 3 tài khoản test
-- Password tất cả: 123456
-- =============================================

USE clothing_shop_db;

-- 1. Thêm cột refresh_token vào bảng user (JWT cần lưu)
ALTER TABLE user ADD COLUMN refresh_token TEXT NULL AFTER is_active;

-- 2. Insert 3 tài khoản test (password: 123456)
INSERT INTO user (username, password_hash, full_name, phone, role, is_active) VALUES
('sale01', '$2a$10$26IhRLEHb3p5ZGoxsLiyCevhGyJyxhgF/eGszXGQyJxFH.QQJD9ya', 'NV Ban Hang',  '0901111111', 'ROLE_SALE', TRUE),
('cs01',   '$2a$10$26IhRLEHb3p5ZGoxsLiyCevhGyJyxhgF/eGszXGQyJxFH.QQJD9ya', 'NV CSKH',      '0902222222', 'ROLE_CS',   TRUE),
('wh01',   '$2a$10$26IhRLEHb3p5ZGoxsLiyCevhGyJyxhgF/eGszXGQyJxFH.QQJD9ya', 'NV Kho',       '0903333333', 'ROLE_WH',   TRUE);

-- =============================================
-- TEST: POST http://localhost:8080/api/v1/auth/login
-- Body: { "username": "sale01", "password": "123456" }
-- =============================================
