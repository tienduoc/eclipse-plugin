package com.aixcoder.utils.shims;

import java.util.List;

public class CollectionUtils {
	public static <T> void removeIf(List<T> list, Predicate<T> p) {
		for (int i = 0; i < list.size(); i++) {
			T t = list.get(i);
			if (p.test(t)) {
				list.remove(i);
				i--;
			}
		}
	}

	public static <T> String join(String separator, Iterable<T> list) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (T t : list) {
			if (!first) {
				sb.append(separator);
			}
			first = false;
			sb.append(String.valueOf(t));
		}
		return sb.toString();
	}

	public static <T> String join(String separator, T[] list) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (T t : list) {
			if (!first) {
				sb.append(separator);
			}
			first = false;
			sb.append(String.valueOf(t));
		}
		return sb.toString();
	}
}
