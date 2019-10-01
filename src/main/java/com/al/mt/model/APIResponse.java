package com.al.mt.model;

import java.util.List;

import com.al.mt.enums.Status;
import com.al.mt.utils.JsonUtils;

public class APIResponse {
	private Status status;
	private String message;
	private Object data;
	private List<Link> links;

	private APIResponse() {
	}

	public final Status getStatus() {
		return this.status;
	}

	public final String getMessage() {
		return this.message;
	}

	public final Object getData() {
		return this.data;
	}

	public final List<Link> getLinks() {
		return this.links;
	}

	public static Builder builder() {
		return new APIResponse().new Builder();
	}

	public class Builder {

		private Builder() {
		}

		public final Builder setStatus(final Status status) {
			APIResponse.this.status = status;

			return this;
		}

		public final Builder setMessage(final String message) {
			APIResponse.this.message = message;

			return this;
		}

		public final Builder setData(final Object data) {
			APIResponse.this.data = data;

			return this;
		}

		public final Builder setLinks(final List<Link> links) {
			APIResponse.this.links = links;

			return this;
		}

		public APIResponse build() {
			return APIResponse.this;
		}
	}

	public String toJson() {
		return JsonUtils.toJson(this);
	}

}
