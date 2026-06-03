CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt password hash',
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT = 'Application users allowed to authenticate in the digital bank API';

INSERT INTO users (
    id,
    username,
    password_hash,
    role,
    enabled
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'admin',
    '$2b$10$Fi36AO3IoxzOPvE.hf3k1OYMoEQnFMXYvn0A2kCqKyI9qKHNvxpam',
    'ADMIN',
    TRUE
);

