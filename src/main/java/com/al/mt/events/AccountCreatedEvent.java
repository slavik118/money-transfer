package com.al.mt.events;

import java.util.Date;
import java.util.UUID;

public class AccountCreatedEvent extends DomainEvent {
	private final String fullName;

	public AccountCreatedEvent(final UUID aggregateID, final String fullName) {
		super(aggregateID, new Date());
		this.fullName = fullName;
	}

	public final String getFullName() {
		return this.fullName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.fullName == null) ? 0 : this.fullName.hashCode());
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
		final AccountCreatedEvent other = (AccountCreatedEvent) obj;
		if (this.fullName == null) {
			if (other.fullName != null)
				return false;
		} else if (!this.fullName.equals(other.fullName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder("AccountCreatedEvent [fullName=")
				.append(this.fullName)
				.append("]")
				.toString();
	}
}
