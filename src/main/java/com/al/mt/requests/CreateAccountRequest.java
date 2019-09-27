package com.al.mt.requests;

public class CreateAccountRequest {
	private String fullName;

	public CreateAccountRequest(final String fullName) {
		this.fullName = fullName;
	}

	public final String getFullName() {
		return this.fullName;
	}

	public final void setFullName(final String fullName) {
		this.fullName = fullName;
	}

}
