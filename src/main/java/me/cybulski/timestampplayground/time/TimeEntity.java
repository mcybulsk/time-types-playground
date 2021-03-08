package me.cybulski.timestampplayground.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Table(name = "time_entity")
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
