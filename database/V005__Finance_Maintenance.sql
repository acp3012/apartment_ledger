-- To hol active maintenance period


CREATE TABLE finance.ledger_period (
	apartment_id  BIGINT PRIMARY KEY ,
	active_year   INTEGER NOT null CHECK (active_year BETWEEN 2000 AND 2999),
	active_month  INTEGER NOT NULL CHECK (active_month BETWEEN 1 AND 12),
	month_name   VARCHAR(20) GENERATED ALWAYS AS (
    CASE active_month
        WHEN 1 THEN 'January'
        WHEN 2 THEN 'February'
        WHEN 3 THEN 'March'
        WHEN 4 THEN 'April'
        WHEN 5 THEN 'May'
        WHEN 6 THEN 'June'
        WHEN 7 THEN 'July'
        WHEN 8 THEN 'August'
        WHEN 9 THEN 'September'
        WHEN 10 THEN 'October'
        WHEN 11 THEN 'November'
        WHEN 12 THEN 'December'
    END
) STORED,
	created_date       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_date       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
	
	CONSTRAINT fk_lp_apartment FOREIGN KEY(apartment_id) REFERENCES core.apartment(apartment_id)
	
);

INSERT INTO finance.ledger_period (apartment_id,active_year,active_month) values (1,2026,8)


/*
CREATE TABLE finance.legacy_arrear
(
    legacy_arrear_id   BIGSERIAL PRIMARY KEY,
    apartment_id       BIGINT NOT NULL,
    flat_id            BIGINT NOT NULL UNIQUE, -- Only one record per flat
    arrear_amount      NUMERIC(10,2) NOT NULL CHECK(arrear_amount > 0),
    is_cleared         BOOLEAN NOT NULL DEFAULT FALSE,
    cleared_date       TIMESTAMP,
    created_date       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_la_apartment FOREIGN KEY(apartment_id) REFERENCES core.apartment(apartment_id),
    CONSTRAINT fk_la_flat FOREIGN KEY(flat_id) REFERENCES core.flat(flat_id)
);
*/
CREATE INDEX idx_legacy_arrear_flat_id
ON finance.legacy_arrear(flat_id);

CREATE TABLE finance.maintenance_advance (
	maintenance_advance BIGSERIAL PRIMARY KEY,
	flat_id                  BIGINT  NOT    NULL,
	receipt_date		 DATE	 NOT    NULL,	
	advance_amount		 Numeric(12,2) NOT NULL,
	remaining_balance 	Numeric(12,2)  NULL,
	payment_mode_id		BIGINT 	      NULL, 
	reference_number	VARCHAR(100) NULL, 
	created_date             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_date             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
	
	
	CONSTRAINT fk_mp_flat
        FOREIGN KEY(flat_id)
        REFERENCES core.flat(flat_id),
        
	CONSTRAINT fk_mp_payment_mode
        FOREIGN KEY(payment_mode_id)
        REFERENCES master.payment_mode(payment_mode_id),
        
);



CREATE TABLE finance.maintenance_rate
(
    maintenance_rate_id      BIGSERIAL PRIMARY KEY,
    apartment_id             BIGINT      NOT NULL,
    flat_id                  BIGINT      NULL,
    effective_from           DATE        NOT NULL,
    effective_to             DATE,
    is_active                BOOLEAN     NOT NULL DEFAULT TRUE,
    created_by               BIGINT      NOT NULL,
    created_date             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

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
CREATE INDEX idx_due_flat
ON finance.maintenance_due(flat_id);

CREATE INDEX idx_due_status
ON finance.maintenance_due(due_status);

CREATE INDEX idx_due_month
ON finance.maintenance_due(txn_year,txn_month);

---- 
CREATE TABLE finance.maintenance_payment
(
    maintenance_payment_id   BIGSERIAL PRIMARY KEY,
    apartment_id             BIGINT NOT NULL,
    flat_id                  BIGINT NOT NULL,
    transaction_date         DATE NOT NULL,
    txn_year                 SMALLINT NOT NULL CHECK (txn_year BETWEEN 2000 AND 2100), -- Typo fixed
    txn_month                SMALLINT NOT NULL CHECK (txn_month BETWEEN 1 AND 12),
    amount                   NUMERIC(12,2) NOT NULL CHECK(amount > 0),
    payment_mode_id          BIGINT NOT NULL,
    reference_number         VARCHAR(50),
    remarks                  VARCHAR(500),
    created_by               BIGINT NOT NULL,
    created_date             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_by              BIGINT NOT NULL,
    approved_date            TIMESTAMP NOT NULL,

    CONSTRAINT fk_mp_apartment
        FOREIGN KEY(apartment_id) REFERENCES core.apartment(apartment_id),
        
    CONSTRAINT fk_mp_flat
        FOREIGN KEY(flat_id) REFERENCES core.flat(flat_id),

    -- Missing FKs added below:
    CONSTRAINT fk_mp_payment_mode
        FOREIGN KEY(payment_mode_id) REFERENCES master.payment_mode(payment_mode_id),

    CONSTRAINT fk_mp_created_by
        FOREIGN KEY(created_by) REFERENCES core.app_user(user_id),

    CONSTRAINT fk_mp_approved_by
        FOREIGN KEY(approved_by) REFERENCES core.app_user(user_id)
);

COMMENT ON TABLE finance.maintenance_payment IS 'Final, approved maintenance payments. Immutable ledger.';


CREATE TABLE finance.maintenance_payment
(
    maintenance_payment_id   BIGSERIAL PRIMARY KEY,
    apartment_id             BIGINT NOT NULL,
    flat_id                  BIGINT NOT NULL,
    transaction_date         DATE NOT NULL,
    txn_year                 SMALLINT NOT NULL CHECK (txn_year BETWEEN 2000 AND 2100), -- Typo fixed
    txn_month                SMALLINT NOT NULL CHECK (txn_month BETWEEN 1 AND 12),
    amount                   NUMERIC(12,2) NOT NULL CHECK(amount > 0),
mvn spring-boot:run    payment_mode_id          BIGINT NOT NULL,
    reference_number         VARCHAR(50),
    remarks                  VARCHAR(500),
    created_by               BIGINT NOT NULL,
    created_date             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_by              BIGINT NOT NULL,
    approved_date            TIMESTAMP NOT NULL,

    CONSTRAINT fk_mp_apartment
        FOREIGN KEY(apartment_id) REFERENCES core.apartment(apartment_id),
        
    CONSTRAINT fk_mp_flat
        FOREIGN KEY(flat_id) REFERENCES core.flat(flat_id),

    -- Missing FKs added below:
    CONSTRAINT fk_mp_payment_mode
        FOREIGN KEY(payment_mode_id) REFERENCES master.payment_mode(payment_mode_id),

    CONSTRAINT fk_mp_created_by
        FOREIGN KEY(created_by) REFERENCES core.app_user(user_id),

    CONSTRAINT fk_mp_approved_by
        FOREIGN KEY(approved_by) REFERENCES core.app_user(user_id)
);

COMMENT ON TABLE finance.maintenance_payment IS 'Final, approved maintenance payments. Immutable ledger.';

CREATE INDEX idx_payment_flat
ON finance.maintenance_payment(apartment_id,flat_id);


---- ledger 
/*
CREATE TABLE finance.monthly_ledger (
    monthly_ledger_id  SERIAL PRIMARY KEY,
    apartment_id       BIGINT NOT NULL, 
    month              SMALLINT NOT NULL CHECK (month BETWEEN 1 AND 12),
    year               SMALLINT NOT NULL CHECK (year BETWEEN 1900 AND 2100),
    opening_balance    NUMERIC(16,2),
    income             NUMERIC(12,2),
    expense            NUMERIC(12,2),
    closing_balance    NUMERIC(16,2) GENERATED ALWAYS AS (
                           COALESCE(opening_balance, 0) + 
                           COALESCE(income, 0) - 
                           COALESCE(expense, 0)
                       ) STORED,
    created_date       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Ensures we only ever have ONE ledger per month per apartment
    CONSTRAINT uq_monthly_ledger_period UNIQUE (apartment_id, year, month)
);
*/

CREATE TABLE finance.monthly_ledger (
    monthly_ledger_id  SERIAL PRIMARY KEY,
    apartment_id       BIGINT NOT NULL, 
    month              SMALLINT NOT NULL CHECK (month BETWEEN 1 AND 12),
    year               SMALLINT NOT NULL CHECK (year BETWEEN 1900 AND 2100),
    opening_balance    NUMERIC(16,2),
    income             NUMERIC(12,2),
    expense            NUMERIC(12,2),
    closing_balance    NUMERIC(16,2) GENERATED ALWAYS AS (
                           COALESCE(opening_balance, 0) + 
                           COALESCE(income, 0) - 
                           COALESCE(expense, 0)
                       ) STORED,
    status 				VARCHAR(15) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED')),
    created_date       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Ensures we only ever have ONE ledger per month per apartment
    CONSTRAINT uq_monthly_ledger_period UNIQUE (apartment_id, year, month)
);

-- Optional: Ensure only ONE month can be OPEN per apartment at any time
CREATE UNIQUE INDEX idx_unique_open_ledger ON finance.monthly_ledger (apartment_id) WHERE status = 'OPEN';



