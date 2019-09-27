package com.al.mt.model;

import static com.al.mt.model.Link.getLinksForAccount;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.al.mt.aggregates.AccountAggregate;
import com.al.mt.events.DomainEvent;

public class Account {
	private UUID accountNumber;
	private String fullName;
	private BigDecimal balance;
	private Map<UUID, BigDecimal> transactionToReservedBalance;
	private Collection<DomainEvent> events;
	private Map<UUID, MoneyTransaction> transactions;
	private Date createdAt;
	private Date lastUpdatedAt;
	private List<Link> links;

	private Account() {
		// private constructor
	}

	public static Builder builder() {
		return new Account().new Builder();
	}

	public class Builder {

		private Builder() {
			// private constructor
		}

		public final Builder setFullName(final String fullName) {
			Account.this.fullName = fullName;

			return this;
		}

		public final Builder setAccountNumber(final UUID accountNumber) {
			Account.this.accountNumber = accountNumber;

			return this;
		}

		public final Builder setBalance(final BigDecimal balance) {
			Account.this.balance = balance;

			return this;
		}

		public final Builder setTransactionToReservedBalance(final Map<UUID, BigDecimal> transactionToReservedBalance) {
			Account.this.transactionToReservedBalance = transactionToReservedBalance;

			return this;
		}

		public final Builder setEvents(final Collection<DomainEvent> events) {
			Account.this.events = events;

			return this;
		}

		public final Builder setTransactions(final Map<UUID, MoneyTransaction> transactions) {
			Account.this.transactions = transactions;

			return this;
		}

		public final Builder setCreatedAt(final Date createdAt) {
			Account.this.createdAt = createdAt;

			return this;
		}

		public final Builder setLastUpdatedAt(final Date lastUpdatedAt) {
			Account.this.lastUpdatedAt = lastUpdatedAt;

			return this;
		}

		public final Builder setLinks(final List<Link> links) {
			Account.this.links = links;

			return this;
		}

		public Account build() {
			return Account.this;
		}
	}

	public static Account from(final AccountAggregate aggregate) {
		return Account.builder().setAccountNumber(aggregate.getId()).setFullName(aggregate.getFullName())
				.setBalance(aggregate.getBalance())
				.setTransactionToReservedBalance(aggregate.getTransactionToReservedBalance())
				.setEvents(aggregate.getDomainEvents())
				.setTransactions(aggregate.getTransactions().entrySet().stream().map(idToTransaction -> MoneyTransaction
						.builder().setTransactionID(idToTransaction.getValue().getTransactionID())
						.setFromID(idToTransaction.getValue().getFromID()).setToID(idToTransaction.getValue().getToID())
						.setValue(idToTransaction.getValue().getValue()).setState(idToTransaction.getValue().getState())
						.setType(idToTransaction.getValue().getType())
						.setLastUpdatedAt(idToTransaction.getValue().getLastUpdatedAt())
						.setCreatedAt(idToTransaction.getValue().getCreatedAt()).build())
						.collect(Collectors.toMap(MoneyTransaction::getTransactionID,
								moneyTransactionDTO -> moneyTransactionDTO)))
				.setCreatedAt(aggregate.getCreatedAt()).setLastUpdatedAt(aggregate.getLastUpdatedAt())
				.setLinks(getLinksForAccount(aggregate.getId())).build();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.accountNumber == null) ? 0 : this.accountNumber.hashCode());
		result = prime * result + ((this.fullName == null) ? 0 : this.fullName.hashCode());
		result = prime * result + ((this.balance == null) ? 0 : this.balance.hashCode());
		result = prime * result
				+ ((this.transactionToReservedBalance == null) ? 0 : this.transactionToReservedBalance.hashCode());
		result = prime * result + ((this.events == null) ? 0 : this.events.hashCode());
		result = prime * result + ((this.transactions == null) ? 0 : this.transactions.hashCode());
		result = prime * result + ((this.createdAt == null) ? 0 : this.createdAt.hashCode());
		result = prime * result + ((this.lastUpdatedAt == null) ? 0 : this.lastUpdatedAt.hashCode());
		result = prime * result + ((this.links == null) ? 0 : this.links.hashCode());

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
		final Account other = (Account) obj;
		if (this.accountNumber == null) {
			if (other.accountNumber != null)
				return false;
		} else if (!this.accountNumber.equals(other.accountNumber))
			return false;
		if (this.balance == null) {
			if (other.balance != null)
				return false;
		} else if (!this.balance.equals(other.balance))
			return false;
		if (this.createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!this.createdAt.equals(other.createdAt))
			return false;
		if (this.events == null) {
			if (other.events != null)
				return false;
		} else if (!this.events.equals(other.events))
			return false;
		if (this.fullName == null) {
			if (other.fullName != null)
				return false;
		} else if (!this.fullName.equals(other.fullName))
			return false;
		if (this.lastUpdatedAt == null) {
			if (other.lastUpdatedAt != null)
				return false;
		} else if (!this.lastUpdatedAt.equals(other.lastUpdatedAt))
			return false;
		if (this.links == null) {
			if (other.links != null)
				return false;
		} else if (!this.links.equals(other.links))
			return false;
		if (this.transactionToReservedBalance == null) {
			if (other.transactionToReservedBalance != null)
				return false;
		} else if (!this.transactionToReservedBalance.equals(other.transactionToReservedBalance))
			return false;
		if (this.transactions == null) {
			if (other.transactions != null)
				return false;
		} else if (!this.transactions.equals(other.transactions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder("Account [fullName=")
				.append(this.fullName)
				.append(", accountNumber=")
				.append(this.accountNumber)
				.append(", balance=")
				.append(this.balance)
				.append(", transactionToReservedBalance=")
				.append(this.transactionToReservedBalance)
				.append(", events=")
				.append(this.events)
				.append(", transactions=")
				.append(this.transactions)
				.append(", createdAt=")
				.append(this.createdAt)
				.append(", lastUpdatedAt=")
				.append(this.lastUpdatedAt)
				.append(", links=")
				.append(this.links)
				.append("]")
				.toString();
	}
}
