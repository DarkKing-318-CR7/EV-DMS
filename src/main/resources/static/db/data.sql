INSERT INTO roles (name) VALUES
                             ('ROLE_ADMIN'),
                             ('ROLE_DEALER_MANAGER'),
                             ('ROLE_DEALER_STAFF'),
                             ('ROLE_EVM_STAFF');

INSERT INTO users (username, password, full_name, email, enabled) VALUES
                                                                      ('admin', '$2a$10$bQnvs8wV56Mv/JhOh2TyK..WacVtpN8Ulmnnt1rLO9bNyL9.j4DtO', 'System Administrator', 'admin@evdms.com', true),
                                                                      ('manager1', '$2a$10$bQnvs8wV56Mv/JhOh2TyK..WacVtpN8Ulmnnt1rLO9bNyL9.j4DtO', 'Dealer Manager A', 'managerA@evdms.com', true),
                                                                      ('staff1', '$2a$10$bQnvs8wV56Mv/JhOh2TyK..WacVtpN8Ulmnnt1rLO9bNyL9.j4DtO', 'Dealer Staff A', 'staffA@evdms.com', true),
                                                                      ('evm1', '$2a$10$bQnvs8wV56Mv/JhOh2TyK..WacVtpN8Ulmnnt1rLO9bNyL9.j4DtO', 'EVM Staff A', 'evmA@evdms.com', true);


INSERT INTO users_roles (user_id, role_id)
VALUES ((SELECT id FROM users WHERE username = 'admin'),
        (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'));


INSERT INTO users_roles (user_id, role_id)
VALUES ((SELECT id FROM users WHERE username = 'manager1'),
        (SELECT id FROM roles WHERE name = 'ROLE_DEALER_MANAGER'));


INSERT INTO users_roles (user_id, role_id)
VALUES ((SELECT id FROM users WHERE username = 'staff1'),
        (SELECT id FROM roles WHERE name = 'ROLE_DEALER_STAFF'));


INSERT INTO users_roles (user_id, role_id)
VALUES ((SELECT id FROM users WHERE username = 'evm1'),
        (SELECT id FROM roles WHERE name = 'ROLE_EVM_STAFF'));



INSERT INTO customers (ten, email, sdt, diachi, ngaytao) VALUES
('Nguyen Van A', 'a.nguyen@example.com', '0905123456', '12 Nguyen Trai, Q1, TP.HCM', '2025-10-01 09:12:00'),
('Tran Thi B', 'b.tran@example.com', '0912345678', '45 Le Loi, Q1, TP.HCM', '2025-10-02 14:30:00'),
('Le Minh C', 'c.le@example.com', '0987654321', '23 Phan Chu Trinh, Q5, TP.HCM', '2025-10-03 08:45:00'),
('Pham Thi D', 'd.pham@example.com', '0909888777', '78 Pasteur, Q3, TP.HCM', '2025-10-05 10:20:00'),
('Vo Van E', 'e.vo@example.com', '0909555666', '56 Cach Mang Thang 8, Q10, TP.HCM', '2025-10-06 11:00:00'),
('Nguyen Thi F', 'f.nguyen@example.com', '0911222333', '32 Hai Ba Trung, Q1, TP.HCM', '2025-10-07 12:15:00'),
('Hoang Van G', 'g.hoang@example.com', '0933444555', '21 Ly Tu Trong, Q3, TP.HCM', '2025-10-08 15:40:00'),
('Tran Quoc H', 'h.tran@example.com', '0977888999', '9 Nguyen Dinh Chieu, Q1, TP.HCM', '2025-10-09 17:25:00'),
('Bui Thi I', 'i.bui@example.com', '0901234987', '100 Dien Bien Phu, Q10, TP.HCM', '2025-10-10 09:50:00'),
('Dang Van K', 'k.dang@example.com', '0922334455', '67 Vo Van Tan, Q3, TP.HCM', '2025-10-11 16:10:00');
