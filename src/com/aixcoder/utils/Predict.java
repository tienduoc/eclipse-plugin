package com.aixcoder.utils;

import java.util.function.Function;
import java.util.stream.Collectors;

import com.aixcoder.lib.HttpRequest;
import com.aixcoder.lib.JSON;

public class Predict {
	public static class PredictResult {
		public String[] tokens;
		public String current;

		public PredictResult(String[] tokens, String current) {
			super();
			this.tokens = tokens;
			this.current = current;
		}
	}

	private final static String URL = "https://api.aixcoder.com/predict";
	private final static int TIME_OUT = 2500;

	public static PredictResult predict(String prefix) {
		try {
			HttpRequest httpRequest = HttpRequest.post(URL).connectTimeout(TIME_OUT).readTimeout(TIME_OUT)
					.useCaches(false).contentType("x-www-form-urlencoded", "UTF-8").form("text", prefix)
					.form("uuid", "eclipse-plugin").form("project", "eclipse-proj").form("ext", "java(Java)")
					.form("fileid", "eclipse-file");
			String string = httpRequest.body();
			httpRequest.disconnect();
			JSON json = JSON.decode(string).getList().get(0);
			String[] tokens = json.getList("tokens").stream().map(new Function<JSON, Object>() {
				@Override
				public String apply(final JSON json) {
					return json.getString();
				}
			}).collect(Collectors.toList()).toArray(new String[0]);
			String current = json.getString("current");
			return new PredictResult(tokens, current);

		} catch (Exception e) {
		}
		return new PredictResult(new String[0], "");
	}

}
