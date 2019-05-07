package com.aixcoder.utils;

import com.aixcoder.core.API;
import com.aixcoder.core.PredictContext;
import com.aixcoder.utils.shims.CollectionUtils;

public class Predict {

	public static class SortResult {
		public double prob;
		public String word;

		public SortResult(double prob, String word) {
			super();
			this.prob = prob;
			this.word = word;
		}

	}

	public static class PredictResult {
		public String[] tokens;
		public String current;
		public String[] rCompletions;
		public SortResult[] sortResults;

		public PredictResult(String[] tokens, String current, String[] rCompletions, SortResult[] sortResults) {
			super();
			this.tokens = tokens;
			this.current = current;
			this.rCompletions = rCompletions;
			this.sortResults = sortResults;
		}

		public String toString() {
			return "PredictResult: (" + current + ")[" + CollectionUtils.join(" ", tokens) + "]";
		}
	}

	public static PredictResult predict(PredictContext predictContext, String remainingText, String UUID) {
		return API.predict(predictContext, remainingText, UUID);
	}
}
