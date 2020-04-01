package com.aixcoder.extension;

import java.io.IOException;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDeltaVisitor;

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
		
		IResourceChangeListener listener = new IResourceChangeListener() {
			@Override
	        public void resourceChanged(IResourceChangeEvent event) {
				if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
					return;
				}
	        	try {
	        		event.getDelta().accept(new IResourceDeltaVisitor() {
	    				@Override
	    				public boolean visit(IResourceDelta delta) throws org.eclipse.core.runtime.CoreException {
	    					if (delta.getResource() != null) {
	    						if (delta.getResource().getType() == IResource.FILE &&
	    								(delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.CHANGED)) {
	    							String filename = delta.getResource().getName();
	    							String fullpath = delta.getResource().getLocation().toString();
	    							String pathWithProj = delta.getResource().getFullPath().toString();
	    							String pathInProj = delta.getProjectRelativePath().toString(); // not include project
	    							System.out.printf("filename=%s \n fullpath=%s \n pathWithProj=%s \n pathInProj=%s\n", filename, fullpath, pathWithProj, pathInProj );
	    						}
	    					}
	    					return true; // to visit child
	    				}
	    			});	        		
	        	} catch (Exception e) {
	        		e.printStackTrace();
	        	}
	           }
		};
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		
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
