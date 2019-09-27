package com.al.mt.exceptions;

/**
 * InSufficientFundException happens when we want to withdraw money which exceed
 * the account balance.
 */
public class InsufficientBalanceException extends RuntimeException {
	public InsufficientBalanceException(final String message) {
		super(message);
	}
}
