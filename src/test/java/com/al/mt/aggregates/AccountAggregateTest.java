package com.al.mt.aggregates;

import static com.al.mt.utils.Constants.FIRST_ACCOUT_FULL_NAME;
import static com.google.common.truth.Truth.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.al.mt.enums.Reason;
import com.al.mt.enums.State;
import com.al.mt.enums.Type;
import com.al.mt.events.AccountCreatedEvent;
import com.al.mt.events.AccountCreditedEvent;
import com.al.mt.events.AccountDebitedEvent;
import com.al.mt.events.DomainEvent;
import com.al.mt.events.MoneyTransferCancelled;
import com.al.mt.events.MoneyTransferSucceeded;
import com.al.mt.events.MoneyTransferredEvent;
import com.al.mt.model.MoneyTransaction;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class AccountAggregateTest {
	private static final UUID FROM_ID = UUID.randomUUID();
	private static final UUID TO_ID = UUID.randomUUID();
	private static final AccountCreatedEvent ACCOUNT_CREATED = new AccountCreatedEvent(UUID.randomUUID(), FIRST_ACCOUT_FULL_NAME);
	private static final MoneyTransferredEvent ISSUER_MONEY_TRANSFERRED = new MoneyTransferredEvent(FROM_ID,
			FROM_ID, TO_ID, UUID.randomUUID(), BigDecimal.TEN);
	private static final MoneyTransferredEvent RECEIVER_MONEY_TRANSFERRED = new MoneyTransferredEvent(
			ISSUER_MONEY_TRANSFERRED.getToID(), ISSUER_MONEY_TRANSFERRED.getFromID(),
			ISSUER_MONEY_TRANSFERRED.getToID(), ISSUER_MONEY_TRANSFERRED.getTransactionID(),
			ISSUER_MONEY_TRANSFERRED.getValue());
	private static final AccountDebitedEvent ACCOUNT_DEBITED = new AccountDebitedEvent(
			ISSUER_MONEY_TRANSFERRED.getAggregateID(), ISSUER_MONEY_TRANSFERRED.getFromID(),
			ISSUER_MONEY_TRANSFERRED.getToID(), ISSUER_MONEY_TRANSFERRED.getTransactionID(),
			ISSUER_MONEY_TRANSFERRED.getValue());
	private static final AccountCreditedEvent ACCOUNT_CREDITED = new AccountCreditedEvent(
			RECEIVER_MONEY_TRANSFERRED.getAggregateID(), RECEIVER_MONEY_TRANSFERRED.getFromID(),
			RECEIVER_MONEY_TRANSFERRED.getToID(), RECEIVER_MONEY_TRANSFERRED.getTransactionID(),
			RECEIVER_MONEY_TRANSFERRED.getValue());
	private static final MoneyTransferCancelled ISSUER_MONEY_TRANSFER_CANCELLED = new MoneyTransferCancelled(
			ISSUER_MONEY_TRANSFERRED.getAggregateID(), ISSUER_MONEY_TRANSFERRED.getFromID(),
			ISSUER_MONEY_TRANSFERRED.getToID(), ISSUER_MONEY_TRANSFERRED.getTransactionID(),
			ISSUER_MONEY_TRANSFERRED.getValue(), Reason.BALANCE_TOO_LOW);
	private static final MoneyTransferCancelled RECEIVER_MONEY_TRANSFER_CANCELLED = new MoneyTransferCancelled(
			RECEIVER_MONEY_TRANSFERRED.getAggregateID(), RECEIVER_MONEY_TRANSFERRED.getFromID(),
			RECEIVER_MONEY_TRANSFERRED.getToID(), RECEIVER_MONEY_TRANSFERRED.getTransactionID(),
			RECEIVER_MONEY_TRANSFERRED.getValue(), Reason.BALANCE_TOO_LOW);
	private static final MoneyTransferSucceeded ISSUER_MONEY_TRANSFER_SUCCEEDED = new MoneyTransferSucceeded(
			ISSUER_MONEY_TRANSFERRED.getAggregateID(), ISSUER_MONEY_TRANSFERRED.getFromID(),
			ISSUER_MONEY_TRANSFERRED.getToID(), ISSUER_MONEY_TRANSFERRED.getTransactionID(),
			ISSUER_MONEY_TRANSFERRED.getValue());
	private static final MoneyTransferSucceeded RECEIVER_MONEY_TRANSFER_SUCCEEDED = new MoneyTransferSucceeded(
			RECEIVER_MONEY_TRANSFERRED.getAggregateID(), RECEIVER_MONEY_TRANSFERRED.getFromID(),
			RECEIVER_MONEY_TRANSFERRED.getToID(), RECEIVER_MONEY_TRANSFERRED.getTransactionID(),
			RECEIVER_MONEY_TRANSFERRED.getValue());

	@Test
	public void accountCreatedEvent() {
		// given
		final ImmutableList<DomainEvent> events = ImmutableList.of(ACCOUNT_CREATED);

		// when
		final AccountAggregate aggregate = AccountEventStorage.recreate(events);

		// assert
		assertThat(aggregate.getFullName()).isEqualTo(ACCOUNT_CREATED.getFullName());
		assertThat(aggregate.getId()).isEqualTo(ACCOUNT_CREATED.getAggregateID());
		assertThat(aggregate.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(aggregate.getTransactions()).isEmpty();
		assertThat(aggregate.getTransactionToReservedBalance()).isEmpty();
		assertThat(aggregate.getCreatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getLastUpdatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getDomainEvents()).isEqualTo(events);
	}

	@Test
	public void moneyTransferredEventIssuer() {
		// given
		final ImmutableList<DomainEvent> events = ImmutableList.of(ACCOUNT_CREATED, ISSUER_MONEY_TRANSFERRED);

		// when
		final AccountAggregate aggregate = AccountEventStorage.recreate(events);

		// assert
		assertThat(aggregate.getFullName()).isEqualTo(ACCOUNT_CREATED.getFullName());
		assertThat(aggregate.getId()).isEqualTo(ACCOUNT_CREATED.getAggregateID());
		assertThat(aggregate.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(aggregate.getTransactions()).isEqualTo(ImmutableMap.of(ISSUER_MONEY_TRANSFERRED.getTransactionID(),
				MoneyTransaction.builder().setFromID(ISSUER_MONEY_TRANSFERRED.getFromID())
						.setToID(ISSUER_MONEY_TRANSFERRED.getToID())
						.setTransactionID(ISSUER_MONEY_TRANSFERRED.getTransactionID()).setState(State.NEW)
						.setType(Type.OUTGOING).setValue(ISSUER_MONEY_TRANSFERRED.getValue().negate())
						.setCreatedAt(ISSUER_MONEY_TRANSFERRED.getCreatedAt())
						.setLastUpdatedAt(ISSUER_MONEY_TRANSFERRED.getCreatedAt()).build()));
		assertThat(aggregate.getTransactionToReservedBalance()).isEmpty();
		assertThat(aggregate.getCreatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getLastUpdatedAt()).isEqualTo(ISSUER_MONEY_TRANSFERRED.getCreatedAt());
		assertThat(aggregate.getDomainEvents()).isEqualTo(events);
	}

	@Test
	public void moneyTransferredEventReceiver() {
		// given
		final ImmutableList<DomainEvent> events = ImmutableList.of(ACCOUNT_CREATED, RECEIVER_MONEY_TRANSFERRED);

		// when
		final AccountAggregate aggregate = AccountEventStorage.recreate(events);

		// assert
		assertThat(aggregate.getFullName()).isEqualTo(ACCOUNT_CREATED.getFullName());
		assertThat(aggregate.getId()).isEqualTo(ACCOUNT_CREATED.getAggregateID());
		assertThat(aggregate.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(aggregate.getTransactions()).isEqualTo(ImmutableMap.of(RECEIVER_MONEY_TRANSFERRED.getTransactionID(),
				MoneyTransaction.builder().setFromID(RECEIVER_MONEY_TRANSFERRED.getFromID())
						.setToID(RECEIVER_MONEY_TRANSFERRED.getToID())
						.setTransactionID(RECEIVER_MONEY_TRANSFERRED.getTransactionID()).setState(State.NEW)
						.setType(Type.INCOMING).setValue(RECEIVER_MONEY_TRANSFERRED.getValue())
						.setCreatedAt(RECEIVER_MONEY_TRANSFERRED.getCreatedAt())
						.setLastUpdatedAt(RECEIVER_MONEY_TRANSFERRED.getCreatedAt()).build()));
		assertThat(aggregate.getTransactionToReservedBalance()).isEmpty();
		assertThat(aggregate.getCreatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getLastUpdatedAt()).isEqualTo(RECEIVER_MONEY_TRANSFERRED.getCreatedAt());
		assertThat(aggregate.getDomainEvents()).isEqualTo(events);
	}

	@Test
	public void accountDebitedEvent() {
		// given
		final ImmutableList<DomainEvent> events = ImmutableList.of(ACCOUNT_CREATED, ISSUER_MONEY_TRANSFERRED,
				ACCOUNT_DEBITED);

		// when
		final AccountAggregate aggregate = AccountEventStorage.recreate(events);

		// assert
		assertThat(aggregate.getFullName()).isEqualTo(ACCOUNT_CREATED.getFullName());
		assertThat(aggregate.getId()).isEqualTo(ACCOUNT_CREATED.getAggregateID());
		assertThat(aggregate.getBalance()).isEqualTo(BigDecimal.valueOf(990).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(aggregate.getTransactions()).isEqualTo(ImmutableMap.of(ISSUER_MONEY_TRANSFERRED.getTransactionID(),
				MoneyTransaction.builder().setFromID(ISSUER_MONEY_TRANSFERRED.getFromID())
						.setToID(ISSUER_MONEY_TRANSFERRED.getToID())
						.setTransactionID(ISSUER_MONEY_TRANSFERRED.getTransactionID()).setState(State.PENDING)
						.setType(Type.OUTGOING).setValue(ISSUER_MONEY_TRANSFERRED.getValue().negate())
						.setCreatedAt(ISSUER_MONEY_TRANSFERRED.getCreatedAt())
						.setLastUpdatedAt(ACCOUNT_DEBITED.getCreatedAt()).build()));
		assertThat(aggregate.getTransactionToReservedBalance())
				.isEqualTo(ImmutableMap.of(ACCOUNT_DEBITED.getTransactionID(), ACCOUNT_DEBITED.getValue().negate()));
		assertThat(aggregate.getCreatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getLastUpdatedAt()).isEqualTo(ACCOUNT_DEBITED.getCreatedAt());
		assertThat(aggregate.getDomainEvents()).isEqualTo(events);
	}

	@Test
	public void accountDebitedEventNoTransaction() {
		// given
		final ImmutableList<DomainEvent> events = ImmutableList.of(ACCOUNT_CREATED, ACCOUNT_DEBITED);

		// when
		final AccountAggregate aggregate = AccountEventStorage.recreate(events);

		// assert
		assertThat(aggregate.getFullName()).isEqualTo(ACCOUNT_CREATED.getFullName());
		assertThat(aggregate.getId()).isEqualTo(ACCOUNT_CREATED.getAggregateID());
		assertThat(aggregate.getBalance()).isEqualTo(BigDecimal.valueOf(990).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(aggregate.getTransactions()).isEmpty();
		assertThat(aggregate.getTransactionToReservedBalance())
				.isEqualTo(ImmutableMap.of(ACCOUNT_DEBITED.getTransactionID(), ACCOUNT_DEBITED.getValue().negate()));
		assertThat(aggregate.getCreatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getLastUpdatedAt()).isEqualTo(ACCOUNT_DEBITED.getCreatedAt());
		assertThat(aggregate.getDomainEvents()).isEqualTo(events);
	}

	@Test
	public void accountCreditedEvent() {
		// given
		final ImmutableList<DomainEvent> events = ImmutableList.of(ACCOUNT_CREATED, RECEIVER_MONEY_TRANSFERRED,
				ACCOUNT_CREDITED);

		// when
		final AccountAggregate aggregate = AccountEventStorage.recreate(events);

		// assert
		assertThat(aggregate.getFullName()).isEqualTo(ACCOUNT_CREATED.getFullName());
		assertThat(aggregate.getId()).isEqualTo(ACCOUNT_CREATED.getAggregateID());
		assertThat(aggregate.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(aggregate.getTransactions()).isEqualTo(ImmutableMap.of(RECEIVER_MONEY_TRANSFERRED.getTransactionID(),
				MoneyTransaction.builder().setFromID(RECEIVER_MONEY_TRANSFERRED.getFromID())
						.setToID(RECEIVER_MONEY_TRANSFERRED.getToID())
						.setTransactionID(RECEIVER_MONEY_TRANSFERRED.getTransactionID()).setState(State.PENDING)
						.setType(Type.INCOMING).setValue(RECEIVER_MONEY_TRANSFERRED.getValue())
						.setCreatedAt(RECEIVER_MONEY_TRANSFERRED.getCreatedAt())
						.setLastUpdatedAt(ACCOUNT_CREDITED.getCreatedAt()).build()));
		assertThat(aggregate.getTransactionToReservedBalance())
				.isEqualTo(ImmutableMap.of(ACCOUNT_CREDITED.getTransactionID(), ACCOUNT_CREDITED.getValue()));
		assertThat(aggregate.getCreatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getLastUpdatedAt()).isEqualTo(ACCOUNT_CREDITED.getCreatedAt());
		assertThat(aggregate.getDomainEvents()).isEqualTo(events);
	}

	@Test
	public void accountCreditedEventWithNoTransaction() {
		// given
		final ImmutableList<DomainEvent> events = ImmutableList.of(ACCOUNT_CREATED, ACCOUNT_CREDITED);

		// when
		final AccountAggregate aggregate = AccountEventStorage.recreate(events);

		// assert
		assertThat(aggregate.getFullName()).isEqualTo(ACCOUNT_CREATED.getFullName());
		assertThat(aggregate.getId()).isEqualTo(ACCOUNT_CREATED.getAggregateID());
		assertThat(aggregate.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(aggregate.getTransactions()).isEmpty();
		assertThat(aggregate.getTransactionToReservedBalance())
				.isEqualTo(ImmutableMap.of(ACCOUNT_CREDITED.getTransactionID(), ACCOUNT_CREDITED.getValue()));
		assertThat(aggregate.getCreatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getLastUpdatedAt()).isEqualTo(ACCOUNT_CREDITED.getCreatedAt());
		assertThat(aggregate.getDomainEvents()).isEqualTo(events);
	}

	@Test
	public void moneyTransferSucceededNoReservedMoney() {
		// given
		final ImmutableList<DomainEvent> events = ImmutableList.of(ACCOUNT_CREATED, ISSUER_MONEY_TRANSFERRED,
				ISSUER_MONEY_TRANSFER_SUCCEEDED);

		// when
		final AccountAggregate aggregate = AccountEventStorage.recreate(events);

		// assert
		assertThat(aggregate.getFullName()).isEqualTo(ACCOUNT_CREATED.getFullName());
		assertThat(aggregate.getId()).isEqualTo(ACCOUNT_CREATED.getAggregateID());
		assertThat(aggregate.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(aggregate.getTransactions()).isEqualTo(ImmutableMap.of(ISSUER_MONEY_TRANSFERRED.getTransactionID(),
				MoneyTransaction.builder().setFromID(ISSUER_MONEY_TRANSFERRED.getFromID())
						.setToID(ISSUER_MONEY_TRANSFERRED.getToID())
						.setTransactionID(ISSUER_MONEY_TRANSFERRED.getTransactionID()).setState(State.SUCCEEDED)
						.setType(Type.OUTGOING).setValue(ISSUER_MONEY_TRANSFERRED.getValue().negate())
						.setCreatedAt(ISSUER_MONEY_TRANSFERRED.getCreatedAt())
						.setLastUpdatedAt(ISSUER_MONEY_TRANSFER_SUCCEEDED.getCreatedAt()).build()));
		assertThat(aggregate.getTransactionToReservedBalance()).isEmpty();
		assertThat(aggregate.getCreatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getLastUpdatedAt()).isEqualTo(ISSUER_MONEY_TRANSFER_SUCCEEDED.getCreatedAt());
		assertThat(aggregate.getDomainEvents()).isEqualTo(events);
	}

	@Test
	public void moneyTransferSucceededIssuer() {
		// given
		final ImmutableList<DomainEvent> events = ImmutableList.of(ACCOUNT_CREATED, ISSUER_MONEY_TRANSFERRED,
				ACCOUNT_DEBITED, ISSUER_MONEY_TRANSFER_SUCCEEDED);

		// when
		final AccountAggregate aggregate = AccountEventStorage.recreate(events);

		// assert
		assertThat(aggregate.getFullName()).isEqualTo(ACCOUNT_CREATED.getFullName());
		assertThat(aggregate.getId()).isEqualTo(ACCOUNT_CREATED.getAggregateID());
		assertThat(aggregate.getBalance()).isEqualTo(BigDecimal.valueOf(990).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(aggregate.getTransactions()).isEqualTo(ImmutableMap.of(ISSUER_MONEY_TRANSFERRED.getTransactionID(),
				MoneyTransaction.builder().setFromID(ISSUER_MONEY_TRANSFERRED.getFromID())
						.setToID(ISSUER_MONEY_TRANSFERRED.getToID())
						.setTransactionID(ISSUER_MONEY_TRANSFERRED.getTransactionID()).setState(State.SUCCEEDED)
						.setType(Type.OUTGOING).setValue(ISSUER_MONEY_TRANSFERRED.getValue().negate())
						.setCreatedAt(ISSUER_MONEY_TRANSFERRED.getCreatedAt())
						.setLastUpdatedAt(ISSUER_MONEY_TRANSFER_SUCCEEDED.getCreatedAt()).build()));
		assertThat(aggregate.getTransactionToReservedBalance()).isNotEmpty();
		assertThat(aggregate.getCreatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getLastUpdatedAt()).isEqualTo(ISSUER_MONEY_TRANSFER_SUCCEEDED.getCreatedAt());
		assertThat(aggregate.getDomainEvents()).isEqualTo(events);
	}

	@Test
	public void moneyTransferSucceededReceiver() {
		// given
		final ImmutableList<DomainEvent> events = ImmutableList.of(ACCOUNT_CREATED, RECEIVER_MONEY_TRANSFERRED,
				ACCOUNT_CREDITED, RECEIVER_MONEY_TRANSFER_SUCCEEDED);

		// when
		final AccountAggregate aggregate = AccountEventStorage.recreate(events);

		// assert
		assertThat(aggregate.getFullName()).isEqualTo(ACCOUNT_CREATED.getFullName());
		assertThat(aggregate.getId()).isEqualTo(ACCOUNT_CREATED.getAggregateID());
		assertThat(aggregate.getBalance()).isEqualTo(BigDecimal.valueOf(1010).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(aggregate.getTransactions()).isEqualTo(ImmutableMap.of(RECEIVER_MONEY_TRANSFERRED.getTransactionID(),
				MoneyTransaction.builder().setFromID(RECEIVER_MONEY_TRANSFERRED.getFromID())
						.setToID(RECEIVER_MONEY_TRANSFERRED.getToID())
						.setTransactionID(RECEIVER_MONEY_TRANSFERRED.getTransactionID()).setState(State.SUCCEEDED)
						.setType(Type.INCOMING).setValue(RECEIVER_MONEY_TRANSFERRED.getValue())
						.setCreatedAt(RECEIVER_MONEY_TRANSFERRED.getCreatedAt())
						.setLastUpdatedAt(RECEIVER_MONEY_TRANSFER_SUCCEEDED.getCreatedAt()).build()));
		assertThat(aggregate.getTransactionToReservedBalance()).isEmpty();
		assertThat(aggregate.getCreatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getLastUpdatedAt()).isEqualTo(RECEIVER_MONEY_TRANSFER_SUCCEEDED.getCreatedAt());
		assertThat(aggregate.getDomainEvents()).isEqualTo(events);
	}

	@Test
	public void moneyTransferCancelledEventIssuer() {
		// given
		final ImmutableList<DomainEvent> events = ImmutableList.of(ACCOUNT_CREATED, ISSUER_MONEY_TRANSFERRED,
				ISSUER_MONEY_TRANSFER_CANCELLED);

		// when
		final AccountAggregate aggregate = AccountEventStorage.recreate(events);

		// assert
		assertThat(aggregate.getFullName()).isEqualTo(ACCOUNT_CREATED.getFullName());
		assertThat(aggregate.getId()).isEqualTo(ACCOUNT_CREATED.getAggregateID());
		assertThat(aggregate.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(aggregate.getTransactions()).isEqualTo(ImmutableMap.of(ISSUER_MONEY_TRANSFERRED.getTransactionID(),
				MoneyTransaction.builder().setFromID(ISSUER_MONEY_TRANSFERRED.getFromID())
						.setToID(ISSUER_MONEY_TRANSFERRED.getToID())
						.setTransactionID(ISSUER_MONEY_TRANSFERRED.getTransactionID()).setState(State.CANCELLED)
						.setType(Type.OUTGOING).setValue(ISSUER_MONEY_TRANSFERRED.getValue().negate())
						.setCreatedAt(ISSUER_MONEY_TRANSFERRED.getCreatedAt())
						.setLastUpdatedAt(ISSUER_MONEY_TRANSFER_CANCELLED.getCreatedAt()).build()));
		assertThat(aggregate.getTransactionToReservedBalance()).isEmpty();
		assertThat(aggregate.getCreatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getLastUpdatedAt()).isEqualTo(ISSUER_MONEY_TRANSFER_CANCELLED.getCreatedAt());
		assertThat(aggregate.getDomainEvents()).isEqualTo(events);
	}

	@Test
	public void moneyTransferCancelledEventIssuerWhenAccountWasDebited() {
		// given
		final ImmutableList<DomainEvent> events = ImmutableList.of(ACCOUNT_CREATED, ISSUER_MONEY_TRANSFERRED,
				ACCOUNT_DEBITED, ISSUER_MONEY_TRANSFER_CANCELLED);

		// when
		final AccountAggregate aggregate = AccountEventStorage.recreate(events);

		// assert
		assertThat(aggregate.getFullName()).isEqualTo(ACCOUNT_CREATED.getFullName());
		assertThat(aggregate.getId()).isEqualTo(ACCOUNT_CREATED.getAggregateID());
		assertThat(aggregate.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(aggregate.getTransactions()).isEqualTo(ImmutableMap.of(ISSUER_MONEY_TRANSFERRED.getTransactionID(),
				MoneyTransaction.builder().setFromID(ISSUER_MONEY_TRANSFERRED.getFromID())
						.setToID(ISSUER_MONEY_TRANSFERRED.getToID())
						.setTransactionID(ISSUER_MONEY_TRANSFERRED.getTransactionID()).setState(State.CANCELLED)
						.setType(Type.OUTGOING).setValue(ISSUER_MONEY_TRANSFERRED.getValue().negate())
						.setCreatedAt(ISSUER_MONEY_TRANSFERRED.getCreatedAt())
						.setLastUpdatedAt(ISSUER_MONEY_TRANSFER_CANCELLED.getCreatedAt()).build()));
		assertThat(aggregate.getTransactionToReservedBalance()).isEmpty();
		assertThat(aggregate.getCreatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getLastUpdatedAt()).isEqualTo(ISSUER_MONEY_TRANSFER_CANCELLED.getCreatedAt());
		assertThat(aggregate.getDomainEvents()).isEqualTo(events);
	}

	@Test
	public void moneyTransferCancelledEventReceiver() {
		// given
		final ImmutableList<DomainEvent> events = ImmutableList.of(ACCOUNT_CREATED, RECEIVER_MONEY_TRANSFERRED,
				ACCOUNT_CREDITED, RECEIVER_MONEY_TRANSFER_CANCELLED);

		// when
		final AccountAggregate aggregate = AccountEventStorage.recreate(events);

		// assert
		assertThat(aggregate.getFullName()).isEqualTo(ACCOUNT_CREATED.getFullName());
		assertThat(aggregate.getId()).isEqualTo(ACCOUNT_CREATED.getAggregateID());
		assertThat(aggregate.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(aggregate.getTransactions()).isEqualTo(ImmutableMap.of(RECEIVER_MONEY_TRANSFERRED.getTransactionID(),
				MoneyTransaction.builder().setFromID(RECEIVER_MONEY_TRANSFERRED.getFromID())
						.setToID(RECEIVER_MONEY_TRANSFERRED.getToID())
						.setTransactionID(RECEIVER_MONEY_TRANSFERRED.getTransactionID()).setState(State.CANCELLED)
						.setType(Type.INCOMING).setValue(RECEIVER_MONEY_TRANSFERRED.getValue())
						.setCreatedAt(RECEIVER_MONEY_TRANSFERRED.getCreatedAt())
						.setLastUpdatedAt(RECEIVER_MONEY_TRANSFER_CANCELLED.getCreatedAt()).build()));
		assertThat(aggregate.getTransactionToReservedBalance()).isEmpty();
		assertThat(aggregate.getCreatedAt()).isEqualTo(ACCOUNT_CREATED.getCreatedAt());
		assertThat(aggregate.getLastUpdatedAt()).isEqualTo(RECEIVER_MONEY_TRANSFER_CANCELLED.getCreatedAt());
		assertThat(aggregate.getDomainEvents()).isEqualTo(events);
	}
}
