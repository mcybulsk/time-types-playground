package me.cybulski.timestampplayground.changedtime;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChangedTimeEntityRepository extends JpaRepository<ChangedTimeEntity, Long> {

    Optional<ChangedTimeEntity> findById(Long id);
}
