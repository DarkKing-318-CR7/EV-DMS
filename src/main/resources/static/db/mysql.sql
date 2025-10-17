
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
