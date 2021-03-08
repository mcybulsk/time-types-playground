# Timestamp MySQL + Hibernate playground

## Preface

This is a simple playground designed to test out some quirks of MySQL and Hibernate time type handling when the app or
DB is run in different timezones or when data types change.

## Base assumptions

### App/Library and DB versions and running

- Spring version 5
- Hibernate version 5
- Latest MySQL version

The tests are running the Spring 5 App with Hibernate 5. App timezone is changed like this (this sets the default JVM
timezone to a given value):

```java
TimeZone.setDefault(...);
```

The MySQL instance is run in a docker container. Image: `mysql`. Database timezone is changed like this:

```java
MySQLContainer mySQLContainer=
    new MySQLContainer<>(DockerImageName.parse("mysql"))
    .withEnv("TZ",timezone)
    ...
```

Source: https://dev.mysql.com/doc/refman/8.0/en/time-zone-support.html#time-zone-variables

### Hibernate mapping and Liquibase migration

Super simple entity:

```java

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
class TimeEntity {

    @Id
    private Long id;

    private Instant instantInTimestamp;
    private Instant instantInDatetime;

    private LocalDateTime localDateTimeInTimestamp;
    private LocalDateTime localDateTimeInDatetime;

    private Date dateInTimestamp;
    private Date dateInDatetime;
}
```

and SQL:

```sql
CREATE TABLE time_entity
(
    id                           BIGINT PRIMARY KEY,

    instant_in_timestamp         TIMESTAMP(3),
    instant_in_datetime          DATETIME(3),

    local_date_time_in_timestamp TIMESTAMP(3),
    local_date_time_in_datetime  DATETIME(3),

    date_in_timestamp            TIMESTAMP(3),
    date_in_datetime             DATETIME(3)
);
```

## Tests

### When writing and reading same entity in different timezones (TZs)

#### Description

`MySqlAndHibernateDifferentTimezoneHandlingIT` is a parametrized test that:

1. Starts up the Spring app, and a MySQL container configured with given timezone settings.
2. It then persists an entity, and shuts down both the app and the db.
3. It starts up Spring app and the db with possibly different timezone settings.
4. App fetches the entity from db.
5. Both entities are then compared field by field, looking for any inconsistencies introduced with changed timezone
   settings.

#### Results

Legend:

- 🟢️ - Written and read values are the same
- 🔴 - Written and read values are different (value mapped by Hibernate changed without our control)

| DB write timezone | App write timezone | DB read timezone | App read timezone | `Instant` in `TIMESTAMP(3)` | `Instant` in `DATETIME(3)` | `LocalDateTime` in `TIMESTAMP(3)` | `LocalDateTime` in `DATETIME(3)` | `Date` in `TIMESTAMP(3)` | `Date` in `DATETIME(3)` |
|-------------------|--------------------|------------------|-------------------|-----------------------------|----------------------------|-----------------------------------|----------------------------------|--------------------------|-------------------------|
| `UTC`             | `UTC`              | `UTC`            | `UTC`             | 🟢️                           | 🟢️                          | 🟢️                                 | 🟢️                                | 🟢️                        | 🟢️                       |
| `UTC`             | `UTC`              | `UTC`            | `Europe/Warsaw`   | 🟢️                           | 🟢️                          | 🔴                                 | 🔴                                | 🟢️                        | 🟢️                       |
| `UTC`             | `Europe/Warsaw`    | `UTC`            | `Europe/Warsaw`   | 🟢️                           | 🟢️                          | 🟢️                                 | 🟢️                                | 🟢️                        | 🟢️                       |
| `UTC`             | `Europe/Warsaw`    | `UTC`            | `UTC`             | 🟢️                           | 🟢️                          | 🔴                                 | 🔴                                | 🟢️                        | 🟢️                       |
| `UTC`             | `UTC`              | `Europe/Warsaw`  | `UTC`             | 🟢️                           | 🔴                          | 🟢️                                 | 🔴                                | 🟢️                        | 🔴                       |
| `Europe/Warsaw`   | `UTC`              | `Europe/Warsaw`  | `UTC`             | 🟢️                           | 🟢️                          | 🟢️                                 | 🟢️                                | 🟢️                        | 🟢️                       |
| `Europe/Warsaw`   | `UTC`              | `UTC`            | `UTC`             | 🟢️                           | 🔴                          | 🟢️                                 | 🔴                                | 🟢️                        | 🔴                       |
| `UTC`             | `UTC`              | `Europe/Warsaw`  | `Europe/Warsaw`   | 🟢️                           | 🔴                          | 🔴️                                 | 🟢                                | 🟢️                        | 🔴                       |

#### Takeaways

- Using Java "point-in-time" types `Instant` or `Date` (and probably `ZonedDateTime` as well, but this was not tested)
  with Hibernate combined with `TIMESTAMP(3)` should be safe no matter the different timezone combinations.
- Using different timezones on the DB and the Hibernate app (e.g. running DB in "UTC", while app in "Europe/Warsaw")
  does not seem to break anything as long as the timezones are never changed later on.
- As long as DB's timezone is never changed, it's fine to store `Instant` and `Date` in `DATETIME(3)` columns.
- Changing the DB timezone breaks all the Hibernate mappings (for all the tested Java types) pointing to `DATETIME(3)`
  columns.
- If the DB timezone is never changed, only the `LocalDateTime` type breaks when Hibernate app runs with changed
  timezone - no matter which SQL type it is mapped to (`TIMESTAMP(3)` or `DATETIME(3)`).
- If app and DB timezones are never changed, all the tested combinations work well.

#### Recommendations

If we want to be super safe from any corruption that might happen when DB or app changes the timezone (for example,
running even one instance of the app on default timezone of the remote OS instead of UTC), we should always use a "
point-in-time" Java types like `Instant` or `Date` combined with `TIMESTAMP(3)` MySQL type.

### When changing Java type without changing the DB type (without any TZ change)

`LocalDateTimeToInstantMigrationIT` is a test that is designed to check whether it is safe to change Java type
from `LocalDateTime` to `Instant` without any change in SQL column types:

1. Starts up the Spring app, and a MySQL container configured with `UTC` timezone.
2. It then persists an entity, and shuts down both the app and the db.
3. It starts up Spring app and the db with `UTC` timezone.
4. App fetches another entity, mapped to the same table, but having `Instant` types instead of `LocalDateTime`.
5. Both entities are then compared field by field, looking for any inconsistencies introduced with changed SQL types.
   During the comparison, `LocalDateTime` is converted to `Instant` in following way:

```java
    private Instant convertToInstant(LocalDateTime localDateTime, String originalTimezone) {
        return localDateTime.atZone(ZoneId.of(originalTimezone)).toInstant();
    }
```

`originalTimezone` is the original timezone that the original entity was written to the DB with.

#### Results

After the migration, all the fields were still properly mapped.

#### Takeaways

- Assuming that a `LocalDateTime` field was meant to represent a point in time (e.g. generated
  with `LocalDateTime.now()`, capturing the time as seen from the current JVM default timezone), migrations of such
  fields to `Instant` are safe as long as the application timezone does not change at the same time. No database type
  changes are needed.

### When changing the SQL type without changing the Java type (without any TZ change)

#### Description

`DatetimeToTimestampMigrationIT` is a test that is designed to check whether it is safe to change column SQL type
from `DATETIME(3)` to `TIMESTAMP(3)` without any change in Java code:

1. Starts up the Spring app, and a MySQL container configured with `UTC` timezone.
2. It then persists an entity, and shuts down both the app and the db.
3. It starts up Spring app and the db with `UTC` timezone.
4. While starting, the app also migrates all the columns from `DATETIME(3)` to `TIMESTAMP(3)` (Liquibase locations are
   set to `classpath:db/migration,classpath:db/sql-type-migrations`).
5. App fetches the entity from db.
6. Both entities are then compared field by field, looking for any inconsistencies introduced with changed SQL types.

#### Results

After the migration, all the fields were still properly mapped.

#### Takeaways

- Migrations of `DATETIME(3)` columns in the form
  of `ALTER TABLE <table_name> MODIFY COLUMN <column_name> TIMESTAMP(3);`
  are safe in regard to timezone data corruption.
