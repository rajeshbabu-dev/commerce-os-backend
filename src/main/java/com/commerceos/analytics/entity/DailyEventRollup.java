package com.commerceos.analytics.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "daily_event_rollup", schema = "analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(DailyEventRollupId.class)
public class DailyEventRollup {

  @Id
  @Column(name = "event_date", nullable = false)
  private LocalDate eventDate;

  @Id
  @Column(name = "event_type", nullable = false, length = 100)
  private String eventType;

  @Column(name = "event_count", nullable = false)
  @Builder.Default
  private long eventCount = 0;
}
