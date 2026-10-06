-- =====================================================
-- ApartmentLedger
-- Master Schema Tables
-- =====================================================

SET search_path TO master;

-- =====================================================
-- ROLE
-- =====================================================

CREATE TABLE master.role
(
    role_id             BIGSERIAL,
    role_name           VARCHAR(30) NOT NULL,
    role_description    VARCHAR(100),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_role
        PRIMARY KEY (role_id),
    CONSTRAINT uq_role_name
        UNIQUE(role_name)
);

COMMENT ON TABLE master.role IS 'Application Roles';

-- =====================================================
-- LEDGER CATEGORY
-- =====================================================

CREATE TABLE master.ledger_category
(
    ledger_category_id      BIGSERIAL PRIMARY KEY,
    category_name           VARCHAR(100) NOT NULL,
    transaction_type        VARCHAR(2) NOT NULL,
    is_flat_maintenance     BOOLEAN NOT NULL DEFAULT FALSE,
    display_order           INTEGER NOT NULL DEFAULT 1,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_date            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ledger_category
        PRIMARY KEY (ledger_category_id),

    CONSTRAINT uq_ledger_category
        UNIQUE(category_name),

    CONSTRAINT chk_transaction_type
        CHECK (transaction_type IN ('CR','DR'))
);

COMMENT ON TABLE master.ledger_category
IS 'Income and Expense Categories';

-- =====================================================
-- PAYMENT MODE
-- =====================================================

CREATE TABLE master.payment_mode
(
    payment_mode_id     BIGSERIAL,
    payment_mode_name   VARCHAR(30) NOT NULL,
    display_order       INTEGER NOT NULL DEFAULT 1,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_payment_mode        PRIMARY KEY(payment_mode_id),
    CONSTRAINT uq_payment_mode         UNIQUE(payment_mode_name)
);

COMMENT ON TABLE master.payment_mode
IS 'Cash / UPI / Cheque / NEFT etc';



CREATE TABLE master.approval_status
(
    approval_status_id      BIGSERIAL,
    status_code             VARCHAR(20) NOT NULL,
    status_name             VARCHAR(50) NOT NULL,
    display_order           INTEGER NOT NULL DEFAULT 1,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_date            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_approval_status
        PRIMARY KEY (approval_status_id),

    CONSTRAINT uq_approval_status_code
        UNIQUE (status_code),

    CONSTRAINT uq_approval_status_name
        UNIQUE (status_name)
);

COMMENT ON TABLE master.approval_status
IS 'Approval workflow status';

insert into master.approval_status (status_code,status_name,display_order) values ('P','PENDING',1), ('A','APPROVED',2),('R','REJECTED',3)

CREATE TABLE

