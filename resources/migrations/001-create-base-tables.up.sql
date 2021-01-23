CREATE TABLE real_estates (
  id bigserial primary key,
  tittle varchar(100) not null,
  phone varchar (20) not null,
  price numeric not null,
  ad_type varchar(10) not null,
  type varchar(10) not null,
  rooms_number numeric not null,
  floor varchar(10),
  description varchar not null,
  location varchar(100) not null,
  micro_location varchar(100) not null,
  geolocation varchar(100) not null,
  url varchar(255) not null,
  living_space_area numeric not null,
  furniture varchar(30),
  heating_type varchar(30),
  has_pictures boolean not null,
  created_on timestamp default current_timestamp not null,
  modified_on timestamp default current_timestamp not null
);

create table real_estates_images (
  id bigserial primary key,
  url varchar unique,
  real_estate_id bigint,
  constraint fk_real_estate
      foreign key(real_estate_id)
  	  references real_estates(id)
  	  on delete cascade
);

create table users(
    id bigserial primary key,
	password varchar(255) not null,
	email varchar(50) unique,
	enabled boolean not null,
	created_on timestamp default current_timestamp not null,
	modified_on timestamp default current_timestamp not null

);


create table persistent_logins (
    id bigserial primary key,
	user_id bigint not null,
	token varchar(64) not null,
	last_used timestamp default current_timestamp not null,
	constraint fk_user_session
          foreign key(user_id)
      	  references users(id)
      	   on delete cascade
);

CREATE TABLE saved_filters (
  id bigserial primary key,
  tittle varchar(100),
  max_price numeric,
  min_price numeric,
  ad_types varchar,
  real_estate_types varchar,
  max_rooms_number numeric,
  min_rooms_number numeric,
  floors varchar,
  locations varchar,
  micro_locations varchar,
  geolocation varchar,
  max_living_space_area numeric,
  min_living_space_area numeric,
  furniture varchar,
  heating_types varchar,
  has_pictures boolean default false,
  email_subscribed boolean default false,
  user_id bigint,
  created_on timestamp default current_timestamp not null,
  modified_on timestamp default current_timestamp not null,
  constraint fk_user
       foreign key(user_id)
    	references users(id)
    	on delete cascade
);