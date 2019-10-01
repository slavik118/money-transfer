package com.al.mt.controllers;

import static com.al.mt.MainApp.ACCOUNT_EVENT_STORAGE;
import static com.al.mt.utils.Constants.FIRST_ACCOUT_FULL_NAME;
import static com.al.mt.utils.Constants.SERVER_URL;
import static com.al.mt.utils.JsonUtils.toJson;
import static com.google.common.truth.Truth.assertThat;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

import java.math.BigDecimal;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.al.mt.AbstractBaseTest;
import com.al.mt.enums.Status;
import com.al.mt.model.APIResponse;
import com.al.mt.model.Link;
import com.al.mt.requests.CreateAccountRequest;
import com.al.mt.requests.TransferMoneyRequest;
import com.google.gson.Gson;

public class AccountControllerTransferMoneyTest extends AbstractBaseTest {
	private static final Gson GSON = new Gson();

	private static CloseableHttpResponse createAccount() throws Exception {
		final HttpPost request = new HttpPost(String.format("%s/api/account", SERVER_URL));
		request.setEntity(new StringEntity(toJson(new CreateAccountRequest(FIRST_ACCOUT_FULL_NAME))));
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
		final HttpPost request = new HttpPost(String.format("%s/api/account/transferMoney", SERVER_URL));
		request.setEntity(new StringEntity(toJson(TransferMoneyRequest.builder()
				.setFomAccountNumber(aggregateID1)
				.setToAccountNumber(aggregateID2)
				.setValue(BigDecimal.TEN)
				.build())));

		// when
		final CloseableHttpResponse response = client.execute(request);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_OK);
		final String expectedResponse = new JSONObject()
				.put("status", Status.OK)
				.put("message", "Money will be transferred")
				.put("links", Link.getLinksForAccounts())
				.toString();
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
		final HttpPost request = new HttpPost(String.format("%s/api/account/transferMoney", SERVER_URL));
		request.setEntity(new StringEntity(
				toJson(TransferMoneyRequest.builder()
	                    .setFomAccountNumber(aggregateID1)
	                    .setToAccountNumber(aggregateID2)
	                    .setValue(BigDecimal.valueOf(-10))
	                    .build())));

		// when
		final CloseableHttpResponse response = client.execute(request);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_BAD_REQUEST);
		final String expectedResponse = new JSONObject().put("status", Status.ERROR)
				.put("message", "There are validation errors")
				.put("data",
						new JSONObject().put("value", new JSONArray().put("Must be provided & " + "be greater than 0")))
				.toString();
		assertResponses(expectedResponse, getResponseBodyAndClose(response));
	}

	@Test
	public void transferMoneyNotValidNoBody() throws Exception {
		// given
		final HttpPost request = new HttpPost(String.format("%s/api/account/transferMoney", SERVER_URL));
		request.setEntity(new StringEntity(toJson(TransferMoneyRequest.builder().build())));

		// when
		final CloseableHttpResponse response = client.execute(request);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_BAD_REQUEST);
		final String expectedResponse = new JSONObject().put("status", Status.ERROR)
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
		final HttpPost request = new HttpPost(String.format("%s/api/account/transferMoney", SERVER_URL));
		request.setEntity(new StringEntity(toJson(TransferMoneyRequest.builder()
                .setFomAccountNumber(aggregateID)
                .setToAccountNumber(aggregateID)
                .setValue(BigDecimal.TEN)
                .build())));

		// when
		final CloseableHttpResponse response = client.execute(request);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_BAD_REQUEST);
		final String expectedResponse = new JSONObject().put("status", Status.ERROR)
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
		final HttpPost request = new HttpPost(String.format("%s/api/account/transferMoney", SERVER_URL));
		request.setEntity(new StringEntity(toJson(
				TransferMoneyRequest.builder()
                .setFomAccountNumber(null)
                .setToAccountNumber(null)
                .setValue(BigDecimal.TEN)
                .build())));

		// when
		final CloseableHttpResponse response = client.execute(request);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_BAD_REQUEST);
		final String expectedResponse = new JSONObject()
				.put("status", Status.ERROR)
				.put("message", "There are validation errors")
				.put("data", new JSONObject().put("fromAccountNumber", 
						new JSONArray().put("Is not a valid ID value"))
						.put("toAccountNumber", new JSONArray().put("Is not a valid ID value")))
				.toString();
		assertResponses(expectedResponse, getResponseBodyAndClose(response));
	}

	@Test
	public void transferMoneyNotValidNotExistingFromAccount() throws Exception {
	    // given
	    final String randomID = UUID.randomUUID().toString();
	    final String aggregateID = extractIDFromResponseAndClose(createAccount());
	    final HttpPost request = new HttpPost(String.format("%s/api/account/transferMoney", SERVER_URL));
	    request.setEntity(
	        new StringEntity(
	            toJson(TransferMoneyRequest.builder()
	                    .setFomAccountNumber(randomID)
	                    .setToAccountNumber(aggregateID)
	                    .setValue(BigDecimal.TEN)
	                    .build())));

	    // when
	    final CloseableHttpResponse response = client.execute(request);

	    // assert
	    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_NOT_FOUND);
	    final String expectedResponse =
	        new JSONObject()
	            .put("status", Status.ERROR)
	            .put("message", String.format("Account with ID: %s doesn't exist", randomID))
	            .toString();
	    assertResponses(expectedResponse, getResponseBodyAndClose(response));
	  }

	@Test
	public void transferMoneyNotValidNotExistingToAccount() throws Exception {
	    // given
	    final String randomID = UUID.randomUUID().toString();
	    final String aggregateID = extractIDFromResponseAndClose(createAccount());
	    final HttpPost request = new HttpPost(String.format("%s/api/account/transferMoney", SERVER_URL));
	    request.setEntity(
	        new StringEntity(
	            toJson(TransferMoneyRequest.builder()
	                    .setFomAccountNumber(aggregateID)
	                    .setToAccountNumber(randomID)
	                    .setValue(BigDecimal.TEN)
	                    .build())));

	    // when
	    final CloseableHttpResponse response = client.execute(request);

	    // assert
	    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_NOT_FOUND);
	    final String expectedResponse =
	        new JSONObject()
	            .put("status", Status.ERROR)
	            .put("message", String.format("Account with ID: %s doesn't exist", randomID))
	            .toString();
	    assertResponses(expectedResponse, getResponseBodyAndClose(response));
	  }

}
