# Exchange Rate API

REST API built with Spring Boot for retrieving exchange rates and converting amounts between currencies.

The API uses an external exchange-rate provider, caches the latest rates for a short period, validates requests, handles provider errors, and applies basic rate limiting per client IP.

## Features

- Get the exchange rate between two currencies
- Get all exchange rates from a base currency
- Convert an amount from one currency to another
- Convert an amount to multiple currencies
- Request validation
- Global error handling
- Caffeine cache for external provider responses
- Basic rate limiting with Bucket4j
- Swagger/OpenAPI documentation
- Unit tests
- Docker support

## Tech Stack

- Java 21
- Spring Boot
- Maven
- Spring Web
- Caffeine Cache
- Bucket4j
- Springdoc OpenAPI
- JUnit 5
- Mockito
- Docker
- Docker Compose

## Configuration

The API requires access to the external exchange-rate provider.

Create a `.env` file based on `.env.example`:

```env
EXCHANGE_RATE_API_BASE_URL=https://api.exchangerate.host
EXCHANGE_RATE_API_ACCESS_KEY=replace-with-your-access-key
```

The `.env` file is ignored by Git and should not be committed.

## Run Locally

### Requirements

- Java 21
- Maven, or the included Maven Wrapper

Configure the required environment variables in your terminal or IntelliJ run configuration.

On Linux or macOS:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

## Run With Docker

Make sure Docker is running and the `.env` file contains valid provider credentials.

Build and start the application:

```bash
docker compose up --build
```

Stop the application:

```bash
docker compose down
```

## API Documentation

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

The OpenAPI specification is available at:

```text
http://localhost:8080/v3/api-docs
```

## Endpoints

### Health Check

```http
GET /api/health
```

Example response:

```text
Exchange Rate API is running
```

### Get Exchange Rate

Returns the exchange rate from one currency to another.

```http
GET /api/rates/{from}/{to}
```

Example:

```http
GET /api/rates/EUR/GBP
```

Example response:

```json
{
  "from": "EUR",
  "to": "GBP",
  "rate": 0.8567340000
}
```

### Get All Rates From a Base Currency

Returns all available exchange rates using the supplied currency as the base.

```http
GET /api/rates/{from}
```

Example:

```http
GET /api/rates/EUR
```

Example response:

```json
{
  "base": "EUR",
  "rates": {
    "GBP": 0.8567340000,
    "JPY": 170.4200000000,
    "USD": 1.0923000000
  }
}
```

### Convert an Amount

Converts a monetary amount from one currency to another.

```http
GET /api/conversions?from={from}&to={to}&amount={amount}
```

Example:

```http
GET /api/conversions?from=EUR&to=GBP&amount=100
```

Example response:

```json
{
  "from": "EUR",
  "to": "GBP",
  "amount": 100,
  "rate": 0.8567340000,
  "convertedAmount": 85.6734000000
}
```

### Convert an Amount to Multiple Currencies

Converts a monetary amount from one currency to a supplied list of target currencies.

```http
GET /api/conversions/multiple?from={from}&to={currencies}&amount={amount}
```

Example:

```http
GET /api/conversions/multiple?from=EUR&to=GBP,USD,JPY&amount=100
```

Example response:

```json
{
  "from": "EUR",
  "amount": 100,
  "conversions": {
    "GBP": 85.6734000000,
    "JPY": 17042.0000000000,
    "USD": 109.2300000000
  }
}
```

## Exchange-Rate Calculation

The external provider returns exchange rates using USD as the base currency.

Rates between two non-USD currencies are calculated using the following cross-rate formula:

```text
source -> target = (USD -> target) / (USD -> source)
```

Example:

```text
EUR -> GBP = (USD -> GBP) / (USD -> EUR)
```

`BigDecimal` is used for exchange-rate and monetary calculations to avoid floating-point precision issues.

Results are calculated with a scale of 10 decimal places using `RoundingMode.HALF_UP`.

## Caching

The latest response from the external provider is cached using Caffeine.

Cache configuration:

- Expiration time: 1 minute
- Maximum entries: 1
- Synchronized cache loading

This reduces the number of external API calls while keeping exchange-rate data within the accepted one-minute delay.

## Rate Limiting

The API allows up to:

```text
60 requests per minute per client IP
```

Requests exceeding this limit receive:

```http
429 Too Many Requests
```

The current rate limiter is stored in application memory and is local to each running application instance.

## Error Responses

The API returns structured error responses.

### Invalid Request

Invalid inputs and unsupported currencies return:

```http
400 Bad Request
```

Example:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Currency must contain exactly 3 letters",
  "timestamp": "2026-08-04T14:30:00"
}
```

### External Provider Failure

Invalid provider responses, HTTP errors and connection failures return:

```http
502 Bad Gateway
```

Example:

```json
{
  "status": 502,
  "error": "Bad Gateway",
  "message": "External provider returned no exchange rates",
  "timestamp": "2026-08-04T14:30:00"
}
```

### Rate Limit Exceeded

Requests exceeding the configured limit return:

```http
429 Too Many Requests
```

## Tests

Run all tests on Linux or macOS:

```bash
./mvnw clean test
```

On Windows:

```powershell
.\mvnw.cmd clean test
```

The tests cover:

- exchange-rate calculations
- USD base-currency handling
- all-rates calculation
- single-currency conversion
- multiple-currency conversion
- unsupported currencies
- rate-limiting behaviour

## Project Structure

```text
src/main/java/com/rho/exchangerate
├── client
├── config
├── controller
├── dto
├── exception
├── service
└── validation
```

### Package Responsibilities

- `client` — integration with the external exchange-rate provider
- `config` — cache and rate-limiting configuration
- `controller` — REST endpoints
- `dto` — API and provider data models
- `exception` — custom exceptions and global error handling
- `service` — exchange-rate and conversion business logic
- `validation` — request validation

## Technical Decisions

### USD-Based Cross Rates

The provider returns rates with USD as the base currency. The service calculates rates between other currencies by dividing the target USD rate by the source USD rate.

### BigDecimal

`BigDecimal` is used instead of `double` or `float` to provide predictable precision for monetary calculations.

### Manual Request Validation

Input validation is implemented through a small `RequestValidator` class.

This keeps validation explicit and easy to understand within the scope of the project.

### Provider Error Handling

Invalid provider responses and `RestClient` failures are converted into a custom `ExchangeRateProviderException`.

These errors are returned to clients as `502 Bad Gateway`.

### No Authentication

Authentication was not implemented because the API does not include users, roles or differentiated permissions.

A production version could introduce API-key authentication, OAuth2 or JWT depending on the required access model.

## Limitations

- Exchange-rate data may be up to one minute old because of caching
- Rate limiting is stored in memory
- Rate limiting uses the request remote address
- Rate-limit state is not shared between multiple application instances
- The application depends on the availability of the external provider
- Authentication is not implemented

## Possible Future Improvements

- Distributed caching with Redis
- Distributed rate limiting
- Authentication and authorization
- GraphQL support
- Additional controller and provider integration tests
- Proxy-aware client IP resolution