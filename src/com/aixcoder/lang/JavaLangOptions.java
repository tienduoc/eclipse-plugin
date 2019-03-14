package com.aixcoder.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.aixcoder.utils.shims.BiFunction;

public class JavaLangOptions extends LangOptions {
	public JavaLangOptions() {
		this.lang = "java";
	}

	private final static HashSet<String> keywords = new HashSet<String>(Arrays.asList("abstract", "continue", "for",
			"new", "switch", "assert", "default", "goto", "package", "synchronized", "boolean", "do", "if", "private",
			"this", "break", "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws",
			"case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char",
			"final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const",
			"float", "native", "super", "while"));

	public HashSet<String> getKeywords() {
		return keywords;
	}

	private void initHardCodeOptions() {
		addSpacingOption(SpacingKeyALL, SpacingKeyALL, true);
		addSpacingOptionAround(SpacingKeyALL, ".", false);
	}

	private boolean isIdentifier(String token) {
		return token.matches("[a-zA-Z_$][$a-zA-Z_0-9]*");
	}

	@Override
	public void initSpacingOptions() {
		initHardCodeOptions();
		initConfigurableOptions();
	}

	@Override
	public String[] getMultiCharacterSymbolList() {
		return new String[] { "+=", "-=", "++", "--", "==", "*=", "/=", "**", "%=", "!=", "<>", "||", "&&", ">=", "<=",
				"&=", "^=", "|=", "<<", ">>", "^|", "->", "::" };
	}

	private void initConfigurableOptions() {
		addSpacingOptionAround("=", SpacingKeyALL, true);
		addSpacingOptionAround("+=", SpacingKeyALL, true);
		addSpacingOptionAround("-=", SpacingKeyALL, true);
		addSpacingOptionAround("*=", SpacingKeyALL, true);
		addSpacingOptionAround("/=", SpacingKeyALL, true);
		addSpacingOptionAround("%=", SpacingKeyALL, true);
		addSpacingOptionAround("<<=", SpacingKeyALL, true);
		addSpacingOptionAround(">>=", SpacingKeyALL, true);
		addSpacingOptionAround("&=", SpacingKeyALL, true);
		addSpacingOptionAround("^=", SpacingKeyALL, true);
		addSpacingOptionAround("|=", SpacingKeyALL, true);

		addSpacingOptionAround("&&", SpacingKeyALL, true);
		addSpacingOptionAround("||", SpacingKeyALL, true);

		addSpacingOptionAround("==", SpacingKeyALL, true);
		addSpacingOptionAround("!=", SpacingKeyALL, true);

		addSpacingOption(">", SpacingKeyALL, true);
		addSpacingOption(SpacingKeyALL, ">", new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				// > ***: has space if (1. is not closing bracket && 2. space around relational
				// op)
				int level = 1;
				for (int i = nextI - 1; i >= 0; i--) {
					if (!tokens.get(i).matches("[a-zA-Z0-9_$]+|[><,]")) {
						break;
					}
					if (tokens.get(i).equals("<")) {
						level--;
						if (level == 0) {
							return false;
						}
					} else if (tokens.get(i).equals(">")) {
						level++;
					}
				}
				return true;
			}
		});
		addSpacingOption("<", SpacingKeyALL, new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				return nextI >= 2 && tokens.size() > nextI - 2 && tokens.get(nextI - 2).matches("[a-z0-9_$]+|[><,]");
			}
		});
		addSpacingOption(SpacingKeyALL, "<", new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				return nextI >= 1 && tokens.size() > nextI - 1 && tokens.get(nextI - 1).matches("[a-z0-9_$]+|[><,]");
			}
		});
		addSpacingOptionAround(">=", SpacingKeyALL, true);
		addSpacingOptionAround("<=", SpacingKeyALL, true);

		addSpacingOptionAround("&", SpacingKeyALL, true);
		addSpacingOptionAround("|", SpacingKeyALL, true);
		addSpacingOptionAround("^", SpacingKeyALL, true);

		final BiFunction<ArrayList<String>, Integer, Boolean> plusMinusSignBinaryOpChecker = new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer preOpTokenI) {
				if (preOpTokenI >= 0) {
					String preOpToken = tokens.get(preOpTokenI);
					if (preOpToken.matches("--|\\+\\+|[)\\]\"']|[a-zA-Z_][a-zA-Z0-9_]+"))
						return false;
					else
						return false;
				}
				return false;
			}
		};
		addSpacingOption("+", SpacingKeyALL, new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				return plusMinusSignBinaryOpChecker.apply(tokens, nextI - 2);
			}
		});
		addSpacingOption(SpacingKeyALL, "+", new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				return plusMinusSignBinaryOpChecker.apply(tokens, nextI - 1);
			}
		});
		addSpacingOption("-", SpacingKeyALL, new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				return plusMinusSignBinaryOpChecker.apply(tokens, nextI - 2);
			}
		});
		addSpacingOption(SpacingKeyALL, "-", new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				return plusMinusSignBinaryOpChecker.apply(tokens, nextI - 1);
			}
		});

		addSpacingOptionAround("*", SpacingKeyALL, true);
		addSpacingOptionAround("/", SpacingKeyALL, true);
		addSpacingOptionAround("%", SpacingKeyALL, true);

		addSpacingOptionAround("<<", SpacingKeyALL, true);
		addSpacingOptionAround(">>", SpacingKeyALL, true);
		addSpacingOptionAround(">>>", SpacingKeyALL, true);

		addSpacingOptionAround("!", SpacingKeyALL, false);
		addSpacingOptionAround("++", SpacingKeyALL, false);
		addSpacingOptionAround("--", SpacingKeyALL, false);

		addSpacingOptionAround("->", SpacingKeyALL, true);

		addSpacingOptionAround("::", SpacingKeyALL, false);

		addSpacingOption(",", SpacingKeyALL, true);
		addSpacingOption(SpacingKeyALL, ",", false);

		addSpacingOption(";", "<ENTER>", false);
		addSpacingOption(";", SpacingKeyALL, false);

		addSpacingOption(SpacingKeyALL, ";", false);

		addSpacingOption("(", SpacingKeyALL, false);
		addSpacingOption(SpacingKeyALL, ")", false);
		addSpacingOption(SpacingKeyALL, "[", false);
		addSpacingOption("[", SpacingKeyALL, false);
		addSpacingOption(SpacingKeyALL, "]", false);

//        addSpacingOption(":", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_WITHIN_ARRAY_INITIALIZER_BRACES);
		addSpacingOption("{", SpacingKeyALL, new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				if (nextI >= 3 && tokens.get(nextI - 2).equals("]") && tokens.get(nextI - 3).equals("["))
					return true;
				return true;
			}
		});
		addSpacingOption(SpacingKeyALL, "}", new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				int i = findMatching(tokens, nextI, "}", "{");
				if (i >= 0) {
					return hasSpaceBetween(tokens, i + 1);
				}
				return true;
			}
		});
//        addSpacingOption( ":", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AFTER_TYPE_CAST);
		addSpacingOption(SpacingKeyALL, "(", false);
//        addSpacingOption( ":", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_BEFORE_METHOD_PARENTHESES);
		addSpacingOption("if", "(", true);
		addSpacingOption("while", "(", true);
		addSpacingOption("for", "(", true);
		addSpacingOption("try", "(", true);
		addSpacingOption("catch", "(", true);
		addSpacingOption("switch", "(", true);
		addSpacingOption("synchronized", "(", true);
//        addSpacingOption( SpacingKeyALL, "{", () -> getCodeStyleSettings().SPACE_BEFORE_CLASS_LBRACE);
		addSpacingOption(")", "{", true);
		addSpacingOption("else", "{", true);
		addSpacingOption("do", "{", true);
		addSpacingOption("finally", "{", true);
//        addSpacingOption( SpacingKeyALL, "{", () -> getCodeStyleSettings().SPACE_BEFORE_ARRAY_INITIALIZER_LBRACE);
//        addSpacingOption( SpacingKeyALL, "{", () -> getCodeStyleSettings().SPACE_BEFORE_ANNOTATION_ARRAY_INITIALIZER_LBRACE);
		addSpacingOption(SpacingKeyALL, "else", true);
		addSpacingOption(SpacingKeyALL, "while", true);
		addSpacingOption(SpacingKeyALL, "catch", true);
		addSpacingOption(SpacingKeyALL, "finally", true);
		addSpacingOption(SpacingKeyALL, "?", true);
		addSpacingOption("?", SpacingKeyALL, true);
		addSpacingOption(SpacingKeyALL, ":", true);
		addSpacingOption(":", SpacingKeyALL, true);
//        addSpacingOption( ":", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_BEFORE_TYPE_PARAMETER_LIST);

		addSpacingOption("@", SpacingKeyALL, false);
	}

	private int findMatching(ArrayList<String> tokens, Integer nextI, String rPar, String lPar) {
		int direction = tokens.get(nextI).equals(rPar) ? -1 : 1;
		int p = 0;
		int i = nextI;
		for (; i >= 0 && i < tokens.size(); i += direction) {
			if (tokens.get(i).equals(rPar)) {
				p++;
			} else if (tokens.get(i).equals(lPar)) {
				p--;
			}
			if (p == 0) {
				break;
			}
		}
		if (tokens.size() - 1 == i)
			i = -1;
		return i;
	}

	@Override
	public String datamask(String s, Set<String> trivialLiterals) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			stringBuilder.append(c);
			if (c == '"') {
				i++;
				int strStart = i;
				for (; i < s.length(); i++) {
					if (s.charAt(i) == '"')
						break;
					if (s.charAt(i) == '\\') {
						i++;
					}
				}
				String strContent = s.substring(strStart, i);
				if (trivialLiterals.contains(strContent)) {
					stringBuilder.append(strContent);
				}
				stringBuilder.append("\"");

			} else if (c == '\'') {
				i++;
				int strStart = i;
				for (; i < s.length(); i++) {
					stringBuilder.append(s.charAt(i));
					if (s.charAt(i) == '\'')
						break;
					if (s.charAt(i) == '\\') {
						i++;
					}
				}
				String strContent = s.substring(strStart, i);
				if (trivialLiterals.contains(strContent)) {
					stringBuilder.append(strContent);
				}
				stringBuilder.append("'");
			}
		}
		return stringBuilder.toString();
	}
}
