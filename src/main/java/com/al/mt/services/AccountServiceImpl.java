package com.al.mt.services;

import java.math.BigDecimal;
import java.util.UUID;

import com.al.mt.aggregates.AccountAggregate;
import com.al.mt.aggregates.EventManager;
import com.al.mt.enums.Reason;
import com.al.mt.events.AccountCreatedEvent;
import com.al.mt.events.MoneyTransferCancelled;
import com.al.mt.events.MoneyTransferredEvent;
import com.google.common.eventbus.EventBus;

/**
 * Handles all account related requests.
 */
public class AccountServiceImpl implements AccountService {

	private final EventBus eventBus;

	public AccountServiceImpl(final EventBus eventBus) {
		this.eventBus = eventBus;
	}

	/**
	 * Command which initializes {@link AccountAggregate} by emitting
	 * {@link AccountCreatedEvent} with only {@code fullName}.
	 * This event is then received by the {@link EventManager}.
	 */
	public UUID createAccount(final String fullName) {
		final UUID aggregateID = UUID.randomUUID();
		this.eventBus.post(new AccountCreatedEvent(aggregateID, fullName));
		return aggregateID;
	}

	/**
	 * Command which initializes money transfer between two accounts
	 * {@link MoneyTransferredEvent}. 
	 * This event is then received by the {@link EventManager}.
	 */
	public void transferMoney(final UUID fromID, final UUID toID, final BigDecimal value) {
		this.eventBus.post(new MoneyTransferredEvent(fromID, fromID, toID, UUID.randomUUID(), value));
	}

	/**
	 * Command which cancels money transfer {@link MoneyTransferCancelled}. 
	 * This event is then received by the {@link EventManager}.
	 */
	public void cancelTransaction(final UUID aggregateID, final UUID fromID, final UUID toID, final UUID transactionID,
			final BigDecimal value, final Reason reason) {
		this.eventBus.post(new MoneyTransferCancelled(aggregateID, fromID, toID, transactionID, value, reason));
	}
	
	
}
