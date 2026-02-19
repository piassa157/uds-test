CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO app_users (username, password, role, enabled)
VALUES
    ('admin', '{noop}admin123', 'ADMIN', TRUE),
    ('usuarioteste', '{noop}senha123', 'USER', TRUE)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    role = VALUES(role),
    enabled = VALUES(enabled);
