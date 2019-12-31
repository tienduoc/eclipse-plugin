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
	public static final String longResultRank = "longResultRank";
	public static final String longResultCut = "longResultCut";
	public static final String longResultCutAuto = "longResultCutAuto";
	public static final String longResultCut0 = "longResultCut0";
	public static final String longResultCut1 = "longResultCut1";
	public static final String longResultCut2 = "longResultCut2";
	public static final String longResultCut3 = "longResultCut3";
	public static final String longResultCut4 = "longResultCut4";
	public static final String longResultCut5 = "longResultCut5";
	public static final String longResultCutSort = "longResultCutSort";
	public static final String longResultCutS2L = "longResultCutS2L";
	public static final String longResultCutL2S = "longResultCutL2S";
	public static final String localServerAutoStartTitle = "localServerAutoStartTitle";
	public static final String localServerAutoStartQuestion = "localServerAutoStartQuestion";
	public static final String selfLearn = "selfLearn";
	public static final String unableToLoginTitle = "unableToLoginTitle";
	public static final String unableToLogin = "unableToLogin";
	public static final String notProfessionalTitle = "notProfessionalTitle";
	public static final String notProfessional = "notProfessional";
	public static final String newVersionTitle = "newVersionTitle";
	public static final String newVersionContent = "newVersionContent";
	public static final String localDownloadTitle = "localDownloadTitle";
	public static final String localDownloadQuestion = "localDownloadQuestion";
	public static final String localInitializing = "localInitializing";
	public static final String allowLocalIncomplete = "allowLocalIncomplete";
	public static final String localInitializingTitle = "localInitializingTitle";

	public static String R(String input) {
		if (Preference.getLanguage().equals(ZH.id)) {
			return ZH.R(input);
		} else {
			return EN.R(input);
		}
	}
}
