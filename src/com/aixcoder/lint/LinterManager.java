package com.aixcoder.lint;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class LinterManager {
	static LinterManager instance;
	private ArrayList<Linter> linters = new ArrayList<Linter>();;

	public static LinterManager getInstance() {
		if (instance == null) {
			instance = new LinterManager();
		}
		return instance;
	}

	protected LinterManager() {
		try {
			linters.add(new P3CLinter());
			linters.add(new AiXLinter());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<LintResult> lint(final String projectPath, final String filePath) {
		final ArrayList<LintResult> results = new ArrayList<LintResult>();
		ArrayList<Thread> threads = new ArrayList<Thread>();
		long _s = System.currentTimeMillis();
		for (final Linter linter : linters) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						results.addAll(linter.lint(projectPath, filePath));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			threads.add(t);
			t.start();
		}
		for (Thread t : threads) {
			try {
				t.join(5000);
				t.interrupt();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("lint time took: " + (System.currentTimeMillis() - _s));
		return results;
	}
}
