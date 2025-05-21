-- V1: 创建 guest_request 表
CREATE TABLE IF NOT EXISTS guest_request (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    requested_at DATETIME NOT NULL,
    status VARCHAR(10) NOT NULL
    );