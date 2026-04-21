# onlineShopping

A Spring Boot ordering platform with separate admin and customer-facing experiences.

This repository combines a Java backend, server-rendered/static web assets, and supporting integrations for login, address book management, shopping cart flows, order submission, category management, and dish / setmeal operations.

## Highlights

- Spring Boot backend with layered `controller -> service -> mapper` structure
- Customer flows for login, address book, cart, and order submission
- Admin flows for category, dish, setmeal, and employee management
- MyBatis-Plus integration for persistence and query handling
- Redis-backed caching support and ShardingSphere dependency wiring
- Utility integrations for SMS, email, and API documentation

## Tech Stack

- Java 17
- Spring Boot 2.4
- MyBatis-Plus
- MySQL
- Redis
- Druid
- ShardingSphere JDBC
- Lombok
- Knife4j
- JUnit 5 / Spring Boot Test

## Repository Layout

```text
src/main/java/com/taiwei/reggie/
├── controller/      # HTTP endpoints
├── service/         # Business services
├── mapper/          # MyBatis-Plus mappers
├── entity/          # Persistence models
├── dto/             # DTOs used by API / UI flows
├── common/          # Shared response, exception, and context helpers
└── config/          # Spring configuration

src/main/resources/
├── backend/         # Admin-facing web assets
└── front/           # Customer-facing web assets
```

## Getting Started

### Prerequisites

- JDK 17
- Maven
- MySQL
- Redis

### Run locally

```bash
mvn spring-boot:run
```

If you want to run the full application end-to-end, make sure the datasource, Redis, and mail / SMS related configuration are set correctly in your local Spring Boot configuration before startup.

## Testing

Run the existing test suite with:

```bash
mvn test
```

## Notes

- This repository is best read as a full-stack coursework / practice project rather than a production-ready deployment template.
- The backend and frontend assets live in the same repository, so the codebase is useful for studying end-to-end CRUD and ordering flows in one place.
