package com.al.mt.events;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public class MoneyTransferSucceeded extends DomainEvent {

	private final UUID transactionID;
	private final UUID fromID;
	private final UUID toID;
	private final BigDecimal value;

	public MoneyTransferSucceeded(final UUID aggregateID, final UUID fromID, final UUID toID, final UUID transactionID,
			final BigDecimal value) {
		super(aggregateID, new Date());
		this.transactionID = transactionID;
		this.fromID = fromID;
		this.toID = toID;
		this.value = value;
	}

	public MoneyTransferSucceeded(final UUID aggregateID, final UUID fromID, final UUID toID, final UUID transactionID,
			final BigDecimal value, final Date date) {
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
	public String toString() {
		return new StringBuilder("MoneyTransferSucceeded [transactionID=").append(this.transactionID)
				.append(", fromID=").append(this.fromID).append(", toID=").append(this.toID).append(", value=")
				.append(this.value).append("]").toString();
	}

}
