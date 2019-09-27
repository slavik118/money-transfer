package com.al.mt.events;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import com.al.mt.enums.Reason;

public class MoneyTransferCancelled extends DomainEvent {
	private UUID transactionID;
	private UUID fromID;
	private UUID toID;
	private BigDecimal value;
	private Reason reason;

	private MoneyTransferCancelled() {
		// private constructr
	}

	public MoneyTransferCancelled(final UUID aggregateID, final UUID fromID, final UUID toID, final UUID transactionID,
			final BigDecimal value, final Reason reason) {
		this(aggregateID, fromID, toID, transactionID, value, reason, new Date());
	}

	public MoneyTransferCancelled(final UUID aggregateID, final UUID fromID, final UUID toID, final UUID transactionID,
			final BigDecimal value, final Reason reason, final Date date) {
		super(aggregateID, date);
		this.transactionID = transactionID;
		this.fromID = fromID;
		this.toID = toID;
		this.value = value;
		this.reason = reason;
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

	public final Reason getReason() {
		return this.reason;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.fromID == null) ? 0 : this.fromID.hashCode());
		result = prime * result + ((this.reason == null) ? 0 : this.reason.hashCode());
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
		final MoneyTransferCancelled other = (MoneyTransferCancelled) obj;
		if (this.fromID == null) {
			if (other.fromID != null)
				return false;
		} else if (!this.fromID.equals(other.fromID))
			return false;
		if (this.reason != other.reason)
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
		return new StringBuilder("MoneyTransferCancelled [transactionID=")
				.append(this.transactionID)
				.append(", fromID=")
				.append(this.fromID)
				.append(", toID=")
				.append(this.toID)
				.append(", value=")
				.append(this.value)
				.append(", reason=")
				.append(this.reason)
				.append("]")
				.toString();
	}

	public Builder toBuilder() {
		return new Builder();
	}

	public static Builder builder() {
		return new MoneyTransferCancelled().new Builder();
	}

	public class Builder {

		private Builder() {
			// private constructor
		}

		public final Builder setTransactionID(final UUID transactionID) {
			MoneyTransferCancelled.this.transactionID = transactionID;

			return this;
		}

		public final Builder setFromID(final UUID fromID) {
			MoneyTransferCancelled.this.fromID = fromID;

			return this;
		}

		public final Builder setToID(final UUID toID) {
			MoneyTransferCancelled.this.toID = toID;

			return this;
		}

		public final Builder setValue(final BigDecimal value) {
			MoneyTransferCancelled.this.value = value;

			return this;
		}

		public final Builder setReason(final Reason reason) {
			MoneyTransferCancelled.this.reason = reason;

			return this;
		}

		public MoneyTransferCancelled build() {
			return MoneyTransferCancelled.this;
		}
	}

}
