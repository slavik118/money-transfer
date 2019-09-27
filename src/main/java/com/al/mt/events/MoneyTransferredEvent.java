package com.al.mt.events;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public class MoneyTransferredEvent extends DomainEvent {
	private final UUID transactionID;
	private final UUID fromID;
	private final UUID toID;
	private final BigDecimal value;

	public MoneyTransferredEvent(final UUID aggregateID, final UUID fromID, final UUID toID, final UUID transactionID,
			final BigDecimal value) {
		this(aggregateID, fromID, toID, transactionID, value, new Date());
	}

	public MoneyTransferredEvent(final UUID aggregateID, final UUID fromID, final UUID toID, final UUID transactionID,
			BigDecimal value, final Date date) {
		super(aggregateID, date);
		this.transactionID = transactionID;
		this.fromID = fromID;
		this.toID = toID;
		this.value = value;
	}

	public final UUID getTransactionID() {
		return this.transactionID;
	}

	public final UUID getFromID() {
		return this.fromID;
	}

	public final UUID getToID() {
		return this.toID;
	}

	public final BigDecimal getValue() {
		return this.value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.fromID == null) ? 0 : this.fromID.hashCode());
		result = prime * result + ((this.toID == null) ? 0 : this.toID.hashCode());
		result = prime * result + ((this.transactionID == null) ? 0 : this.transactionID.hashCode());
		result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MoneyTransferredEvent other = (MoneyTransferredEvent) obj;
		if (this.fromID == null) {
			if (other.fromID != null)
				return false;
		} else if (!this.fromID.equals(other.fromID))
			return false;
		if (this.toID == null) {
			if (other.toID != null)
				return false;
		} else if (!this.toID.equals(other.toID))
			return false;
		if (this.transactionID == null) {
			if (other.transactionID != null)
				return false;
		} else if (!this.transactionID.equals(other.transactionID))
			return false;
		if (this.value == null) {
			if (other.value != null)
				return false;
		} else if (!this.value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder("MoneyTransferredEvent [transactionID=")
				.append(this.transactionID)
				.append(", fromID=")
				.append(this.fromID)
				.append(", toID=")
				.append(this.toID)
				.append(", value=")
				.append(this.value)
				.append("]")
				.toString();
	}
}
