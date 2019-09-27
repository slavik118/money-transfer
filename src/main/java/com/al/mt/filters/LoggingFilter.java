package com.al.mt.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Filter;
import spark.Request;
import spark.Response;

/**
 * Logging filter which is applied to every incoming request.
 *
 * <p>
 * Example: `127.0.0.1 GET [/api/accounts]`
 */
public class LoggingFilter implements Filter {
	private final static Logger LOG = LoggerFactory.getLogger(LoggingFilter.class);

	@Override
	public void handle(final Request request, final Response response) {
		LOG.info("{} {} [{}]", request.ip(), request.requestMethod(), request.pathInfo());
	}
}