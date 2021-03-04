package me.cybulski.timestampplayground.time;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

class DatetimeToTimestampMigrationIT extends BaseTimeHandlingTest {

    private static final String TIMEZONE = "UTC";

    @Test
    void whenMigratingFromDatetimeToTimestamp_thenItIsResolvedProperly() throws IOException {
        // given
        Path tempDir = givenTempDirForDbExists();

        // and MySQL and Spring app are started with given TZs
        MySQLContainer<?> writeMySql = runMySql(TIMEZONE, tempDir);
        ApplicationContext writeContext = runSpringBootApp(TimeZone.getTimeZone(TIMEZONE), writeMySql);

        // and write the entity
        TimeEntity written = entityIsWritten(writeContext);

        // and stop Spring and MySQL
        exitSpringBootApp(writeContext);
        exitMySql(writeMySql);

        // and MySQL and Spring app are started again with given TZs
        MySQLContainer<?> readMySql = runMySql(TIMEZONE, tempDir);
        ApplicationContext readContext = runSpringBootApp(
            TimeZone.getTimeZone(TIMEZONE),
            readMySql,
            // while also running a migration, changing all the `DATETIME(3)` columns to `TIMESTAMP(3)` columns
            ImmutableList.of("spring.flyway.locations=classpath:db/migration,classpath:db/sql-type-migrations")
        );

        // when entity is read
        Optional<TimeEntity> read = entityIsRead(readContext, written.getId());

        // and Spring and MySQL are stopped
        exitSpringBootApp(readContext);
        exitMySql(readMySql);

        // then both entities are the same
        assertEntitiesAreTheSame(written, read);
    }
}
