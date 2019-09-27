package com.al.mt.services;

import java.math.BigDecimal;
import java.util.UUID;

import com.al.mt.enums.Reason;

public interface AccountService {

	UUID createAccount(final String fullName);

	void transferMoney(final UUID fromID, final UUID toID, final BigDecimal value);

	void cancelTransaction(final UUID aggregateID, final UUID fromID, final UUID toID, final UUID transactionID,
			final BigDecimal value, final Reason reason);

}
