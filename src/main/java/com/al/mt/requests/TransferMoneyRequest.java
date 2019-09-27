package com.al.mt.requests;

import java.math.BigDecimal;

public class TransferMoneyRequest {
	private String fromAccountNumber;
	private String toAccountNumber;
	private BigDecimal value;

	public TransferMoneyRequest() {
	}

	public TransferMoneyRequest(final String fromAccountNumber, final String toAccountNumber, final BigDecimal value) {
		this.fromAccountNumber = fromAccountNumber;
		this.toAccountNumber = toAccountNumber;
		this.value = value;
	}

	public final String getFromAccountNumber() {
		return this.fromAccountNumber;
	}

	public void setFromAccountNumber(final String fromAccountNumber) {
		this.fromAccountNumber = fromAccountNumber;
	}

	public final String getToAccountNumber() {
		return this.toAccountNumber;
	}

	public final void setToAccountNumber(final String toAccountNumber) {
		this.toAccountNumber = toAccountNumber;
	}

	public final BigDecimal getValue() {
		return this.value;
	}

	public final void setValue(final BigDecimal value) {
		this.value = value;
	}

}
