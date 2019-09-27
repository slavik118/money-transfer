package com.al.mt.controllers;

import static com.al.mt.utils.JsonUtils.toJson;
import static com.google.common.truth.Truth.assertThat;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.al.mt.model.APIResponse;
import com.al.mt.model.Link;
import com.al.mt.requests.CreateAccountRequest;
import com.google.gson.Gson;

public class AccountControllerGetAccountTest extends AbstractControllerTest {
  private static final Gson GSON = new Gson();

  private static CloseableHttpResponse createAccount() throws Exception {
    final HttpPost request = new HttpPost(SERVER_URL + "/api/account");
    request.setEntity(new StringEntity(toJson(new CreateAccountRequest("Sam Willis"))));
    return client.execute(request);
  }

  private static String extractIDFromResponseAndClose(final CloseableHttpResponse response)
      throws Exception {
    return (String) GSON.fromJson(getResponseBodyAndClose(response), APIResponse.class).getData();
  }

  @Test
  public void getAccountValid() throws Exception {
    // given
    final String aggregateID = extractIDFromResponseAndClose(createAccount());
    final HttpGet request = new HttpGet(SERVER_URL + "/api/account/" + aggregateID);

    // when
    final CloseableHttpResponse response = client.execute(request);

    // assert
    final String responseJson = getResponseBodyAndClose(response);
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_OK);
    final String createdAt = getFieldFromEvents(responseJson, 0, "createdAt");
    final String expectedResponse =
        new JSONObject()
            .put("status", "OK")
            .put("message", "SUCCESS")
            .put("links", Link.getLinksForAccount(aggregateID))
            .put(
                "data",
                new JSONObject()
                    .put("links", Link.getLinksForAccount(aggregateID))
                    .put("fullName", "Sam Willis")
                    .put("accountNumber", aggregateID)
                    .put("balance", 1000.0)
                    .put("transactionToReservedBalance", new JSONObject())
                    .put(
                        "events",
                        new JSONArray()
                            .put(
                                new JSONObject()
                                    .put("fullName", "Sam Willis")
                                    .put("eventType", "ACCOUNT_CREATED_EVENT")
                                    .put("aggregateID", aggregateID)
                                    .put("createdAt", createdAt)))
                    .put("createdAt", createdAt)
                    .put("lastUpdatedAt", createdAt)
                    .put("transactions", new JSONObject()))
            .toString();

    assertResponses(expectedResponse, responseJson);
  }

  @Test
  public void getAccountNotValidInvalidID() throws Exception {
    // given
    final HttpGet request = new HttpGet(SERVER_URL + "/api/account/asd");

    // when
    final CloseableHttpResponse response = client.execute(request);

    // assert
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_BAD_REQUEST);
    final String expectedResponse =
        new JSONObject()
            .put("status", "ERROR")
            .put("message", "There are validation errors")
            .put(
                "data",
                new JSONObject().put("id", new JSONArray().put("Is not a valid ID value")))
            .toString();
    assertResponses(expectedResponse, getResponseBodyAndClose(response));
  }

  @Test
  public void getAccountNotValidAggregateDoesNotExist() throws Exception {
    // given
    final UUID aggregateID = UUID.randomUUID();
    final HttpGet request = new HttpGet(SERVER_URL + "/api/account/" + aggregateID.toString());

    // when
    final CloseableHttpResponse response = client.execute(request);

    // assert
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_NOT_FOUND);
    final String expectedResponse =
        new JSONObject()
            .put("status", "ERROR")
            .put(
                "message",
                String.format("Account with ID: %s was not found", aggregateID.toString()))
            .toString();
    assertResponses(expectedResponse, getResponseBodyAndClose(response));
  }
}
