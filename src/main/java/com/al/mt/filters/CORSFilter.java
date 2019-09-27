package com.al.mt.filters;

import spark.Filter;
import spark.Request;
import spark.Response;

/**
 *  Allows anyone to connect to the server 
 */
public class CORSFilter implements Filter {

	@Override
	public void handle(final Request request, final Response response) {
		response.header("Access-Control-Allow-Origin", "*");
		response.header("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
	}
}
