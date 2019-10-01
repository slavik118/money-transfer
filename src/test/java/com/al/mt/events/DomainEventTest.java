package com.al.mt.events;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.al.mt.utils.Constants.DUMMY_EVENT;

public class DomainEventTest {

	private static final class DummyEvent extends DomainEvent {
	}

	@Test
	public void testEventTypeConversion() {
		assertThat(new DummyEvent().getEventType()).isEqualTo(DUMMY_EVENT);
	}

}
