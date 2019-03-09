package com.aixcoder.utils;

import java.util.Collections;
import java.util.HashMap;

import com.aixcoder.lang.LangOptions;

public class CodeUtils {
	private static HashMap<String, CodeUtils> instances = new HashMap<>();
	private String lang;

	public CodeUtils(String lang) {
		this.lang = lang;
	}

	public static CodeUtils getInstance(String lang) {
		if (!instances.containsKey(lang)) {
			instances.put(lang, new CodeUtils(lang));
		}
		return instances.get(lang);
	}

	public int getCurrentTokenStartOffset(String text, int offset) {
		if (offset == 0)
			return 0;
		// 存在java与py文件未分离处理
		String[] tokenList = LangOptions.getInstance(lang).getMultiCharacterSymbolList();
		if (tokenList != null) {
			for (String token : tokenList) {
				if (offset >= token.length() && text.substring(offset - token.length(), offset).equals(token)) {
					return offset - token.length();
				}
			}
		}

		int pos = offset - 1;
		if (isCharacter(text.charAt(pos))) {
			while (pos >= 0 && isCharacter(text.charAt(pos)))
				pos--;
			pos++;
		} else if (text.charAt(pos) == ' ') {
			return offset;
		}
		return pos;
	}

	public int getCurrentTokenEndOffset(String text, int offset) {
		if (offset == 0)
			return 0;
		int pos = offset - 1;
		if (pos >= 0 && pos < text.length()) {
			if (isCharacter(text.charAt(pos))) {
				while (pos >= 0 && pos < text.length() && isCharacter(text.charAt(pos))) {
					pos++;
				}
			} else if (text.charAt(pos) == ' ') {
				pos = offset;
			}
		} else {
			pos = text.length();
		}
		return pos;
	}

	public boolean isCharacter(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}

	public int getIndentSize(String text, int caretPos) {
		// current indent
		int line_start = text.lastIndexOf('\n', caretPos) + 1;

		int indent = 0;
		while (line_start < text.length()) {
			if (text.charAt(line_start) == ' ') {
				indent++;
			} else if (text.charAt(line_start) == '\t') {
				indent += LangOptions.getInstance(lang).getTabSize();
			} else {
				break;
			}
			line_start++;
		}
		return indent;
	}

	public String getIndentString(int size) {
		return getIndentString(size, false);
	}

	public String getIndentString(int size, boolean forceUseSpace) {
		size = Math.max(size, 0);
		if (forceUseSpace || LangOptions.getInstance(lang).getIndentType() == ' ') {
			return String.join("", Collections.nCopies(size, " "));
		} else {
			int tabSize = LangOptions.getInstance(lang).getTabSize();
			return String.join("", Collections.nCopies(size / tabSize, "\t")) + getIndentString(size % tabSize, true);
		}
	}
}