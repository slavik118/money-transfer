package com.al.mt.aggregates;

import static io.vavr.collection.List.ofAll;

import com.google.common.collect.ImmutableList;
import com.al.mt.events.DomainEvent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory storage for events that make {@link AccountAggregate}.
 */
public class AccountEventStorage {
	private final Map<UUID, List<DomainEvent>> events = new ConcurrentHashMap<>();

	public static AccountAggregate recreate(final Collection<DomainEvent> events) {
		return ofAll(events).foldLeft(new AccountAggregate(events), (AccountAggregate::apply));
	}

	public ImmutableList<AccountAggregate> findAll() {
		return this.events.values().stream().map(AccountEventStorage::recreate)
				.collect(ImmutableList.toImmutableList());
	}

	public AccountAggregate get(final UUID id) {
		if (this.events.containsKey(id)) {
			return recreate(this.events.get(id));
		}
		return null;
	}

	public boolean exists(final UUID id) {
		return this.events.containsKey(id);
	}

	public void save(final DomainEvent domainEvent) {
		this.events.compute(domainEvent.getAggregateID(),
				(id, ev) -> (ev == null) ? ImmutableList.of(domainEvent)
						: new ImmutableList.Builder<DomainEvent>().addAll(ev).add(domainEvent).build());
	}
}
