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
		if (preferenceManager.getString("UUID") == null || preferenceManager.getString("UUID").isEmpty()) {
			synchronized (id) {
				preferenceManager.setValue("UUID", UUID.randomUUID().toString());
			}
		}
		return "eclipse-" + preferenceManager.getString("UUID");
	}
	
	public static String getParams() {
		return preferenceManager.getString("PARAMS");
	}
	
	public static String getSocketEndpoint() {
		String endpoint = preferenceManager.getString("SOCKET_ENDPOINT");
		return endpoint.substring(0, endpoint.lastIndexOf(":"));
	}
	
	public static int getSocketEndpointPort() {
		String endpoint = preferenceManager.getString("SOCKET_ENDPOINT");
		return Integer.parseInt(endpoint.substring(endpoint.lastIndexOf(":") + 1));
	}
}
