package com.aixcoder.utils;

import com.aixcoder.core.API;
import com.aixcoder.core.PredictContext;

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
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < tokens.length; i++) {
				sb.append(tokens[i]);
				sb.append(" ");
			}
			if (rCompletions.length > 0) {
				sb.append(" -> ");
				for (int i = 0; i < rCompletions.length; i++) {
					sb.append(rCompletions[i]);
					sb.append(" ");
				}
			}
			return sb.toString();
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

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < longPredicts.length; i++) {
				sb.append(longPredicts[i].toString());
				sb.append(" ");
			}
			return "PredictResult: " + sb.toString() + "]";
		}
	}

	public static PredictResult predict(PredictContext predictContext, String remainingText, String UUID) {
		return API.predict(predictContext, remainingText, UUID);
	}
}
