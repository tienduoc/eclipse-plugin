package com.aixcoder.lint;

import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class P3CLinter extends Linter {

	private BufferedReader is;
	private OutputStreamWriter os;
	private Process p;

	public P3CLinter() throws IOException, URISyntaxException {
		startProcess();
	}

	private void startProcess() throws IOException, URISyntaxException {
		String javaHome = System.getProperty("java.home");
		String javaExecPath = Paths.get(javaHome, "bin", "java.exe").toAbsolutePath().toString();
		String jarPath = new File(P3CLinter.class.getProtectionDomain().getCodeSource().getLocation().toURI())
				.getParent();
		jarPath = Paths.get(jarPath, "codestyleworker.jar").toAbsolutePath().toString();
		System.out.println("jarPath=" + jarPath);
		ProcessBuilder pb = new ProcessBuilder(javaExecPath, "-jar", jarPath);
		p = pb.start();
		is = new BufferedReader(new InputStreamReader(p.getInputStream()));
		os = new OutputStreamWriter(p.getOutputStream());
	}

	@Override
	public ArrayList<LintResult> lint(String projectPath, String filePath) throws IOException, URISyntaxException {
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
			results.add(new LintResult(badDetail, locfile, Severity.WARNING, beginLine, beginColumn, endLine,
					endColumn + 1));
		}
		return results;
	}

}
