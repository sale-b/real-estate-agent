CREATE TABLE real_estates (
  id serial primary key,
  name varchar(100) not null,
  description varchar not null,
  location varchar(100) not null,
  url varchar(255) not null,
  living_space_area varchar(100) not null,
  created_on timestamp default current_timestamp,
  modified_on timestamp default current_timestamp
);

create table users(
    id serial primary key,
	username varchar(50) not null unique,
	password varchar(50) not null,
	email varchar(50) unique,
	enabled boolean not null,
	created_on timestamp default current_timestamp,
	modified_on timestamp default current_timestamp

);


create table persistent_logins (
    id serial primary key,
	username varchar(64) not null,
	token varchar(64) not null,
	last_used timestamp not null
);