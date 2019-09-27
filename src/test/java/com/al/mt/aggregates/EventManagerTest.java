package com.al.mt.aggregates;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.al.mt.enums.Reason;
import com.al.mt.events.AccountCreatedEvent;
import com.al.mt.events.AccountCreditedEvent;
import com.al.mt.events.AccountDebitedEvent;
import com.al.mt.events.MoneyTransferCancelled;
import com.al.mt.events.MoneyTransferSucceeded;
import com.al.mt.events.MoneyTransferredEvent;
import com.al.mt.exceptions.AggregateDoesNotExistException;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EventManagerTest {
	private static final UUID FROM_ID = UUID.randomUUID();
	private static final UUID TO_ID = UUID.randomUUID();
	private static final AccountCreatedEvent ACCOUNT_CREATED = new AccountCreatedEvent(UUID.randomUUID(), "Sam Willis");
	private static final MoneyTransferredEvent ISSUER_MONEY_TRANSFERRED = new MoneyTransferredEvent(FROM_ID, FROM_ID,
			TO_ID, UUID.randomUUID(), BigDecimal.TEN);
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
	private static final MoneyTransferCancelled MONEY_TRANSFER_CANCELLED = new MoneyTransferCancelled(
			ISSUER_MONEY_TRANSFERRED.getAggregateID(), ISSUER_MONEY_TRANSFERRED.getFromID(),
			ISSUER_MONEY_TRANSFERRED.getToID(), ISSUER_MONEY_TRANSFERRED.getTransactionID(),
			ISSUER_MONEY_TRANSFERRED.getValue(), Reason.BALANCE_TOO_LOW);
	private static final MoneyTransferSucceeded ISSUER_MONEY_TRANSFER_SUCCEEDED = new MoneyTransferSucceeded(
			ISSUER_MONEY_TRANSFERRED.getAggregateID(), ISSUER_MONEY_TRANSFERRED.getFromID(),
			ISSUER_MONEY_TRANSFERRED.getToID(), ISSUER_MONEY_TRANSFERRED.getTransactionID(),
			ISSUER_MONEY_TRANSFERRED.getValue());
	@Mock
	private EventBus eventBus;

	@Mock
	private AccountEventStorage accountEventStorage;

	@InjectMocks
	private EventManager eventManager;

	@Test
	public void accountCreatedEvent() {
		// when
		this.eventManager.handle(ACCOUNT_CREATED);

		// assert
		verify(this.accountEventStorage).save(ACCOUNT_CREATED);
	}

	@Test
	public void moneyTransferredEventAggregateDoesNotExist() {
		// given
		when(this.accountEventStorage.exists(any())).thenReturn(false);

		// when
		assertThrows(AggregateDoesNotExistException.class, () -> this.eventManager.handle(ISSUER_MONEY_TRANSFERRED));

		// assert
		verify(this.accountEventStorage).exists(ISSUER_MONEY_TRANSFERRED.getAggregateID());
		verifyNoMoreInteractions(this.accountEventStorage);
		verifyZeroInteractions(this.eventBus);
	}

	@Test
	public void accountDebitedEventAggregateExists() {
		// given
		when(this.accountEventStorage.exists(any())).thenReturn(true);
		when(this.accountEventStorage.get(ACCOUNT_DEBITED.getAggregateID()))
				.thenReturn(AccountEventStorage.recreate(ImmutableList.of(ACCOUNT_CREATED, ISSUER_MONEY_TRANSFERRED)));

		// when
		this.eventManager.handle(ACCOUNT_DEBITED);

		// assert
		verify(this.accountEventStorage).save(ACCOUNT_DEBITED);
		verify(this.accountEventStorage).save(RECEIVER_MONEY_TRANSFERRED);
		verify(this.eventBus).post(ACCOUNT_CREDITED);
	}

	@Test
	public void accountDebitedEventAggregateDoesNotExist() {
		// given
		when(this.accountEventStorage.exists(any())).thenReturn(false);
		when(this.accountEventStorage.get(ACCOUNT_DEBITED.getAggregateID())).thenReturn(null);

		// when
		assertThrows(AggregateDoesNotExistException.class, () -> this.eventManager.handle(ACCOUNT_DEBITED));

		// assert
		verify(this.accountEventStorage).get(ACCOUNT_DEBITED.getAggregateID());
		verifyNoMoreInteractions(this.accountEventStorage);
		verifyZeroInteractions(this.eventBus);
	}

	@Test
	public void accountDebitedEventAggregateExistsNotEnoughMoney() {
		// given
		final AccountDebitedEvent accountDebited = ACCOUNT_DEBITED.toBuilder().setValue(BigDecimal.valueOf(2000))
				.build();
		final MoneyTransferCancelled moneyTransferCancelled = MONEY_TRANSFER_CANCELLED.toBuilder()
				.setValue(accountDebited.getValue()).build();
		when(this.accountEventStorage.exists(any())).thenReturn(true);
		when(this.accountEventStorage.get(ACCOUNT_DEBITED.getAggregateID()))
				.thenReturn(AccountEventStorage.recreate(ImmutableList.of(ACCOUNT_CREATED, ISSUER_MONEY_TRANSFERRED)));

		// when
		this.eventManager.handle(accountDebited);

		// assert
		verify(this.accountEventStorage).get(ACCOUNT_DEBITED.getAggregateID());
		verify(this.eventBus).post(moneyTransferCancelled);
		verifyNoMoreInteractions(this.accountEventStorage);
		verifyNoMoreInteractions(this.eventBus);
	}

	@Test
	public void moneyTransferCancelledEventAggregateExists() {
		// given
		when(this.accountEventStorage.exists(any())).thenReturn(true);

		// when
		this.eventManager.handle(MONEY_TRANSFER_CANCELLED);

		// assert
		verify(this.accountEventStorage).save(MONEY_TRANSFER_CANCELLED);
	}

	@Test
	public void moneyTransferCancelledEventAggregateDoesNotExist() {
		// given
		when(this.accountEventStorage.exists(any())).thenReturn(false);

		// when
		assertThrows(AggregateDoesNotExistException.class, () -> this.eventManager.handle(MONEY_TRANSFER_CANCELLED));

		// assert
		verify(this.accountEventStorage).exists(MONEY_TRANSFER_CANCELLED.getAggregateID());
		verifyNoMoreInteractions(this.accountEventStorage);
	}

	@Test
	public void moneyTransferSucceededEventAggregateExists() {
		// given
		when(this.accountEventStorage.exists(any())).thenReturn(true);

		// when
		this.eventManager.handle(ISSUER_MONEY_TRANSFER_SUCCEEDED);

		// assert
		verify(this.accountEventStorage).save(ISSUER_MONEY_TRANSFER_SUCCEEDED);
	}

	@Test
	public void moneyTransferSucceededEventAggregateDoesNotExist() {
		// given
		when(accountEventStorage.exists(any())).thenReturn(false);

		// when
		assertThrows(AggregateDoesNotExistException.class, () -> eventManager.handle(ISSUER_MONEY_TRANSFER_SUCCEEDED));

		// assert
		verify(accountEventStorage).exists(ISSUER_MONEY_TRANSFER_SUCCEEDED.getAggregateID());
		verifyNoMoreInteractions(accountEventStorage);
	}
}
