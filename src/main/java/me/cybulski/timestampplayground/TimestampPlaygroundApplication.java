package me.cybulski.timestampplayground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@EntityScan(basePackageClasses = { TimestampPlaygroundApplication.class, Jsr310JpaConverters.class })
public class TimestampPlaygroundApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimestampPlaygroundApplication.class, args);
	}

}
