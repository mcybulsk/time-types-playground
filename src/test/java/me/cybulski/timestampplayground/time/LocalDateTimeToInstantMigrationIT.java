package me.cybulski.timestampplayground.time;

import me.cybulski.timestampplayground.changedtime.ChangedTimeEntity;
import me.cybulski.timestampplayground.changedtime.ChangedTimeEntityRepository;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.TimeZone;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.testcontainers.containers.MySQLContainer;

class LocalDateTimeToInstantMigrationIT extends BaseTimeHandlingTest {

    private static final String TIMEZONE = "UTC";

    @Test
    void whenLocalDateTimeIsChangedToInstant_thenItShouldBeProperlyMappedWithoutDbChanges() throws IOException {
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
        ApplicationContext readContext = runSpringBootApp(TimeZone.getTimeZone(TIMEZONE), readMySql);

        // when migrated entity is read
        ChangedTimeEntityRepository readTimeEntityRepository = readContext.getBean(ChangedTimeEntityRepository.class);
        Optional<ChangedTimeEntity> read = readTimeEntityRepository.findById(written.getId());

        // and Spring and MySQL are stopped
        exitSpringBootApp(readContext);
        exitMySql(readMySql);

        // then both entities are the same
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(read).isPresent();
        soft.assertThat(read.get().getId()).isEqualTo(written.getId());
        soft.assertThat(read.get().getInstantInTimestamp()).isEqualTo(written.getInstantInTimestamp());
        soft.assertThat(read.get().getInstantInDatetime()).isEqualTo(written.getInstantInDatetime());
        soft.assertThat(read.get().getLocalDateTimeChangedToInstantInTimestamp()).isEqualTo(
            convertToInstant(written.getLocalDateTimeInTimestamp(), TIMEZONE)
        );
        soft.assertThat(read.get().getLocalDateTimeChangedToInstantInDatetime()).isEqualTo(
            convertToInstant(written.getLocalDateTimeInDatetime(), TIMEZONE)
        );

        // and
        soft.assertAll();
    }

    private Instant convertToInstant(LocalDateTime localDateTime, String originalTimezone) {
        return localDateTime.atZone(ZoneId.of(originalTimezone)).toInstant();
    }
}
