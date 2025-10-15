-- ROLES
CREATE TABLE IF NOT EXISTS role (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    code VARCHAR(30) UNIQUE NOT NULL -- ADMIN, EVM, MANAGER, DEALER
    );

-- USERS
CREATE TABLE IF NOT EXISTS user (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    username VARCHAR(60) UNIQUE NOT NULL,
    password VARCHAR(200) NOT NULL,
    full_name VARCHAR(120),
    active BOOLEAN DEFAULT TRUE,
    dealer_id BIGINT NULL
    );

-- USER_ROLE (many-to-many)
CREATE TABLE IF NOT EXISTS user_role (
                                         user_id BIGINT NOT NULL,
                                         role_id BIGINT NOT NULL,
                                         PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES role(id)
    );

-- Seed roles
INSERT IGNORE INTO role(code) VALUES ('ADMIN'),('EVM'),('MANAGER'),('DEALER');

-- Mật khẩu đã mã hoá BCrypt: "admin123" / "evm123" / "manager123" / "dealer123"
-- Bạn có thể thay bằng hash khác, miễn là BCrypt.
INSERT IGNORE INTO user(username,password,full_name,active) VALUES
('admin',   '$2a$10$g8nT4E.5mQWf3H5QYt8h6uh66w9e4K2C2p6x4Y3g1kC2P2cZJpQ7G', 'System Admin', TRUE),
('evm1',    '$2a$10$5aQ3fQwq6Qnq7T7g1sJ2euZr2q7e9dM1zU7o9G8t3e3s4T8P6Yl0e', 'EVM Staff 1', TRUE),
('manager1','$2a$10$kP1Q2R3S4T5U6V7W8X9Y0eF1gH2iJ3kL4mN5oP6qR7sT8uV9wX0y', 'Dealer Manager 1', TRUE),
('dealer1', '$2a$10$0b1c2d3e4f5g6h7i8j9k0lM1nO2pQ3rS4tU5vW6xY7zA8bC9dE0F', 'Dealer Staff 1', TRUE);

-- Gán role
INSERT IGNORE INTO user_role SELECT u.id, r.id FROM user u JOIN role r ON u.username='admin'    AND r.code='ADMIN';
INSERT IGNORE INTO user_role SELECT u.id, r.id FROM user u JOIN role r ON u.username='evm1'     AND r.code='EVM';
INSERT IGNORE INTO user_role SELECT u.id, r.id FROM user u JOIN role r ON u.username='manager1' AND r.code='MANAGER';
INSERT IGNORE INTO user_role SELECT u.id, r.id FROM user u JOIN role r ON u.username='dealer1'  AND r.code='DEALER';
