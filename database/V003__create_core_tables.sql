-- ============================================================
-- ApartmentLedger
-- V003__create_core_tables.sql
-- Core Schema
-- ============================================================

SET search_path TO core;

-- ============================================================
-- APARTMENT
-- ============================================================

CREATE TABLE core.apartment
(
    apartment_id           BIGSERIAL,
    apartment_code         VARCHAR(20)  NOT NULL,
    apartment_name         VARCHAR(150) NOT NULL,
    address                VARCHAR(200),
    city                   VARCHAR(100),
    state                  VARCHAR(100),
    pincode                VARCHAR(10),
    bank_name              VARCHAR(100),
    account_number         VARCHAR(30),
    ifsc_code              VARCHAR(20),
    is_active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date 	  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_apartment     PRIMARY KEY (apartment_id),

    CONSTRAINT uq_apartment_code    UNIQUE (apartment_code)
);

COMMENT ON TABLE core.apartment
IS 'Apartment Master';

CREATE INDEX idx_apartment_name
ON core.apartment(apartment_name);

-- ============================================================
-- FLAT
-- ============================================================

CREATE TABLE core.flat
(
    flat_id                BIGSERIAL,
    apartment_id           BIGINT       NOT NULL,
    flat_number            VARCHAR(20)  NOT NULL,
    floor_number           SMALLINT,
    owner_name             VARCHAR(150) NOT NULL,
    owner_mobile           VARCHAR(20)  NOT NULL,
    owner_email            VARCHAR(150),
    maintenance_amount     NUMERIC(12,2) NOT NULL DEFAULT 0,
    is_active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_flat     PRIMARY KEY (flat_id),
    CONSTRAINT fk_flat_apartment   FOREIGN KEY (apartment_id)  REFERENCES core.apartment(apartment_id),
    CONSTRAINT uq_flat_number   UNIQUE (apartment_id, flat_number)
);

COMMENT ON TABLE core.flat IS 'Apartment Flat Details';

CREATE INDEX idx_flat_apartment ON core.flat(apartment_id);

CREATE INDEX idx_flat_owner_mobile ON core.flat(owner_mobile);

-- ============================================================
-- APP USER
-- ============================================================

CREATE TABLE core.app_user
(
    user_id                BIGSERIAL,
    apartment_id           BIGINT       NOT NULL,
    flat_id                BIGINT       NOT NULL,
    email                  VARCHAR(100) NOT NULL,
    password_hash          VARCHAR(255) NOT NULL,
    mobile_number          VARCHAR(20)  NOT NULL,
    display_name           VARCHAR(100),
    last_login_date        TIMESTAMP,
    is_active              BOOLEAN      NOT NULL DEFAULT TRUE,
    is_primary_contact      BOOLEAN NOT NULL DEFAULT FALSE,
    is_email_verified       BOOLEAN NOT NULL DEFAULT FALSE,
    is_locked               BOOLEAN NOT NULL DEFAULT FALSE,
    failed_attempts         SMALLINT NOT NULL DEFAULT 0,
    password_changed_date   TIMESTAMP,
    profile_image 	   VARCHAR(255),
    last_login_ip          VARCHAR(45),
    created_date           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT pk_app_user      PRIMARY KEY(user_id),
    CONSTRAINT fk_user_apartment   FOREIGN KEY(apartment_id)  REFERENCES core.apartment(apartment_id),
    CONSTRAINT fk_user_flat        FOREIGN KEY(flat_id)      REFERENCES core.flat(flat_id),

    CONSTRAINT uq_user_email         UNIQUE(email)
);

COMMENT ON TABLE core.app_user
IS 'Application Login Users';

CREATE INDEX idx_user_apartment
ON core.app_user(apartment_id);

CREATE INDEX idx_user_flat
ON core.app_user(flat_id);

CREATE INDEX idx_user_mobile
ON core.app_user(mobile_number);

-- ============================================================
-- USER ROLE
-- ============================================================

CREATE TABLE core.user_role
(
    user_role_id           BIGSERIAL,
    user_id                BIGINT NOT NULL,
    role_id                BIGINT NOT NULL,
    is_active              Boolean NOT NULL DEFAULT TRUE,
    created_date           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT pk_user_role    PRIMARY KEY(user_role_id),
    CONSTRAINT fk_user_role_user  FOREIGN KEY(user_id)  REFERENCES core.app_user(user_id),
    CONSTRAINT fk_user_role_role  FOREIGN KEY(role_id)  REFERENCES master.role(role_id),

    CONSTRAINT uq_user_role    UNIQUE(user_id, role_id)
);

COMMENT ON TABLE core.user_role
IS 'Maps Users to Roles';

