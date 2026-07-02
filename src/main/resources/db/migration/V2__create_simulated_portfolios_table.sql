CREATE TABLE simulated_portfolios (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL REFERENCES users(id),
    name            VARCHAR(100)  NOT NULL,
    initial_capital DECIMAL(19,2) NOT NULL,
    cash_balance    DECIMAL(19,2) NOT NULL,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL,
    CONSTRAINT uq_user_portfolio_name UNIQUE (user_id, name)
);