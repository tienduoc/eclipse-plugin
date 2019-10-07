package com.aixcoder.lint;

import java.io.IOException;
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
		}
	}

	public ArrayList<LintResult> lint(String projectPath, String filePath) {
		ArrayList<LintResult> results = new ArrayList<LintResult>();
		for (Linter linter: linters) {
			try {
				results.addAll(linter.lint(projectPath, filePath));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return results;
	}
}
