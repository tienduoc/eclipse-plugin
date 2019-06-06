package com.aixcoder.lib;

import java.util.UUID;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.aixcoder.extension.Activator;
import com.aixcoder.extension.AiXPreInitializer;

public class Preference {

	public static final String ACTIVE = "ACTIVE";
	public static final String ENDPOINT = "ENDPOINT";
	public static final String MODEL = "MODEL";
	public static final String P_UUID = "UUID";
	public static final String PARAMS = "PARAMS";
	public static final String SEARCH_ENDPOINT = "SEARCH_ENDPOINT";
	public static final String AUTO_IMPORT = "AUTO_IMPORT";
	public static final String ALLOW_TELEMETRY = "ALLOW_TELEMETRY";
	public static final String ASKED_TELEMETRY = "ASKED_TELEMETRY";

	public static final String id = Activator.PLUGIN_ID + ".preferences.page";
	public static ScopedPreferenceStore preferenceManager = new ScopedPreferenceStore(InstanceScope.INSTANCE, id);

	public static boolean isActive() {
		new AiXPreInitializer().initializeDefaultPreferences();
		return preferenceManager.getBoolean(ACTIVE);
	}

	public static String getEndpoint() {
		return preferenceManager.getString(ENDPOINT);
	}

	public static String getModel() {
		return preferenceManager.getString(MODEL);
	}

	public static String getUUID() {
		if (preferenceManager.getString(P_UUID) == null || preferenceManager.getString(P_UUID).isEmpty()) {
			synchronized (id) {
				preferenceManager.setValue(P_UUID, UUID.randomUUID().toString());
			}
		}
		return "eclipse-" + preferenceManager.getString(P_UUID);
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

	public static boolean allowTelemetry() {
		new AiXPreInitializer().initializeDefaultPreferences();
		return preferenceManager.getBoolean(ALLOW_TELEMETRY);
	}

	public static boolean askedTelemetry() {
		return preferenceManager.getBoolean(ASKED_TELEMETRY);
	}
}
