CREATE TABLE finance.maintenance_rate
(
    maintenance_rate_id      BIGSERIAL PRIMARY KEY,

    apartment_id             BIGINT      NOT NULL,
    flat_id                  BIGINT      NULL,

    effective_from           DATE        NOT NULL,
    effective_to             DATE,

    calculation_method       VARCHAR(20) NOT NULL,

    fixed_fee                NUMERIC(12,2),
    per_sqft_rate            NUMERIC(10,2),

    is_active                BOOLEAN     NOT NULL DEFAULT TRUE,

    created_by               BIGINT      NOT NULL,
    created_date             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_maintenance_rate_method
        CHECK (calculation_method IN ('FIXED','PER_SQFT')),

    CONSTRAINT chk_maintenance_rate_amount
        CHECK (
            (calculation_method='FIXED'
                AND fixed_fee IS NOT NULL
                AND per_sqft_rate IS NULL)
         OR
            (calculation_method='PER_SQFT'
                AND fixed_fee IS NULL
                AND per_sqft_rate IS NOT NULL)
        ),

    CONSTRAINT fk_mr_apartment
        FOREIGN KEY(apartment_id)
        REFERENCES core.apartment(apartment_id),

    CONSTRAINT fk_mr_flat
        FOREIGN KEY(flat_id)
        REFERENCES core.flat(flat_id),

    CONSTRAINT fk_mr_created_by
        FOREIGN KEY(created_by)
        REFERENCES core.app_user(user_id)
);
CREATE TABLE finance.maintenance_rate
(
    maintenance_rate_id      BIGSERIAL PRIMARY KEY,

    apartment_id             BIGINT      NOT NULL,
    flat_id                  BIGINT      NULL,

    effective_from           DATE        NOT NULL,
    effective_to             DATE,

    calculation_method       VARCHAR(20) NOT NULL,

    fixed_fee                NUMERIC(12,2),
    per_sqft_rate            NUMERIC(10,2),

    is_active                BOOLEAN     NOT NULL DEFAULT TRUE,

    created_by               BIGINT      NOT NULL,
    created_date             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_maintenance_rate_method
        CHECK (calculation_method IN ('FIXED','PER_SQFT')),

    CONSTRAINT chk_maintenance_rate_amount
        CHECK (
            (calculation_method='FIXED'
                AND fixed_fee IS NOT NULL
                AND per_sqft_rate IS NULL)
         OR
            (calculation_method='PER_SQFT'
                AND fixed_fee IS NULL
                AND per_sqft_rate IS NOT NULL)
        ),

    CONSTRAINT fk_mr_apartment
        FOREIGN KEY(apartment_id)
        REFERENCES core.apartment(apartment_id),

    CONSTRAINT fk_mr_flat
        FOREIGN KEY(flat_id)
        REFERENCES core.flat(flat_id),

    CONSTRAINT fk_mr_created_by
        FOREIGN KEY(created_by)
        REFERENCES core.app_user(user_id)
);


CREATE TABLE finance.maintenance_due
(
    maintenance_due_id      BIGSERIAL PRIMARY KEY,

    apartment_id            BIGINT      NOT NULL,
    flat_id                 BIGINT      NOT NULL,

    txn_year                INTEGER     NOT NULL,
    txn_month               INTEGER     NOT NULL,

    due_amount              NUMERIC(12,2) NOT NULL,

    paid_amount             NUMERIC(12,2) NOT NULL DEFAULT 0,

    balance_amount          NUMERIC(12,2)
        GENERATED ALWAYS AS (due_amount - paid_amount) STORED,

    due_status              VARCHAR(20) NOT NULL,

    generated_date          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_due_status
        CHECK (due_status IN ('PENDING','PARTIAL','PAID')),

    CONSTRAINT uq_due
        UNIQUE
        (
            apartment_id,
            flat_id,
            txn_year,
            txn_month
        ),

    CONSTRAINT fk_due_apartment
        FOREIGN KEY(apartment_id)
        REFERENCES core.apartment(apartment_id),

    CONSTRAINT fk_due_flat
        FOREIGN KEY(flat_id)
        REFERENCES core.flat(flat_id)
);


CREATE TABLE finance.maintenance_payment
(
    maintenance_payment_id      BIGSERIAL PRIMARY KEY,

    apartment_id               BIGINT NOT NULL,

    flat_id                    BIGINT NOT NULL,

    income_id                  BIGINT NOT NULL,

    txn_year                   INTEGER NOT NULL,

    txn_month                  INTEGER NOT NULL,

    amount                     NUMERIC(12,2) NOT NULL
                               CHECK(amount>0),

    created_date               TIMESTAMP NOT NULL
                               DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mp_apartment
        FOREIGN KEY(apartment_id)
        REFERENCES core.apartment(apartment_id),

    CONSTRAINT fk_mp_flat
        FOREIGN KEY(flat_id)
        REFERENCES core.flat(flat_id),

    CONSTRAINT fk_mp_income
        FOREIGN KEY(income_id)
        REFERENCES finance.income(income_id)
);

CREATE INDEX idx_due_flat
ON finance.maintenance_due(flat_id);

CREATE INDEX idx_due_status
ON finance.maintenance_due(due_status);

CREATE INDEX idx_due_month
ON finance.maintenance_due(txn_year,txn_month);

CREATE INDEX idx_payment_flat
ON finance.maintenance_payment(flat_id);

CREATE INDEX idx_payment_income
ON finance.maintenance_payment(income_id);
