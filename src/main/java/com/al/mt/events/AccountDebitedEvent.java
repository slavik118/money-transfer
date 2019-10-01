
package com.al.mt.events;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public class AccountDebitedEvent extends DomainEvent {
	private UUID transactionID;
	private UUID fromID;
	private UUID toID;
	private BigDecimal value;

	private AccountDebitedEvent() {
	}

	public AccountDebitedEvent(final UUID aggregateID, final UUID fromID, final UUID toID, final UUID transactionID,
			final BigDecimal value) {
		super(aggregateID, new Date());
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
		return new StringBuilder("AccountDebitedEvent [transactionID=")
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

	public Builder toBuilder() {
		return new Builder();
	}

	public static Builder builder() {
		return new AccountDebitedEvent().new Builder();
	}

	public class Builder {

		private Builder() {
		}

		public final Builder setTransactionID(final UUID transactionID) {
			AccountDebitedEvent.this.transactionID = transactionID;

			return this;
		}

		public final Builder setFromID(final UUID fromID) {
			AccountDebitedEvent.this.fromID = fromID;

			return this;
		}

		public final Builder setToID(final UUID toID) {
			AccountDebitedEvent.this.toID = toID;

			return this;
		}

		public final Builder setValue(final BigDecimal value) {
			AccountDebitedEvent.this.value = value;

			return this;
		}

		public AccountDebitedEvent build() {
			return AccountDebitedEvent.this;
		}
	}

}
