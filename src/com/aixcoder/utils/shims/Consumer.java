package com.aixcoder.utils.shims;

public interface Consumer<T> {
	public void apply(T t);

	public static class ConsumerAdapter<T> implements Consumer<T> {

		@Override
		public void apply(T t) {
		}

	}
}
