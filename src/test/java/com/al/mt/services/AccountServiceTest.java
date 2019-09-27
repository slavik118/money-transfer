package com.al.mt.services;

import com.al.mt.enums.Reason;
import com.al.mt.events.MoneyTransferCancelled;
import com.al.mt.services.AccountServiceImpl;
import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AccountServiceTest {

  @Mock 
  private EventBus eventBus;

  @InjectMocks 
  private AccountServiceImpl service;

  @Test
  public void cancelMoneyTransfer() {
    // given
    final UUID aggregateID = UUID.randomUUID();
    final UUID fromID = UUID.randomUUID();
    final UUID toID = UUID.randomUUID();
    final UUID transactionID = UUID.randomUUID();
    final BigDecimal value = BigDecimal.TEN;
    final Reason reason = Reason.INTERNAL_SERVER_ERROR;

    // when
    this.service.cancelTransaction(
        aggregateID, fromID, toID, transactionID, value, reason);

    // assert
    verify(this.eventBus)
        .post(
            new MoneyTransferCancelled(
                aggregateID, fromID, toID, transactionID, value, reason));
  }
}
