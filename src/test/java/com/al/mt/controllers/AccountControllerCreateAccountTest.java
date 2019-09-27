package com.al.mt.controllers;

import static com.al.mt.utils.JsonUtils.toJson;
import static com.google.common.truth.Truth.assertThat;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.al.mt.model.Link;
import com.al.mt.requests.CreateAccountRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class AccountControllerCreateAccountTest extends AbstractControllerTest {
	private static final Gson GSON = new Gson();

	private static CloseableHttpResponse createAccount() throws Exception {
		final HttpPost request = new HttpPost(SERVER_URL + "/api/account");
		request.setEntity(new StringEntity(toJson(new CreateAccountRequest("Sam Willis"))));
		return client.execute(request);
	}

	@Test
	public void createAccountValid() throws Exception {
		// when
		final CloseableHttpResponse response = createAccount();

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_CREATED);
		final String responseJson = getResponseBodyAndClose(response);
		final String aggregateID = GSON.fromJson(responseJson, JsonObject.class).get("data").getAsString();
		final String expectedResponse = new JSONObject().put("status", "OK").put("message", "Account will be created")
				.put("links", Link.getLinksForAccount(aggregateID)).put("data", aggregateID).toString();
		assertResponses(expectedResponse, responseJson);
	}

	@Test
	public void createAccountNotValidNoFullName() throws Exception {
		// given
		final HttpPost request = new HttpPost(SERVER_URL + "/api/account");
		request.setEntity(new StringEntity("{}"));

		// when
		final CloseableHttpResponse response = client.execute(request);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_BAD_REQUEST);
		final String expectedResponse = new JSONObject().put("status", "ERROR")
				.put("message", "There are validation errors")
				.put("data", new JSONObject().put("fullName", new JSONArray().put("Cannot be empty"))).toString();
		assertResponses(expectedResponse, getResponseBodyAndClose(response));
	}

	@Test
	public void createAccountNotValidNullFullName() throws Exception {
		// given
		final HttpPost request = new HttpPost(SERVER_URL + "/api/account");
		request.setEntity(new StringEntity("{\"fullName\": null}"));

		// when
		final CloseableHttpResponse response = client.execute(request);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_BAD_REQUEST);
		final String expectedResponse = new JSONObject().put("status", "ERROR")
				.put("message", "There are validation errors")
				.put("data", new JSONObject().put("fullName", new JSONArray().put("Cannot be empty"))).toString();
		assertResponses(expectedResponse, getResponseBodyAndClose(response));
	}

	@Test
	public void createAccountNotValidEmptyFullName() throws Exception {
		// given
		final HttpPost request = new HttpPost(SERVER_URL + "/api/account");
		request.setEntity(new StringEntity("{\"fullName\": \"\"}"));

		// when
		final CloseableHttpResponse response = client.execute(request);

		// assert
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_BAD_REQUEST);
		final String expectedResponse = new JSONObject().put("status", "ERROR")
				.put("message", "There are validation errors")
				.put("data", new JSONObject().put("fullName", new JSONArray().put("Cannot be empty"))).toString();
		assertResponses(expectedResponse, getResponseBodyAndClose(response));
	}
}
