# Device Management API

A Spring Boot service that stores devices and exposes them over a REST API.
You can create a device, fetch one, list them with filters and paging, update
them, and delete them. Once a device goes into use, a few rules kick in to stop
you from changing things out from under whoever is using it.

## Device rules

The service enforces these rules:

* `createdAt` can not be changed through the API.
* While a device is `in-use`, its name and brand can not change.
* An `in-use` device can not be deleted. It has to leave that state first.

A device is `available`, `in-use`, or `inactive`.

The OpenAPI specification is the source of truth for the API contract, and
controller interfaces are generated from it.

## Tech stack

| Concern            | Choice                                                     |
|--------------------|------------------------------------------------------------|
| Language / runtime | Java 25                                                    |
| Framework          | Spring Boot 4.1.1                                          |
| Build              | Maven                                                      |
| Database           | PostgreSQL                                                 |
| Migrations         | Liquibase                                                  |
| API contract       | OpenAPI 3, generated with `openapi-generator-maven-plugin` |
| API docs           | Swagger UI                                                 |
| Testing            | JUnit 5, Mockito, AssertJ, Testcontainers                  |
| Packaging          | Docker, Docker Compose                                     |

## Configuration

Configuration is provided through environment variables. The defaults below
are intended for local development and should be overridden in other
environments.

| Variable      | Default                | What it's for                   |
|---------------|------------------------|---------------------------------|
| `SERVER_PORT` | `8081`                 | Port the application listens on |
| `DB_HOST`     | `localhost`            | PostgreSQL host                 |
| `DB_PORT`     | `5433`                 | PostgreSQL port                 |
| `DB_NAME`     | `device_management_be` | Database name                   |
| `DB_USERNAME` | `postgres`             | Database user                   |
| `DB_PASSWORD` | `postgres`             | Database password               |

`DB_PORT` is `5433` for local and `5432` inside Docker.

### Profiles

| Profile  | When it applies                             |
|----------|---------------------------------------------|
| `local`  | Development against PostgreSQL on localhost |
| `docker` | Application running inside Docker Compose   |
| `test`   | Automated tests using Testcontainers        |

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
mvn spring-boot:run -Dspring-boot.run.profiles=local
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

The repository also contains a pre-generated static API reference at
[Device management API documentation](src/main/resources/docs/device-management-api.html) using [Redocly](https://redocly.com/). It can be opened directly in a browser without running the
application.

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

## Assumptions

The requirements left a few things open. I made assumptions as defined below.

Device fields

* Name and brand are always required. Any characters are allowed for now, 1 to
  255 characters.
* Two devices can have the same name and brand. Nothing in the requirements
  suggested they need to be unique.
* State defaults to available when the create request does not provide state.
* The server owns id and createdAt. Client can not set them.
* `updatedAt` is not in the required domain, I added it for tracing last update.
* `createdAt` and `updatedAt` both timestamps are always UTC, so they end with `Z` in response.

Fetching

* Filtering by brand and/or state are added in single GET API.
* Brand matching is exact and case sensitive.
* Get all is paginated with page metadata. The requirements didn't ask for paging, but an endpoint that returns the
  whole table is not something I would ship.
* Page size is between 1 and 100, default 20.
* Default sort is createdAt desc, so the newest device comes first. Sorting is allowed on createdAt, name, brand and
  state. Id is added as a tiebreaker to have consistent order.
* If filter found nothing, it will be empty list response.

Changing devices

* Delete removes the row. There is no soft delete or archive (audit or history).
* Patch covers full update as well, because every mutable field is required. So, if want full update provide all mutable
  fields.
* Only the fields which are in patch request body are considered for change. A field left out is not touched, so
  there is no way to clear one and as all mutable field are required so it does not matter.
* Sending not changed values in patch while device is in-use, are not rejected. Request will be rejected when values are
  different.
* If device is in-use and patch changes state along with other fields, it will not be allowed. State should be changed
  first, then updates of other data are allowed.

## Out of scope / future improvements

* Authentication and authorization. The API is open right now. In a real
  deployment it would sit behind OAuth2 or JWT with an identity provider in
  front of it.
* Centralized logging and tracing. Logs and the access log both go to stdout,
  which is the right thing inside a container, but there is nothing collecting
  them and no trace id to follow a request across services.
* State transition rules. Any state can move to any other state today. The
  requirements do not describe a device lifecycle, so I did not invent one.
* Audit or history. There is no record of what changed. This is also what a soft
  delete would need.
* Auditing the user. When authorization is implemented, record who created and who
  last updated a device. These details can be got from the security context.
* Rate limiting. Nothing stops a client from hammering the list endpoint.
* Brand as its own thing. It is a free text column, so two spellings of the same
  brand are two brands. A lookup table would fix that, and would make the brand
  filter behave the way people expect.
