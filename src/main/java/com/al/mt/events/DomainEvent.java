package com.al.mt.events;

import java.util.Date;
import java.util.UUID;

import com.google.common.base.CaseFormat;

/**
 * Base event for all the other events that mutate aggregates and that should be
 * stored.
 */
public abstract class DomainEvent {
	private UUID aggregateID;
	private String eventType;
	protected Date createdAt;

	protected DomainEvent() {
		this.eventType = classNameToUpperCase();
	}

	protected DomainEvent(final UUID aggregateID, final Date createdAt) {
		this.aggregateID = aggregateID;
		this.createdAt = createdAt;
		this.eventType = classNameToUpperCase();
	}

	public final UUID getAggregateID() {
		return this.aggregateID;
	}

	public final String getEventType() {
		return this.eventType;
	}

	public final Date getCreatedAt() {
		return this.createdAt;
	}

	private String classNameToUpperCase() {
		return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, this.getClass().getSimpleName());
	}

}
