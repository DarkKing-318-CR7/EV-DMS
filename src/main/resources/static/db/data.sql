

INSERT INTO roles (name) VALUES
('ROLE_ADMIN'),
('ROLE_DEALER_MANAGER'),
('ROLE_DEALER_STAFF'),
('ROLE_EVM_STAFF');


INSERT INTO users (username, password, full_name, email, enabled) VALUES
('admin',
 '$2a$10$o2C8qLzPdFkfBRwulPj30uqwkmE5MR3hqh4NwmfOyStkXwtsq/tZK',
 'System Administrator',
 'admin@evdms.com',
 true),
('manager1',
 '$2a$10$o2C8qLzPdFkfBRwulPj30uqwkmE5MR3hqh4NwmfOyStkXwtsq/tZK',
 'Dealer Manager A',
 'managerA@evdms.com',
 true),
('staff1',
 '$2a$10$o2C8qLzPdFkfBRwulPj30uqwkmE5MR3hqh4NwmfOyStkXwtsq/tZK',
 'Dealer Staff A',
 'staffA@evdms.com',
 true),
('evm1',
 '$2a$10$o2C8qLzPdFkfBRwulPj30uqwkmE5MR3hqh4NwmfOyStkXwtsq/tZK',
 'EVM Staff A',
 'evmA@evdms.com',
 true);



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
