# Currency Exchange Service

Spring Boot service for reading exchange rates and converting amounts between currencies.

The service uses provider fallback by priority:

1. `EXCHANGERATE_HOST`
2. `FRANKFURTER`

Exchange-rate snapshots are cached by base currency for 1 minute. For example, these calls share the same cached `EUR` snapshot:

- `GET /api/v1/exchange-rates/EUR`
- `GET /api/v1/exchange-rates/EUR/USD`
- `GET /api/v1/conversions?from=EUR&to=USD&amount=100`
- `POST /api/v1/conversions/batch`

## Requirements

- Java 21
- Docker and Docker Compose, for Docker-based runs
- No local Gradle installation is required. Use the included Gradle wrapper.

## Configuration

The application reads configuration from `src/main/resources/application.yaml`.

Important environment variable:

```text
EXCHANGE_RATE_HOST_ACCESS_KEY
```

This key is used by `exchangerate.host`. If it is missing or invalid, the service should fall back to Frankfurter when Frankfurter supports the requested rates.

Default port:

```text
8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Run With Docker Compose

From the project root:

```powershell
docker compose up --build
```

The service will be available at:

```text
http://localhost:8080
```

### Docker Environment Variable

`docker-compose.yml` currently contains:

```yaml
environment:
  EXCHANGE_RATE_HOST_ACCESS_KEY: CHANGE_ME
```

Replace `CHANGE_ME` with a real exchangerate.host access key if you want the high-priority provider to succeed.

## Run Locally With Gradle

Make sure Java 21 is available.

```bash
export EXCHANGE_RATE_HOST_ACCESS_KEY="your_key_here"
./gradlew bootRun
```

The service will be available at:

```text
http://localhost:8080
```

If you do not have an exchangerate.host key, you can still run the service. The high-priority provider may fail, and the router should fall back to Frankfurter.


## Main Endpoints

```text
GET  /api/v1/exchange-rates/{base}
GET  /api/v1/exchange-rates/{base}/{target}
GET  /api/v1/conversions?from={from}&to={to}&amount={amount}
POST /api/v1/conversions/batch
```
## Potential evolution point

Current solutions operates with in-memory cache (Caffeine), and it would be good enough to work as single pod app in cluster environment.
### BUT
There is a potential evolution possibility with moving/adding to additional data storage (Redis for example). Such option can be overengineering
for most cases and possibly can have a collision when pod1 and pod2 at the same time trying to access same base currency data.

Other way- is to introduce some kind of Carlson, JobRunr or (God, I'm sorry :)) Quartz Scheduler to handle scheduled cache updates every minute BY ONE POD only.
(or just add a spring job with leader flag and have fun like it was 5-6 years ago :))