-- A competition groups multiple users who each manage their own portfolio
-- starting with the same initial capital, competing over a defined period.
CREATE TABLE competitions (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100)  NOT NULL,
    description     VARCHAR(500),
    initial_capital DECIMAL(19,2) NOT NULL,
    start_date      TIMESTAMP     NOT NULL,
    end_date        TIMESTAMP     NOT NULL,
    status          VARCHAR(10)   NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'ACTIVE', 'FINISHED')),
    created_by      BIGINT        NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP     NOT NULL,
    CONSTRAINT chk_dates CHECK (end_date > start_date)
);

-- Links a user to a competition with their dedicated competition portfolio.
CREATE TABLE competition_participants (
    id              BIGSERIAL PRIMARY KEY,
    competition_id  BIGINT NOT NULL REFERENCES competitions(id),
    user_id         BIGINT NOT NULL REFERENCES users(id),
    portfolio_id    BIGINT NOT NULL REFERENCES simulated_portfolios(id),
    joined_at       TIMESTAMP NOT NULL,
    CONSTRAINT uq_competition_user UNIQUE (competition_id, user_id)
);