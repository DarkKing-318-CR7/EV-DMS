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

-- Admin
INSERT INTO users_roles (user_id, role_id)
VALUES ((SELECT id FROM users WHERE username = 'admin'),
        (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'));

-- Dealer Manager
INSERT INTO users_roles (user_id, role_id)
VALUES ((SELECT id FROM users WHERE username = 'manager1'),
        (SELECT id FROM roles WHERE name = 'ROLE_DEALER_MANAGER'));

-- Dealer Staff
INSERT INTO users_roles (user_id, role_id)
VALUES ((SELECT id FROM users WHERE username = 'staff1'),
        (SELECT id FROM roles WHERE name = 'ROLE_DEALER_STAFF'));

-- EVM Staff
INSERT INTO users_roles (user_id, role_id)
VALUES ((SELECT id FROM users WHERE username = 'evm1'),
        (SELECT id FROM roles WHERE name = 'ROLE_EVM_STAFF'));

------------------------------------
insert into vehicle_model(id, name) values (1,'EV One');
insert into vehicle_trim(id, model_id, name, msrp) values (11,1,'EV One Standard', 35000.00);

insert into customer(id, full_name, phone, email) values (101,'Nguyen Van A','0900000001','a@example.com');

-- ton kho demo (neu da co bang inventory)
insert into inventory(id, trim_id, qty_onhand) values (1001, 11, 5);




