package com.aixcoder.lib;

import java.util.UUID;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.aixcoder.extension.Activator;
import com.aixcoder.extension.AiXPreInitializer;

public class Preference {

	public static final String id = Activator.PLUGIN_ID + ".preferences.page";
	public static ScopedPreferenceStore preferenceManager = new ScopedPreferenceStore(InstanceScope.INSTANCE, id);
	
	public static boolean isActive() {
		new AiXPreInitializer().initializeDefaultPreferences();
		return preferenceManager.getBoolean("ACTIVE");
	}

	public static String getEndpoint() {
		return preferenceManager.getString("ENDPOINT");
	}
	
	public static String getModel() {
		return preferenceManager.getString("MODEL");
	}
	
	public static String getUUID() {
		if (preferenceManager.getString("UUID") == null) {
			preferenceManager.setValue("UUID", UUID.randomUUID().toString());
		}
		return preferenceManager.getString("UUID");
	}
	
	public static String getParams() {
		return preferenceManager.getString("PARAMS");
	}
}
