-- =============================================================================
-- Seed Data: Simulated Intel Global Employee Directory
-- =============================================================================

-- Main employees for Case A
INSERT INTO EMPLOYEE (id, full_name, role, manager, email, phone_number) VALUES ('550e8400-e29b-11d4-a716-446655440001', 'Eaton Zveare', 'Security Researcher', 'Jane Doe', 'eaton@intel.com', '+1-555-0000');
INSERT INTO EMPLOYEE (id, full_name, role, manager, email, phone_number) VALUES ('550e8400-e29b-11d4-a716-446655440002', 'Aditya Sharma', 'Engineer Level 1', 'Manager Patel', 'aditya.sharma@intel.com', '+91-555-0001');
INSERT INTO EMPLOYEE (id, full_name, role, manager, email, phone_number) VALUES ('550e8400-e29b-11d4-a716-446655440003', 'Priya Nair', 'Engineer Level 2', 'Manager Patel', 'priya.nair@intel.com', '+91-555-0002');
INSERT INTO EMPLOYEE (id, full_name, role, manager, email, phone_number) VALUES ('550e8400-e29b-11d4-a716-446655440004', 'James Wilson', 'Senior Engineer', 'Manager Smith', 'james.wilson@intel.com', '+1-555-0003');
INSERT INTO EMPLOYEE (id, full_name, role, manager, email, phone_number) VALUES ('550e8400-e29b-11d4-a716-446655440005', 'Sarah Chen', 'Staff Engineer', 'Manager Li', 'sarah.chen@intel.com', '+1-555-0004');
INSERT INTO EMPLOYEE (id, full_name, role, manager, email, phone_number) VALUES ('550e8400-e29b-11d4-a716-446655440006', 'Raj Patel', 'Principal Engineer', 'Manager Kumar', 'raj.patel@intel.com', '+91-555-0005');
INSERT INTO EMPLOYEE (id, full_name, role, manager, email, phone_number) VALUES ('550e8400-e29b-11d4-a716-446655440007', 'Maria Garcia', 'Engineer Level 3', 'Manager Rodriguez', 'maria.garcia@intel.com', '+1-555-0006');

-- =============================================================================
-- KASUS B: Product Hierarchy + Hardened Credentials
-- =============================================================================

-- Product hierarchy
INSERT INTO PRODUCT (id, product_name, product_code, category, status, parent_id, created_by) VALUES ('P-ROOT-100', 'Intel Processors', 'PROC-ROOT', 'Processors', 'Launched', NULL, 'admin');
INSERT INTO PRODUCT (id, product_name, product_code, category, status, parent_id, created_by) VALUES ('P-ROOT-200', 'Intel Chipsets', 'CHIP-ROOT', 'Chipsets', 'Launched', NULL, 'admin');
INSERT INTO PRODUCT (id, product_name, product_code, category, status, parent_id, created_by) VALUES ('P-CHILD-101', 'Core i9-14900K', 'PROC-14900K', 'Processors', 'Launched', 'P-ROOT-100', 'admin');

-- ADMIN_CREDENTIAL (Deep Model: BCrypt)
-- admin: password123
INSERT INTO ADMIN_CREDENTIAL (id, username, password, role, github_token, basic_auth) VALUES ('U-ADMIN-001', 'admin', '$2a$10$LKpcehXbRdSWRhnTMhkjY./cVoYGlPQkm9Zu/E7Bz3zHjqmOMig/m', 'ADMIN', '[MASKED]', '[MASKED]');
-- product-admin: IntelP@ssw0rd2024!
INSERT INTO ADMIN_CREDENTIAL (id, username, password, role, github_token, basic_auth) VALUES ('U-ADMIN-002', 'product-admin', '$2a$10$AJXoROEkKXI8cBaAlRevCudcZdHcS00pos689.FKW6DHzmp.LdQv6', 'ADMIN', '[MASKED]', '[MASKED]');
-- employee-admin: emp123
INSERT INTO ADMIN_CREDENTIAL (id, username, password, role, github_token, basic_auth) VALUES ('U-ADMIN-003', 'employee-admin', '$2a$10$MJS0RFsVbqIImZlMwlix/eEfFv1BOgO7OiJaMf69cowN/7wSExJHO', 'ADMIN', '[MASKED]', '[MASKED]');
-- seims-admin: seims123
INSERT INTO ADMIN_CREDENTIAL (id, username, password, role, github_token, basic_auth) VALUES ('U-ADMIN-004', 'seims-admin', '$2a$10$uOgBLHiJPlpRvqRj9lTNyek3rhoUMa4X7w/sFnQsE53yQ7KQVZmJC', 'ADMIN', '[MASKED]', '[MASKED]');

-- =============================================================================
-- KASUS C: SEIMS — Suppliers & NDAs
-- =============================================================================

INSERT INTO SUPPLIER (id, company_name, contact_person, email, phone, country, ehs_status) VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Taiwan Semiconductor Manufacturing', 'Wei-Lin Chang', 'wchang@tsmc.com', '+886-3-5636688', 'Taiwan', 'Compliant');
INSERT INTO SUPPLIER (id, company_name, contact_person, email, phone, country, ehs_status) VALUES ('661f9511-f30c-52e5-b827-557766551111', 'Samsung Electronics', 'Min-Soo Park', 'mspark@samsung.com', '+82-2-2255-0114', 'South Korea', 'Compliant');

-- NDAs
INSERT INTO SUPPLIER_NDA (id, supplier_id, nda_title, signed_date, expiry_date, classification, document_url) VALUES ('NDA-UUID-100', '550e8400-e29b-41d4-a716-446655440000', 'TSMC 3nm Process Node IP Agreement', '2023-01-15', '2028-01-15', 'Top Secret', '/docs/nda/tsmc-3nm-ip-2023.pdf');
INSERT INTO SUPPLIER_NDA (id, supplier_id, nda_title, signed_date, expiry_date, classification, document_url) VALUES ('NDA-UUID-101', '550e8400-e29b-41d4-a716-446655440000', 'TSMC CoWoS Packaging Agreement', '2023-06-01', '2026-06-01', 'Confidential', '/docs/nda/tsmc-cowos-2023.pdf');
INSERT INTO SUPPLIER_NDA (id, supplier_id, nda_title, signed_date, expiry_date, classification, document_url) VALUES ('NDA-UUID-200', '661f9511-f30c-52e5-b827-557766551111', 'Samsung HBM4 Supply Agreement', '2024-03-10', '2027-03-10', 'Top Secret', '/docs/nda/samsung-hbm4-2024.pdf');

-- Sessions
INSERT INTO USER_SESSION (id, user_id, token, ip_address, created_at, expires_at) VALUES ('SESS-001', 'user001', 'SESSION_TOKEN_1', '192.168.1.100', '2025-01-01 08:00:00', '2025-01-01 20:00:00');
