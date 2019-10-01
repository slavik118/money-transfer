package com.al.mt.controllers;

import static com.al.mt.model.Link.getLinksForAccount;
import static com.al.mt.model.Link.getLinksForAccounts;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

import java.math.BigDecimal;
import java.util.UUID;

import com.al.mt.aggregates.AccountEventStorage;
import com.al.mt.enums.Status;
import com.al.mt.model.APIResponse;
import com.al.mt.model.Account;
import com.al.mt.model.Link;
import com.al.mt.requests.CreateAccountRequest;
import com.al.mt.requests.TransferMoneyRequest;
import com.al.mt.services.AccountService;
import com.al.mt.services.AccountServiceImpl;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;

import spark.Route;

/**
 * Account Controller.
 *
 * <p>
 * During request only basic field validation is done as endpoints are async.
 * Checking for if all requirements/dependencies are met is done later with
 * events.
 *
 * <p>
 * It's possible to:
 *
 * <ul>
 * <li>GET to fetch a list of accounts on `/api/account`
 * <li>GET to fetch a single account on `/api/account/ID`
 * <li>POST to create account on `/api/account`
 * <li>POST to transfer money on `/api/account/transferMoney`
 * </ul>
 */
public class AccountController {
	private static final String VALIDATION_ERROR_MESSAGE = "There are validation errors";
	private static final Gson GSON = new Gson();
	
	private final AccountService accountService;
	private final AccountEventStorage eventStorage;

	public AccountController(final AccountService accountService, final AccountEventStorage eventStorage) {
		this.accountService = accountService;
		this.eventStorage = eventStorage;
	}

	private static boolean isIDNotValid(final String value) {
		if (value == null || value.isEmpty()) {
			return true;
		}
		try {
			UUID.fromString(value);
			return false;
		} catch (final IllegalArgumentException e) {
			return true;
		}
	}

	private static ListMultimap<String, String> validationErrorsMap() {
		return MultimapBuilder.hashKeys().arrayListValues().build();
	}

	private static void validateString(final String fieldName, final String value,
			final ListMultimap<String, String> validationErrors) {
		if (value == null || value.isEmpty()) {
			validationErrors.put(fieldName, "Cannot be empty");
		}
	}

	private static void validateID(final String fieldName, final String value,
			final ListMultimap<String, String> validationErrors) {
		if (isIDNotValid(value)) {
			validationErrors.put(fieldName, "Is not a valid ID value");
		}
	}

	/**
	 * Handles GET requests on `/api/account/listAccounts`
	 *
	 * @return A list of {@link Account} of all registered accounts.
	 */
	public Route listAccounts() {
		return (request, response) -> 		
		APIResponse.builder()
		.setStatus(Status.OK)
		.setMessage("SUCCESS")
		.setData(this.eventStorage.findAll().stream()
				.map(Account::from)
				.collect(toImmutableList()))
		.setLinks(getLinksForAccounts())
		.build();
	}

	/**
	 * Handles GET requests on `/api/account/getAccount/accountID`
	 *
	 * @return {@link Account} for specified account.
	 */
	public Route getAccount() {
		return (request, response) -> {
			final ListMultimap<String, String> validationErrors = validationErrorsMap();

			validateID("id", request.params(":id"), validationErrors);

			if (!validationErrors.isEmpty()) {
				response.status(HTTP_BAD_REQUEST);
				return APIResponse.builder()
						.setStatus(Status.ERROR)
						.setMessage(VALIDATION_ERROR_MESSAGE)
						.setData(validationErrors.asMap())
						.build();
			}

			final UUID aggregateID = UUID.fromString(request.params(":id"));
			// Verifies if requested aggregate exists
			if (this.eventStorage.exists(aggregateID)) {
				return APIResponse.builder()
						.setStatus(Status.OK)
						.setMessage("SUCCESS")
						.setData(Account.from(this.eventStorage.get(aggregateID)))
						.setLinks(getLinksForAccount(aggregateID))
						.build();
			} else {
				response.status(HTTP_NOT_FOUND);
				return APIResponse.builder()
						.setStatus(Status.ERROR)
						.setMessage( String.format("Account with ID: %s was not found", aggregateID))
						.build();
			}
		};
	}

	/**
	 * Handles POST requests on `/api/account/createAccount`.
	 *
	 * <p>
	 * This endpoint issues {@link AccountServiceImpl#asyncCreateAccountCommand}.
	 *
	 * <p>
	 * {@link CreateAccountRequest} is used as a request DTO with {@code fullName}
	 * as a required field.
	 *
	 * @return ACK if command was issued properly, HTTP 400 in case of validation
	 *         errors.
	 */
	public Route createAccount() {
		return (request, response) -> {
			final CreateAccountRequest payload = GSON.fromJson(request.body(), CreateAccountRequest.class);
			final ListMultimap<String, String> validationErrors = validationErrorsMap();

			// Validates request
			validateString("fullName", payload.getFullName(), validationErrors);

			if (!validationErrors.isEmpty()) {
				response.status(HTTP_BAD_REQUEST);
				return APIResponse.builder()
						.setStatus(Status.ERROR)
						.setMessage(VALIDATION_ERROR_MESSAGE)
						.setData(validationErrors.asMap())
						.build();
			}

			// Issues CreateAccountCommand
			final UUID aggregateID = this.accountService.createAccount(payload.getFullName());
			response.status(HTTP_CREATED);
			return APIResponse.builder()
					.setStatus(Status.OK)
					.setMessage("Account will be created")
					.setData(aggregateID)
					.setLinks(Link.getLinksForAccount(aggregateID))
					.build();
		};
	}

	/**
	 * Handles POST requests on `/api/account/transferMoney`.
	 *
	 * <p>
	 * This endpoint issues {@link AccountServiceImpl#asyncTransferMoneyCommand}}.
	 *
	 * <p>
	 * {@link TransferMoneyRequest} is used as a request DTO with these fields:
	 *
	 * <ul>
	 * <li>{@code fromAccountNumber} - Valid ID of issuer aggregate
	 * <li>{@code toAccountNumber} - Valid ID of receiver aggregate
	 * <li>{@code value} - Valid, positive double which represent amount of money to
	 * transfer.
	 * </ul>
	 *
	 * @return ACK if command was issued properly, HTTP 404 when aggregate is not
	 *         found, HTTP 400 in case of validation errors.
	 */
	public Route transferMoney() {
		return ((request, response) -> {
			final TransferMoneyRequest payload = GSON.fromJson(request.body(), TransferMoneyRequest.class);
			final ListMultimap<String, String> validationErrors = validationErrorsMap();

			// Validates request
			validateID("fromAccountNumber", payload.getFromAccountNumber(), validationErrors);
			validateID("toAccountNumber", payload.getToAccountNumber(), validationErrors);

			if (payload.getFromAccountNumber() != null && payload.getToAccountNumber() != null
					&& payload.getFromAccountNumber().equals(payload.getToAccountNumber())) {
				validationErrors.put("toAccountNumber", "Is not possible to transfer money to the same account");
			}

			if (payload.getValue() == null || payload.getValue().compareTo(BigDecimal.ZERO) <= 0) {
				validationErrors.put("value", "Must be provided & be greater than 0");
			}

			if (!validationErrors.isEmpty()) {
				response.status(HTTP_BAD_REQUEST);
				return APIResponse.builder()
						.setStatus(Status.ERROR)
						.setMessage(VALIDATION_ERROR_MESSAGE)
						.setData(validationErrors.asMap())
						.build();
			}

			// Validates existence
			final UUID fromID = UUID.fromString(payload.getFromAccountNumber());
			final UUID toID = UUID.fromString(payload.getToAccountNumber());
			if (!this.eventStorage.exists(fromID)) {
				response.status(HTTP_NOT_FOUND);
				return APIResponse.builder()
						.setStatus(Status.ERROR)
						.setMessage(String.format("Account with ID: %s doesn't exist", fromID))
						.build();
			}

			if (!this.eventStorage.exists(toID)) {
				response.status(HTTP_NOT_FOUND);
				return APIResponse.builder()
						.setStatus(Status.ERROR)
						.setMessage(String.format("Account with ID: %s doesn't exist", toID))
						.build();
			}

			// Issues money transfer
			this.accountService.transferMoney(fromID, toID, payload.getValue());
			response.status(HTTP_OK);
			return APIResponse.builder()
					.setStatus(Status.OK)
					.setMessage("Money will be transferred")
					.setLinks(getLinksForAccounts())
					.build();
		});
	}
}
