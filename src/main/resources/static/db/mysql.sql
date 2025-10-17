
create table roles (
                       id bigint primary key auto_increment,
                       name varchar(50) not null unique
);

create table users (
                       id bigint primary key auto_increment,
                       username varchar(100) not null unique,
                       password varchar(255) not null,
                       full_name varchar(150),
                       email varchar(150),
                       enabled boolean not null default true
);

create table users_roles (
                             user_id bigint not null,
                             role_id bigint not null,
                             primary key (user_id, role_id),
                             foreign key (user_id) references users(id),
                             foreign key (role_id) references roles(id)
);

SELECT username, password, enabled FROM users;

CREATE TABLE IF NOT EXISTS quote (
                                     id SERIAL PRIMARY KEY,
                                     customer_id INT,
                                     total_amount DECIMAL(12,2),
                                     status VARCHAR(20),
                                     created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS quote_item (
                                          id SERIAL PRIMARY KEY,
                                          quote_id INT REFERENCES quote(id),
                                          vehicle_id INT,
                                          quantity INT,
                                          unit_price DECIMAL(12,2)
);

CREATE TABLE IF NOT EXISTS order_hdr (
                                         id SERIAL PRIMARY KEY,
                                         quote_id INT,
                                         dealer_id INT,
                                         total_amount DECIMAL(12,2),
                                         status VARCHAR(20),
                                         created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_item (
                                          id SERIAL PRIMARY KEY,
                                          order_id INT REFERENCES order_hdr(id),
                                          vehicle_id INT,
                                          quantity INT,
                                          unit_price DECIMAL(12,2)
);

CREATE TABLE IF NOT EXISTS payment (
                                       id SERIAL PRIMARY KEY,
                                       order_id INT REFERENCES order_hdr(id),
                                       payment_type VARCHAR(20),
                                       amount DECIMAL(12,2),
                                       payment_date TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS installment_plan (
                                                id SERIAL PRIMARY KEY,
                                                order_id INT REFERENCES order_hdr(id),
                                                months INT,
                                                monthly_amount DECIMAL(12,2),
                                                interest_rate DECIMAL(5,2)
);
