-- =============================================================================
-- KASUS A: Business Card Portal
-- =============================================================================
CREATE TABLE IF NOT EXISTS EMPLOYEE (
    id           VARCHAR(255) PRIMARY KEY,
    full_name    VARCHAR(255),
    role         VARCHAR(255),
    manager      VARCHAR(255),
    email        VARCHAR(255),
    phone_number VARCHAR(50)
);

-- =============================================================================
-- KASUS B: Product Hierarchy System
-- =============================================================================

-- Tabel produk
CREATE TABLE IF NOT EXISTS PRODUCT (
    id              VARCHAR(255) PRIMARY KEY,
    product_name    VARCHAR(255),
    product_code    VARCHAR(100),
    category        VARCHAR(100),
    status          VARCHAR(50),
    parent_id       VARCHAR(255),
    created_by      VARCHAR(100),
    FOREIGN KEY (parent_id) REFERENCES PRODUCT(id)
);

-- Tabel kredensial admin
CREATE TABLE IF NOT EXISTS ADMIN_CREDENTIAL (
    id              VARCHAR(255) PRIMARY KEY,
    username        VARCHAR(100),
    password        VARCHAR(255),
    role            VARCHAR(100),
    github_token    VARCHAR(255),
    basic_auth      VARCHAR(255)
);

-- =============================================================================
-- KASUS C: SEIMS (Supplier EHS IP Management System)
-- =============================================================================

-- Tabel supplier
CREATE TABLE IF NOT EXISTS SUPPLIER (
    id              VARCHAR(255) PRIMARY KEY,
    company_name    VARCHAR(255),
    contact_person  VARCHAR(255),
    email           VARCHAR(255),
    phone           VARCHAR(50),
    country         VARCHAR(100),
    ehs_status      VARCHAR(50)
);

-- Tabel NDA
CREATE TABLE IF NOT EXISTS SUPPLIER_NDA (
    id              VARCHAR(255) PRIMARY KEY,
    supplier_id     VARCHAR(255),
    nda_title       VARCHAR(255),
    signed_date     DATE,
    expiry_date     DATE,
    classification  VARCHAR(50),
    document_url    VARCHAR(500),
    FOREIGN KEY (supplier_id) REFERENCES SUPPLIER(id)
);

-- Tabel sesi
CREATE TABLE IF NOT EXISTS USER_SESSION (
    id              VARCHAR(255) PRIMARY KEY,
    user_id         VARCHAR(100),
    token           VARCHAR(500),
    ip_address      VARCHAR(50),
    created_at      TIMESTAMP,
    expires_at      TIMESTAMP
);
