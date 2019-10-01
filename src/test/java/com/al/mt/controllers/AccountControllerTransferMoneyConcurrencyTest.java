package com.al.mt.controllers;

import static com.al.mt.MainApp.ACCOUNT_EVENT_STORAGE;
import static com.al.mt.utils.Constants.FIRST_ACCOUT_FULL_NAME;
import static com.al.mt.utils.Constants.SERVER_URL;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.al.mt.AbstractBaseTest;
import com.al.mt.model.APIResponse;
import com.al.mt.requests.CreateAccountRequest;
import com.al.mt.requests.TransferMoneyRequest;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

public class AccountControllerTransferMoneyConcurrencyTest extends AbstractBaseTest {
	private final static Logger LOG = LoggerFactory.getLogger(AccountControllerTransferMoneyConcurrencyTest.class);
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
				.setValue(BigDecimal.ONE).build())));

		final ExecutorService threadPool = Executors.newCachedThreadPool();

		// when
		final ImmutableList<Future<?>> futures = IntStream.range(1, 501)
				.boxed()
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
			} catch (final InterruptedException e) {
				 LOG.error("An interrupted exception has happened: {}", e.getMessage());
			} catch (final ExecutionException e) {
				LOG.error("An execution exception has happened: {}", e.getMessage());
			}
		});
		// assert
		assertThat(ACCOUNT_EVENT_STORAGE.get(UUID.fromString(aggregateID1))
				.getBalance()
				.compareTo(BigDecimal.valueOf(500)))
		.isEqualTo(0);
		assertThat(ACCOUNT_EVENT_STORAGE.get(UUID.fromString(aggregateID2))
				.getBalance()
				.compareTo(BigDecimal.valueOf(1500)))
		.isEqualTo(0);
	}
}
