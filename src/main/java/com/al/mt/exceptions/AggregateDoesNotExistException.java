package com.al.mt.exceptions;

/**
 * InSufficientFundException happens when we want to withdraw money which exceed
 * the account balance.
 */
public class AggregateDoesNotExistException extends RuntimeException {
	public AggregateDoesNotExistException(final String message) {
		super(message);
	}
}
