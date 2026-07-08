
# Portfolio Simulator API

A REST API for simulating stock and ETF portfolio management with real-time market data and competitive trading features.

Built with Java 21 and Spring Boot 4, this project allows users to create simulated portfolios, trade stocks using real market prices from Alpha Vantage, and compete against other users in trading competitions.

---

## Features

- **Authentication** — JWT-based register and login
- **Portfolio Management** — Create and manage multiple simulated portfolios with configurable starting capital
- **Real-Time Market Data** — Search stocks and ETFs, fetch live prices via Alpha Vantage
- **Trading** — Buy and sell shares with real market prices, automatic weighted average price calculation
- **Holdings Tracking** — View current positions with live unrealized P&L
- **Transaction History** — Complete audit trail of all buy/sell operations
- **Competitions** — Create and join trading competitions with leaderboard ranked by return percentage

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Spring Boot | 4.0.7 | Framework |
| Spring Security | 7.0.6 | Authentication & Authorization |
| Spring Data JPA | 4.0.6 | Database ORM |
| PostgreSQL | 16 | Database |
| Flyway | 11.14.1 | Database migrations |
| JWT (jjwt) | 0.12.6 | Token generation and validation |
| Springdoc OpenAPI | 3.0.2 | API documentation (Swagger) |
| Docker & Docker Compose | — | Containerization |
| Lombok | 1.18.46 | Boilerplate reduction |

---

## Prerequisites

- Docker and Docker Compose
- Alpha Vantage API key (free tier at [alphavantage.co](https://www.alphavantage.co/support/#api-key))

---

## Getting Started

**1. Clone the repository**
```bash
git clone https://github.com/AmosMVL2261/portfolio-simulator.git
cd portfolio-simulator
```

**2. Create the `.env` file** in the project root:
```env
DB_NAME=portfoliosimulator
DB_USER=your_db_user
DB_PASSWORD=your_db_password
JWT_SECRET=your_base64_encoded_secret_minimum_32_characters
ALPHAVANTAGE_API_KEY=your_api_key
ALPHAVANTAGE_BASE_URL=https://www.alphavantage.co/query
```

**3. Start the application**
```bash
docker compose up --build
```

**4. Access the API documentation**

Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) in your browser.

---

## API Reference

### Authentication

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/auth/register` | Register a new user | No |
| POST | `/auth/login` | Login and receive JWT token | No |

### Portfolios

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/portfolios` | Create a simulated portfolio | Yes |
| GET | `/portfolios` | List all your portfolios | Yes |
| GET | `/portfolios/{id}` | Get a portfolio with metrics | Yes |

### Market Data

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/market/search?q={keyword}` | Search stocks and ETFs | Yes |
| GET | `/market/price/{symbol}` | Get current market price | Yes |

### Transactions

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/portfolios/{id}/buy` | Buy shares at market price | Yes |
| POST | `/portfolios/{id}/sell` | Sell shares at market price | Yes |
| GET | `/portfolios/{id}/holdings` | View positions with live P&L | Yes |
| GET | `/portfolios/{id}/transactions` | Full transaction history | Yes |

### Competitions

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/competitions` | Create a competition | Yes |
| GET | `/competitions?status={status}` | List competitions by status | Yes |
| POST | `/competitions/{id}/join` | Join a competition | Yes |
| GET | `/competitions/{id}/leaderboard` | Live leaderboard | Yes |

---

## Authentication

All protected endpoints require a Bearer token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

To authenticate in Swagger UI, click the **Authorize** button and paste your token (without the `Bearer` prefix).

---

## Database Schema

### users
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| username | VARCHAR(50) | NOT NULL, UNIQUE |
| email | VARCHAR(100) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL |
| role | VARCHAR(20) | NOT NULL |
| enabled | BOOLEAN | NOT NULL, DEFAULT TRUE |
| created_at | TIMESTAMP | NOT NULL |

### simulated_portfolios
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| user_id | BIGINT | NOT NULL, FK → users |
| name | VARCHAR(100) | NOT NULL |
| initial_capital | DECIMAL(19,2) | NOT NULL |
| cash_balance | DECIMAL(19,2) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### holdings
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| portfolio_id | BIGINT | NOT NULL, FK → simulated_portfolios |
| symbol | VARCHAR(20) | NOT NULL |
| quantity | DECIMAL(19,6) | NOT NULL |
| average_buy_price | DECIMAL(19,2) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### transactions
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| portfolio_id | BIGINT | NOT NULL, FK → simulated_portfolios |
| symbol | VARCHAR(20) | NOT NULL |
| type | VARCHAR(4) | NOT NULL, CHECK IN ('BUY', 'SELL') |
| quantity | DECIMAL(19,6) | NOT NULL |
| price | DECIMAL(19,2) | NOT NULL |
| total_amount | DECIMAL(19,2) | NOT NULL |
| executed_at | TIMESTAMP | NOT NULL |

### competitions
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| name | VARCHAR(100) | NOT NULL |
| description | VARCHAR(500) | |
| initial_capital | DECIMAL(19,2) | NOT NULL |
| start_date | TIMESTAMP | NOT NULL |
| end_date | TIMESTAMP | NOT NULL |
| status | VARCHAR(10) | NOT NULL, CHECK IN ('PENDING', 'ACTIVE', 'FINISHED') |
| created_by | BIGINT | NOT NULL, FK → users |
| created_at | TIMESTAMP | NOT NULL |

### competition_participants
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| competition_id | BIGINT | NOT NULL, FK → competitions |
| user_id | BIGINT | NOT NULL, FK → users |
| portfolio_id | BIGINT | NOT NULL, FK → simulated_portfolios |
| joined_at | TIMESTAMP | NOT NULL |

---

## Notes

- Alpha Vantage free tier allows **25 API calls per day**. Market data is cached per session to minimize usage.
- Competition status (`PENDING` → `ACTIVE` → `FINISHED`) is not automatically updated — a scheduled job can be added with `@Scheduled` without modifying existing logic.
- The leaderboard uses live market prices. If the Alpha Vantage rate limit is reached, holdings are valued at their average buy price as a fallback.

---

## Related Projects

- [investment-tracker](https://github.com/AmosMVL2261/investment-tracker) — Phase 1 and 2 investment tracking API with Spring Security, JWT, and Alpha Vantage integration.