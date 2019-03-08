package com.aixcoder.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;


public class JavaLangOptions extends LangOptions {
    private final static HashSet<String> keywords = new HashSet<>(Arrays.asList("abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package", "synchronized",
            "boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte",
            "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient", "catch",
            "extends", "int", "short", "try", "char", "final", "interface", "static", "void", "class", "finally",
            "long", "strictfp", "volatile", "const", "float", "native", "super", "while"));
	private Map<String, String> options;

    public JavaLangOptions(Map<String, String> options) {
		this.options = options;
	}


	public HashSet<String> getKeywords() {
        return keywords;
    }


    private void initHardCodeOptions() {
        addSpacingOption(SpacingKeyALL, SpacingKeyALL, true);
        addSpacingOptionAround(SpacingKeyALL, ".", false);
    }

    private boolean isIdentifier(String token) {
        return token.matches("[a-zA-Z_][a-zA-Z_0-9]*");
    }

    @Override
    public void initSpacingOptions() {
        initHardCodeOptions();
        initConfigurableOptions();
    }

    @Override
    public String[] getMultiCharacterSymbolList() {
        return new String[]{"+=", "-=", "++", "--", "==", "*=", "/=", "**", "%=", "!=", "<>", "||", "&&", ">=", "<=", "&=", "^=", "|=", "<<", ">>", "^|", "->", "::"};
    }

    private void initConfigurableOptions() {
//        addSpacingOptionAround("=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS);
//        addSpacingOptionAround("+=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS);
//        addSpacingOptionAround("-=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS);
//        addSpacingOptionAround("*=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS);
//        addSpacingOptionAround("/=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS);
//        addSpacingOptionAround("%=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS);
//        addSpacingOptionAround("<<=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS);
//        addSpacingOptionAround(">>=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS);
//        addSpacingOptionAround("&=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS);
//        addSpacingOptionAround("^=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS);
//        addSpacingOptionAround("|=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_ASSIGNMENT_OPERATORS);
//
//        addSpacingOptionAround("&&", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_LOGICAL_OPERATORS);
//        addSpacingOptionAround("||", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_LOGICAL_OPERATORS);
//
//        addSpacingOptionAround("==", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_EQUALITY_OPERATORS);
//        addSpacingOptionAround("!=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_EQUALITY_OPERATORS);

//        addSpacingOption(">", SpacingKeyALL, (tokens, nextI) -> {
//            // > ***: has space if (1. is closing bracket || 2. space around relational op)
//            int level = 1;
//            for (int i = nextI - 2; i >= 0; i--) {
//                if (!tokens.get(i).matches("[a-zA-Z0-9_$]+|[><,]")) {
//                    break;
//                }
//                switch (tokens.get(i)) {
//                    case "<":
//                        level--;
//                        if (level == 0) {
//                            return true;
//                        }
//                        break;
//                    case ">":
//                        level++;
//                        break;
//                }
//            }
//            return getCodeStyleSettings().SPACE_AROUND_RELATIONAL_OPERATORS;
//        });
//        addSpacingOption(SpacingKeyALL, ">", (tokens, nextI) -> {
//            // > ***: has space if (1. is not closing bracket && 2. space around relational op)
//            int level = 1;
//            for (int i = nextI - 1; i >= 0; i--) {
//                if (!tokens.get(i).matches("[a-zA-Z0-9_$]+|[><,]")) {
//                    break;
//                }
//                switch (tokens.get(i)) {
//                    case "<":
//                        level--;
//                        if (level == 0) {
//                            return false;
//                        }
//                        break;
//                    case ">":
//                        level++;
//                        break;
//                }
//            }
//            return getCodeStyleSettings().SPACE_AROUND_RELATIONAL_OPERATORS;
//        });
//        addSpacingOption("<", SpacingKeyALL, (tokens, nextI) -> {
//            // > ***: has space if (1. is not closing bracket && 2. space around relational op)
//            int level = 1;
//            for (int i = nextI; i < tokens.size(); i++) {
//                if (!tokens.get(i).matches("[a-zA-Z0-9_$]+|[><,]")) {
//                    break;
//                }
//                switch (tokens.get(i)) {
//                    case "<":
//                        level++;
//                        break;
//                    case ">":
//                        level--;
//                        if (level == 0) {
//                            return false;
//                        }
//                        break;
//                }
//            }
//            return getCodeStyleSettings().SPACE_AROUND_RELATIONAL_OPERATORS;
//        });
//        addSpacingOption(SpacingKeyALL, "<", (tokens, nextI) -> {
//            // > ***: has space if (1. is not closing bracket && 2. space around relational op)
//            int level = 1;
//            for (int i = nextI + 1; i < tokens.size(); i++) {
//                if (!tokens.get(i).matches("[a-zA-Z0-9_$]+|[><,]")) {
//                    break;
//                }
//                switch (tokens.get(i)) {
//                    case "<":
//                        level++;
//                        break;
//                    case ">":
//                        level--;
//                        if (level == 0) {
//                            return false;
//                        }
//                        break;
//                }
//            }
//            return getCodeStyleSettings().SPACE_AROUND_RELATIONAL_OPERATORS;
//        });
//        addSpacingOptionAround(">=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_RELATIONAL_OPERATORS);
//        addSpacingOptionAround("<=", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_RELATIONAL_OPERATORS);
//
//        addSpacingOptionAround("&", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_BITWISE_OPERATORS);
//        addSpacingOptionAround("|", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_BITWISE_OPERATORS);
//        addSpacingOptionAround("^", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_BITWISE_OPERATORS);
//
//        BiFunction<ArrayList<String>, Integer, Boolean> plusMinusSignBinaryOpChecker = (tokens, preOpTokenI) -> {
//            if (preOpTokenI >= 0) {
//                String preOpToken = tokens.get(preOpTokenI);
//                if (preOpToken.matches("--|\\+\\+|[)\\]\"']|[a-zA-Z_][a-zA-Z0-9_]+"))
//                    return getCodeStyleSettings().SPACE_AROUND_ADDITIVE_OPERATORS;
//                else
//                    return getCodeStyleSettings().SPACE_AROUND_UNARY_OPERATOR;
//            }
//            return getCodeStyleSettings().SPACE_AROUND_UNARY_OPERATOR;
//        };
//        addSpacingOption("+", SpacingKeyALL, (tokens, nextI) -> plusMinusSignBinaryOpChecker.apply(tokens, nextI - 2));
//        addSpacingOption(SpacingKeyALL, "+", (tokens, nextI) -> plusMinusSignBinaryOpChecker.apply(tokens, nextI - 1));
//        addSpacingOption("-", SpacingKeyALL, (tokens, nextI) -> plusMinusSignBinaryOpChecker.apply(tokens, nextI - 2));
//        addSpacingOption(SpacingKeyALL, "-", (tokens, nextI) -> plusMinusSignBinaryOpChecker.apply(tokens, nextI - 1));
//
//        addSpacingOptionAround("*", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_MULTIPLICATIVE_OPERATORS);
//        addSpacingOptionAround("/", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_MULTIPLICATIVE_OPERATORS);
//        addSpacingOptionAround("%", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_MULTIPLICATIVE_OPERATORS);
//
//        addSpacingOptionAround("<<", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_SHIFT_OPERATORS);
//        addSpacingOptionAround(">>", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_SHIFT_OPERATORS);
//        addSpacingOptionAround(">>>", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_SHIFT_OPERATORS);
//
//        addSpacingOptionAround("!", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_UNARY_OPERATOR);
//        addSpacingOptionAround("++", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_UNARY_OPERATOR);
//        addSpacingOptionAround("--", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_UNARY_OPERATOR);
//
//        addSpacingOptionAround("->", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_LAMBDA_ARROW);
//
//        addSpacingOptionAround("::", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AROUND_METHOD_REF_DBL_COLON);
//
//        addSpacingOption(",", SpacingKeyALL, (tokens, nextI) -> {
//            int templateBracketsLeft = 0;
//            for (int i = nextI - 1; i >= 0; i--) {
//                if (tokens.get(i).equals(">"))
//                    templateBracketsLeft += 1;
//                else if (tokens.get(i).equals("<"))
//                    templateBracketsLeft -= 1;
//            }
//            int templateBracketsRight = 0;
//            for (int i = nextI; i < tokens.size(); i++) {
//                if (tokens.get(i).equals(">"))
//                    templateBracketsRight += 1;
//                else if (tokens.get(i).equals("<"))
//                    templateBracketsRight -= 1;
//            }
//            if (templateBracketsLeft != 0 || templateBracketsRight != 0) {
//                return getCodeStyleSettings().SPACE_AFTER_COMMA_IN_TYPE_ARGUMENTS;
//            }
//
//            return options.get("");
//        });
//        addSpacingOption(SpacingKeyALL, ",", () -> getCodeStyleSettings().SPACE_BEFORE_COMMA);
//
//        addSpacingOption(";", "<ENTER>", false);
//        addSpacingOption(";", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AFTER_SEMICOLON);
//
//        addSpacingOption(SpacingKeyALL, ";", () -> getCodeStyleSettings().SPACE_BEFORE_SEMICOLON);
//
//        addSpacingOption("(", SpacingKeyALL, (tokens, nextI) -> {
//            CommonCodeStyleSettings style = getCodeStyleSettings();
//            if (nextI >= 2) {
//                String pprev = tokens.get(nextI - 2);
//                if (isIdentifier(pprev)) {
//                    // method related
//                    if (nextI >= 3 && !isIdentifier(tokens.get(nextI - 3))) {
//                        return style.SPACE_WITHIN_METHOD_CALL_PARENTHESES;
//                    } else if (tokens.get(nextI).equals(")")) {
//                        return style.SPACE_WITHIN_EMPTY_METHOD_CALL_PARENTHESES;
//                    } else if (nextI >= 3 && isIdentifier(tokens.get(nextI - 3))) {
//                        if (tokens.get(nextI).equals(")"))
//                            return style.SPACE_WITHIN_EMPTY_METHOD_PARENTHESES;
//                        else
//                            return style.SPACE_WITHIN_METHOD_PARENTHESES;
//                    }
//                } else if (pprev.equals("if")) {
//                    return style.SPACE_WITHIN_IF_PARENTHESES;
//                } else if (pprev.equals("while")) {
//                    return style.SPACE_WITHIN_IF_PARENTHESES;
//                } else if (pprev.equals("for")) {
//                    return style.SPACE_WITHIN_IF_PARENTHESES;
//                } else if (pprev.equals("try")) {
//                    return style.SPACE_WITHIN_IF_PARENTHESES;
//                } else if (pprev.equals("catch")) {
//                    return style.SPACE_WITHIN_IF_PARENTHESES;
//                } else if (pprev.equals("switch")) {
//                    return style.SPACE_WITHIN_IF_PARENTHESES;
//                } else if (pprev.equals("synchronized")) {
//                    return style.SPACE_WITHIN_IF_PARENTHESES;
//                } else if (isIdentifier(pprev) && nextI >= 4) {
//                    int i = nextI - 4;
//                    if (i >= 1 && !tokens.get(i).equals("(")) {
//                        return style.SPACE_WITHIN_CAST_PARENTHESES;
//                    }
//                }
//            }
//            return style.SPACE_WITHIN_PARENTHESES;
//        });
//        addSpacingOption(SpacingKeyALL, ")", (tokens, nextI) -> {
//            int i = findMatching(tokens, nextI, ")", "(");
//            if (i >= 0) {
//                return hasSpaceBetween(tokens, i + 1);
//            }
//            return getCodeStyleSettings().SPACE_WITHIN_PARENTHESES;
//        });
//        addSpacingOption("[", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_WITHIN_BRACKETS);
//        addSpacingOption(SpacingKeyALL, "]", () -> getCodeStyleSettings().SPACE_WITHIN_BRACKETS);
//
////        addSpacingOption(":", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_WITHIN_ARRAY_INITIALIZER_BRACES);
//        addSpacingOption("{", SpacingKeyALL, (tokens, nextI) -> {
//            if (nextI >= 3 && tokens.get(nextI - 2).equals("]") && tokens.get(nextI - 3).equals("["))
//                return getCodeStyleSettings().SPACE_WITHIN_EMPTY_ARRAY_INITIALIZER_BRACES;
//            return getCodeStyleSettings().SPACE_WITHIN_BRACES;
//        });
//        addSpacingOption(SpacingKeyALL, "}", (tokens, nextI) -> {
//            int i = findMatching(tokens, nextI, "}", "{");
//            if (i >= 0) {
//                return hasSpaceBetween(tokens, i + 1);
//            }
//            return getCodeStyleSettings().SPACE_WITHIN_BRACES;
//        });
////        addSpacingOption( ":", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AFTER_TYPE_CAST);
//        addSpacingOption(SpacingKeyALL, "(", () -> getCodeStyleSettings().SPACE_BEFORE_METHOD_CALL_PARENTHESES);
////        addSpacingOption( ":", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_BEFORE_METHOD_PARENTHESES);
//        addSpacingOption("if", "(", () -> getCodeStyleSettings().SPACE_BEFORE_IF_PARENTHESES);
//        addSpacingOption("while", "(", () -> getCodeStyleSettings().SPACE_BEFORE_WHILE_PARENTHESES);
//        addSpacingOption("for", "(", () -> getCodeStyleSettings().SPACE_BEFORE_FOR_PARENTHESES);
//        addSpacingOption("try", "(", () -> getCodeStyleSettings().SPACE_BEFORE_TRY_PARENTHESES);
//        addSpacingOption("catch", "(", () -> getCodeStyleSettings().SPACE_BEFORE_CATCH_PARENTHESES);
//        addSpacingOption("switch", "(", () -> getCodeStyleSettings().SPACE_BEFORE_SWITCH_PARENTHESES);
//        addSpacingOption("synchronized", "(", () -> getCodeStyleSettings().SPACE_BEFORE_SYNCHRONIZED_PARENTHESES);
////        addSpacingOption( SpacingKeyALL, "{", () -> getCodeStyleSettings().SPACE_BEFORE_CLASS_LBRACE);
//        addSpacingOption(")", "{", (tokens, nextI) -> {
//            int i = findMatching(tokens, nextI - 1, ")", "(");
//            if (i >= 1) {
//                CommonCodeStyleSettings style = getCodeStyleSettings();
//                String pprev = tokens.get(i - 1);
//                switch (pprev) {
//                    case "if":
//                        return style.SPACE_BEFORE_IF_LBRACE;
//                    case "for":
//                        return style.SPACE_BEFORE_FOR_LBRACE;
//                    case "while":
//                        return style.SPACE_BEFORE_WHILE_LBRACE;
//                    case "switch":
//                        return style.SPACE_BEFORE_SWITCH_LBRACE;
//                    case "try":
//                        return style.SPACE_BEFORE_TRY_LBRACE;
//                    case "catch":
//                        return style.SPACE_BEFORE_CATCH_LBRACE;
//                    case "synchronized":
//                        return style.SPACE_BEFORE_SYNCHRONIZED_LBRACE;
//                }
//                if (isIdentifier(pprev))
//                    return style.SPACE_BEFORE_METHOD_LBRACE;
//            }
//            return true;
//        });
//        addSpacingOption("else", "{", () -> getCodeStyleSettings().SPACE_BEFORE_ELSE_LBRACE);
//        addSpacingOption("do", "{", () -> getCodeStyleSettings().SPACE_BEFORE_DO_LBRACE);
//        addSpacingOption("finally", "{", () -> getCodeStyleSettings().SPACE_BEFORE_FINALLY_LBRACE);
////        addSpacingOption( SpacingKeyALL, "{", () -> getCodeStyleSettings().SPACE_BEFORE_ARRAY_INITIALIZER_LBRACE);
////        addSpacingOption( SpacingKeyALL, "{", () -> getCodeStyleSettings().SPACE_BEFORE_ANNOTATION_ARRAY_INITIALIZER_LBRACE);
//        addSpacingOption(SpacingKeyALL, "else", () -> getCodeStyleSettings().SPACE_BEFORE_ELSE_KEYWORD);
//        addSpacingOption(SpacingKeyALL, "while", () -> getCodeStyleSettings().SPACE_BEFORE_WHILE_KEYWORD);
//        addSpacingOption(SpacingKeyALL, "catch", () -> getCodeStyleSettings().SPACE_BEFORE_CATCH_KEYWORD);
//        addSpacingOption(SpacingKeyALL, "finally", () -> getCodeStyleSettings().SPACE_BEFORE_FINALLY_KEYWORD);
//        addSpacingOption(SpacingKeyALL, "?", () -> getCodeStyleSettings().SPACE_BEFORE_QUEST);
//        addSpacingOption("?", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AFTER_QUEST);
//        addSpacingOption(SpacingKeyALL, ":", () -> getCodeStyleSettings().SPACE_BEFORE_COLON);
//        addSpacingOption(":", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_AFTER_COLON);
////        addSpacingOption( ":", SpacingKeyALL, () -> getCodeStyleSettings().SPACE_BEFORE_TYPE_PARAMETER_LIST);
//
//        addSpacingOption("@", SpacingKeyALL, false);
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
                    if (s.charAt(i) == '"') break;
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
                    if (s.charAt(i) == '\'') break;
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
