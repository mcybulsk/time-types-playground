package me.cybulski.timestampplayground.time;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeEntityRepository extends JpaRepository<TimeEntity, Long> {
    Optional<TimeEntity> findById(Long id);
}
