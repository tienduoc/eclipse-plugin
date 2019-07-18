package com.aixcoder.extension;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.aixcoder.i18n.EN;
import com.aixcoder.i18n.Localization;
import com.aixcoder.lib.Preference;

public class AiXPreInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		ScopedPreferenceStore scopedPreferenceStore = Preference.preferenceManager;
		if (!scopedPreferenceStore.getBoolean("INITIALIZED")) {
			scopedPreferenceStore.setDefault(Preference.ACTIVE, true);
			scopedPreferenceStore.setDefault(Preference.ENDPOINT, "https://api.aixcoder.com/");
			scopedPreferenceStore.setDefault(Preference.SEARCH_ENDPOINT, "https://search.aixcoder.com/");
			scopedPreferenceStore.setDefault(Preference.MODEL, "java(Java)");
			scopedPreferenceStore.setDefault(Preference.AUTO_IMPORT, true);
			scopedPreferenceStore.setDefault(Preference.SORT_ONLY, false);
			scopedPreferenceStore.setDefault(Preference.LONG_RESULT_CUT, Localization.longResultCutAuto);
			scopedPreferenceStore.setDefault(Preference.LONG_RESULT_CUT_SORT, Localization.longResultCutL2S);
			scopedPreferenceStore.setDefault(Preference.ALLOW_TELEMETRY, true);
			scopedPreferenceStore.setDefault("INITIALIZED", true);
			scopedPreferenceStore.setDefault(Preference.PARAMS, "");
			scopedPreferenceStore.setDefault(Preference.LANGUAGE, EN.id);
			scopedPreferenceStore.setDefault(Preference.LONG_RESULT_RANK, 1);
			Preference.getUUID();
			try {
				scopedPreferenceStore.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
