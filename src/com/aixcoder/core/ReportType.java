package com.aixcoder.core;

public enum ReportType {
	LONG_SHOW("001"), SHORT_SHOW("002"), LONG_USE("003"), SHORT_USE("004"), SYSTEM_SHOW("005"), SYSTEM_USE("007"), USE_COUNT("008");

	private final String value;

	ReportType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
