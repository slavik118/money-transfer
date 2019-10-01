package com.al.mt.aggregates;

import static io.vavr.API.Case;
import static io.vavr.API.Match.Pattern0.of;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.al.mt.enums.State;
import com.al.mt.enums.Type;
import com.al.mt.events.AccountCreatedEvent;
import com.al.mt.events.AccountCreditedEvent;
import com.al.mt.events.AccountDebitedEvent;
import com.al.mt.events.DomainEvent;
import com.al.mt.events.MoneyTransferCancelled;
import com.al.mt.events.MoneyTransferSucceeded;
import com.al.mt.events.MoneyTransferredEvent;
import com.al.mt.exceptions.InsufficientBalanceException;
import com.al.mt.model.MoneyTransaction;

import io.vavr.API;

/**
 * AccountAggregate is constructed based on events that are stored in
 * {@link AccountEventStorage}. Each stored event mutates the state which
 * eventually leads into final object.
 *
 * <p>
 * All operations that modify the aggregate from external packages must use
 * commands which then validate the input and asynchronously trigger event
 * handlers which will store the mutation event {@link DomainEvent} and trigger
 * other actions if needed.
 *
 * <p>
 * Each {@link DomainEvent} related to AccountAggregate mutates it in a specific
 * order therefore events must be stored in order and must be emitted by FIFO
 * PubSub/EventBus for each aggregate.
 */
public class AccountAggregate {
	private static final double INITIAL_BALANCE = 1000;
	
	public static double getInitialBalance() {
		return INITIAL_BALANCE;
	}

	private final Collection<DomainEvent> domainEvents;

	private UUID id;
	private String fullName;

	private BigDecimal balance;
	private Map<UUID, BigDecimal> transactionToReservedBalance;
	private Map<UUID, MoneyTransaction> transactions;
	private Date createdAt;
	private Date lastUpdatedAt;

	public AccountAggregate(final Collection<DomainEvent> domainEvents) {
		this.domainEvents = domainEvents;
	}

	/** 
	 * Applies stored event by re-routing it to proper handler. 
	 */
	AccountAggregate apply(final DomainEvent event) {
		return API.Match(event).of(Case(of(AccountCreatedEvent.class), this::apply),
				Case(of(MoneyTransferredEvent.class), this::apply),
				Case(of(AccountDebitedEvent.class), this::apply),
				Case(of(AccountCreditedEvent.class), this::apply), 
				Case(of(MoneyTransferCancelled.class), this::apply),
				Case(of(MoneyTransferSucceeded.class), this::apply));
	}

	/**
	 * Event that initializes aggregate.
	 *
	 * <p>
	 * Sets balance to {@link AccountAggregate#INITIAL_BALANCE} for simplicity.
	 */
	AccountAggregate apply(final AccountCreatedEvent event) {
		this.id = event.getAggregateID();
		this.transactionToReservedBalance = new TreeMap<>(); 
		this.balance = BigDecimal.valueOf(INITIAL_BALANCE).setScale(2, RoundingMode.HALF_EVEN);
		this.fullName = event.getFullName();
		this.transactions = new TreeMap<>(); 
		this.createdAt = event.getCreatedAt();
		this.lastUpdatedAt = event.getCreatedAt();
		return this;
	}

	/**
	 * Appends new transaction to {@link AccountAggregate#transactions} with
	 * {@link MoneyTransaction.State#NEW}.
	 *
	 * <p>
	 * If the event is applied on the issuer aggregate (account from money should be
	 * subtracted) then the transaction is set to
	 * {@link MoneyTransaction.Type#OUTGOING} with negated value, otherwise it's set
	 * to {@link MoneyTransaction.Type#INCOMING} with raw value.
	 */
	AccountAggregate apply(final MoneyTransferredEvent event) {
		final BigDecimal value;
		final Type type;
		this.lastUpdatedAt = event.getCreatedAt();
		if (event.getAggregateID().equals(event.getFromID())) {
			// Outgoing money transfer
			value = event.getValue().negate();
			type = Type.OUTGOING;
		} else {
			// Incoming money transfer
			value = event.getValue();
			type = Type.INCOMING;
		}
		this.transactions.put(event.getTransactionID(),
				MoneyTransaction.builder().setTransactionID(event.getTransactionID()).setFromID(event.getFromID())
						.setToID(event.getToID()).setValue(value).setState(State.NEW).setType(type)
						.setCreatedAt(event.getCreatedAt()).setLastUpdatedAt(event.getCreatedAt()).build());
		return this;
	}

	/**
	 * Reserves balance on account that's about to have it's balance transferred and
	 * subtracts that amount from the main balance.
	 *
	 * <p>
	 * If the event was followed by {@link MoneyTransferredEvent} (there's a
	 * transaction) then it also updates the transaction state to
	 * {@link MoneyTransaction.State#PENDING}.
	 *
	 * @throws InsufficientBalanceException when {@link AccountAggregate#balance} is not
	 *                                sufficient.
	 */
	AccountAggregate apply(final AccountDebitedEvent event) {
		if (this.balance.subtract(event.getValue()).compareTo(BigDecimal.ZERO) >= 0) {
			this.lastUpdatedAt = event.getCreatedAt();
			// Reserves balance for receiver
			this.balance = balance.subtract(event.getValue());
			this.transactionToReservedBalance.put(event.getTransactionID(), event.getValue().negate());
			if (this.transactions.containsKey(event.getTransactionID())) {
				changeTransactionState(event.getTransactionID(), State.PENDING, event.getCreatedAt());
			}
			return this;
		} else {
			throw new InsufficientBalanceException("Balance too low.");
		}
	}

	/**
	 * Reserves balance on account that's about to have it's balance increased
	 * (receiver or the money transfer).
	 *
	 * <p>
	 * If the event was followed by {@link MoneyTransferredEvent} (there's a
	 * transaction) then it also updates the transaction state to
	 * {@link MoneyTransaction.State#PENDING}.
	 */
	AccountAggregate apply(final AccountCreditedEvent event) {
		this.lastUpdatedAt = event.getCreatedAt();
		// Adds a temporal balance
		this.transactionToReservedBalance.put(event.getTransactionID(), event.getValue());
		if (this.transactions.containsKey(event.getTransactionID())) {
			changeTransactionState(event.getTransactionID(), State.PENDING, event.getCreatedAt());
		}
		return this;
	}

	/**
	 * Marks transaction as {@link MoneyTransaction.State#SUCCEEDED}. Releases
	 * reserved balance and increments it for receiver of the money transfer.
	 */
	AccountAggregate apply(final MoneyTransferSucceeded event) {
		this.lastUpdatedAt = event.getCreatedAt();
		changeTransactionState(event.getTransactionID(), State.SUCCEEDED, event.getCreatedAt());
		if (this.transactionToReservedBalance.containsKey(event.getTransactionID())) {
			// Increments receiver's account
			if (event.getToID().equals(event.getAggregateID())) {
				this.balance = this.balance.add(this.transactionToReservedBalance.remove(event.getTransactionID()));
			}
		}
		return this;
	}

	/** 
	 * Cancels ongoing transaction. 
	 */
	AccountAggregate apply(final MoneyTransferCancelled event) {
		this.lastUpdatedAt = event.getCreatedAt();
		if (event.getToID().equals(event.getAggregateID())) {
			// Canceling money transfer for receiver
			this.transactionToReservedBalance.remove(event.getTransactionID());
		} else if (this.transactionToReservedBalance.containsKey(event.getTransactionID())) {
			this.balance = this.balance.add(this.transactionToReservedBalance.get(event.getTransactionID()).negate());
			this.transactionToReservedBalance.remove(event.getTransactionID());
		}

		changeTransactionState(event.getTransactionID(), State.CANCELLED, event.getCreatedAt());
		return this;
	}

	public final Collection<DomainEvent> getDomainEvents() {
		return this.domainEvents;
	}

	public final UUID getId() {
		return this.id;
	}

	public final String getFullName() {
		return this.fullName;
	}

	public final BigDecimal getBalance() {
		return this.balance;
	}

	public final Map<UUID, BigDecimal> getTransactionToReservedBalance() {
		return this.transactionToReservedBalance;
	}

	public final Map<UUID, MoneyTransaction> getTransactions() {
		return this.transactions;
	}

	public final Date getCreatedAt() {
		return this.createdAt;
	}

	public final Date getLastUpdatedAt() {
		return this.lastUpdatedAt;
	}

	private void changeTransactionState(final UUID transactionID, final State state, final Date lastUpdatedAt) {
		final MoneyTransaction transaction = transactions.get(transactionID);
		transaction.setState(state);
		transaction.setLastUpdatedAt(lastUpdatedAt);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.balance == null) ? 0 : this.balance.hashCode());
		result = prime * result + ((this.createdAt == null) ? 0 : this.createdAt.hashCode());
		result = prime * result + ((this.domainEvents == null) ? 0 : this.domainEvents.hashCode());
		result = prime * result + ((this.fullName == null) ? 0 : this.fullName.hashCode());
		result = prime * result + ((this.lastUpdatedAt == null) ? 0 : this.lastUpdatedAt.hashCode());
		result = prime * result
				+ ((this.transactionToReservedBalance == null) ? 0 : this.transactionToReservedBalance.hashCode());
		result = prime * result + ((this.transactions == null) ? 0 : this.transactions.hashCode());
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
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
		final AccountAggregate other = (AccountAggregate) obj;
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
		if (this.domainEvents == null) {
			if (other.domainEvents != null)
				return false;
		} else if (!this.domainEvents.equals(other.domainEvents))
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
		if (this.id == null) {
			if (other.id != null)
				return false;
		} else if (!this.id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder("AccountAggregate [id=").append(this.id).append(", fullName=").append(this.fullName)
				.append(", balance=").append(this.balance).append(", transactionToReservedBalance=")
				.append(this.transactionToReservedBalance).append(", domainEvents=").append(this.domainEvents)
				.append(", transactions=").append(this.transactions).append(", createdAt=").append(this.createdAt)
				.append(", lastUpdatedAt=").append(this.lastUpdatedAt).append("]").toString();
	}

}
