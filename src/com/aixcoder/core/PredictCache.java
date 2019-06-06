package com.aixcoder.core;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import com.aixcoder.utils.Pair;
import com.aixcoder.utils.Predict.PredictResult;

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

	public static PredictResult update(String prefix, String newPrefix, PredictResult second) {
		if (newPrefix.startsWith(prefix)) {
			String newString = newPrefix.substring(prefix.length());

			int i = 0;
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
					String[] newTokens = Arrays.copyOfRange(second.tokens, i, second.tokens.length);
					newTokens[0] = newTokens[0].substring(newString.length());
					return new PredictResult(newTokens, second.current + newString, second.rCompletions,
							second.sortResults, second.rescues);
				} else {
					if (newString.isEmpty()) {
						// at end of word
						// cache : St [ring, s, =]
						// newPrefix: String
						// newString: ring
						// => tokens: ["", s, =]
						// => current: String
						String[] newTokens = new String[second.tokens.length - i + 1];
						System.arraycopy(second.tokens, i, newTokens, 1, second.tokens.length - i);
						newTokens[0] = "";
						String newCurrent = second.tokens[i - 1];
						if (i == 1) {
							newCurrent = second.current + newCurrent;
						}
						return new PredictResult(newTokens, newCurrent, second.rCompletions, second.sortResults,
								second.rescues);
					} else if (newString.trim().isEmpty()) {
						// at start of next word
						// cache : St [ring, s, =]
						// newPrefix: String_
						// newString: _
						// => tokens: [s, =]
						// => current: ""
						String[] newTokens = Arrays.copyOfRange(second.tokens, i, second.tokens.length);
						return new PredictResult(newTokens, "", second.rCompletions, null, second.rescues);
					} else {
						// cache : St [ring, str, =]
						// newPrefix: String s
						// newString: _s
						// => tokens: [tr, =]
						// => current: s
						newString = newString.trim();
						String[] newTokens = Arrays.copyOfRange(second.tokens, i, second.tokens.length);
						newTokens[0] = newTokens[0].substring(newString.length());
						return new PredictResult(newTokens, newString, second.rCompletions, null, second.rescues);
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