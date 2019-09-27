package com.al.mt.enums;

public enum Status {
	OK("OK"), ERROR("ERROR");

	private final String value;

	private Status(final String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
