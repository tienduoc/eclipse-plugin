package com.aixcoder.core;

public enum ReportType {
	SHOW(0),
	USE(1),
	EMPTY(2),
	ERROR(3),
	INTERRUPT(4),
	USEBUILTIN(5);
	
	private final int value;
	ReportType (int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
}
