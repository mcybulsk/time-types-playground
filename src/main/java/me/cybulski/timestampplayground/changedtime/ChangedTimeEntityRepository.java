package me.cybulski.timestampplayground.changedtime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChangedTimeEntityRepository extends JpaRepository<ChangedTimeEntity, Long> {

}
