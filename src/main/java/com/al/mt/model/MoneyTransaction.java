package com.al.mt.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import com.al.mt.enums.State;
import com.al.mt.enums.Type;

public class MoneyTransaction {

	private UUID transactionID;
	private UUID fromID;
	private UUID toID;
	private BigDecimal value;
	private State state;
	private Type type;
	private Date createdAt;
	private Date lastUpdatedAt;

	private MoneyTransaction() {
		// private constructr
	}

	public final UUID getTransactionID() {
		return this.transactionID;
	}

	public final void setTransactionID(final UUID transactionID) {
		this.transactionID = transactionID;
	}

	public final UUID getFromID() {
		return this.fromID;
	}

	public final void setFromID(final UUID fromID) {
		this.fromID = fromID;
	}

	public final UUID getToID() {
		return this.toID;
	}

	public final void setToUUID(final UUID toID) {
		this.toID = toID;
	}

	public final BigDecimal getValue() {
		return this.value;
	}

	public final void setValue(final BigDecimal value) {
		this.value = value;
	}

	public final State getState() {
		return this.state;
	}

	public final void setState(final State state) {
		this.state = state;
	}

	public final Type getType() {
		return this.type;
	}

	public final void setType(final Type type) {
		this.type = type;
	}

	public final Date getCreatedAt() {
		return this.createdAt;
	}

	public final void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt;
	}

	public final Date getLastUpdatedAt() {
		return this.lastUpdatedAt;
	}

	public final void setLastUpdatedAt(final Date lastUpdatedAt) {
		this.lastUpdatedAt = lastUpdatedAt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.createdAt == null) ? 0 : this.createdAt.hashCode());
		result = prime * result + ((this.fromID == null) ? 0 : this.fromID.hashCode());
		result = prime * result + ((this.lastUpdatedAt == null) ? 0 : this.lastUpdatedAt.hashCode());
		result = prime * result + ((this.state == null) ? 0 : this.state.hashCode());
		result = prime * result + ((this.toID == null) ? 0 : this.toID.hashCode());
		result = prime * result + ((this.transactionID == null) ? 0 : this.transactionID.hashCode());
		result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
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
		final MoneyTransaction other = (MoneyTransaction) obj;
		if (this.createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!this.createdAt.equals(other.createdAt))
			return false;
		if (this.fromID == null) {
			if (other.fromID != null)
				return false;
		} else if (!this.fromID.equals(other.fromID))
			return false;
		if (this.lastUpdatedAt == null) {
			if (other.lastUpdatedAt != null)
				return false;
		} else if (!this.lastUpdatedAt.equals(other.lastUpdatedAt))
			return false;
		if (this.state != other.state)
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
		if (this.type != other.type)
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
		return new StringBuilder("MoneyTransaction [transactionID=").append(this.transactionID).append(", fromID=")
				.append(this.fromID).append(", toID=").append(this.toID).append(", value=").append(this.value)
				.append(", state=").append(this.state).append(", type=").append(this.type).append(", createdAt=")
				.append(this.createdAt).append(", lastUpdatedAt=").append(this.lastUpdatedAt).append("]").toString();
	}

	public static Builder builder() {
		return new MoneyTransaction().new Builder();
	}

	public class Builder {

		private Builder() {
			// private constructor
		}

		public final Builder setTransactionID(final UUID transactionID) {
			MoneyTransaction.this.transactionID = transactionID;

			return this;
		}

		public final Builder setFromID(final UUID fromID) {
			MoneyTransaction.this.fromID = fromID;

			return this;
		}

		public final Builder setToID(final UUID toID) {
			MoneyTransaction.this.toID = toID;

			return this;
		}

		public final Builder setValue(final BigDecimal value) {
			MoneyTransaction.this.value = value;

			return this;
		}

		public final Builder setState(final State state) {
			MoneyTransaction.this.state = state;

			return this;
		}

		public final Builder setType(final Type type) {
			MoneyTransaction.this.type = type;

			return this;
		}

		public final Builder setCreatedAt(final Date createdAt) {
			MoneyTransaction.this.createdAt = createdAt;

			return this;
		}

		public final Builder setLastUpdatedAt(final Date lastUpdatedAt) {
			MoneyTransaction.this.lastUpdatedAt = lastUpdatedAt;

			return this;
		}

		public MoneyTransaction build() {
			return MoneyTransaction.this;
		}
	}

}
