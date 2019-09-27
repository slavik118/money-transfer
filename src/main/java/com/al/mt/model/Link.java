package com.al.mt.model;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableList;

import spark.route.HttpMethod;

public class Link {

	private Link() {
		// private constructor
	}

	private static final ImmutableList<Link> LINKS_FOR_ACCOUNTS = ImmutableList.of(
			Link.builder().setRel("self").setHref("/api/account").setHttpMethod(HttpMethod.get).build(),
			Link.builder().setRel("self").setHref("/api/account").setHttpMethod(HttpMethod.post).build(),
			Link.builder().setRel("self").setHref("/api/account/transferMoney").setHttpMethod(HttpMethod.post)
					.build());

	private String rel;
	private String href;
	private HttpMethod method;

	public final String getRel() {
		return this.rel;
	}

	public final String getHref() {
		return this.href;
	}

	public final HttpMethod getMethod() {
		return this.method;
	}

	public static List<Link> getLinksForAccounts() {
		return LINKS_FOR_ACCOUNTS;
	}

	public static List<Link> getLinksForAccount(final UUID aggreagateID) {
		return getLinksForAccount(aggreagateID.toString());
	}

	public static List<Link> getLinksForAccount(final String aggregateID) {
		return ImmutableList.of(Link.builder().setRel("self").setHref(String.format("/api/account/%s", aggregateID))
				.setHttpMethod(HttpMethod.get).build());
	}

	public static Builder builder() {
		return new Link().new Builder();
	}

	public class Builder {

		private Builder() {
			// private constructor
		}

		public final Builder setRel(final String rel) {
			Link.this.rel = rel;

			return this;
		}

		public final Builder setHref(final String href) {
			Link.this.href = href;

			return this;
		}

		public final Builder setHttpMethod(final HttpMethod method) {
			Link.this.method = method;

			return this;
		}

		public Link build() {
			return Link.this;
		}

	}
}
