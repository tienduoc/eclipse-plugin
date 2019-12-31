package com.aixcoder.extension;

import static com.aixcoder.i18n.Localization.R;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;

import com.aixcoder.core.API;
import com.aixcoder.i18n.EN;
import com.aixcoder.i18n.Localization;
import com.aixcoder.i18n.ZH;
import com.aixcoder.lib.Preference;

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
		if (!Preference.askedLanguage() || !Preference.askedTelemetry()) {
			new UIJob("Prompt aiXcoder initialize") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					// Ask language
					if (!Preference.askedLanguage()) {
						MessageDialog dialog = new MessageDialog(null, "Language?", null,
								"Which language do you prefer aiXcoder using? You can change it later in preferences page.",
								MessageDialog.QUESTION, new String[] { EN.display, ZH.display }, -1);
						int choice = dialog.open();
						String[] values = new String[] { EN.id, ZH.id };
						if (choice == 0 || choice == 1) {
							Preference.preferenceManager.setValue(Preference.LANGUAGE, values[choice]);
							Preference.preferenceManager.setValue(Preference.ASKED_LANGUAGE, true);
							try {
								Preference.preferenceManager.save();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					// Ask telemetry
					if (!Preference.askedTelemetry()) {
						MessageDialog dialog = new MessageDialog(null, R(Localization.telemetryTitle), null,
								R(Localization.telemetryQuestion), MessageDialog.QUESTION,
								new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
						int choice = dialog.open();
						if (choice == 0 || choice == 1) {
							Preference.preferenceManager.setValue(Preference.ALLOW_TELEMETRY, choice == 0);
							Preference.preferenceManager.setValue(Preference.ASKED_TELEMETRY, true);
							try {
								Preference.preferenceManager.save();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					return Status.OK_STATUS;
				}
			}.schedule();
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
