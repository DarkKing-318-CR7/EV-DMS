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

-- ===========================
-- SEED DATA: VEHICLES (EVM)
-- ===========================

INSERT INTO vehicles (model_code, model_name, brand, body_type, warranty_months, created_at, updated_at)
VALUES
    ('EV6',       'EV6 GT-Line',             'EVM', 'SUV',      36, NOW(), NOW()),
    ('EX90',      'EX90 Performance',        'EVM', 'SUV',      36, NOW(), NOW()),
    ('E1',        'E1 Urban',                'EVM', 'Hatchback',24, NOW(), NOW()),
    ('EQS',       'EQS Sedan Luxury',        'EVM', 'Sedan',    48, NOW(), NOW()),
    ('EQB',       'EQB Family Plus',         'EVM', 'SUV',      36, NOW(), NOW()),
    ('EV4',       'EV4 Compact',             'EVM', 'Crossover',24, NOW(), NOW()),
    ('EVM9',      'EVM9 Commercial',         'EVM', 'Van',      24, NOW(), NOW()),
    ('E8',        'E8 Executive',            'EVM', 'Sedan',    36, NOW(), NOW()),
    ('E-Sport',   'EVM Sport Edition',       'EVM', 'Coupe',    24, NOW(), NOW()),
    ('E-Pickup',  'EVM Pickup Pro',          'EVM', 'Pickup',   30, NOW(), NOW());




