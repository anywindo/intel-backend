-- Schema for the Employee table (simulates Intel's global employee directory)
CREATE TABLE IF NOT EXISTS EMPLOYEE (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(255),
    role        VARCHAR(255),
    manager     VARCHAR(255),
    email       VARCHAR(255),
    phone_number VARCHAR(50)
);
