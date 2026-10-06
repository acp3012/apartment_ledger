/*==============================================================
 V004__Finance_01_Income_Expense.sql
 ApartmentLedger
 Finance Module - Income & Expense
 PostgreSQL 17
==============================================================*/

CREATE SCHEMA IF NOT EXISTS finance;

-- ============================================================
-- INCOME DRAFT
-- ============================================================

CREATE TABLE finance.income_draft
(
    income_draft_id         BIGSERIAL,
    apartment_id            BIGINT NOT NULL,
    flat_id                 BIGINT,
    transaction_date       DATE NOT NULL,
    ledger_category_id      BIGINT NOT NULL,
    payment_mode_id         BIGINT NOT NULL,
    reference_number           VARCHAR(50),
    txn_year INT GENERATED ALWAYS AS
    (
        EXTRACT(YEAR FROM transaction_date)::INT
    ) STORED,

    txn_month INT GENERATED ALWAYS AS
    (
        EXTRACT(MONTH FROM transaction_date)::INT
    ) STORED,
    amount                   NUMERIC(12,2)  NOT NULL  CHECK(amount > 0),
    remarks                  VARCHAR(500),
    created_by              BIGINT NOT NULL,
    created_date            TIMESTAMP   NOT NULL     DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT,
    updated_date            TIMESTAMP   NOT NULL     DEFAULT CURRENT_TIMESTAMP,
    approval_status_id       BIGINT     NOT NULL   ,
    approval_comments       VARCHAR(500),
    approved_by             BIGINT,
    approved_date           TIMESTAMP,

    CONSTRAINT pk_income_draft
        PRIMARY KEY(income_draft_id),

    CONSTRAINT fk_income_draft_apartment
        FOREIGN KEY(apartment_id)
        REFERENCES core.apartment(apartment_id),

    CONSTRAINT fk_income_draft_flat
        FOREIGN KEY(flat_id)
        REFERENCES core.flat(flat_id),

    CONSTRAINT fk_income_draft_category
        FOREIGN KEY(ledger_category_id)
        REFERENCES master.ledger_category(ledger_category_id),

    CONSTRAINT fk_income_draft_payment_mode
        FOREIGN KEY(payment_mode_id)
        REFERENCES master.payment_mode(payment_mode_id),

     CONSTRAINT fk_income_draft_created_by
        FOREIGN KEY(created_by)
        REFERENCES core.app_user(user_id),

    CONSTRAINT fk_income_draft_updated_by
        FOREIGN KEY(updated_by)
        REFERENCES core.app_user(user_id),

    CONSTRAINT fk_income_draft_approved_by
        FOREIGN KEY(approved_by)
        REFERENCES core.app_user(user_id),

    
    CONSTRAINT fk_income_draft_approval_status
        FOREIGN KEY(approval_status_id)
        REFERENCES master.approval_status(approval_status_id)

    
);

COMMENT ON TABLE finance.income_draft
IS 'Income waiting for approval'; 


CREATE INDEX idx_income_draft_status
ON finance.income_draft(approval_status_id);

CREATE INDEX idx_income_draft_period
ON finance.income_draft
(
    apartment_id,
    txn_year,
    txn_month
);

CREATE INDEX idx_income_draft_flat
ON finance.income_draft(flat_id);

CREATE INDEX idx_income_draft_transaction_date
ON finance.income_draft(transaction_date);



-- ============================================================
-- APPROVED INCOME
-- ============================================================

CREATE TABLE finance.income
(
    income_id            BIGSERIAL PRIMARY KEY,
    apartment_id         BIGINT NOT NULL,
    flat_id              BIGINT,
    ledger_category_id   BIGINT NOT NULL,
    transaction_date     DATE NOT NULL,
    
    txn_year INT GENERATED ALWAYS AS
    (
        EXTRACT(YEAR FROM transaction_date)::INT
    ) STORED,

    txn_month INT GENERATED ALWAYS AS
    (
        EXTRACT(MONTH FROM transaction_date)::INT
    ) STORED,

    amount               NUMERIC(12,2) NOT NULL
                          CHECK (amount > 0),
    payment_mode_id      BIGINT NOT NULL,
    reference_number      VARCHAR(50),
    remarks              VARCHAR(500),
    created_by           BIGINT NOT NULL,
    created_date         TIMESTAMP NOT NULL      DEFAULT CURRENT_TIMESTAMP,
    approved_by          BIGINT NOT NULL,
    approved_date        TIMESTAMP NOT NULL,

    CONSTRAINT fk_income_apartment
        FOREIGN KEY (apartment_id)
        REFERENCES core.apartment(apartment_id),

    CONSTRAINT fk_income_flat
        FOREIGN KEY (flat_id)
        REFERENCES core.flat(flat_id),

    CONSTRAINT fk_income_category
        FOREIGN KEY (ledger_category_id)
        REFERENCES master.ledger_category(ledger_category_id),

    CONSTRAINT fk_income_payment_mode
        FOREIGN KEY (payment_mode_id)
        REFERENCES master.payment_mode(payment_mode_id),

    CONSTRAINT fk_income_reference_type
        FOREIGN KEY (reference_type_id)
        REFERENCES master.reference_type(reference_type_id),

    CONSTRAINT fk_income_created_by
        FOREIGN KEY (created_by)
        REFERENCES core.app_user(user_id),

    CONSTRAINT fk_income_approved_by
        FOREIGN KEY (approved_by)
        REFERENCES core.app_user(user_id)
);

COMMENT ON TABLE finance.income IS
'Approved income transactions. Records are immutable.';

CREATE INDEX idx_income__period
ON finance.income
(
    apartment_id,
    txn_year,
    txn_month
);


-- ============================================================
-- EXPENSE DRAFT
-- ============================================================
CREATE TABLE finance.expense_draft
(
    expense_draft_id     BIGSERIAL PRIMARY KEY,
    apartment_id         BIGINT NOT NULL,
    ledger_category_id   BIGINT NOT NULL,
    transaction_date     DATE NOT NULL,
    txn_year INT GENERATED ALWAYS AS
    (
        EXTRACT(YEAR FROM transaction_date)::INT
    ) STORED,

    txn_month INT GENERATED ALWAYS AS
    (
        EXTRACT(MONTH FROM transaction_date)::INT
    ) STORED,
    amount               NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    payment_mode_id      BIGINT NOT NULL,
    reference_number     VARCHAR(50), 
    remarks              VARCHAR(500),
    created_by           BIGINT NOT NULL,
    created_date         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              BIGINT,
    updated_date            TIMESTAMP   NOT NULL     DEFAULT CURRENT_TIMESTAMP,
    approval_status_id       BIGINT     NOT NULL   ,
    approval_comments       VARCHAR(500),
    approved_by             BIGINT,
    approved_date           TIMESTAMP,
   
    CONSTRAINT fk_expense_draft_apartment
        FOREIGN KEY (apartment_id) REFERENCES core.apartment(apartment_id),
    CONSTRAINT fk_expense_draft_category
        FOREIGN KEY (ledger_category_id) REFERENCES master.ledger_category(ledger_category_id),
    CONSTRAINT fk_expense_draft_payment_mode
        FOREIGN KEY (payment_mode_id) REFERENCES master.payment_mode(payment_mode_id),
    CONSTRAINT fk_expense_draft_status
        FOREIGN KEY (approval_status_id) REFERENCES master.approval_status(approval_status_id),
    CONSTRAINT fk_expense_draft_created_by
        FOREIGN KEY (created_by) REFERENCES core.app_user(user_id),
        
       
    CONSTRAINT fk_expense_draft_updated_by
        FOREIGN KEY(updated_by)
        REFERENCES core.app_user(user_id),

    CONSTRAINT fk_expense_draft_approved_by
        FOREIGN KEY(approved_by)
        REFERENCES core.app_user(user_id),

        CONSTRAINT fk_expense_draft_approval_status
        FOREIGN KEY(approval_status_id)
        REFERENCES master.approval_status(approval_status_id)

);

COMMENT ON TABLE finance.expense_draft IS 'Expense entries waiting for approval.';
CREATE INDEX idx_expense_draft__period
ON finance.income
(
    apartment_id,
    txn_year,
    txn_month
);
-- ============================================================
-- APPROVED EXPENSE
-- ============================================================

CREATE TABLE finance.expense
(
    expense_id           BIGSERIAL PRIMARY KEY,
    apartment_id         BIGINT NOT NULL,
    ledger_category_id   BIGINT NOT NULL, 
    transaction_date     DATE NOT NULL,
    txn_year             SMALLINT NOT NULL CHECK (txn_year BETWEEN 2000 AND 2100),
    txn_month            SMALLINT NOT NULL CHECK (txn_month BETWEEN 1 AND 12),
    amount               NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    payment_mode_id      BIGINT NOT NULL,
    reference_number     VARCHAR(50), 
    remarks              VARCHAR(500),
    created_by           BIGINT NOT NULL,
    created_date         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_by          BIGINT NOT NULL,
    approved_date        TIMESTAMP NOT NULL,

    CONSTRAINT fk_expense_apartment
        FOREIGN KEY (apartment_id) REFERENCES core.apartment(apartment_id),
    CONSTRAINT fk_expense_category
        FOREIGN KEY (ledger_category_id) REFERENCES master.ledger_category(ledger_category_id),
    CONSTRAINT fk_expense_payment_mode
        FOREIGN KEY (payment_mode_id) REFERENCES master.payment_mode(payment_mode_id),
    CONSTRAINT fk_expense_created_by
        FOREIGN KEY (created_by) REFERENCES core.app_user(user_id),
    CONSTRAINT fk_expense_approved_by
        FOREIGN KEY (approved_by) REFERENCES core.app_user(user_id)
);

COMMENT ON TABLE finance.expense IS 'Approved expense transactions. Records are immutable.';
-- ============================================================
-- INDEXES
-- ============================================================

