package com.al.mt.controllers;

import static com.al.mt.MainApp.ACCOUNT_EVENT_STORAGE;
import static com.al.mt.utils.JsonUtils.toJson;
import static com.google.common.truth.Truth.assertThat;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

import java.math.BigDecimal;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.al.mt.model.APIResponse;
import com.al.mt.model.Link;
import com.al.mt.requests.CreateAccountRequest;
import com.al.mt.requests.TransferMoneyRequest;
import com.google.gson.Gson;

public class AccountControllerTransferMoneyTest extends AbstractControllerTest {
	private static final Gson GSON = new Gson();

	private static CloseableHttpResponse createAccount() throws Exception {
		final HttpPost request = new HttpPost(SERVER_URL + "/api/account");
		request.setEntity(new StringEntity(toJson(new CreateAccountRequest("Sam Willis"))));
		return client.execute(request);
	}

	private static String extractIDFromResponseAndClose(final CloseableHttpResponse response) throws Exception {
		return (String) GSON.fromJson(getResponseBodyAndClose(response), APIResponse.class).getData();
	}

	@Test
	public void transferMoneyValid() throws Exception {
		// given
		final String aggregateID1 = extractIDFromResponseAndClose(createAccount());
		final String aggregateID2 = extractIDFromResponseAndClose(createAccount());
		final HttpPost request = new HttpPost(SERVER_URL + "/api/account/transferMoney");
		request.setEntity(
				new StringEntity(toJson(new TransferMoneyRequest(aggregateID1, aggregateID2, BigDecimal.TEN))));

		// when
		final CloseableHttpResponse response = client.execute(request);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_OK);
		final String expectedResponse = new JSONObject().put("status", "OK").put("message", "Money will be transferred")
				.put("links", Link.getLinksForAccounts()).toString();
		assertResponses(expectedResponse, getResponseBodyAndClose(response));
		assertThat(ACCOUNT_EVENT_STORAGE.get(UUID.fromString(aggregateID1)).getBalance()
				.compareTo(BigDecimal.valueOf(990))).isEqualTo(0);
		assertThat(ACCOUNT_EVENT_STORAGE.get(UUID.fromString(aggregateID2)).getBalance()
				.compareTo(BigDecimal.valueOf(1010))).isEqualTo(0);
	}

	@Test
	public void transferMoneyNotValidNegativeValue() throws Exception {
		// given
		final String aggregateID1 = extractIDFromResponseAndClose(createAccount());
		final String aggregateID2 = extractIDFromResponseAndClose(createAccount());
		final HttpPost request = new HttpPost(SERVER_URL + "/api/account/transferMoney");
		request.setEntity(new StringEntity(
				toJson(new TransferMoneyRequest(aggregateID1, aggregateID2, BigDecimal.valueOf(-10)))));

		// when
		final CloseableHttpResponse response = client.execute(request);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_BAD_REQUEST);
		final String expectedResponse = new JSONObject().put("status", "ERROR")
				.put("message", "There are validation errors")
				.put("data",
						new JSONObject().put("value", new JSONArray().put("Must be provided & " + "be greater than 0")))
				.toString();
		assertResponses(expectedResponse, getResponseBodyAndClose(response));
	}

	@Test
	public void transferMoneyNotValidNoBody() throws Exception {
		// given
		final HttpPost request = new HttpPost(SERVER_URL + "/api/account/transferMoney");
		request.setEntity(new StringEntity(toJson(new TransferMoneyRequest())));

		// when
		final CloseableHttpResponse response = client.execute(request);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_BAD_REQUEST);
		final String expectedResponse = new JSONObject().put("status", "ERROR")
				.put("message", "There are validation errors")
				.put("data",
						new JSONObject().put("fromAccountNumber", new JSONArray().put("Is not a valid ID value"))
								.put("toAccountNumber", new JSONArray().put("Is not a valid ID value"))
								.put("value", new JSONArray().put("Must be provided & be greater than 0")))
				.toString();
		assertResponses(expectedResponse, getResponseBodyAndClose(response));
	}

	@Test
	public void transferMoneyNotValidSameAccountNumbers() throws Exception {
		// given
		final String aggregateID = extractIDFromResponseAndClose(createAccount());
		final HttpPost request = new HttpPost(SERVER_URL + "/api/account/transferMoney");
		request.setEntity(new StringEntity(toJson(new TransferMoneyRequest(aggregateID, aggregateID, BigDecimal.TEN))));

		// when
		final CloseableHttpResponse response = client.execute(request);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_BAD_REQUEST);
		final String expectedResponse = new JSONObject().put("status", "ERROR")
				.put("message", "There are validation errors")
				.put("data",
						new JSONObject().put("toAccountNumber",
								new JSONArray().put("Is not possible to transfer money to the same account")))
				.toString();
		assertResponses(expectedResponse, getResponseBodyAndClose(response));
	}

	@Test
	public void transferMoneyNotValidAccountNumbersAreNulls() throws Exception {
		// given
		final HttpPost request = new HttpPost(SERVER_URL + "/api/account/transferMoney");
		request.setEntity(new StringEntity(toJson(new TransferMoneyRequest(null, null, BigDecimal.TEN))));

		// when
		final CloseableHttpResponse response = client.execute(request);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_BAD_REQUEST);
		final String expectedResponse = new JSONObject().put("status", "ERROR")
				.put("message", "There are validation errors")
				.put("data", new JSONObject().put("fromAccountNumber", new JSONArray().put("Is not a valid ID value"))
						.put("toAccountNumber", new JSONArray().put("Is not a valid ID value")))
				.toString();
		assertResponses(expectedResponse, getResponseBodyAndClose(response));
	}
}
