-- Holds the current position of a user in a specific stock within a portfolio.
-- Updated on every buy/sell to reflect the latest quantity and average price.
CREATE TABLE holdings (
    id                  BIGSERIAL PRIMARY KEY,
    portfolio_id        BIGINT         NOT NULL REFERENCES simulated_portfolios(id),
    symbol              VARCHAR(20)    NOT NULL,
    quantity            DECIMAL(19,6)  NOT NULL,
    average_buy_price   DECIMAL(19,2)  NOT NULL,
    created_at          TIMESTAMP      NOT NULL,
    updated_at          TIMESTAMP      NOT NULL,
    CONSTRAINT uq_portfolio_symbol UNIQUE (portfolio_id, symbol)
);

-- Immutable record of every buy or sell operation in a portfolio.
-- Never updated after creation — represents the historical audit trail.
CREATE TABLE transactions (
    id              BIGSERIAL PRIMARY KEY,
    portfolio_id    BIGINT         NOT NULL REFERENCES simulated_portfolios(id),
    symbol          VARCHAR(20)    NOT NULL,
    type            VARCHAR(4)     NOT NULL CHECK (type IN ('BUY', 'SELL')),
    quantity        DECIMAL(19,6)  NOT NULL,
    price           DECIMAL(19,2)  NOT NULL,
    total_amount    DECIMAL(19,2)  NOT NULL,
    executed_at     TIMESTAMP      NOT NULL
);