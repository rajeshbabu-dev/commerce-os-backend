package com.commerceos.analytics.entity;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyEventRollupId implements Serializable {

  private LocalDate eventDate;
  private String eventType;
}
