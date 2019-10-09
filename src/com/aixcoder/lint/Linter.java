package com.aixcoder.lint;

import java.util.ArrayList;

public abstract class Linter {
	public abstract ArrayList<LintResult> lint(String projectPath, String filePath) throws Exception;
}
