package com.aixcoder.i18n;

import java.util.HashMap;
import java.util.Map;

public class EN extends Localization {
	public static final String id = "en-us";
	static Map<String, String> m = new HashMap<String, String>();
	static {
		m.put(enableAiXCoder, "&Enable aiXcoder");
		m.put(serverURL, "&Server URL");
		m.put(searchURL, "Searc&h URL");
		m.put(autoImportClasses, "Auto &Import Classes");
		m.put(sortOnly, "S&ort only (will decrease delay)");
		m.put(allowTelemetry, "Allow &Telemetry");
		m.put(language, "&Language 🌏 (need to restart preference window)");
		m.put(model, "&Model");
		m.put(additionalParameters, "Additional &Parameters");
		m.put(description,
				"AiXcoder is an AI-powered code completion service. Visit https://aixcoder.com for more information.");
	}

	public static String R(String input) {
		if (m.containsKey(input))
			return m.get(input);
		return input;
	}
}
