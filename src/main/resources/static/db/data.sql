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

------------------------------------
insert into vehicle_model(id, name) values (1,'EV One');
insert into vehicle_trim(id, model_id, name, msrp) values (11,1,'EV One Standard', 35000.00);

insert into customer(id, full_name, phone, email) values (101,'Nguyen Van A','0900000001','a@example.com');

-- ton kho demo (neu da co bang inventory)
insert into inventory(id, trim_id, qty_onhand) values (1001, 11, 5);




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

USE ev_dms;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE test_drives;
SET FOREIGN_KEY_CHECKS = 1;
USE ev_dms;

-- Giả sử trong bảng users đã có sẵn một vài người dùng:
-- id=1: admin, id=2: manager, id=3: staff A, id=4: staff B

INSERT INTO test_drives (customer_name, customer_phone, vehicle_name, location, schedule_at, notes, status, created_by_id, assigned_staff_id)
VALUES
-- Các yêu cầu mới tạo
('Nguyen Van A', '0909123456', 'Hyundai Ioniq 5', 'Đại lý EV Quận 7', '2025-11-02 09:00:00', 'Khách muốn lái thử bản cao cấp', 'REQUESTED', 3, NULL),
('Tran Thi B', '0988777666', 'VinFast VF8', 'Đại lý EV Quận 1', '2025-11-02 14:30:00', 'Lái thử trong khu đô thị Vinhomes', 'REQUESTED', 3, NULL),

-- Các lịch đã được xác nhận
('Le Minh C', '0911222333', 'Kia EV6', 'Đại lý EV Thủ Đức', '2025-11-03 10:00:00', 'Đã xác nhận bởi quản lý', 'CONFIRMED', 3, 4),
('Pham Thi D', '0977555444', 'Tesla Model Y', 'Đại lý EV Quận 3', '2025-11-03 15:00:00', 'Khách VIP, có yêu cầu riêng', 'CONFIRMED', 3, 4),

-- Lịch đã hoàn tất
('Bui Van E', '0909888777', 'VinFast VF9', 'Đại lý EV Bình Thạnh', '2025-10-30 09:30:00', 'Khách hài lòng, có khả năng mua', 'COMPLETED', 3, 4),
('Do Thi F', '0933111222', 'BYD Atto 3', 'Đại lý EV Quận 5', '2025-10-29 14:00:00', 'Đã lái thử và ký hợp đồng', 'COMPLETED', 3, 4),

-- Lịch bị hủy
('Nguyen Van G', '0909666555', 'Hyundai Kona EV', 'Đại lý EV Quận 10', '2025-11-01 10:00:00', 'Khách bận, hủy lịch', 'CANCELLED', 3, NULL);

