package com.al.mt.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public final class JsonUtils {
	private static final Gson GSON = new Gson();

	private JsonUtils() {
		throw new AssertionError();
	}

	/**
	 * Translates any {@code} model to JSON formatted string.
	 */
	public static String toJson(final Object model) {
		return GSON.toJson(model);
	}

	/**
	 * Checks whether {@code jsonInString} is a valid JSON formatted string.
	 */
	public static boolean isJSONValid(final String jsonInString) {
		try {
			GSON.fromJson(jsonInString, Object.class);
			return true;
		} catch (final JsonSyntaxException e) {
			return false;
		}
	}
}
