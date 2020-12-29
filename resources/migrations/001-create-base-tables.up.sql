CREATE TABLE real_estates (
  id bigserial primary key,
  tittle varchar(100) not null,
  price numeric not null,
  type varchar(10) not null,
  rooms_number numeric,
  floor smallint,
  description varchar not null,
  geolocation varchar(100) not null,
  url varchar(255) not null,
  living_space_area numeric not null,
  furniture varchar(30),
  heating_type varchar(30),
  created_on timestamp default current_timestamp not null,
  modified_on timestamp default current_timestamp not null
);

CREATE TABLE real_estates_images (
  url varchar primary key,
  real_estate_id bigint,
  CONSTRAINT fk_real_estate
      FOREIGN KEY(real_estate_id)
  	  REFERENCES real_estates(id)
  	  ON DELETE CASCADE
);

create table users(
    id serial primary key,
	username varchar(50) not null unique,
	password varchar(50) not null,
	email varchar(50) unique,
	enabled boolean not null,
	created_on timestamp default current_timestamp not null,
	modified_on timestamp default current_timestamp not null

);


create table persistent_logins (
    id serial primary key,
	username varchar(64) not null,
	token varchar(64) not null,
	last_used timestamp not null
);