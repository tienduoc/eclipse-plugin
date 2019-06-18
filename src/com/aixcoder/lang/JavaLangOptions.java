package com.aixcoder.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import com.aixcoder.lib.Preference;
import com.aixcoder.utils.Pair;
import com.aixcoder.utils.Rescue;
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

	boolean isType(String s) {
		return s != null && !s.isEmpty() && (Character.isUpperCase(s.charAt(0)) || keywords.contains(s));
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
		addSpacingOption(">", "(", false); // new ArrayList<Byte>()
		addSpacingOption("?", ">", false);
		addSpacingOption(">", ">", false);
		addSpacingOption(SpacingKeyALL, ">", new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				if (nextI > 0 && isType(tokens.get(nextI - 1)))
					return false;
				if (nextI < tokens.size() - 1 && isType(tokens.get(nextI + 1)))
					return false;
				return true;
			}
		});
		addSpacingOption("<", SpacingKeyALL, new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				nextI -= 1;
				if (nextI > 0 && isType(tokens.get(nextI - 1)))
					return false;
				if (nextI < tokens.size() - 1 && isType(tokens.get(nextI + 1)))
					return false;
				return true;
			}
		});
		addSpacingOption(SpacingKeyALL, "<", new BiFunction<ArrayList<String>, Integer, Boolean>() {
			@Override
			public Boolean apply(ArrayList<String> tokens, Integer nextI) {
				if (nextI > 0 && isType(tokens.get(nextI - 1)))
					return false;
				if (nextI < tokens.size() - 1 && isType(tokens.get(nextI + 1)))
					return false;
				return true;
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
			if (c == '"' || c == '\'') {
				i = skipString(s, trivialLiterals, stringBuilder, i, c);
			}
		}
		return stringBuilder.toString();
	}

	private int skipString(String s, Set<String> trivialLiterals, StringBuilder stringBuilder, int i, char c) {
		i++;
		int strStart = i;
		for (; i < s.length(); i++) {
			if (s.charAt(i) == c)
				break;
			if (s.charAt(i) == '\\') {
				i++;
			}
		}
		String strContent = s.substring(strStart, i);
		if (trivialLiterals.contains(strContent)) {
			stringBuilder.append(strContent);
		}
		stringBuilder.append(c);
		return i;
	}

	Pattern packagePattern = Pattern.compile("^\\s*package\\s.*$");
	Pattern importPattern = Pattern.compile("^\\s*import\\s+(.*)$");

	private int prepareImports(ArrayList<Pair<String, Integer>> imports, String[] lines, int importStart) {
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (importStart == -1 && packagePattern.matcher(line).matches()) {
				importStart = i;
				for (i++; i < lines.length; i++) {
					line = lines[i];
					if (!line.trim().isEmpty()) {
						break;
					}
					importStart = i;
				}
			}
			Matcher m = importPattern.matcher(line);
			if (m.matches()) {
				imports.add(new Pair<String, Integer>(m.group(1), i));
			}
		}
		return importStart;
	}

	private void rescueImport(Rescue rescue, int importStart, ArrayList<Pair<String, Integer>> imports,
			IDocument document) {
		try {
			int prevImportStart = importStart;
			for (int i = 0; i < imports.size(); i++) {
				String importContent = imports.get(i).first;
				int compareResult = importContent.compareTo(rescue.value);
				if (compareResult > 0) {
					// stop here
					imports.add(i, new Pair<String, Integer>(rescue.value, prevImportStart + 1));
					int offset = document.getLineInformation(prevImportStart + 1).getOffset();
					document.replace(offset, 0, String.format("import %s;\n", rescue.value));
					for (i += 1; i < imports.size(); i++) {
						imports.get(i).second++;
					}
					return;
				}
			}
			imports.add(new Pair<String, Integer>(rescue.value, prevImportStart + 1));
			int offset = document.getLineInformation(prevImportStart + 1).getOffset();
			document.replace(offset, 0, String.format("import %s;\n", rescue.value));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void rescue(IDocument document, Rescue[] rescues) {
		ArrayList<Pair<String, Integer>> imports = null;
		String text = document.get();
		String[] lines = text.split("\r?\n");
		int importStart = -1;

		for (Rescue rescue : rescues) {
			if (rescue.type.equals("import")) {
				if (Preference.getAutoImport()) {
					if (imports == null) {
						imports = new ArrayList<Pair<String, Integer>>();
						importStart = prepareImports(imports, lines, importStart);
					}
					rescueImport(rescue, importStart, imports, document);
				}
			} else {
				System.out.println(String.format("Unknown rescue type %s with value=%s", rescue.type, rescue.value));
			}
		}
	}
}
