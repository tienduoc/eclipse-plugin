package com.aixcoder.extension;

import java.io.IOException;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.aixcoder.core.API;
import com.aixcoder.i18n.EN;
import com.aixcoder.i18n.Localization;
import com.aixcoder.i18n.ZH;
import com.aixcoder.lib.Preference;
import com.aixcoder.utils.PromptUtils;
import com.aixcoder.utils.shims.Consumer;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "aiXcoder"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	public WebView webview;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		super.start(context);
		plugin = this;
		new AiXPreInitializer().initializeDefaultPreferences();

		// Ask language
		if (!Preference.askedLanguage()) {
			final String[] choices = new String[] { EN.display, ZH.display };
			PromptUtils.promptQuestion("Prompt aiXcoder initialize", "Language?",
					"Which language do you prefer aiXcoder using? You can change it later in preferences page.",
					choices, new Consumer<String>() {

						@Override
						public void apply(String choice) {
							String[] values = new String[] { EN.id, ZH.id };
							if (choice != null) {
								Preference.preferenceManager.setValue(Preference.LANGUAGE,
										values[java.util.Arrays.asList(choices).indexOf(choice)]);
								Preference.preferenceManager.setValue(Preference.ASKED_LANGUAGE, true);
								try {
									Preference.preferenceManager.save();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}

					});
		}
		// Ask telemetry
		if (!Preference.askedTelemetry()) {
			PromptUtils.promptYesNoQuestion("Prompt aiXcoder initialize", Localization.telemetryTitle,
					Localization.telemetryQuestion, new Consumer<Boolean>() {

						@Override
						public void apply(Boolean choice) {
							if (choice != null) {
								Preference.preferenceManager.setValue(Preference.ALLOW_TELEMETRY, choice);
								Preference.preferenceManager.setValue(Preference.ASKED_TELEMETRY, true);
								try {
									Preference.preferenceManager.save();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}

					});
		}
		API.checkUpdate(context.getBundle().getVersion());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
