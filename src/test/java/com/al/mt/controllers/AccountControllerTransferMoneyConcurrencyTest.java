package com.al.mt.controllers;

import static com.al.mt.MainApp.ACCOUNT_EVENT_STORAGE;
import static com.al.mt.utils.JsonUtils.toJson;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;

import com.al.mt.model.APIResponse;
import com.al.mt.requests.CreateAccountRequest;
import com.al.mt.requests.TransferMoneyRequest;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

public class AccountControllerTransferMoneyConcurrencyTest extends AbstractControllerTest {
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
  public void transferMoneyValid() throws Exception {
    // given
    final String aggregateID1 = extractIDFromResponseAndClose(createAccount());
    final String aggregateID2 = extractIDFromResponseAndClose(createAccount());
    final HttpPost request = new HttpPost(SERVER_URL + "/api/account/transferMoney");
    request.setEntity(
        new StringEntity(
            toJson(new TransferMoneyRequest(aggregateID1, aggregateID2, BigDecimal.ONE))));
    
    final ExecutorService threadPool = Executors.newCachedThreadPool();

    // when
    final ImmutableList<Future<?>> futures = IntStream.range(1, 501).boxed()
        .map(i -> threadPool.submit(() -> {
          try {
            client.execute(request).close();
          } catch (final IOException e) {
            throw new IllegalStateException(e);
          }
        })).collect(toImmutableList());

    futures.forEach(future -> {
      try {
        future.get();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    });
    // assert
    assertThat(ACCOUNT_EVENT_STORAGE.get(UUID.fromString(aggregateID1)).getBalance()
        .compareTo(BigDecimal.valueOf(500))).isEqualTo(0);
    assertThat(ACCOUNT_EVENT_STORAGE.get(UUID.fromString(aggregateID2)).getBalance()
        .compareTo(BigDecimal.valueOf(1500))).isEqualTo(0);
  }
}
