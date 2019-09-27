package com.al.mt.aggregates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.al.mt.enums.Reason;
import com.al.mt.events.AccountCreatedEvent;
import com.al.mt.events.AccountCreditedEvent;
import com.al.mt.events.AccountDebitedEvent;
import com.al.mt.events.DomainEvent;
import com.al.mt.events.MoneyTransferCancelled;
import com.al.mt.events.MoneyTransferSucceeded;
import com.al.mt.events.MoneyTransferredEvent;
import com.al.mt.exceptions.AggregateDoesNotExistException;
import com.al.mt.exceptions.InsufficientBalanceException;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Listener and dispatcher of events in whole system. It contains dependencies
 * between events.
 */
public class EventManager {
	private final static Logger LOG = LoggerFactory.getLogger(EventManager.class);

	private final EventBus eventBus;
	private final AccountEventStorage eventStorage;

	public EventManager(final EventBus eventBus, final AccountEventStorage eventStorage) {
		this.eventBus = eventBus;
		this.eventStorage = eventStorage;
	}

	/**
	 * Handles {@link AccountCreatedEvent} by persisting it to event storage.
	 */
	@Subscribe
	public void handle(final AccountCreatedEvent event) {
		logEvent(event);
		this.eventStorage.save(event);
	}

	/**
	 * Handles {@link MoneyTransferredEvent} by persisting it to event storage.
	 *
	 * <p>
	 * This event calls a chain of events, in case then {@link AccountAggregate} can
	 * be debited (can apply {@link AccountDebitedEvent}) then it will result in:
	 *
	 * <ul>
	 * <li>{@link MoneyTransferredEvent} on issuer aggregate which appends
	 * transaction to aggregate.
	 * <li>{@link AccountDebitedEvent} on issuer which reserves balance.
	 * <li>{@link MoneyTransferredEvent} on receiver which appends transaction to
	 * aggregate.
	 * <li>{@link AccountCreatedEvent} on receiver which reserves money on aggregate
	 * <li>{@link MoneyTransferSucceeded} on issuer's & receiver's aggregate which
	 * updates status of transactions and increments receiver's account.
	 * </ul>
	 *
	 * If issuer's aggregate cannot be debited then it will result in this chain of
	 * events:
	 *
	 * <ul>
	 * <li>{@link MoneyTransferredEvent} on issuer aggregate which appends
	 * transaction to aggregate.
	 * <li>{@link MoneyTransferCancelled} on issuer's aggregate with balance
	 * {@link Reason#BALANCE_TOO_LOW} which releases reserved money.
	 * </ul>
	 */
	@Subscribe
	public void handle(final MoneyTransferredEvent event) {
		logEvent(event);
		persistIfAggregateExists(event);
		this.eventBus.post(new AccountDebitedEvent(event.getAggregateID(), event.getFromID(), event.getToID(),
				event.getTransactionID(), event.getValue()));
	}

	@Subscribe
	public void handle(final AccountDebitedEvent event) {
		logEvent(event);
		final AccountAggregate dirtyAggregate = this.eventStorage.get(event.getFromID());
		if (dirtyAggregate == null) {
			throw new AggregateDoesNotExistException(event.toString());
		}
		try {
			dirtyAggregate.apply(event);
		} catch (final InsufficientBalanceException e) {
			// When there's not enough money MoneyTransferCancelled should be emitted only
			// to issuer
			this.eventBus.post(new MoneyTransferCancelled(event.getAggregateID(), event.getFromID(), event.getToID(),
					event.getTransactionID(), event.getValue(), Reason.BALANCE_TOO_LOW));
			return;
		}
		// When there's enough money we persist event and progress further
		persistIfAggregateExists(event);

		// Saves MoneyTransferredEvent in receiver's aggregate
		persistIfAggregateExists(new MoneyTransferredEvent(event.getToID(), event.getFromID(), event.getToID(),
				event.getTransactionID(), event.getValue()));

		// Requests crediting receiver's aggregate
		this.eventBus.post(new AccountCreditedEvent(event.getToID(), event.getFromID(), event.getToID(),
				event.getTransactionID(), event.getValue()));
	}

	@Subscribe
	public void handle(final AccountCreditedEvent event) {
		logEvent(event);
		persistIfAggregateExists(event);

		// Marks transfer as succeeded in issuer account
		this.eventBus.post(new MoneyTransferSucceeded(event.getFromID(), event.getFromID(), event.getToID(),
				event.getTransactionID(), event.getValue()));

		// Marks transfer as succeeded in receiver account
		this.eventBus.post(new MoneyTransferSucceeded(event.getToID(), event.getFromID(), event.getToID(),
				event.getTransactionID(), event.getValue()));
	}

	@Subscribe
	public void handle(final MoneyTransferSucceeded event) {
		logEvent(event);
		persistIfAggregateExists(event);
	}

	@Subscribe
	public void handle(final MoneyTransferCancelled event) {
		logEvent(event);
		persistIfAggregateExists(event);
	}

	private void logEvent(final DomainEvent event) {
		LOG.info("Received event: {}", event);
	}

	private void persistIfAggregateExists(final DomainEvent event) {
		if (this.eventStorage.exists(event.getAggregateID())) {
			this.eventStorage.save(event);
		} else {
			throw new AggregateDoesNotExistException(event.toString());
		}
	}
}
