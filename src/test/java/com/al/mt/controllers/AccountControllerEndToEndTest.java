package com.al.mt.controllers;

import static com.al.mt.utils.Constants.FIRST_ACCOUT_FULL_NAME;
import static com.al.mt.utils.Constants.SECOND_ACCOUT_FULL_NAME;
import static com.al.mt.utils.Constants.SERVER_URL;
import static com.al.mt.utils.JsonUtils.toJson;
import static com.google.common.truth.Truth.assertThat;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

import java.math.BigDecimal;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.al.mt.AbstractBaseTest;
import com.al.mt.enums.Status;
import com.al.mt.model.Link;
import com.al.mt.requests.CreateAccountRequest;
import com.al.mt.requests.TransferMoneyRequest;
import com.google.gson.JsonObject;

public class AccountControllerEndToEndTest extends AbstractBaseTest {

	private static String createAndAssertAccount(final String fullName) throws Exception {
		// given
		final HttpPost createAccountRequest = createAccountRequest(fullName);

		// when
		final CloseableHttpResponse response = client.execute(createAccountRequest);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_CREATED);
		final String createAccountJson = getResponseBodyAndClose(response);
		final String aggregateID = GSON.fromJson(createAccountJson, JsonObject.class)
				.get("data")
				.getAsString();
		assertCreateAccountResponse(createAccountJson, aggregateID);
		return aggregateID;
	}

	private static HttpPost createAccountRequest(final String fullName) throws Exception {
		final HttpPost createAccountRequest = new HttpPost(String.format("%s/api/account", SERVER_URL));
		createAccountRequest.setEntity(new StringEntity(toJson(new CreateAccountRequest(fullName))));
		return createAccountRequest;
	}

	private static void assertCreateAccountResponse(final String createAccountJson, final String aggregateID) {
		final String expectedResponse = new JSONObject()
				.put("status", Status.OK)
				.put("message", "Account will be created")
				.put("data", aggregateID)
				.put("links", Link.getLinksForAccount(aggregateID))
				.toString();
		assertResponses(expectedResponse, createAccountJson);
	}

	private static String getAccount(final String aggregateID) throws Exception {
		// given
		final HttpGet getAccountRequest = new HttpGet(String.format("%s/api/account/%s", SERVER_URL, aggregateID));

		// when
		final CloseableHttpResponse response = client.execute(getAccountRequest);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_OK);
		return getResponseBodyAndClose(response);
	}

	@Test
	public void testGetAccountEndpoint() throws Exception {
		// given
		final String aggregateID = createAndAssertAccount(FIRST_ACCOUT_FULL_NAME);

		// when
		final String getAccountJson = getAccount(aggregateID);

		// assert
		final String createdAt = getFieldFromEvents(getAccountJson, 0, "createdAt");
		final String expectedResponse = new JSONObject().put("status", Status.OK)
				.put("message", "SUCCESS")
				.put("links", Link.getLinksForAccount(aggregateID))
				.put("data",
						new JSONObject().put("fullName", FIRST_ACCOUT_FULL_NAME).put("accountNumber", aggregateID)
								.put("balance", 1000.0).put("transactionToReservedBalance", new JSONObject())
								.put("events",
										new JSONArray().put(new JSONObject().put("fullName", FIRST_ACCOUT_FULL_NAME)
												.put("eventType", "ACCOUNT_CREATED_EVENT")
												.put("aggregateID", aggregateID).put("createdAt", createdAt)))
								.put("createdAt", createdAt).put("lastUpdatedAt", createdAt)
								.put("links", Link.getLinksForAccount(aggregateID))
								.put("transactions", new JSONObject()))
				.toString();
		assertResponses(expectedResponse, getAccountJson);
	}

	@Test
	public void testMoneyTransferEndpointWhenIssuerHasEnoughMoney() throws Exception {
		// given
		final String aggregateID1 = createAndAssertAccount(FIRST_ACCOUT_FULL_NAME);
		final String aggregateID2 = createAndAssertAccount(SECOND_ACCOUT_FULL_NAME);
		transferMoney(aggregateID1, aggregateID2, 25.01);

		// when
		final String getAccountJson1 = getAccount(aggregateID1);
		// assert
		final String createdAt1 = getFieldFromEvents(getAccountJson1, 0, "createdAt");
		final String lastUpdatedAt1 = getFieldFromEvents(getAccountJson1, 3, "createdAt");
		final String expectedResponse1 = new JSONObject().put("status", Status.OK)
				.put("message", "SUCCESS")
				.put("links", Link.getLinksForAccount(aggregateID1))
				.put("data",
						new JSONObject().put("fullName", FIRST_ACCOUT_FULL_NAME)
						.put("accountNumber", aggregateID1)
								.put("balance", 974.99)
								.put("transactionToReservedBalance", new JSONObject()
									.put(getFieldFromEvents(getAccountJson1, 1, "transactionID"), -25.01))
								.put("links", Link.getLinksForAccount(aggregateID1))
								.put("events", new JSONArray().put(new JSONObject()
										.put("eventType", "ACCOUNT_CREATED_EVENT")
										.put("fullName", FIRST_ACCOUT_FULL_NAME)
										.put("aggregateID", aggregateID1)
										.put("createdAt", createdAt1))
										.put(new JSONObject()
												.put("eventType", "MONEY_TRANSFERRED_EVENT")
												.put("transactionID",
														getFieldFromEvents(getAccountJson1, 1, "transactionID"))
												.put("fromID", aggregateID1)
												.put("toID", aggregateID2)
												.put("value", 25.01)
												.put("aggregateID", aggregateID1)
												.put("createdAt", getFieldFromEvents(getAccountJson1, 1, "createdAt")))
										.put(new JSONObject()
												.put("eventType", "ACCOUNT_DEBITED_EVENT")
												.put("transactionID",
														getFieldFromEvents(getAccountJson1, 2, "transactionID"))
												.put("fromID", aggregateID1)
												.put("toID", aggregateID2)
												.put("value", 25.01)
												.put("aggregateID", aggregateID1)
												.put("createdAt", getFieldFromEvents(getAccountJson1, 2, "createdAt")))
										.put(new JSONObject().put("eventType", "MONEY_TRANSFER_SUCCEEDED")
												.put("transactionID",
														getFieldFromEvents(getAccountJson1, 3, "transactionID"))
												.put("fromID", aggregateID1)
												.put("toID", aggregateID2)
												.put("value", 25.01)
												.put("aggregateID", aggregateID1)
												.put("createdAt", getFieldFromEvents(getAccountJson1, 3, "createdAt"))))
								.put("createdAt", createdAt1)
								.put("lastUpdatedAt", lastUpdatedAt1)
								.put("transactions",
										new JSONObject().put(getFieldFromEvents(getAccountJson1, 1, "transactionID"),
												new JSONObject()
														.put("transactionID",
																getFieldFromEvents(getAccountJson1, 1,
																		"transactionID"))
														.put("fromID", aggregateID1)
														.put("toID", aggregateID2)
														.put("value", -25.01)
														.put("state", "SUCCEEDED")
														.put("type", "OUTGOING")
														.put("createdAt",
																getFieldFromEvents(getAccountJson1, 1, "createdAt"))
														.put("lastUpdatedAt",
																getFieldFromEvents(getAccountJson1, 3, "createdAt")))))
				.toString();
		assertResponses(expectedResponse1, getAccountJson1);

		// when
		final String getAccountJson2 = getAccount(aggregateID2);

		// assert
		final String createdAt2 = getFieldFromEvents(getAccountJson2, 0, "createdAt");
		final String lastUpdatedAt2 = getFieldFromEvents(getAccountJson2, 3, "createdAt");
		final String expectedResponse2 = new JSONObject()
				.put("status", Status.OK)
				.put("message", "SUCCESS")
				.put("links", Link.getLinksForAccount(aggregateID2))
				.put("data",
						new JSONObject().put("fullName", SECOND_ACCOUT_FULL_NAME)
						.put("accountNumber", aggregateID2)
								.put("balance", 1025.01)
								.put("transactionToReservedBalance", new JSONObject())
								.put("events", new JSONArray()
										.put(new JSONObject()
										.put("eventType", "ACCOUNT_CREATED_EVENT")
										.put("fullName", SECOND_ACCOUT_FULL_NAME)
										.put("aggregateID", aggregateID2)
										.put("createdAt", createdAt2))
										.put(new JSONObject()
												.put("eventType", "MONEY_TRANSFERRED_EVENT")
												.put("transactionID",
														getFieldFromEvents(getAccountJson2, 1, "transactionID"))
												.put("fromID", aggregateID1)
												.put("toID", aggregateID2)
												.put("value", 25.01)
												.put("aggregateID", aggregateID2)
												.put("createdAt", getFieldFromEvents(getAccountJson2, 1, "createdAt")))
										.put(new JSONObject()
												.put("eventType", "ACCOUNT_CREDITED_EVENT")
												.put("transactionID",
														getFieldFromEvents(getAccountJson2, 2, "transactionID"))
												.put("fromID", aggregateID1)
												.put("toID", aggregateID2)
												.put("value", 25.01)
												.put("aggregateID", aggregateID2)
												.put("createdAt", getFieldFromEvents(getAccountJson2, 2, "createdAt")))
										.put(new JSONObject().put("eventType", "MONEY_TRANSFER_SUCCEEDED")
												.put("transactionID",
														getFieldFromEvents(getAccountJson2, 3, "transactionID"))
												.put("fromID", aggregateID1)
												.put("toID", aggregateID2)
												.put("value", 25.01)
												.put("aggregateID", aggregateID2)
												.put("createdAt", getFieldFromEvents(getAccountJson2, 3, "createdAt"))))
								.put("createdAt", createdAt2)
								.put("lastUpdatedAt", lastUpdatedAt2)
								.put("links", Link.getLinksForAccount(aggregateID2))
								.put("transactions",
										new JSONObject().put(getFieldFromEvents(getAccountJson2, 1, "transactionID"),
												new JSONObject()
														.put("transactionID",
																getFieldFromEvents(getAccountJson2, 1,
																		"transactionID"))
														.put("fromID", aggregateID1)
														.put("toID", aggregateID2)
														.put("value", 25.01)
														.put("state", "SUCCEEDED")
														.put("type", "INCOMING")
														.put("createdAt",
																getFieldFromEvents(getAccountJson2, 1, "createdAt"))
														.put("lastUpdatedAt",
																getFieldFromEvents(getAccountJson2, 3, "createdAt")))))
				.toString();
		assertResponses(expectedResponse2, getAccountJson2);
	}

	@Test
	 public void testMoneyTransferEndpointWhenIssuerDoesNotHaveEnoughMoney() throws Exception {
		    // given
		    final String aggregateID1 = createAndAssertAccount(FIRST_ACCOUT_FULL_NAME);
		    final String aggregateID2 = createAndAssertAccount(SECOND_ACCOUT_FULL_NAME);
		    transferMoney(aggregateID1, aggregateID2, 2600.01);

		    // when
		    final String getAccountJson1 = getAccount(aggregateID1);

		    // assert
		    final String createdAt1 = getFieldFromEvents(getAccountJson1, 0, "createdAt");
		    final String lastUpdatedAt1 = getFieldFromEvents(getAccountJson1, 2, "createdAt");
		    final String expectedResponse1 =
		        new JSONObject()
		            .put("status", Status.OK)
		            .put("message", "SUCCESS")
		            .put("links", Link.getLinksForAccount(aggregateID1))
		            .put(
		                "data",
		                new JSONObject()
		                    .put("fullName", FIRST_ACCOUT_FULL_NAME)
		                    .put("accountNumber", aggregateID1)
		                    .put("balance", 1000)
		                    .put("transactionToReservedBalance", new JSONObject())
		                    .put("links", Link.getLinksForAccount(aggregateID1))
		                    .put(
		                        "events",
		                        new JSONArray()
		                            .put(
		                                new JSONObject()
		                                    .put("eventType", "ACCOUNT_CREATED_EVENT")
		                                    .put("fullName", FIRST_ACCOUT_FULL_NAME)
		                                    .put("aggregateID", aggregateID1)
		                                    .put("createdAt", createdAt1))
		                            .put(
		                                new JSONObject()
		                                    .put("eventType", "MONEY_TRANSFERRED_EVENT")
		                                    .put(
		                                        "transactionID",
		                                        getFieldFromEvents(getAccountJson1, 1, "transactionID"))
		                                    .put("fromID", aggregateID1)
		                                    .put("toID", aggregateID2)
		                                    .put("value", 2600.01)
		                                    .put("aggregateID", aggregateID1)
		                                    .put(
		                                        "createdAt",
		                                        getFieldFromEvents(getAccountJson1, 1, "createdAt")))
		                            .put(
		                                new JSONObject()
		                                    .put("eventType", "MONEY_TRANSFER_CANCELLED")
		                                    .put(
		                                        "transactionID",
		                                        getFieldFromEvents(getAccountJson1, 2, "transactionID"))
		                                    .put("fromID", aggregateID1)
		                                    .put("toID", aggregateID2)
		                                    .put("value", 2600.01)
		                                    .put("reason", "BALANCE_TOO_LOW")
		                                    .put("aggregateID", aggregateID1)
		                                    .put(
		                                        "createdAt",
		                                        getFieldFromEvents(getAccountJson1, 2, "createdAt"))))
		                    .put("createdAt", createdAt1)
		                    .put("lastUpdatedAt", lastUpdatedAt1)
		                    .put(
		                        "transactions",
		                        new JSONObject()
		                            .put(
		                                getFieldFromEvents(getAccountJson1, 1, "transactionID"),
		                                new JSONObject()
		                                    .put(
		                                        "transactionID",
		                                        getFieldFromEvents(getAccountJson1, 1, "transactionID"))
		                                    .put("fromID", aggregateID1)
		                                    .put("toID", aggregateID2)
		                                    .put("value", -2600.01)
		                                    .put("state", "CANCELLED")
		                                    .put("type", "OUTGOING")
		                                    .put(
		                                        "createdAt",
		                                        getFieldFromEvents(getAccountJson1, 1, "createdAt"))
		                                    .put(
		                                        "lastUpdatedAt",
		                                        getFieldFromEvents(getAccountJson1, 2, "createdAt")))))
		            .toString();
		    assertResponses(expectedResponse1, getAccountJson1);

		    // when
		    final String getAccountJson2 = getAccount(aggregateID2);

		    // assert
		    final String createdAt2 = getFieldFromEvents(getAccountJson2, 0, "createdAt");
		    final String expectedResponse2 =
		        new JSONObject()
		            .put("status", Status.OK)
		            .put("message", "SUCCESS")
		            .put("links", Link.getLinksForAccount(aggregateID2))
		            .put(
		                "data",
		                new JSONObject()
		                    .put("links", Link.getLinksForAccount(aggregateID2))
		                    .put("fullName", SECOND_ACCOUT_FULL_NAME)
		                    .put("accountNumber", aggregateID2)
		                    .put("balance", 1000.0)
		                    .put("transactionToReservedBalance", new JSONObject())
		                    .put(
		                        "events",
		                        new JSONArray()
		                            .put(
		                                new JSONObject()
		                                    .put("fullName", SECOND_ACCOUT_FULL_NAME)
		                                    .put("eventType", "ACCOUNT_CREATED_EVENT")
		                                    .put("aggregateID", aggregateID2)
		                                    .put("createdAt", createdAt2)))
		                    .put("createdAt", createdAt2)
		                    .put("lastUpdatedAt", createdAt2)
		                    .put("transactions", new JSONObject()))
		            .toString();
		    assertResponses(expectedResponse2, getAccountJson2);
		  }

	 private void transferMoney(final String aggregateID1, final String aggregateID2, final double value)
		      throws Exception {
		    final HttpPost transferMoneyRequest = new HttpPost(String.format("%s/api/account/transferMoney", SERVER_URL));
		    transferMoneyRequest.setEntity(
		        new StringEntity(
		            toJson(
		                TransferMoneyRequest.builder()
		                    .setFomAccountNumber(aggregateID1)
		                    .setToAccountNumber(aggregateID2)
		                    .setValue(BigDecimal.valueOf(value))
		                    .build())));

		    // when
		    final CloseableHttpResponse transferMoneyResponse = client.execute(transferMoneyRequest);

		    // assert
		    final String transferMoneyExpectedResponse =
		        new JSONObject()
		            .put("status", Status.OK)
		            .put("message", "Money will be transferred")
		            .put("links", Link.getLinksForAccounts())
		            .toString();
		    assertResponses(transferMoneyExpectedResponse, getResponseBodyAndClose(transferMoneyResponse));
		  }

}
