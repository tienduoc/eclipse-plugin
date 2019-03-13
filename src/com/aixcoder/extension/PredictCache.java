package com.aixcoder.extension;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.aixcoder.utils.Pair;
import com.aixcoder.utils.Predict.PredictResult;

public class PredictCache {
	protected static PredictCache instance;

	public static PredictCache getInstance() {
		if (instance == null) {
			instance = new PredictCache();
		}
		return instance;
	}

	List<Pair<String, PredictResult>> cache = new LinkedList<Pair<String, PredictResult>>();

	public void put(String prefix, PredictResult predictResult) {
		cache.add(new Pair<String, PredictResult>(prefix, predictResult));
		if (cache.size() > 5) {
			cache.remove(0);
		}
	}

	public PredictResult get(String prefix) {
		for (Pair<String, PredictResult> pair : cache) {
			PredictResult newPR = update(pair.getFirst(), prefix, pair.getSecond());
			if (newPR != null) {
				return newPR;
			}
		}
		return null;
	}

	public static PredictResult update(String prefix, String newPrefix, PredictResult second) {
		if (newPrefix.startsWith(prefix)) {
			String newString = newPrefix.substring(prefix.length()).trim();

			int i = 0;
			for (; i < second.tokens.length; i++) {
				if (newString.startsWith(second.tokens[i])) {
					newString = newString.substring(second.tokens[i].length()).trim();
				} else {
					break;
				}
			}
			if (second.tokens.length > i && second.tokens[i].startsWith(newString)) {
				if (i == 0) {
					// cache : St [ring, s, =]
					// newPrefix: Str
					// newString: r
					// => tokens: [ing, s, =]
					// => current: Str
					String[] newTokens = Arrays.copyOfRange(second.tokens, i, second.tokens.length);
					newTokens[0] = newTokens[0].substring(newString.length());
					return new PredictResult(newTokens, second.current + newString);
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
						return new PredictResult(newTokens, newCurrent);
					} else {
						// cache : St [ring, str, =]
						// newPrefix: String s
						// newString: s
						// => tokens: [tr, =]
						// => current: s
						String[] newTokens = Arrays.copyOfRange(second.tokens, i, second.tokens.length);
						newTokens[0] = newTokens[0].substring(newString.length());
						return new PredictResult(newTokens, newString);
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