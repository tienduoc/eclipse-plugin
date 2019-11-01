package com.aixcoder.i18n;

import java.util.HashMap;
import java.util.Map;

public class EN extends Localization {
	public static final String id = "en-us";
	public static final String display = "English";
	static Map<String, String> m = new HashMap<String, String>();
	static {
		m.put(enableAiXCoder, "&Enable aiXcoder");
		m.put(serverURL, "&Server URL");
		m.put(searchURL, "Searc&h URL");
		m.put(autoImportClasses, "Auto &Import Classes");
		m.put(sortOnly, "S&ort only (will decrease delay)");
		m.put(allowTelemetry, "Allow &Telemetry");
//		m.put(language, "&Language 🌏 (need to restart preference window)");
		m.put(language, "&Language \ud83c\udf0f (need to restart preference window)");
		m.put(model, "&Model");
		m.put(additionalParameters, "Additional &Parameters");
		m.put(description,
				"AiXcoder is an AI-powered code completion service. Visit https://aixcoder.com for more information.");
		m.put(telemetryTitle, "aiXcoder user statistics collection");
		m.put(telemetryQuestion, "Are you willing to send anonymous usage data to improve user experience? You can later change it in preferences page.");
		m.put(endpointEmptyTitle, "aiXcoder endpoint is empty!");
		m.put(endpointEmptyWarning, "The endpoint of aiXcoder is not set. Please set it manually in Window->Preferences->AiXCoder Preferences. Our public endpoint is https://api.aixcoder.com/");
		m.put(longResultRank, "The preferred position of the long result");
		m.put(longResultCut, "Number of shorter results");
		m.put(longResultCutAuto, "Auto");
		m.put(longResultCut0, "0-None");
		m.put(longResultCut1, "1");
		m.put(longResultCut2, "2");
		m.put(longResultCut3, "3");
		m.put(longResultCut4, "4");
		m.put(longResultCut5, "5");
		m.put(longResultCutSort, "The order of shorter results");
		m.put(longResultCutS2L, "short to long");
		m.put(longResultCutL2S, "long to short");
		m.put(missingP3Cjar, "cannot locate codestyleworker.jar");
		m.put(enterprisePort, "Enterprise update port");
		m.put(localServerAutoStartTitle, "aiXcoder local service is not responding");
		m.put(localServerAutoStartQuestion, "AiXcoder Local service is not responding, start it now?");
	}

	public static String R(String input) {
		if (m.containsKey(input))
			return m.get(input);
		return input;
	}
}
