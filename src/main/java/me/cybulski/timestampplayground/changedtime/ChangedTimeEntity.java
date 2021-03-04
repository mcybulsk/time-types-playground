package me.cybulski.timestampplayground.changedtime;

import java.time.Instant;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "time_entity")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class ChangedTimeEntity {
    @Id
    private Long id;

    private Instant instantInTimestamp;
    private Instant instantInDatetime;

    @Column(name = "localDateTimeInTimestamp")
    private Instant localDateTimeChangedToInstantInTimestamp;
    @Column(name = "localDateTimeInDatetime")
    private Instant localDateTimeChangedToInstantInDatetime;
}
