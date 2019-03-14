package com.aixcoder.lib;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.aixcoder.extension.Activator;

public class Preference {

	public static final String id = Activator.PLUGIN_ID + ".preferences.page";
	public static ScopedPreferenceStore preferenceManager = new ScopedPreferenceStore(InstanceScope.INSTANCE, id);
	
	public static boolean isActive() {
		return preferenceManager.getBoolean("ACTIVE");
	}

	public static String getEndpoint() {
		return preferenceManager.getString("ENDPOINT");
	}
	
	public static String getModel() {
		return preferenceManager.getString("MODEL");
	}
}
