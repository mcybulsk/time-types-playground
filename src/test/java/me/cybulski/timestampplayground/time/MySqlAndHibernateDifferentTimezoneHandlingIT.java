package me.cybulski.timestampplayground.time;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.ApplicationContext;
import org.testcontainers.containers.MySQLContainer;

class MySqlAndHibernateDifferentTimezoneHandlingIT extends BaseTimeHandlingTest {

    private static Stream<Arguments> testParams() {
        return Stream.of(
            // same TZs everywhere
            Arguments.of("UTC", "UTC", "UTC", "UTC"),
            // same TZs in DB, different in app
            Arguments.of("UTC", "UTC", "UTC", "Europe/Warsaw"),
            // same TZs in DB, same in app
            Arguments.of("UTC", "Europe/Warsaw", "UTC", "Europe/Warsaw"),
            // same TZs in DB, different in app
            Arguments.of("UTC", "Europe/Warsaw", "UTC", "UTC"),
            // different TZs in DB, same in app
            Arguments.of("UTC", "UTC", "Europe/Warsaw", "UTC"),
            // same TZs in DB, same in app
            Arguments.of("Europe/Warsaw", "UTC", "Europe/Warsaw", "UTC"),
            // different TZs in DB, same in app
            Arguments.of("Europe/Warsaw", "UTC", "UTC", "UTC"),
            // both in DB and app the TZs are changed, but in the same way
            Arguments.of("UTC", "UTC", "Europe/Warsaw", "Europe/Warsaw")
        );
    }

    @ParameterizedTest
    @MethodSource("testParams")
    void whenTimezonesOfWritingAndReadingEntitiesAreSet_thenMappingsAreResolvedProperly(
        String writeDbTimezone,
        String writeAppTimezone,
        String readDbTimezone,
        String readAppTimezone
    ) throws IOException {
        // given
        Path tempDir = givenTempDirForDbExists();

        // and MySQL and Spring app are started with given TZs
        MySQLContainer<?> writeMySql = runMySql(writeDbTimezone, tempDir);
        ApplicationContext writeContext = runSpringBootApp(TimeZone.getTimeZone(writeAppTimezone), writeMySql);

        // and write the entity
        TimeEntity written = entityIsWritten(writeContext);

        // and stop Spring and MySQL
        exitSpringBootApp(writeContext);
        exitMySql(writeMySql);

        // and MySQL and Spring app are started again with given TZs
        MySQLContainer<?> readMySql = runMySql(readDbTimezone, tempDir);
        ApplicationContext readContext = runSpringBootApp(TimeZone.getTimeZone(readAppTimezone), readMySql);

        // when entity is read
        Optional<TimeEntity> read = entityIsRead(readContext, written.getId());

        // and Spring and MySQL are stopped
        exitSpringBootApp(readContext);
        exitMySql(readMySql);

        // then both entities are the same
        assertEntitiesAreTheSame(written, read);
    }
}
