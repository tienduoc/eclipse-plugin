package com.aixcoder.lib;

import java.util.UUID;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.aixcoder.extension.Activator;
import com.aixcoder.extension.AiXPreInitializer;
import com.aixcoder.i18n.Localization;

public class Preference {

	public static final String ACTIVE = "ACTIVE";
	public static final String ENDPOINT = "ENDPOINT";
	public static final String ENTERPRISE_PORT = "ENTERPRISE_PORT";
	public static final String MODEL = "MODEL";
	public static final String P_UUID = "UUID";
	public static final String PARAMS = "PARAMS";
	public static final String SEARCH_ENDPOINT = "SEARCH_ENDPOINT";
	public static final String AUTO_IMPORT = "AUTO_IMPORT";
	public static final String SORT_ONLY = "SORT_ONLY";
	public static final String LANGUAGE = "LANGUAGE";
	public static final String ASKED_LANGUAGE = "ASKED_LANGUAGE";
	public static final String ALLOW_TELEMETRY = "ALLOW_TELEMETRY";
	public static final String ASKED_TELEMETRY = "ASKED_TELEMETRY";
	public static final String LONG_RESULT_RANK = "LONG_RESULT_RANK";
	public static final String LONG_RESULT_CUT = "LONG_RESULT_CUT";
	public static final String LONG_RESULT_CUT_SORT = "LONG_RESULT_CUT_SORT";

	public static final String id = Activator.PLUGIN_ID + ".preferences.page";
	public static ScopedPreferenceStore preferenceManager = new ScopedPreferenceStore(InstanceScope.INSTANCE, id);

	public static boolean isActive() {
		new AiXPreInitializer().initializeDefaultPreferences();
		return preferenceManager.getBoolean(ACTIVE);
	}

	public static String getEndpoint() {
		String endpoint = preferenceManager.getString(ENDPOINT);
		if (!endpoint.endsWith("/")) {
			endpoint = endpoint + "/";
		}
		return endpoint;
	}

	public static int getEnterprisePort() {
		return preferenceManager.getInt(ENTERPRISE_PORT);
	}

	public static String getModel() {
		return preferenceManager.getString(MODEL);
	}

	public static String getUUID() {
		if (preferenceManager.getString(P_UUID) == null || preferenceManager.getString(P_UUID).isEmpty()) {
			synchronized (id) {
				if (preferenceManager.getString(P_UUID) == null || preferenceManager.getString(P_UUID).isEmpty()) {
					preferenceManager.setValue(P_UUID, "eclipse-" + UUID.randomUUID().toString());
				}
			}
		}
		String corpUser = System.getProperty("user.name");
		String uuid = preferenceManager.getString(P_UUID);
		if (corpUser != null && uuid.indexOf(corpUser + "=>") < 0) {
			uuid = corpUser + "=>" + uuid;
			preferenceManager.setValue(P_UUID, uuid);
		}
		return preferenceManager.getString(P_UUID);
	}

	public static String getParams() {
		return preferenceManager.getString(PARAMS);
	}

	public static String getSearchEndpoint() {
		new AiXPreInitializer().initializeDefaultPreferences();
		return preferenceManager.getString(SEARCH_ENDPOINT);
	}

	public static boolean getAutoImport() {
		new AiXPreInitializer().initializeDefaultPreferences();
		return preferenceManager.getBoolean(AUTO_IMPORT);
	}

	public static boolean sortOnly() {
		new AiXPreInitializer().initializeDefaultPreferences();
		return preferenceManager.getBoolean(SORT_ONLY);
	}

	public static boolean allowTelemetry() {
		new AiXPreInitializer().initializeDefaultPreferences();
		return preferenceManager.getBoolean(ALLOW_TELEMETRY);
	}

	public static boolean askedTelemetry() {
		return preferenceManager.getBoolean(ASKED_TELEMETRY);
	}

	public static String getLanguage() {
		return preferenceManager.getString(LANGUAGE);
	}

	public static boolean askedLanguage() {
		return preferenceManager.getBoolean(ASKED_LANGUAGE);
	}

	public static int getLongResultRank() {
		return preferenceManager.getInt(LONG_RESULT_RANK);
	}

	/**
	 * -1 : auto
	 * 
	 * @return
	 */
	public static int getLongResultCuts() {
		new AiXPreInitializer().initializeDefaultPreferences();
		String cuts = preferenceManager.getString(LONG_RESULT_CUT);
		if (cuts.equals(Localization.longResultCutAuto)) {
			return -1;
		} else if (cuts.equals(Localization.longResultCut0)) {
			return 0;
		} else if (cuts.equals(Localization.longResultCut1)) {
			return 1;
		} else if (cuts.equals(Localization.longResultCut2)) {
			return 2;
		} else if (cuts.equals(Localization.longResultCut3)) {
			return 3;
		} else if (cuts.equals(Localization.longResultCut4)) {
			return 4;
		} else if (cuts.equals(Localization.longResultCut5)) {
			return 5;
		}
		return -1;
	}

	public static String getLongResultCutsOrder() {
		return preferenceManager.getString(LONG_RESULT_CUT_SORT);
	}
}
