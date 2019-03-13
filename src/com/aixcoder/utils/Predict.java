package com.aixcoder.utils;

import java.util.List;

import com.aixcoder.lib.HttpRequest;
import com.aixcoder.lib.JSON;

public class Predict {
	public static class PredictResult {
		public String[] tokens;
		public String current;
		public String[] rCompletions;

		public PredictResult(String[] tokens, String current, String[] rCompletions) {
			super();
			this.tokens = tokens;
			this.current = current;
			this.rCompletions = rCompletions;
		}

		public String toString() {
			return "PredictResult: (" + current + ")[" + String.join(" ", tokens) + "]";
		}
	}

	private final static String URL = "https://api.aixcoder.com/predict";
	private final static int TIME_OUT = 2500;

	private static String[] getStringList(List<JSON> list) {
		String[] r = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			r[i] = list.get(i).getString();
		}
		return r;
	}

	public static PredictResult predict(String prefix, String remainingText) {
		try {
			HttpRequest httpRequest = HttpRequest.post(URL).connectTimeout(TIME_OUT).readTimeout(TIME_OUT)
					.useCaches(false).contentType("x-www-form-urlencoded", "UTF-8").form("text", prefix)
					.form("uuid", "eclipse-plugin").form("project", "eclipse-proj").form("ext", "java(Java)")
					.form("fileid", "eclipse-file").form("remaining_text", remainingText);
			String string = httpRequest.body();
			httpRequest.disconnect();
			JSON json = JSON.decode(string).getList().get(0);
			String[] tokens = getStringList(json.getList("tokens"));
			String current = json.getString("current");
			String[] rCompletion = getStringList(json.getList("r_completion"));
			return new PredictResult(tokens, current, rCompletion);
		} catch (Exception e) {
		}
		return new PredictResult(new String[0], "", null);
	}
}
