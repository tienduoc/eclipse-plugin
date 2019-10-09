package com.aixcoder.lint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.aixcoder.lib.HttpRequest;
import com.aixcoder.lib.Preference;
import com.aixcoder.utils.HttpHelper;
import com.aixcoder.utils.shims.Consumer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class AiXLinter extends Linter {

	@Override
	public ArrayList<LintResult> lint(String projectPath, String filePath) throws Exception {
		String content = "";
		try {
			content = new String(Files.readAllBytes(Paths.get(projectPath, filePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("project", projectPath);
		queryParams.put("file", filePath);
		final byte[] bytes = content.getBytes("utf-8");
		String url = Preference.getEndpoint() + "lint";
//		String url = "http://localhost:7700/lint";
		String string = HttpHelper.post(url, queryParams, new Consumer<HttpRequest>() {
			@Override
			public void apply(HttpRequest httpRequest) {
				// send request
				httpRequest.contentType("text/plain", "UTF-8").send(bytes);
			}
		});
		if (string == null) {
			return null;
		}
		ArrayList<LintResult> results = new ArrayList<LintResult>();
		JsonArray jo = new Gson().fromJson(string, JsonArray.class);
		if (jo != null) {
			for (int i = 0; i < jo.size(); i++) {
				JsonObject j = jo.get(i).getAsJsonObject();
				String ruleId = j.get("rule").getAsString();
				String desc = j.get("desc").getAsString();
				JsonArray r = j.getAsJsonArray("result");
				for (int k = 0; k < r.size();k++) {
					JsonObject single = r.get(k).getAsJsonObject();
					String description = single.has("detail") ? "\n" + single.get("detail").getAsString() : "";
					int start = single.get("start").getAsInt();
					int length = single.get("length").getAsInt();
					results.add(new LintResult(desc + description + "\n[" + ruleId + "]", filePath, Severity.WARNING, start, length));
				}
			}
		}
		return results;
	}

}
