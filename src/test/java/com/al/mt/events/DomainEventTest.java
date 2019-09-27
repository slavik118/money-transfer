package com.al.mt.events;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class DomainEventTest {

  @Test
  public void testEventTypeConversion() {
    assertThat(new DummyEvent().getEventType()).isEqualTo("DUMMY_EVENT");
  }

  private static final class DummyEvent extends DomainEvent {}
}
