package com.aixcoder.lint;

public class LintResult {
	public String detail;
	public String filePath;
	public int offset = -1;
	public int beginLine = -1;
	public int beginColumn = -1;
	public int endLine = -1;
	public int endColumn = -1;
	public Severity severity;
	public int length = -1;

	public LintResult(String detail, String filePath, Severity severity, int offset, int length) {
		super();
		this.detail = detail;
		this.filePath = filePath;
		this.offset = offset;
		this.length = length;
		this.severity = severity;
	}

	public LintResult(String detail, String filePath, Severity severity, int beginLine, int beginColumn, int endLine, int endColumn) {
		super();
		this.detail = detail;
		this.filePath = filePath;
		this.beginLine = beginLine;
		this.beginColumn = beginColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
		this.severity = severity;
	}
}
