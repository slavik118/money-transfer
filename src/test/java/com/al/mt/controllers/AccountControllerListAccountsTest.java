package com.al.mt.controllers;

import static com.al.mt.utils.Constants.FIRST_ACCOUT_FULL_NAME;
import static com.al.mt.utils.Constants.SERVER_URL;
import static com.al.mt.utils.JsonUtils.toJson;
import static com.google.common.truth.Truth.assertThat;
import static java.net.HttpURLConnection.HTTP_OK;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;

import com.al.mt.AbstractBaseTest;
import com.al.mt.requests.CreateAccountRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class AccountControllerListAccountsTest extends AbstractBaseTest {
  private static final Gson GSON = new Gson();

  private static CloseableHttpResponse createAccount() throws Exception {
    final HttpPost request = new HttpPost(String.format("%s/api/account", SERVER_URL));
    request.setEntity(new StringEntity(toJson(new CreateAccountRequest(FIRST_ACCOUT_FULL_NAME))));
    return client.execute(request);
  }

  @Test
  public void listAccounts() throws Exception {
    // given
    final CloseableHttpResponse response1 = createAccount();
    final CloseableHttpResponse response2 = createAccount();
    response1.close();
    response2.close();
    final HttpGet request = new HttpGet(String.format("%s/api/account", SERVER_URL));

    // when
    final CloseableHttpResponse response = client.execute(request);

    // assert
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HTTP_OK);
    final String responseJson = getResponseBodyAndClose(response);
    assertThat(GSON.fromJson(responseJson, JsonObject.class).get("data").getAsJsonArray().size())
        .isGreaterThan(1);
  }
}
