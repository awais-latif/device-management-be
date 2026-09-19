# Device Management API

A Spring Boot service that stores devices and exposes them over a REST API.
You can create a device, fetch one, list them with filters and paging, update
them, and delete them. Once a device goes into use, a few rules kick in to stop
you from changing things out from under whoever is using it.

## Device rules

The service enforces these rules:

* The server sets `creationTime` when it creates the device. It can't be set or
  changed through the API.
* While a device is `IN_USE`, its name and brand are frozen.
* An `IN_USE` device can't be deleted. It has to leave that state first.
* List endpoints are paginated to avoid unbounded responses.
* The brand and state filters can be combined.

A device is `AVAILABLE`, `IN_USE`, or `INACTIVE`.

The OpenAPI specification is the source of truth for the API contract, and
controller interfaces are generated from it.

## Tech stack

| Concern            | Choice                                                     |
| ------------------ | ---------------------------------------------------------- |
| Language / runtime | Java 25                                                    |
| Framework          | Spring Boot 4.1.1                                          |
| Build              | Maven                                                      |
| Database           | PostgreSQL                                                 |
| Migrations         | Liquibase                                                  |
| API contract       | OpenAPI 3, generated with `openapi-generator-maven-plugin` |
| API docs           | Swagger UI                    |
| Testing            | JUnit 5, Mockito, Testcontainers                           |
| Packaging          | Docker, Docker Compose                                     |

## Configuration

Configuration is provided through environment variables. The defaults below
are intended for local development and should be overridden in other
environments.

| Variable      | Default                | What it's for                   |
| ------------- | ---------------------- | ------------------------------- |
| `SERVER_PORT` | `8081`                 | Port the application listens on |
| `DB_HOST`     | `localhost`            | PostgreSQL host                 |
| `DB_PORT`     | `5432`                 | PostgreSQL port                 |
| `DB_NAME`     | `device_management_be` | Database name                   |
| `DB_USERNAME` | `postgres`             | Database user                   |
| `DB_PASSWORD` | `postgres`             | Database password               |

### Profiles

| Profile  | When it applies                                              |
| -------- | ------------------------------------------------------------ |
| `local`  | Default profile. Development against PostgreSQL on localhost |
| `docker` | Application running inside Docker Compose                    |
| `test`   | Automated tests using Testcontainers                         |

The profile can be selected with:

```bash
SPRING_PROFILES_ACTIVE=docker
```

or when running through Maven:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

## Running it locally

You'll need Java 25, Maven 3.9 or newer, and Docker with Compose.

Start the database:

```bash
docker compose up -d postgres
```

Then build and run:

```bash
mvn clean verify
mvn spring-boot:run
```

Liquibase runs the database migrations on startup, so there is no separate
schema setup step.

The application listens on port `8081`.

To run the application and PostgreSQL in containers:

```bash
docker compose up --build
```

This uses the docker profile and connects to the postgres service from inside the Docker network.

## Tests

Run the test suite with:

```bash
mvn test
```

The integration tests use Testcontainers with a real PostgreSQL instance.

Docker needs to be running when executing the integration tests.


## API documentation

With the application running:

* Swagger UI: `http://localhost:8081/swagger-ui/index.html`
* OpenAPI JSON: `http://localhost:8081/v3/api-docs`


## Why it's built this way

**The contract comes first.** The OpenAPI specification is written by hand and
controller interfaces are generated from it. This keeps the API contract
separate from the implementation and reduces the chance of the documentation
and controller definitions drifting apart. Generated sources are written to
`target/` and are not committed.

**Liquibase owns the schema.** Hibernate runs with
`spring.jpa.hibernate.ddl-auto=validate`, so it checks the entity mappings
against the actual database schema and refuses to start if they don't match.
Hibernate never creates or alters the schema; database changes go through
Liquibase.

**Open Session in View is off.** With
`spring.jpa.open-in-view=false`, database access stays within the service
layer rather than relying on lazy loading during HTTP response processing.

**The connection pool is deliberately small.** HikariCP is configured with a
small maximum pool size, which is appropriate for a service of this size.
These values can be changed through configuration if the deployment requires
it.

## Out of scope / future improvements

* **Authentication and authorization.** The API is currently open. A
  production deployment would typically put OAuth2/JWT authentication and
  authorization in front of the service, backed by an external identity
  provider.
* **Distributed tracing and centralized logging.** Application logs currently
  go to stdout. A deployed environment would typically add centralized log
  collection and distributed tracing.
* **State transition rules.** Any supported state can currently transition to
  any other state. The requirements don't define a device lifecycle, so no
  additional transition rules were introduced.
