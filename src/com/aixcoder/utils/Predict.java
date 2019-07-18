package com.aixcoder.utils;

import com.aixcoder.core.API;
import com.aixcoder.core.PredictContext;
import com.aixcoder.utils.shims.CollectionUtils;

public class Predict {

	public static class SortResult {
		public double prob;
		public String word;
		public CompletionOptions options;

		public SortResult(double prob, String word, CompletionOptions options) {
			super();
			this.prob = prob;
			this.word = word;
			this.options = options;
		}

	}

	public static class LongPredictResult {
		public String current;
		public String[] tokens;
		public Rescue[] rescues;
		public String[] rCompletions;
		
		public LongPredictResult(String[] tokens, String current, Rescue[] rescues, String[] rCompletions) {
			super();
			this.tokens = tokens;
			this.current = current;
			this.rescues = rescues;
			this.rCompletions = rCompletions;
		}
	}
	
	public static class PredictResult {
		public SortResult[] sortResults;
		public LongPredictResult[] longPredicts;

		public PredictResult(LongPredictResult[] longPredicts, SortResult[] sortResults) {
			super();
			this.longPredicts = longPredicts;
			this.sortResults = sortResults;
		}

		public String toString() {
			return "PredictResult: " + CollectionUtils.join(" ", longPredicts) + "]";
		}
	}

	public static PredictResult predict(PredictContext predictContext, String remainingText, String UUID) {
		return API.predict(predictContext, remainingText, UUID);
	}
}
