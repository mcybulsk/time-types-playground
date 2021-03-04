package me.cybulski.timestampplayground.time;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import me.cybulski.timestampplayground.TimestampPlaygroundApplication;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

abstract class BaseTimeHandlingTest {

    @BeforeAll
    static void setup() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.getLogger("root").setLevel(Level.INFO);
    }

    protected MySQLContainer<?> runMySql(String timezone, Path tempDir) {
        MySQLContainer mySQLContainer =
            new MySQLContainer<>(DockerImageName.parse("mysql"))
                .withEnv("TZ", timezone)
                .withFileSystemBind(tempDir.toString(), "/var/lib/mysql");
        mySQLContainer.start();
        return mySQLContainer;
    }

    protected void exitMySql(MySQLContainer<?> writeMySql) {
        writeMySql.stop();
    }

    protected ApplicationContext runSpringBootApp(TimeZone springTimezone, MySQLContainer<?> mySQLContainer) {
        return runSpringBootApp(springTimezone, mySQLContainer, Lists.emptyList());
    }

    protected ApplicationContext runSpringBootApp(
        TimeZone springTimezone,
        MySQLContainer<?> mySQLContainer,
        List<String> additionalProperties
    ) {
        // set JVM timezone
        TimeZone.setDefault(springTimezone);
        // generate Spring app properties
        List<String> properties = prepareSpringAppProperties(mySQLContainer, additionalProperties);
        return new SpringApplicationBuilder(TimestampPlaygroundApplication.class)
            .properties(properties.toArray(new String[0]))
            .run();
    }

    private List<String> prepareSpringAppProperties(MySQLContainer<?> mySQLContainer, List<String> additionalProperties) {
        List<String> properties = Lists.newArrayList(
            "spring.datasource.driver-class-name=com.mysql.jdbc.Driver",
            "spring.datasource.url=" + mySQLContainer.getJdbcUrl(),
            "spring.datasource.username=" + mySQLContainer.getUsername(),
            "spring.datasource.password=" + mySQLContainer.getPassword(),
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect"
        );
        properties.addAll(additionalProperties);
        return properties;
    }

    protected void exitSpringBootApp(ApplicationContext ctx) {
        SpringApplication.exit(ctx, () -> {
            // no errors
            return 0;
        });
    }

    protected Path givenTempDirForDbExists() throws IOException {
        return Files.createTempDirectory("mysql-timestamp-playground");
    }

    protected TimeEntity entityIsWritten(ApplicationContext writeContext) {
        Instant instant = Instant.now();
        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = new Date();
        TimeEntity written = new TimeEntity(1L, instant, instant, localDateTime, localDateTime, date, date);
        TimeEntityRepository timeEntityRepository = writeContext.getBean(TimeEntityRepository.class);
        timeEntityRepository.save(written);
        return written;
    }

    protected Optional<TimeEntity> entityIsRead(ApplicationContext readContext, Long id) {
        TimeEntityRepository readTimeEntityRepository = readContext.getBean(TimeEntityRepository.class);
        return readTimeEntityRepository.findById(id);
    }

    protected void assertEntitiesAreTheSame(TimeEntity written, Optional<TimeEntity> read) {
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(read).isPresent();
        soft.assertThat(read.get().getId()).isEqualTo(written.getId());
        soft.assertThat(read.get().getInstantInTimestamp()).isEqualTo(written.getInstantInTimestamp());
        soft.assertThat(read.get().getInstantInDatetime()).isEqualTo(written.getInstantInDatetime());
        soft.assertThat(read.get().getLocalDateTimeInTimestamp()).isEqualTo(written.getLocalDateTimeInTimestamp());
        soft.assertThat(read.get().getLocalDateTimeInDatetime()).isEqualTo(written.getLocalDateTimeInDatetime());
        // these two need to be compared by getTime, because written is java.util.Date and read java.sql.Timestamp (because Hibernate, that's why)
        soft.assertThat(read.get().getDateInTimestamp().getTime()).isEqualTo(written.getDateInTimestamp().getTime());
        soft.assertThat(read.get().getDateInDatetime().getTime()).isEqualTo(written.getDateInDatetime().getTime());

        // and
        soft.assertAll();
    }
}
