# Exchange Rate API

REST and GraphQL API built with Java 21 and Spring Boot for retrieving exchange rates and converting monetary amounts between currencies.

The application integrates with `exchangerate.host` and includes caching, Jakarta validation, JWT authentication, distributed rate limiting with Redis, Swagger documentation, GraphQL, tests and Docker support.

## Features

- Get an exchange rate between two currencies
- Get all exchange rates from a base currency
- Convert an amount to one currency
- Convert an amount to multiple currencies
- Access the same operations through REST and GraphQL
- Cache provider responses for one minute
- Validate requests and return structured errors
- Authenticate requests with JWT
- Apply distributed rate limiting with Redis
- Swagger/OpenAPI and GraphiQL interfaces
- Unit tests
- Dockerized setup

## Technologies

- Java 21
- Spring Boot
- Spring Security
- OAuth2 Resource Server / JWT
- Keycloak
- Spring GraphQL
- Maven
- Caffeine
- Redis
- Spring Data Redis
- Springdoc OpenAPI
- JUnit 5 and Mockito
- Docker and Docker Compose

## Configuration

Create a `.env` file based on `.env.example`:

```env
EXCHANGE_RATE_API_ACCESS_KEY=replace-with-your-access-key

JWT_ISSUER_URI=http://localhost:9090/realms/exchange-rate
JWT_JWK_SET_URI=http://localhost:9090/realms/exchange-rate/protocol/openid-connect/certs

REDIS_HOST=localhost
REDIS_PORT=6379
```

The provider base URL is configured in `application.properties`.

The provider HTTP client uses configurable timeouts:

- `exchange-rate.api.connect-timeout=2s`
- `exchange-rate.api.read-timeout=5s`

The `.env` file is ignored by Git and must not be committed.

## Run Locally

Run Keycloak and Redis, then configure the required environment variables in your terminal or IntelliJ run configuration.

Linux or macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs at:

```text
http://localhost:8080
```

## Run With Docker

Build and start the API, Keycloak and Redis:

```bash
docker compose up --build
```

Stop the application:

```bash
docker compose down
```

## Authentication

Protected endpoints use JWT authentication through Spring Security OAuth2 Resource Server.

Keycloak runs at:

```text
http://localhost:9090
```

A token can be obtained using the `exchange-rate-api` client and then sent using:

```http
Authorization: Bearer <token>
```

The health endpoint and Swagger documentation are public.

## Swagger

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Use the **Authorize** button and provide a valid JWT.

## REST Endpoints

### Health Check

```http
GET /api/health
```

### Get Exchange Rate

```http
GET /api/rates/{from}/{to}
```

Example:

```http
GET /api/rates/EUR/GBP
```

### Get All Rates From a Base Currency

```http
GET /api/rates/{from}
```

Example:

```http
GET /api/rates/EUR
```

### Convert an Amount

```http
GET /api/conversions?from={from}&to={to}&amount={amount}
```

Example:

```http
GET /api/conversions?from=EUR&to=GBP&amount=100
```

### Convert an Amount to Multiple Currencies

```http
GET /api/conversions/multiple?from={from}&to={currencies}&amount={amount}
```

Example:

```http
GET /api/conversions/multiple?from=EUR&to=GBP,USD,JPY&amount=100
```

## GraphQL

GraphQL endpoint:

```text
http://localhost:8080/graphql
```

GraphiQL interface:

```text
http://localhost:8080/graphiql
```

GraphQL uses the same JWT authentication as the REST endpoints.

Example query:

```graphql
query {
  exchangeRate(from: "EUR", to: "GBP") {
    from
    to
    rate
  }
}
```

Available queries: `exchangeRate`, `allRates`, `convert` and `convertMultiple`.

## Technical Decisions

The provider returns rates using USD as the base currency. Cross rates are calculated with:

```text
source -> target = (USD -> target) / (USD -> source)
```

`BigDecimal` is used for monetary calculations, with a scale of 10 decimal places and `RoundingMode.HALF_UP`.

The latest provider response is cached for one minute using Caffeine to reduce external API calls.

Rate limiting allows 60 requests per minute per authenticated client and stores the counters in Redis, allowing the limit to be shared between application instances.

Authentication is handled using JWT tokens issued by Keycloak and validated by Spring Security OAuth2 Resource Server.

Provider failures are converted into `502 Bad Gateway` responses.

REST and GraphQL reuse the same service layer, avoiding duplicated business logic.

## Error Codes

- `400 Bad Request` - invalid input, missing required parameters, invalid parameter type, or unsupported currency
- `401 Unauthorized` - missing or invalid JWT
- `429 Too Many Requests` - rate limit exceeded
- `502 Bad Gateway` - external provider failure

## Tests

Linux or macOS:

```bash
./mvnw clean test
```

Windows:

```powershell
.\mvnw.cmd clean test
```

## Limitations

- Exchange-rate data may be up to one minute old
- Keycloak and Redis must be available for authenticated requests and distributed rate limiting