package com.aixcoder.utils;

import java.util.HashSet;
import java.util.Set;

import com.aixcoder.core.API;
import com.aixcoder.lang.LangOptions;

/**
 * 数据脱敏
 */
public class DataMasking {
	/**
	 * 不需要脱敏的简单字符串，lazy loading
	 */
	static Set<String> trivialLiterals;

	/**
	 * 字符串脱敏，将除trivialLiterals里的简单字符串以外的其它字符串变成空字符串
	 *
	 * @param s 原始字符串
	 * @return 脱敏后字符串
	 */
	public static String mask(String s) {
		if (trivialLiterals == null) {
			try {
				trivialLiterals = new HashSet<String>();
				String[] literals = API.getTrivialLiterals();
				for (String l : literals) {
					String lit = String.valueOf(l);
					if (lit.startsWith("<str>")) {
						trivialLiterals.add(lit.substring("<str>".length()).replaceAll("<str_space>", " "));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return s;
		} else {
			return LangOptions.getInstance("java").datamask(s, trivialLiterals);
		}
	}
}
