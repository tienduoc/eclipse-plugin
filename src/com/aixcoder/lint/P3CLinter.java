package com.aixcoder.lint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class P3CLinter extends Linter {

	private BufferedReader is;
	private OutputStreamWriter os;
	private Process p;

	public P3CLinter() throws IOException {
		startProcess();
	}

	private void startProcess() throws IOException {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "D:\\workspace-2019-06\\Tensorflow-AutoComplete\\codestyleworker.jar");
		p = pb.start();
		is = new BufferedReader(new InputStreamReader(p.getInputStream()));
		os = new OutputStreamWriter(p.getOutputStream());
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		p.destroy();
	}

	@Override
	public ArrayList<LintResult> lint(String projectPath, String filePath) throws IOException {
		if (p == null || !p.isAlive()) {
			startProcess();
		}
		os.write(projectPath + "," + filePath + "\n");
		os.flush();
		String line = is.readLine();
		JsonObject jo = new Gson().fromJson(line, JsonObject.class);
		JsonArray list = jo.getAsJsonObject("data").getAsJsonArray("p3c");
		ArrayList<LintResult> results = new ArrayList<LintResult>();
		for (int i = 0; i < list.size(); i++) {
			JsonObject j = list.get(i).getAsJsonObject();
//			String className = j.get("className").getAsString();
			String badDetail = j.get("bad_detail").getAsString();
			String locfile = j.get("locfile").getAsString();
			int beginLine = j.get("beginLine").getAsInt();
			int beginColumn = j.get("beginColumn").getAsInt();
			int endLine = j.get("endLine").getAsInt();
			int endColumn = j.get("endColumn").getAsInt();
//			int beginOffset = j.get("beginOffset").getAsInt();
//			int endOffset = j.get("endOffset").getAsInt();
			results.add(new LintResult(badDetail, locfile, Severity.WARNING, beginLine, beginColumn, endLine, endColumn + 1));
		}
		return results;
	}

}
