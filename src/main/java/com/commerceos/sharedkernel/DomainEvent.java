package com.commerceos.sharedkernel;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the system.
 *
 * <p>Every event carries a unique event ID (generated at creation), the aggregate ID it originated
 * from, a timestamp, and an event type. Events are immutable after creation.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * public record StockAdjusted(
 *     UUID productId, int quantityBefore, int quantityAfter, UUID adjustedBy
 * ) extends DomainEvent(productId) {}
 * }</pre>
 */
public abstract class DomainEvent {

  private final UUID eventId;
  private final UUID aggregateId;
  private final Instant occurredAt;
  private final String eventType;

  protected DomainEvent(UUID aggregateId) {
    this.eventId = UUID.randomUUID();
    this.aggregateId = aggregateId;
    this.occurredAt = Instant.now();
    this.eventType = getClass().getSimpleName();
  }

  /** Globally unique identifier for this event instance. */
  public UUID getEventId() {
    return eventId;
  }

  /** The aggregate that published this event. */
  public UUID getAggregateId() {
    return aggregateId;
  }

  /** When this event occurred (wall-clock time on the publishing node). */
  public Instant getOccurredAt() {
    return occurredAt;
  }

  /** The simple class name, e.g. {@code StockAdjusted} — useful as a routing key. */
  public String getEventType() {
    return eventType;
  }
}
