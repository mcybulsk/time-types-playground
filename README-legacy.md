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

##### New versions results

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

##### Legacy versions results

| DB write timezone | App write timezone | DB read timezone | App read timezone | `Instant` in `TIMESTAMP(3)` | `Instant` in `DATETIME(3)` | `LocalDateTime` in `TIMESTAMP(3)` | `LocalDateTime` in `DATETIME(3)` | `Date` in `TIMESTAMP(3)` | `Date` in `DATETIME(3)` |
|-------------------|--------------------|------------------|-------------------|-----------------------------|----------------------------|-----------------------------------|----------------------------------|--------------------------|-------------------------|
| `UTC`             | `UTC`              | `UTC`            | `UTC`             | 🟢️                           | 🟢️                          | 🟢️                                 | 🟢️                                | 🟢️                        | 🟢️                       |
| `UTC`             | `UTC`              | `UTC`            | `Europe/Warsaw`   | 🔴️                           | 🔴️                          | 🟢                                 | 🟢                                | 🔴️                        | 🔴️                       |
| `UTC`             | `Europe/Warsaw`    | `UTC`            | `Europe/Warsaw`   | 🟢️                           | 🟢️                          | 🟢️                                 | 🟢️                                | 🟢️                        | 🟢️                       |
| `UTC`             | `Europe/Warsaw`    | `UTC`            | `UTC`             | 🔴️                           | 🔴️                          | 🟢                                 | 🟢                                | 🔴️                        | 🔴️                       |
| `UTC`             | `UTC`              | `Europe/Warsaw`  | `UTC`             | 🔴️                           | 🟢                          | 🔴️                                 | 🟢                                | 🔴️                        | 🟢                       |
| `Europe/Warsaw`   | `UTC`              | `Europe/Warsaw`  | `UTC`             | 🟢️                           | 🟢️                          | 🟢️                                 | 🟢️                                | 🟢️                        | 🟢️                       |
| `Europe/Warsaw`   | `UTC`              | `UTC`            | `UTC`             | 🔴️                           | 🟢                          | 🔴️                                 | 🟢                                | 🔴️                        | 🟢                       |
| `UTC`             | `UTC`              | `Europe/Warsaw`  | `Europe/Warsaw`   | 🟢️                           | 🔴                          | 🔴️                                 | 🟢                                | 🟢️                        | 🔴                       |

##### Merged results (latest versions / legacy versions)

| DB write timezone | App write timezone | DB read timezone | App read timezone | `Instant` in `TIMESTAMP(3)` | `Instant` in `DATETIME(3)` | `LocalDateTime` in `TIMESTAMP(3)` | `LocalDateTime` in `DATETIME(3)` | `Date` in `TIMESTAMP(3)` | `Date` in `DATETIME(3)` |
|-------------------|--------------------|------------------|-------------------|-----------------------------|----------------------------|-----------------------------------|----------------------------------|--------------------------|-------------------------|
| `UTC`             | `UTC`              | `UTC`            | `UTC`             | 🟢️/ 🟢                           | 🟢️/ 🟢                          | 🟢️/ 🟢                                 | 🟢️/ 🟢                                | 🟢️/ 🟢                        | 🟢️/ 🟢                       |
| `UTC`             | `UTC`              | `UTC`            | `Europe/Warsaw`   | 🟢️/ 🔴                           | 🟢️/ 🔴                          | 🔴/ 🟢                                 | 🔴/ 🟢                                | 🟢️/ 🔴                        | 🟢️/ 🔴                       |
| `UTC`             | `Europe/Warsaw`    | `UTC`            | `Europe/Warsaw`   | 🟢️/ 🟢                           | 🟢️/ 🟢                          | 🟢️/ 🟢                                 | 🟢️/ 🟢                                | 🟢️/ 🟢                        | 🟢️/ 🟢                       |
| `UTC`             | `Europe/Warsaw`    | `UTC`            | `UTC`             | 🟢️/ 🔴                           | 🟢️/ 🔴                          | 🔴/ 🟢                                 | 🔴/ 🟢                                | 🟢️/ 🔴                        | 🟢️/ 🔴                       |
| `UTC`             | `UTC`              | `Europe/Warsaw`  | `UTC`             | 🟢️/ 🔴                           | 🔴/ 🟢                          | 🟢️/ 🔴                                 | 🔴/ 🟢                                | 🟢️/ 🔴                        | 🔴/ 🟢                       |
| `Europe/Warsaw`   | `UTC`              | `Europe/Warsaw`  | `UTC`             | 🟢️/ 🟢                           | 🟢️/ 🟢                          | 🟢️/ 🟢                                 | 🟢️/ 🟢                                | 🟢️/ 🟢                        | 🟢️/ 🟢                       |
| `Europe/Warsaw`   | `UTC`              | `UTC`            | `UTC`             | 🟢️/ 🔴                           | 🔴/ 🟢                          | 🟢️/ 🔴                                 | 🔴/ 🟢                                | 🟢️/ 🔴                        | 🔴/ 🟢                       |
| `UTC`             | `UTC`              | `Europe/Warsaw`  | `Europe/Warsaw`   | 🟢️/ 🟢                           | 🔴/ 🔴                          | 🔴️/ 🔴                                 | 🟢/ 🟢                                | 🟢️/ 🟢                        | 🔴/ 🔴                       |

#### Takeaways

- Different versions of App/Hibernate and MySQL provide totally different results. Seems like there is no universal
  silver bullet when it comes to Hibernate-MySQL time mapping that would shield us from the timezone mismatch between
  app/db runs.
