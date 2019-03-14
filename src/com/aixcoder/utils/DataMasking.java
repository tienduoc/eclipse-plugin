package com.aixcoder.utils;

import java.util.Set;

import com.aixcoder.lang.LangOptions;
import com.aixcoder.lib.HttpRequest;
import com.aixcoder.lib.JSON;

/**
 * 数据脱敏
 */
public class DataMasking {
	/**
	 * 不需要脱隐的简单字符串，lazy loading
	 */
	static Set<String> trivialLiterals;

	/**
	 * 字符串脱隐，将除trivialLiterals里的简单字符串以外的其它字符串变成空字符串
	 *
	 * @param s 原始字符串
	 * @return 脱隐后字符串
	 */
	public static String mask(String s) {
		if (trivialLiterals == null) {
			HttpRequest httpRequest = HttpRequest.post(Predict.URL + "trivial_literals")
					.connectTimeout(Predict.TIME_OUT).readTimeout(Predict.TIME_OUT).useCaches(false)
					.contentType("x-www-form-urlencoded", "UTF-8").form("uuid", "eclipse-plugin")
					.form("ext", "java(Java)");
			String string = httpRequest.body();
			JSON json = JSON.decode(string);
			String[] literals = JSON.getStringList(json.getList());
			for (String l : literals) {
				String lit = String.valueOf(l);
				if (lit.startsWith("<str>")) {
					trivialLiterals.add(lit.substring("<str>".length()).replaceAll("<str_space>", " "));
				}
			}
		}
		return LangOptions.getInstance("java").datamask(s, trivialLiterals);
	}
}
