package com.al.mt.aggregates;

import com.al.mt.aggregates.AccountAggregate;
import com.al.mt.aggregates.AccountEventStorage;
import com.al.mt.events.AccountCreatedEvent;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;

public class AccountEventStorageTest {

	@Test
	public void loadAll() {
		// given
		final AccountCreatedEvent event1 = new AccountCreatedEvent(UUID.randomUUID(), "Sam Willis");
		final AccountCreatedEvent event2 = new AccountCreatedEvent(UUID.randomUUID(), "John Black");
		final AccountEventStorage storage = new AccountEventStorage();
		storage.save(event1);
		storage.save(event2);

		// when
		final ImmutableList<AccountAggregate> aggregates = storage.findAll();
		// assert
		assertThat(aggregates).containsExactly(AccountEventStorage.recreate(ImmutableList.of(event1)),
				AccountEventStorage.recreate(ImmutableList.of(event2)));
	}

	@Test
	public void loadByIDExistingAggregate() {
		// given
		final AccountCreatedEvent event = new AccountCreatedEvent(UUID.randomUUID(), "Sam Willis");
		final AccountEventStorage storage = new AccountEventStorage();
		storage.save(event);

		// when
		final AccountAggregate aggregate = storage.get(event.getAggregateID());

		// assert
		assertThat(aggregate).isEqualTo(AccountEventStorage.recreate(ImmutableList.of(event)));
	}

	@Test
	public void loadByUUIDNotExistingAggregate() {
		// given
		final AccountEventStorage storage = new AccountEventStorage();

		// when & assert
		assertThat(storage.get(UUID.randomUUID())).isNull();
	}

	@Test
	public void existsExistingAggregate() {
		// given
		final AccountCreatedEvent event = new AccountCreatedEvent(UUID.randomUUID(), "Sam Willis");
		final AccountEventStorage storage = new AccountEventStorage();
		storage.save(event);

		// when & assert
		assertThat(storage.exists(event.getAggregateID())).isTrue();
	}

	@Test
	public void existsNotExistingAggregate() {
		// given
		final AccountEventStorage storage = new AccountEventStorage();

		// when & assert
		assertThat(storage.exists(UUID.randomUUID())).isFalse();
	}
}
