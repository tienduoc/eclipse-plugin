package com.aixcoder.core;

public class PredictContext {
	public final String prefix;
	public final String proj;
	public final String filename;
	public final String projRoot;

	public PredictContext(String prefix, String proj, String filename, String projRoot) {
		super();
		this.prefix = prefix;
		this.proj = proj;
		this.filename = filename;
		this.projRoot = projRoot;
	}

}