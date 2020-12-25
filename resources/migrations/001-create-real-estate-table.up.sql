CREATE TABLE real_estate (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description VARCHAR NOT NULL,
  price VARCHAR(100) NOT NULL,
  location VARCHAR(100) NOT NULL,
  url VARCHAR(255) NOT NULL,
  living_space_area VARCHAR(100) NOT NULL
);