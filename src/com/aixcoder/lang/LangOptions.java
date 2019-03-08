package com.aixcoder.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class LangOptions {

    protected final static HashMap<Boolean, BiFunction<ArrayList<String>, Integer, Boolean>> defaultSupplier = new HashMap<>();
    static final String SpacingKeyALL = "#All#";

    static {
        defaultSupplier.put(Boolean.TRUE, (tokens, nextI) -> Boolean.TRUE);
        defaultSupplier.put(Boolean.FALSE, (tokens, nextI) -> Boolean.FALSE);
    }

    // <previousToken, <nextToken, (tokens, nextIndex) -> hasSpace>>
    protected final HashMap<String, HashMap<String, BiFunction<ArrayList<String>, Integer, Boolean>>> hasSpaceBetweenMap = new HashMap<>();

    public static LangOptions getInstance(String lang, Map<String, String> options) {
        LangOptions impl;
        switch (lang) {
            case "java":
                impl = new JavaLangOptions(options);
                break;
            default:
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
        return new HashSet<>();
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

    protected void addSpacingOption(String previousToken, String nextToken, Supplier<Boolean> sup) {
        addSpacingOption(previousToken, nextToken, (tokens, nextI) -> sup.get());
    }

    protected void addSpacingOption(String previousToken, String nextToken, BiFunction<ArrayList<String>, Integer, Boolean> sup) {
        if (!hasSpaceBetweenMap.containsKey(previousToken)) {
            hasSpaceBetweenMap.put(previousToken, new HashMap<>());
        }
        HashMap<String, BiFunction<ArrayList<String>, Integer, Boolean>> _map = hasSpaceBetweenMap.get(previousToken);
        assert !_map.containsKey(nextToken);
        _map.put(nextToken, sup);
    }


    protected void addSpacingOptionLeftKeywords(String previousToken, Boolean hasSpace) {
        addSpacingOptionLeftKeywords(previousToken, defaultSupplier.get(hasSpace));
    }

    protected void addSpacingOptionLeftKeywords(String previousToken, Supplier<Boolean> sup) {
        addSpacingOptionLeftKeywords(previousToken, (tokens, nextI) -> sup.get());
    }

    protected void addSpacingOptionLeftKeywords(String previousToken, BiFunction<ArrayList<String>, Integer, Boolean> sup) {
        for (String keyword : getKeywords()) {
            addSpacingOption(previousToken, keyword, sup);
        }
    }


    protected void addSpacingOptionRightKeywords(String nextToken, Boolean hasSpace) {
        addSpacingOptionRightKeywords(nextToken, defaultSupplier.get(hasSpace));
    }

    protected void addSpacingOptionRightKeywords(String nextToken, Supplier<Boolean> sup) {
        addSpacingOptionRightKeywords(nextToken, (tokens, nextI) -> sup.get());
    }

    protected void addSpacingOptionRightKeywords(String nextToken, BiFunction<ArrayList<String>, Integer, Boolean> sup) {
        for (String keyword : getKeywords()) {
            addSpacingOption(keyword, nextToken, sup);
        }
    }

    public abstract void initSpacingOptions();

    public boolean hasSpaceBetween(ArrayList<String> tokens, int nextI) {
        String previousToken = nextI >= 1 ? tokens.get(nextI - 1) : null;
        String nextToken = nextI > 0 && nextI < tokens.size() ? tokens.get(nextI) : null;
        if (previousToken == null || nextToken == null) return false;
        BiFunction<ArrayList<String>, Integer, Boolean> getter = getHasSpaceBetweenGetter(previousToken, nextToken);
        return getter.apply(tokens, nextI);
    }

    private BiFunction<ArrayList<String>, Integer, Boolean> getHasSpaceBetweenGetter(String previousToken, String nextToken) {
        // Checking order:
        // 1. A->B
        // 2. A->All
        // 3. All->B
        // 4. All->All
        if (hasSpaceBetweenMap.containsKey(previousToken)) {
            HashMap<String, BiFunction<ArrayList<String>, Integer, Boolean>> _map = hasSpaceBetweenMap.get(previousToken);
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
        return s.replaceAll("<char>", "''")
                .replaceAll("<float>", "0.0")
                .replaceAll("<double>", "0.0")
                .replaceAll("<int>", "0")
                .replaceAll("<long>", "0L")
                .replaceAll("<str>", "\"\"")
                .replaceAll("<bool>", "true")
                .replaceAll("<null>", "null");
    }

    public abstract String[] getMultiCharacterSymbolList();

    public abstract String datamask(String s, Set<String> trivialLiterals);
}
