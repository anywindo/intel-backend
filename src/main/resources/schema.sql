-- =============================================================================
-- KASUS A: Business Card Portal
-- Simulates Intel's global employee directory (270,000 records in real breach)
-- =============================================================================
CREATE TABLE IF NOT EXISTS EMPLOYEE (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name    VARCHAR(255),
    role         VARCHAR(255),
    manager      VARCHAR(255),
    email        VARCHAR(255),
    phone_number VARCHAR(50)
);

-- =============================================================================
-- KASUS B: Product Hierarchy System
-- Simulates Intel's product hierarchy management + hardcoded admin credentials
-- =============================================================================

-- Tabel produk: hierarki produk Intel (ARK database)
CREATE TABLE IF NOT EXISTS PRODUCT (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name    VARCHAR(255),
    product_code    VARCHAR(100),
    category        VARCHAR(100),
    status          VARCHAR(50),
    parent_id       BIGINT,
    created_by      VARCHAR(100),
    FOREIGN KEY (parent_id) REFERENCES PRODUCT(id)
);

-- Tabel kredensial admin: SENGAJA INSECURE (Shallow Model)
-- Dalam sistem asli, credentials hardcoded di JavaScript klien
CREATE TABLE IF NOT EXISTS ADMIN_CREDENTIAL (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(100),
    password        VARCHAR(255),
    role            VARCHAR(100),
    github_token    VARCHAR(255),
    basic_auth      VARCHAR(255)
);

-- =============================================================================
-- KASUS C: SEIMS (Supplier EHS IP Management System)
-- Simulates supplier data, NDAs, and session management
-- =============================================================================

-- Tabel supplier: data pemasok Intel
CREATE TABLE IF NOT EXISTS SUPPLIER (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name    VARCHAR(255),
    contact_person  VARCHAR(255),
    email           VARCHAR(255),
    phone           VARCHAR(50),
    country         VARCHAR(100),
    ehs_status      VARCHAR(50)
);

-- Tabel NDA: Non-Disclosure Agreements (data sangat sensitif)
CREATE TABLE IF NOT EXISTS SUPPLIER_NDA (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id     BIGINT,
    nda_title       VARCHAR(255),
    signed_date     DATE,
    expiry_date     DATE,
    classification  VARCHAR(50),
    document_url    VARCHAR(500),
    FOREIGN KEY (supplier_id) REFERENCES SUPPLIER(id)
);

-- Tabel sesi: user session management (SHALLOW: no binding)
CREATE TABLE IF NOT EXISTS USER_SESSION (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         VARCHAR(100),
    token           VARCHAR(500),
    ip_address      VARCHAR(50),
    created_at      TIMESTAMP,
    expires_at      TIMESTAMP
);
