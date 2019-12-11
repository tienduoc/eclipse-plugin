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
		m.put(localServerAutoStartTitle, "aiXcoder service is not responding");
		m.put(localServerAutoStartQuestion, "Failed to start aiXcoder service in one minute. You can report this issue on https://github.com/aixcoder-plugin/eclipse-plugin/issues");
		m.put(selfLearn, "(Professional Edition) Allows aiXcoder to learn your coding idiom");
		m.put(unableToLoginTitle, "Unable to login aiXcoder");
		m.put(unableToLogin, "Login now?");
		m.put(notProfessionalTitle, "You are not using aiXcoder Professional Edition");
		m.put(notProfessional, "Some features are only available in Professional Edition. Learn more?");
		m.put(newVersionTitle, "New version available");
		m.put(newVersionContent, "A new version of aiXcoder %s is available, update now?");
		m.put(localDownloadTitle, "Download failed");
		m.put(localDownloadQuestion, "aiXcoder service update failed. You can manually download the latest service zipfile/tarball from: %s. And then unzip it here: %s.");
		m.put(localInitializingTitle, "aiXcoder is indexing your project.");
		m.put(localInitializing, "aiXcoder is indexing your project for the first time. The suggestions may not be accurate until it is done. Do you want to show incomplete suggestions while wait for indexing to finish?");
		m.put(allowLocalIncomplete, "Show incomplete suggestions before indexing finishes");
	}

	public static String R(String input) {
		if (m.containsKey(input))
			return m.get(input);
		return input;
	}
}
