package com.aixcoder.core;

public class PredictContext {
	public final String prefix;
	public final String proj;
	public final String filename;
	public final String projRoot;
	
	public final String suffix;

	public PredictContext(String prefix, String proj, String filename, String projRoot,String suffix) {
		super();
		this.prefix = prefix;
		this.proj = proj;
		this.filename = filename;
		this.projRoot = projRoot;
		this.suffix = suffix;
	}

}