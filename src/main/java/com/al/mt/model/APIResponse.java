package com.al.mt.model;

import java.util.List;

import com.al.mt.enums.Status;
import com.al.mt.utils.JsonUtils;

public class APIResponse {
	private Status status;
	private String message;
	private Object data;
	private List<Link> links;

	public APIResponse() {
	}

	public APIResponse(final String message) {
		this(Status.OK, message, null, null);
	}

	public APIResponse(final Status status, final String message) {
		this(status, message, null, null);
	}

	public APIResponse(final Status status, final String message, final Object data) {
		this(status, message, data, null);
	}

	public APIResponse(final Object data, final List<Link> links) {
		this(Status.OK, "SUCCESS", data, links);
	}

	public APIResponse(final String message, final List<Link> links) {
		this(Status.OK, message, null, links);
	}

	public APIResponse(final Status status, final String message, final Object data, final List<Link> links) {
		this.status = status;
		this.message = message;
		this.data = data;
		this.links = links;
	}

	public final Status getStatus() {
		return this.status;
	}

	public final void setStatus(final Status status) {
		this.status = status;
	}

	public final String getMessage() {
		return this.message;
	}

	public final void setMessage(final String message) {
		this.message = message;
	}

	public final Object getData() {
		return this.data;
	}

	public final void setData(final Object data) {
		this.data = data;
	}

	public final List<Link> getLinks() {
		return this.links;
	}

	public final void setLinks(final List<Link> links) {
		this.links = links;
	}

	public String toJson() {
		return JsonUtils.toJson(this);
	}

}
