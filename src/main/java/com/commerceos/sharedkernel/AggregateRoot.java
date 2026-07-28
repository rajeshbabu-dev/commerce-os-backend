package com.commerceos.sharedkernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Base class for all aggregate roots in the system.
 *
 * <p>Provides a mechanism to collect domain events during a transaction and publish them afterward.
 * Every aggregate root has a unique {@link #getId()} that identifies it across persistence and
 * event boundaries.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * public class Product extends AggregateRoot {
 *     public void adjustStock(int quantity) {
 *         // business logic...
 *         registerEvent(new StockAdjusted(getId(), quantity));
 *     }
 * }
 * }</pre>
 */
public abstract class AggregateRoot {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  /** Every aggregate must expose its unique identifier. */
  public abstract UUID getId();

  /**
   * Registers a domain event to be published after the current transaction completes. Events are
   * cleared once published.
   */
  protected void registerEvent(DomainEvent event) {
    domainEvents.add(event);
  }

  /** Returns an unmodifiable view of pending domain events. */
  public List<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  /** Clears all pending domain events — called after successful publication. */
  public void clearDomainEvents() {
    domainEvents.clear();
  }
}
