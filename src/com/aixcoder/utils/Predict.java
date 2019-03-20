package com.aixcoder.utils;

import com.aixcoder.core.API;
import com.aixcoder.core.PredictContext;
import com.aixcoder.utils.shims.CollectionUtils;

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
			return "PredictResult: (" + current + ")[" + CollectionUtils.join(" ", tokens) + "]";
		}
	}

	public static PredictResult predict(PredictContext predictContext, String remainingText, String UUID) {
		return API.predict(predictContext, remainingText, UUID);
	}
}
