package com.al.mt.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public interface JsonUtils {
	final static Gson GSON = new Gson();

	/**
	 * Translates any {@code} model to JSON formatted string.
	 */
	static String toJson(final Object model) {
		return GSON.toJson(model);
	}

	/**
	 * Checks whether {@code jsonInString} is a valid JSON formatted string.
	 */
	static boolean isJSONValid(final String jsonInString) {
		try {
			GSON.fromJson(jsonInString, Object.class);
			return true;
		} catch (final JsonSyntaxException e) {
			return false;
		}
	}
}
