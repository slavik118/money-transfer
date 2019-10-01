package com.al.mt.requests;

import java.math.BigDecimal;

public class TransferMoneyRequest {
	private String fromAccountNumber;
	private String toAccountNumber;
	private BigDecimal value;

	private TransferMoneyRequest() {
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

	public static Builder builder() {
		return new TransferMoneyRequest().new Builder();
	}

	public class Builder {

		private Builder() {
		}

		public final Builder setFomAccountNumber(final String fromAccountNumber) {
			TransferMoneyRequest.this.fromAccountNumber = fromAccountNumber;

			return this;
		}

		public final Builder setToAccountNumber(final String toAccountNumber) {
			TransferMoneyRequest.this.toAccountNumber = toAccountNumber;

			return this;
		}

		public final Builder setValue(final BigDecimal value) {
			TransferMoneyRequest.this.value = value;

			return this;
		}

		public TransferMoneyRequest build() {
			return TransferMoneyRequest.this;
		}
	}

}
