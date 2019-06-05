package com.aixcoder.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.text.IDocument;

import com.aixcoder.utils.Rescue;
import com.aixcoder.utils.shims.BiFunction;
import com.aixcoder.utils.shims.Supplier;

public abstract class LangOptions {

	protected final static HashMap<Boolean, BiFunction<ArrayList<String>, Integer, Boolean>> defaultSupplier = new HashMap<Boolean, BiFunction<ArrayList<String>, Integer, Boolean>>();
	static final String SpacingKeyALL = "#All#";
	public String lang = null;

	static {
		defaultSupplier.put(Boolean.TRUE, new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				return Boolean.TRUE;
			}
		});
		defaultSupplier.put(Boolean.FALSE, new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				return Boolean.FALSE;
			}
		});
	}

	// <previousToken, <nextToken, (tokens, nextIndex) -> hasSpace>>
	protected final HashMap<String, HashMap<String, BiFunction<ArrayList<String>, Integer, Boolean>>> hasSpaceBetweenMap = new HashMap<String, HashMap<String, BiFunction<ArrayList<String>, Integer, Boolean>>>();

	public static LangOptions getInstance(String lang) {
		LangOptions impl;
		if (lang.equals("java")) {
			impl = new JavaLangOptions();
		} else {
			return null;
		}
		impl.addSpacingOptionAround("<ENTER>", SpacingKeyALL, false);
		impl.addSpacingOptionAround("\n", SpacingKeyALL, false);
		impl.addSpacingOptionAround("<IND>", SpacingKeyALL, false);
		impl.addSpacingOptionAround("<UNIND>", SpacingKeyALL, false);
		impl.addSpacingOptionAround("\t", SpacingKeyALL, false);
		impl.initSpacingOptions();
		return impl;
	}

	boolean hasTwoLines(String str) {
		int lastIndex = str.indexOf("\n") + 1;
		return str.indexOf("\n", lastIndex) >= 0;
	}

	public HashSet<String> getKeywords() {
		return new HashSet<String>();
	}

	protected void addSpacingOptionAround(String token, String otherToken, Boolean hasSpace) {
		addSpacingOption(token, otherToken, defaultSupplier.get(hasSpace));
		addSpacingOption(otherToken, token, defaultSupplier.get(hasSpace));
	}

	protected void addSpacingOptionAround(String token, String otherToken, Supplier<Boolean> sup) {
		addSpacingOption(token, otherToken, sup);
		addSpacingOption(otherToken, token, sup);
	}

	protected void addSpacingOption(String previousToken, String nextToken, Boolean hasSpace) {
		addSpacingOption(previousToken, nextToken, defaultSupplier.get(hasSpace));
	}

	protected void addSpacingOption(String previousToken, String nextToken, final Supplier<Boolean> sup) {
		addSpacingOption(previousToken, nextToken, new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				return sup.get();
			}
		});
	}

	protected void addSpacingOption(String previousToken, String nextToken,
			BiFunction<ArrayList<String>, Integer, Boolean> sup) {
		if (!hasSpaceBetweenMap.containsKey(previousToken)) {
			hasSpaceBetweenMap.put(previousToken,
					new HashMap<String, BiFunction<ArrayList<String>, Integer, Boolean>>());
		}
		HashMap<String, BiFunction<ArrayList<String>, Integer, Boolean>> _map = hasSpaceBetweenMap.get(previousToken);
		assert !_map.containsKey(nextToken);
		_map.put(nextToken, sup);
	}

	protected void addSpacingOptionLeftKeywords(String previousToken, Boolean hasSpace) {
		addSpacingOptionLeftKeywords(previousToken, defaultSupplier.get(hasSpace));
	}

	protected void addSpacingOptionLeftKeywords(String previousToken, final Supplier<Boolean> sup) {
		addSpacingOptionLeftKeywords(previousToken, new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				return sup.get();
			}
		});
	}

	protected void addSpacingOptionLeftKeywords(String previousToken,
			BiFunction<ArrayList<String>, Integer, Boolean> sup) {
		for (String keyword : getKeywords()) {
			addSpacingOption(previousToken, keyword, sup);
		}
	}

	protected void addSpacingOptionRightKeywords(String nextToken, Boolean hasSpace) {
		addSpacingOptionRightKeywords(nextToken, defaultSupplier.get(hasSpace));
	}

	protected void addSpacingOptionRightKeywords(String nextToken, final Supplier<Boolean> sup) {
		addSpacingOptionRightKeywords(nextToken, new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				return sup.get();
			}
		});
	}

	protected void addSpacingOptionRightKeywords(String nextToken,
			BiFunction<ArrayList<String>, Integer, Boolean> sup) {
		for (String keyword : getKeywords()) {
			addSpacingOption(keyword, nextToken, sup);
		}
	}

	public abstract void initSpacingOptions();

	public boolean hasSpaceBetween(ArrayList<String> tokens, int nextI) {
		String previousToken = nextI >= 1 ? tokens.get(nextI - 1) : null;
		String nextToken = nextI > 0 && nextI < tokens.size() ? tokens.get(nextI) : null;
		if (previousToken == null || nextToken == null)
			return false;
		BiFunction<ArrayList<String>, Integer, Boolean> getter = getHasSpaceBetweenGetter(previousToken, nextToken);
		return getter.apply(tokens, nextI);
	}

	private BiFunction<ArrayList<String>, Integer, Boolean> getHasSpaceBetweenGetter(String previousToken,
			String nextToken) {
		// Checking order:
		// 1. A->B
		// 2. A->All
		// 3. All->B
		// 4. All->All
		if (hasSpaceBetweenMap.containsKey(previousToken)) {
			HashMap<String, BiFunction<ArrayList<String>, Integer, Boolean>> _map = hasSpaceBetweenMap
					.get(previousToken);
			if (_map.containsKey(nextToken)) {
				return _map.get(nextToken);
			} else if (_map.containsKey(SpacingKeyALL)) {
				return _map.get(SpacingKeyALL);
			}
		}
		HashMap<String, BiFunction<ArrayList<String>, Integer, Boolean>> _map = hasSpaceBetweenMap.get(SpacingKeyALL);
		if (_map.containsKey(nextToken)) {
			return _map.get(nextToken);
		} else {
			return _map.get(SpacingKeyALL);
		}
	}

	public int getTabSize() {
		return 4;
	}

	public char getIndentType() {
		return '\t';
	}

	public int getIndentSize() {
		return 4;
	}

	public String replaceTags(String s) {
		return s.replaceAll("<char>", "''").replaceAll("<float>", "0.0").replaceAll("<double>", "0.0")
				.replaceAll("<int>", "0").replaceAll("<long>", "0L").replaceAll("<str>", "\"\"")
				.replaceAll("<bool>", "true").replaceAll("<null>", "null");
	}

	public abstract String[] getMultiCharacterSymbolList();

	public abstract String datamask(String s, Set<String> trivialLiterals);

	public void rescue(IDocument document, Rescue[] rescues) {
	};
}
