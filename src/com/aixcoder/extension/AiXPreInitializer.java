package com.aixcoder.extension;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.aixcoder.lib.Preference;

public class AiXPreInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		ScopedPreferenceStore scopedPreferenceStore = Preference.preferenceManager;
		if (!scopedPreferenceStore.getBoolean("INITIALIZED")) {
			scopedPreferenceStore.setDefault("ACTIVE", true);
			scopedPreferenceStore.setDefault("ENDPOINT", "https://api.aixcoder.com/");
			scopedPreferenceStore.setDefault("MODEL", "java(Java)");
			scopedPreferenceStore.setDefault("INITIALIZED", true);
			scopedPreferenceStore.setDefault("PARAMS", "");
			Preference.getUUID();
			try {
				scopedPreferenceStore.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
