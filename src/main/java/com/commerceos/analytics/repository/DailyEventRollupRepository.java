package com.commerceos.analytics.repository;

import com.commerceos.analytics.entity.DailyEventRollup;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyEventRollupRepository
    extends JpaRepository<DailyEventRollup, com.commerceos.analytics.entity.DailyEventRollupId> {

  Optional<DailyEventRollup> findByEventDateAndEventType(LocalDate eventDate, String eventType);

  List<DailyEventRollup> findByEventDateGreaterThanEqual(LocalDate startDate);
}
