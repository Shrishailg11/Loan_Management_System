-- ============ USERS AND ROLE PROFILES ============

CREATE TABLE users (
                       id             BIGSERIAL PRIMARY KEY,
                       email          VARCHAR(255) NOT NULL UNIQUE,
                       password_hash  VARCHAR(255) NOT NULL,
                       name           VARCHAR(100) NOT NULL,
                       phone          VARCHAR(15),
                       role           VARCHAR(20)  NOT NULL
                           CHECK (role IN ('CUSTOMER', 'LOAN_OFFICER', 'ADMIN')),
                       enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customers (
                           id              BIGSERIAL PRIMARY KEY,
                           user_id         BIGINT        NOT NULL UNIQUE REFERENCES users (id),
                           pan_number      VARCHAR(10)   NOT NULL UNIQUE,
                           monthly_income  NUMERIC(15,2) NOT NULL CHECK (monthly_income >= 0),
                           date_of_birth   DATE          NOT NULL,
                           credit_score    INT           CHECK (credit_score BETWEEN 300 AND 900)
);

CREATE TABLE loan_officers (
                               id              BIGSERIAL PRIMARY KEY,
                               user_id         BIGINT        NOT NULL UNIQUE REFERENCES users (id),
                               employee_id     VARCHAR(30)   NOT NULL UNIQUE,
                               approval_limit  NUMERIC(15,2) NOT NULL CHECK (approval_limit >= 0)
);

CREATE TABLE admins (
                        id           BIGSERIAL PRIMARY KEY,
                        user_id      BIGINT       NOT NULL UNIQUE REFERENCES users (id),
                        employee_id  VARCHAR(30)  NOT NULL UNIQUE,
                        department   VARCHAR(100)
);

-- ============ LOAN PRODUCTS (the catalogue) ============

CREATE TABLE loan_products (
                               id                 BIGSERIAL PRIMARY KEY,
                               name               VARCHAR(100)  NOT NULL UNIQUE,
                               interest_rate      NUMERIC(5,2)  NOT NULL CHECK (interest_rate > 0),
                               min_amount         NUMERIC(15,2) NOT NULL CHECK (min_amount > 0),
                               max_amount         NUMERIC(15,2) NOT NULL,
                               min_tenure_months  INT           NOT NULL CHECK (min_tenure_months > 0),
                               max_tenure_months  INT           NOT NULL,
                               active             BOOLEAN       NOT NULL DEFAULT TRUE,
                               CHECK (max_amount >= min_amount),
                               CHECK (max_tenure_months >= min_tenure_months)
);

-- ============ LOANS ============

CREATE TABLE loans (
                       id             BIGSERIAL PRIMARY KEY,
                       loan_no        VARCHAR(30)   NOT NULL UNIQUE,
                       customer_id    BIGINT        NOT NULL REFERENCES customers (id),
                       product_id     BIGINT        NOT NULL REFERENCES loan_products (id),
                       approved_by    BIGINT        REFERENCES loan_officers (id),
                       principal      NUMERIC(15,2) NOT NULL CHECK (principal > 0),
                       tenure_months  INT           NOT NULL CHECK (tenure_months > 0),
                       interest_rate  NUMERIC(5,2),          -- frozen at approval
                       emi_amount     NUMERIC(15,2),         -- calculated at approval
                       status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                           CHECK (status IN ('PENDING','APPROVED','ACTIVE','REJECTED','COMPLETED')),
                       applied_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       decided_at     TIMESTAMP,
                       disbursed_at   TIMESTAMP
);

CREATE INDEX idx_loans_customer ON loans (customer_id);
CREATE INDEX idx_loans_product  ON loans (product_id);
CREATE INDEX idx_loans_status   ON loans (status);

-- ============ REPAYMENT SCHEDULE ============

CREATE TABLE repayment_schedules (
                                     id                   BIGSERIAL PRIMARY KEY,
                                     loan_id              BIGINT        NOT NULL REFERENCES loans (id),
                                     installment_no       INT           NOT NULL CHECK (installment_no > 0),
                                     due_date             DATE          NOT NULL,
                                     principal_component  NUMERIC(15,2) NOT NULL,
                                     interest_component   NUMERIC(15,2) NOT NULL,
                                     emi_amount           NUMERIC(15,2) NOT NULL,
                                     paid_at              TIMESTAMP,
                                     UNIQUE (loan_id, installment_no)
);

-- fast lookup of unpaid installments (overdue checks, next EMI due)
CREATE INDEX idx_schedule_unpaid_due
    ON repayment_schedules (due_date) WHERE paid_at IS NULL;

-- ============ TRANSACTIONS ============

CREATE TABLE transactions (
                              id            BIGSERIAL PRIMARY KEY,
                              loan_id       BIGINT        NOT NULL REFERENCES loans (id),
                              schedule_id   BIGINT        REFERENCES repayment_schedules (id),
                              type          VARCHAR(20)   NOT NULL CHECK (type IN ('DISBURSEMENT', 'PAYMENT')),
                              amount        NUMERIC(15,2) NOT NULL CHECK (amount > 0),
                              reference_no  VARCHAR(50)   NOT NULL UNIQUE,
                              created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_loan ON transactions (loan_id);