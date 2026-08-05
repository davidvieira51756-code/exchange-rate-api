# Exchange Rate API

REST API built with Java 21 and Spring Boot for retrieving exchange rates and converting monetary amounts between currencies.

The application integrates with `exchangerate.host` and includes caching, validation, rate limiting, HTTP Basic authentication, Swagger documentation, tests and Docker support.

## Features

- Get an exchange rate between two currencies
- Get all exchange rates from a base currency
- Convert an amount to one currency
- Convert an amount to multiple currencies
- Cache provider responses for one minute
- Validate requests and return structured errors
- Limit requests per client IP
- Protect endpoints with HTTP Basic
- Swagger/OpenAPI documentation
- Unit tests
- Dockerized setup

## Technologies

- Java 21
- Spring Boot
- Spring Security
- Maven
- Caffeine
- Bucket4j
- Springdoc OpenAPI
- JUnit 5 and Mockito
- Docker and Docker Compose

## Configuration

Create a `.env` file based on `.env.example`:

```env
EXCHANGE_RATE_API_ACCESS_KEY=replace-with-your-access-key
APP_SECURITY_USERNAME=admin
APP_SECURITY_PASSWORD=replace-with-your-password
```

The provider base URL is configured in `application.properties`.

The `.env` file is ignored by Git and must not be committed.

## Run Locally

Configure the required environment variables in your terminal or IntelliJ run configuration.

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

Build and start the application:

```bash
docker compose up --build
```

Stop the application:

```bash
docker compose down
```

## Swagger

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Use the **Authorize** button in Swagger with the configured username and password.

The health endpoint and Swagger documentation are public. Exchange-rate and conversion endpoints require HTTP Basic authentication.

Example authenticated request:

```bash
curl -u admin:replace-with-your-password http://localhost:8080/api/rates/EUR/GBP
```

## Endpoints

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

## Technical Decisions

The provider returns rates using USD as the base currency. Cross rates are calculated with:

```text
source -> target = (USD -> target) / (USD -> source)
```

`BigDecimal` is used for monetary calculations, with a scale of 10 decimal places and `RoundingMode.HALF_UP`.

The latest provider response is cached for one minute using Caffeine to reduce external API calls.

Rate limiting allows 60 requests per minute per client IP.

Provider failures are converted into `502 Bad Gateway` responses.

HTTP Basic authentication uses one in-memory user configured through environment variables. The application is stateless, so credentials are checked on every protected request.

## Error Codes

- `400 Bad Request` - invalid input or unsupported currency
- `401 Unauthorized` - missing or invalid credentials
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
- Rate limiting is stored in memory and is local to each application instance
- Authentication uses a single in-memory user
