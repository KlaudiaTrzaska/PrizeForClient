# PrizeForClient

A Java 17 / Spring Boot 3.2.4 service for managing prizes and their inventory,
with a reusable Java HTTP client. Data is stored in PostgreSQL; Flyway manages
the database schema and indexes.

## Run locally

Requirements: JDK 17, Maven, and a running PostgreSQL instance. The Maven wrapper
scripts are present, but their `.mvn` configuration is missing, so use an installed
`mvn` command.

Create a database named `prizes`. By default, the application connects to
`jdbc:postgresql://localhost:5433/prizes` and listens on port `8081`.
Override the datasource settings with environment variables:

```sh
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/prizes
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your-local-password
mvn spring-boot:run
```

Flyway creates the `prizes` and `inventory` tables and indexes on prize name and
threshold at startup. Hibernate validates the schema. Defaults are defined in
`src/main/resources/application.properties`; use environment variables for local
credentials.

Interactive API documentation: <http://localhost:8081/swagger-ui.html>.
OpenAPI JSON: <http://localhost:8081/v3/api-docs>.

## API

| Method | Path | Behavior |
| --- | --- | --- |
| POST | `/addPrize` | Create a prize from `prizeName` and `threshold`; returns a confirmation string. |
| GET | `/prizes?page=0&size=100` | Return a Spring Data page of prizes with threshold **less than or equal to 4**. |
| GET | `/prizes/{name}/amount` | Return the inventory amount, or `404` if no matching inventory exists. |
| POST | `/prizes/{name}/take` | Decrease available inventory by one and return the remaining amount; `404` if missing, `409` if out of stock. |
| DELETE | `/prizes/{name}` | Delete a prize only when its inventory amount is zero; `400` for nonzero inventory, `404` for missing inventory or prize. |

Names are normalized to lowercase when creating, looking up, taking, or deleting
prizes. Pagination is zero-based; a negative page or size below one returns `400`.
The listing threshold is currently fixed in `PrizeController` and is not a query
parameter. Listing does not filter by stock availability.

```sh
curl -X POST http://localhost:8081/addPrize \
  -H 'Content-Type: application/json' \
  -d '{"prizeName":"Book","threshold":3}'

curl 'http://localhost:8081/prizes?page=0&size=20'
```

Creating a prize does **not** create an inventory row. There is currently no API
for stocking inventory. To try inventory operations, insert a row into PostgreSQL
using the prize's ID, for example:

```sql
INSERT INTO inventory (id, prize_amount)
SELECT id, 2 FROM prizes WHERE prize_name = 'book';
```

Then query or take the prize:

```sh
curl http://localhost:8081/prizes/book/amount
curl -X POST http://localhost:8081/prizes/book/take
```

After the amount reaches zero, `DELETE /prizes/book` removes the prize and its
inventory row through the database's cascading foreign key.

## Build and test

```sh
mvn test
mvn package
java -jar target/Prizes-1.3-SNAPSHOT.jar
```

The existing tests use JUnit and Mockito to cover prize services, inventory
operations, and client pagination without requiring PostgreSQL.

Packaging also produces `target/Prizes-1.3-SNAPSHOT-client.jar`, containing the
`com.example.prizes.client` package. The client uses Spring's `RestClient`; its
consuming application must provide the Spring Web and Jackson dependencies.

```java
PrizeClient client = new PrizeClientImpl("http://localhost:8081");
PrizePage page = client.getPrizes(0, 20);
List<Prize> prizes = client.getPrizes(); // Fetches all pages, 1,000 items at a time.
int remaining = client.takePrize("book");
```

These types are in `com.example.prizes.client`; `List` is from `java.util`.
When discovered through Spring component scanning, `PrizeClientImpl` reads
`prizes.service.url`, which defaults to `http://localhost:8081`.

## Project layout

- `controllers`: HTTP endpoints and response status handling.
- `services`: prize and inventory business rules.
- `data` and `model`: Spring Data repositories and JPA entities.
- `client`: HTTP client interface, implementation, and response records.
- `src/main/resources/db/migration`: Flyway schema migrations.
- `src/test/java`: service and client unit tests.
