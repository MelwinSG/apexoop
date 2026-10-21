CREATE DATABASE IF NOT EXISTS feedback_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE feedback_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS products (
    product_id INT NOT NULL AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL DEFAULT '',
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (product_id),
    UNIQUE KEY uq_products_name (product_name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS feedback (
    feedback_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    rating TINYINT NOT NULL,
    comment_text VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (feedback_id),
    KEY ix_feedback_product_rating (product_id, rating),
    KEY ix_feedback_user_created (user_id, created_at),
    CONSTRAINT chk_feedback_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_feedback_product FOREIGN KEY (product_id) REFERENCES products (product_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS responses (
    response_id INT NOT NULL AUTO_INCREMENT,
    feedback_id INT NOT NULL,
    admin_id INT NOT NULL,
    response_text VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (response_id),
    UNIQUE KEY uq_responses_feedback (feedback_id),
    CONSTRAINT fk_responses_feedback FOREIGN KEY (feedback_id)
        REFERENCES feedback (feedback_id) ON DELETE CASCADE,
    CONSTRAINT fk_responses_admin FOREIGN KEY (admin_id) REFERENCES users (user_id)
) ENGINE=InnoDB;

INSERT INTO products (product_name, description, price)
SELECT 'Wireless Headphones', 'Over-ear headphones with Bluetooth connectivity.', 79.99
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Wireless Headphones');

INSERT INTO products (product_name, description, price)
SELECT 'Smart Watch', 'A wearable device for notifications and activity tracking.', 129.99
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Smart Watch');

INSERT INTO products (product_name, description, price)
SELECT 'Portable Speaker', 'A compact Bluetooth speaker for everyday listening.', 49.99
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Portable Speaker');
