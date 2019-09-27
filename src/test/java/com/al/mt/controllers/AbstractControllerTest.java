package com.al.mt.controllers;

import com.al.mt.MainApp;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeAll;
import spark.utils.IOUtils;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public abstract class AbstractControllerTest {
	protected static final Gson GSON = new Gson();
	protected static final JsonParser JSON_PARSER = new JsonParser();
	protected static final String SERVER_URL = "http://localhost:8000";
	protected static final CloseableHttpClient client = HttpClients.custom().build();
	
	private static final Object GUARD = new Object();
	private static boolean isRunning = false;

	@BeforeAll
	public static void setUp() {
		synchronized (GUARD) {
			if (!isRunning) {
				MainApp.main(null);
				isRunning = true;
			}
		}
	}

	protected static String getResponseBodyAndClose(final CloseableHttpResponse response) throws IOException {
		final String value = IOUtils.toString(response.getEntity().getContent());
		response.close();
		return value;
	}

	protected static void assertResponses(final String expectedResponse, final String actualResponse) {
		assertThat(GSON.fromJson(actualResponse, JsonObject.class)).isEqualTo(JSON_PARSER.parse(expectedResponse));
	}

	protected static String getFieldFromEvents(final String json, final int index, final String fieldName) {
		return GSON.fromJson(json, JsonObject.class).getAsJsonObject("data").getAsJsonArray("events").get(index)
				.getAsJsonObject().get(fieldName).getAsString();
	}
}
