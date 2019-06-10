package com.aixcoder.i18n;

import com.aixcoder.lib.Preference;

public class Localization {
	public static final String enableAiXCoder = "enableAiXCoder";
	public static final String serverURL = "serverURL";
	public static final String searchURL = "searchURL";
	public static final String autoImportClasses = "autoImportClasses";
	public static final String sortOnly = "sortOnly";
	public static final String allowTelemetry = "allowTelemetry";
	public static final String language = "language";
	public static final String model = "model";
	public static final String additionalParameters = "additionalParameters";
	public static final String description = "description";
	public static final String telemetryTitle = "telemetryTitle";
	public static final String telemetryQuestion = "telemetryQuestion";
	public static final String endpointEmptyTitle = "endpointEmptyTitle";
	public static final String endpointEmptyWarning = "endpointEmptyWarning";

	public static String R(String input) {
		if (Preference.getLanguage().equals(ZH.id)) {
			return ZH.R(input);
		} else {
			return EN.R(input);
		}
	}
}
