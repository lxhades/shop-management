CREATE DATABASE IF NOT EXISTS shop_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE shop_management;
CREATE TABLE IF NOT EXISTS categories (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(255) NOT NULL,
 description VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS products (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(255) NOT NULL,
 price DOUBLE NOT NULL,
 stock INT NOT NULL,
 category_id BIGINT NOT NULL,
 CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id),
 CONSTRAINT ck_price CHECK (price > 0),
 CONSTRAINT ck_stock CHECK (stock >= 0)
);
-- Optional manual schema; Hibernate update also creates tables automatically.
