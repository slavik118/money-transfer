package com.al.mt.controllers;

import static com.al.mt.utils.JsonUtils.toJson;
import static com.google.common.truth.Truth.assertThat;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.al.mt.model.Link;
import com.al.mt.requests.CreateAccountRequest;
import com.google.gson.JsonObject;

public class AccountControllerEndToEndTest extends AbstractControllerTest {

	private static String createAndAssertAccount(final String fullName) throws Exception {
		// given
		final HttpPost createAccountRequest = createAccountRequest(fullName);

		// when
		final CloseableHttpResponse response = client.execute(createAccountRequest);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_CREATED);
		final String createAccountJson = getResponseBodyAndClose(response);
		final String aggregateID = GSON.fromJson(createAccountJson, JsonObject.class).get("data").getAsString();
		assertCreateAccountResponse(createAccountJson, aggregateID);
		return aggregateID;
	}

	private static HttpPost createAccountRequest(final String fullName) throws Exception {
		final HttpPost createAccountRequest = new HttpPost(SERVER_URL + "/api/account");
		createAccountRequest.setEntity(new StringEntity(toJson(new CreateAccountRequest(fullName))));
		return createAccountRequest;
	}

	private static void assertCreateAccountResponse(final String createAccountJson, final String aggregateID) {
		final String expectedResponse = new JSONObject().put("status", "OK").put("message", "Account will be created")
				.put("data", aggregateID).put("links", Link.getLinksForAccount(aggregateID)).toString();
		assertResponses(expectedResponse, createAccountJson);
	}

	private static String getAccount(final String aggregateID) throws Exception {
		// given
		final HttpGet getAccountRequest = new HttpGet(SERVER_URL + "/api/account/" + aggregateID);

		// when
		final CloseableHttpResponse response = client.execute(getAccountRequest);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_OK);
		return getResponseBodyAndClose(response);
	}

	@Test
	public void testGetAccountEndpoint() throws Exception {
		// given
		final String aggregateID = createAndAssertAccount("Sam Willis");

		// when
		final String getAccountJson = getAccount(aggregateID);

		// assert
		final String createdAt = getFieldFromEvents(getAccountJson, 0, "createdAt");
		final String expectedResponse = new JSONObject().put("status", "OK").put("message", "SUCCESS")
				.put("links", Link.getLinksForAccount(aggregateID))
				.put("data",
						new JSONObject().put("fullName", "Sam Willis").put("accountNumber", aggregateID)
								.put("balance", 1000.0).put("transactionToReservedBalance", new JSONObject())
								.put("events",
										new JSONArray().put(new JSONObject().put("fullName", "Sam Willis")
												.put("eventType", "ACCOUNT_CREATED_EVENT")
												.put("aggregateID", aggregateID).put("createdAt", createdAt)))
								.put("createdAt", createdAt).put("lastUpdatedAt", createdAt)
								.put("links", Link.getLinksForAccount(aggregateID))
								.put("transactions", new JSONObject()))
				.toString();
		assertResponses(expectedResponse, getAccountJson);
	}
}
