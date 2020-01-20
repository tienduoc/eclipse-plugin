package com.aixcoder.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import com.aixcoder.utils.Pair;
import com.aixcoder.utils.Predict.LongPredictResult;
import com.aixcoder.utils.Predict.PredictResult;
import com.aixcoder.utils.Predict.SortResult;

public class PredictCache {
	protected static PredictCache instance;

	public synchronized static PredictCache getInstance() {
		if (instance == null) {
			instance = new PredictCache();
		}
		return instance;
	}

	public List<Pair<String, PredictResult>> cache = new LinkedList<Pair<String, PredictResult>>();

	public synchronized void put(String prefix, PredictResult predictResult) {
		cache.add(new Pair<String, PredictResult>(prefix, predictResult));
		if (cache.size() > 5) {
			cache.remove(0);
		}
	}

	public synchronized PredictResult get(String prefix) {
		for (Pair<String, PredictResult> pair : cache) {
			PredictResult newPR = update(pair.getFirst(), prefix, pair.getSecond());
			if (newPR != null) {
				return newPR;
			}
		}
		return null;
	}

	private final static Pattern LTRIM = Pattern.compile("^\\s+");

	public static String ltrim(String s) {
		return LTRIM.matcher(s).replaceAll("");
	}

	public static PredictResult update(String prefix, String newPrefix, PredictResult pr) {
		if (newPrefix.startsWith(prefix)) {
			if (newPrefix.equals(prefix)) {
				return pr;
			}
			String newString = newPrefix.substring(prefix.length());

			int i = 0;
			int bestLength = -1;
			LongPredictResult second = null;
			for (LongPredictResult predict : pr.longPredicts) {
				if (predict.tokens.length > bestLength) {
					bestLength = predict.tokens.length;
					second = predict;
				}
			}
			if (second == null) return null;
			SortResult[] sortResults = pr.sortResults;
			for (; i < second.tokens.length; i++) {
				String ltrimedNewString = ltrim(newString);
				if (ltrimedNewString.startsWith(second.tokens[i])) {
					newString = ltrimedNewString.substring(second.tokens[i].length());
				} else {
					break;
				}
			}
			if (second.tokens.length > i && second.tokens[i].startsWith(newString.trim())) {
				if (i == 0) {
					// cache : St [ring, s, =]
					// newPrefix: Str
					// newString: r
					// => tokens: [ing, s, =]
					// => current: Str
					List<LongPredictResult> newTokensList = new ArrayList<LongPredictResult>();
					for (LongPredictResult longPredict : pr.longPredicts) {
						if (longPredict.tokens.length > i) {
							String[] newTokens = Arrays.copyOfRange(longPredict.tokens, i, longPredict.tokens.length);
							newTokens[0] = newTokens[0].substring(newString.length());
							newTokensList.add(new LongPredictResult(newTokens, longPredict.current + newString, longPredict.rescues,
									longPredict.rCompletions));
						}
					}
					return new PredictResult(newTokensList.toArray(new LongPredictResult[0]), sortResults);
				} else {
					if (newString.isEmpty()) {
						// at end of word
						// cache : St [ring, s, =]
						// newPrefix: String
						// newString: ring
						// => tokens: ["", s, =]
						// => current: String
						List<LongPredictResult> newTokensList = new ArrayList<LongPredictResult>();
						for (LongPredictResult longPredict : pr.longPredicts) {
							if (longPredict.tokens.length > i) {
								String[] newTokens = new String[second.tokens.length - i + 1];
								System.arraycopy(second.tokens, i, newTokens, 1, second.tokens.length - i);
								newTokens[0] = "";
								String newCurrent = second.tokens[i - 1];
								if (i == 1) {
									newCurrent = second.current + newCurrent;
								}
								
								newTokensList.add(new LongPredictResult(newTokens, newCurrent, longPredict.rescues,
										longPredict.rCompletions));
							}
						}
						return new PredictResult(newTokensList.toArray(new LongPredictResult[0]), sortResults);
					} else if (newString.trim().isEmpty()) {
						// at start of next word
						// cache : St [ring, s, =]
						// newPrefix: String_
						// newString: _
						// => tokens: [s, =]
						// => current: ""
						List<LongPredictResult> newTokensList = new ArrayList<LongPredictResult>();
						for (LongPredictResult longPredict : pr.longPredicts) {
							if (longPredict.tokens.length > i) {
								String[] newTokens = Arrays.copyOfRange(second.tokens, i, second.tokens.length);
								newTokensList.add(new LongPredictResult(newTokens, "", longPredict.rescues,
										longPredict.rCompletions));
							}
						}
						return new PredictResult(newTokensList.toArray(new LongPredictResult[0]), sortResults);
					} else {
						// cache : St [ring, str, =]
						// newPrefix: String s
						// newString: _s
						// => tokens: [tr, =]
						// => current: s
						newString = newString.trim();
						List<LongPredictResult> newTokensList = new ArrayList<LongPredictResult>();
						for (LongPredictResult longPredict : pr.longPredicts) {
							if (longPredict.tokens.length > i) {
								String[] newTokens = Arrays.copyOfRange(second.tokens, i, second.tokens.length);
								newTokens[0] = newTokens[0].substring(newString.length());
								newTokensList.add(new LongPredictResult(newTokens, newString, longPredict.rescues,
										longPredict.rCompletions));
							}
						}
						return new PredictResult(newTokensList.toArray(new LongPredictResult[0]), sortResults);
					}
				}
			} else {
				// mismatch
				return null;
			}
		}
		return null;
	}
}