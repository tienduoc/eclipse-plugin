package com.aixcoder.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.aixcoder.lang.LangOptions;
import com.aixcoder.utils.shims.CollectionUtils;
import com.aixcoder.utils.shims.Predicate;

public class TokenUtils {

	static class TokensUpdate {
		int newIndent;
		int newIndex;
	}

	public static TokensUpdate renderToken(StringBuilder builder, List<String> tokens, int index, int indent,
			LangOptions langOptions) {
		CodeUtils utils = CodeUtils.getInstance(langOptions.lang);
		while (index < tokens.size()) {
			boolean shouldBreak = false;
			String token = tokens.get(index);
			if (token.equals("<ENTER>")) {
				builder.append("\n");
				String indentString = utils.getIndentString(indent);
				builder.append(indentString);
			} else if (token.equals("<IND>")) {
				int indentSize = langOptions.getIndentSize();
				indent += indentSize;
				builder.append(utils.getIndentString(indentSize));
			} else if (token.equals("<BREAK>")) {
			}

			else if (token.equals("<UNIND>")) {
				int remainingIndentSize = langOptions.getIndentSize();
				indent -= remainingIndentSize;
				int i;
				for (i = builder.length() - 1; i >= 0 && remainingIndentSize > 0; i--) {
					char c = builder.charAt(i);
					if (c == ' ') {
						remainingIndentSize -= 1;
					} else if (c == '\t') {
						remainingIndentSize -= langOptions.getTabSize();
					} else {
						break;
					}
				}
				// remove the indents
				builder.delete(i + 1, builder.length());
				while (remainingIndentSize++ < 0) {
					// compensate with spaces
					builder.append(' ');
				}
			} else {
				for (String s : new String[] { "int", "float", "double", "long", "bool", "str", "char" }) {
					String prefix = "<" + s + ">";
					if (token.startsWith(prefix)) {
						if (token.length() > prefix.length()) {
							String newToken = token.substring(prefix.length());
							if (token.startsWith("<str>")) {
								newToken = "\"" + newToken.replaceAll("<str_space>", " ") + "\"";
							} else if (token.startsWith("<char>")) {
								newToken = "'" + newToken + "'";
							}
							token = newToken;
						}
						break;
					}
				}
				builder.append(token);
				shouldBreak = true;
			}
			index++;
			if (shouldBreak)
				break;
		}

		TokensUpdate r = new TokensUpdate();
		r.newIndent = indent;
		r.newIndex = index;
		return r;
	}

	public static RenderedInfo renderTokens(String lang, String line, List<String> tokens, String current_yu) {
		CodeUtils utils = CodeUtils.getInstance(lang);
		// 将<BREAK>标签移除,不做字符前后的空格处理
		if (tokens == null || tokens.isEmpty()) {
			return null;
		}
		tokens = new ArrayList<String>(tokens);
		tokens.removeAll(Collections.singleton("<BREAK>"));
		ArrayList<String> allTokens = new ArrayList<String>(
				Arrays.asList(line.split("(?<=[^a-zA-Z0-9_]) *|(?=[^a-zA-Z0-9_]) *")));
		CollectionUtils.removeIf(allTokens, new Predicate<String>() {
			@Override
			public boolean test(String token) {
				return token.length() == 0;
			}
		});
		int indexInAllTokens = allTokens.size();
		String firstToken = tokens.isEmpty() ? "" : tokens.get(0);
		int indexInTokens = 0;
		StringBuilder builder = new StringBuilder();
		if (current_yu.length() > 0) {
			// 当前光标下的词current_yu没处理完，和tokens第一个拼接
			String lastAllToken = allTokens.size() > 0 ? allTokens.get(allTokens.size() - 1) : "";
			lastAllToken = lastAllToken + firstToken;
			if (allTokens.size() > 0) {
				allTokens.set(allTokens.size() - 1, lastAllToken);
				allTokens.addAll(tokens.subList(1, tokens.size()));
				indexInTokens = 1;
				builder.append(tokens.get(0));
			}
		} else {
			// 立刻开始下一个词
			if (firstToken.length() == 0) {
				// 第一个有可能是空，跳过
				allTokens.addAll(tokens.subList(1, tokens.size()));
				indexInTokens = 1;
			} else {
				allTokens.addAll(tokens);
			}
		}

		int indent = utils.getIndentSize(line, line.length());
		LangOptions langOptions = LangOptions.getInstance(lang);
		for (int i = indexInTokens; i < tokens.size();) {
			if (langOptions.hasSpaceBetween(allTokens, indexInAllTokens)) {
				builder.append(' ');
			}
			TokensUpdate update = renderToken(builder, tokens, i, indent, langOptions);
			indent = update.newIndent;
			int nextIndexInAllTokens = indexInAllTokens + update.newIndex - i;
			indexInAllTokens = nextIndexInAllTokens;
			i = update.newIndex;
		}
		String rendered = builder.toString();

		// 遗留问题:如果两个token一样时,第一个token输入完成后,第二个token无法正常显示
		if (rendered.length() > 0 && line.length() > 0
				&& (rendered.charAt(0) == ' ' && line.charAt(line.length() - 1) == ' ')) {
			// If first space is extraneous, remove it
			rendered = rendered.substring(1);
		}
		if (rendered.startsWith("<UNK>")) {
			// Hide first <UNK>
			rendered = rendered.substring("<UNK>".length());
		}

		rendered = replaceTags(rendered, langOptions);
		return new RenderedInfo(current_yu + rendered, rendered);
	}

	private static String replaceTags(String s, LangOptions langOptions) {
		s = s.replaceAll("<UNK>", "id");
		s = langOptions.replaceTags(s);
		return s;
	}
}