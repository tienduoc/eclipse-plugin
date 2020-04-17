package com.aixcoder.extension;

import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import org.eclipse.core.resources.IResourceDelta;
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
	
	public volatile static Map<String, Long> filesChangeTimestampMap = new ConcurrentHashMap<>();

	public WebView webview;

	/**
	 * The constructor
	 */
	public Activator() {
	}
	
	/**
	 * 
	 * let localserver know, there are file changes
	 *
	 */
	class FileChangeTimerTask extends java.util.TimerTask {
		private long currentTimestamp;
		private String langFromExt;
		private String fullpath;
		private String projectName;
		private String projectPath;
		
		FileChangeTimerTask (long currentTimestamp, String langFromExt, String fullpath, String projectName, String projectPath) {
			this.currentTimestamp = currentTimestamp;
			this.langFromExt = langFromExt;
			this.fullpath = fullpath;
			this.projectName = projectName;
			this.projectPath = projectPath;
		}
        public void run() {
        	long filesChangeTimestamp = filesChangeTimestampMap.get(fullpath);
        	if (currentTimestamp == filesChangeTimestamp) {
        		String fileStr = "";
        		try {
        			if (new File(fullpath).exists()) {
        				try
        		        {
        					fileStr = new String (Files.readAllBytes(Paths.get(fullpath)));
        					com.aixcoder.core.API.notifyFileChange(fileStr, langFromExt, fullpath, projectName, projectPath);
        		        } 
        		        catch (IOException e) 
        		        {
        		            e.printStackTrace();
        		        }
        			}
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
        }
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
	    							String ext = Preference.getModel();
	    							String langFromExt = "java";
	    							final long MATURE_DELAY_SECONDS = 5000L;
	    							if (langFromExt.equals(delta.getResource().getFileExtension())) {
		    							String fullpath = delta.getResource().getLocation().toString();
		    							String projectName = delta.getResource().getProject().getName();
		    							String projectPath = delta.getResource().getProject().getLocation().toString();
		    							if (fullpath != null) {
			    							long currentTimestamp = System.currentTimeMillis();
		    								filesChangeTimestampMap.put(fullpath, currentTimestamp);
			    							java.util.Timer timer = new java.util.Timer();
			    							timer.schedule(new FileChangeTimerTask(currentTimestamp, langFromExt, fullpath, projectName, projectPath), MATURE_DELAY_SECONDS);
		    							}
	    							}
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
		// read maven user settings
		String mavenUserSettingXML = null;
		String workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		Path mavenSettingfilePath = Paths.get(workspaceRoot, ".metadata", ".plugins", "org.eclipse.core.runtime", ".settings", "org.eclipse.m2e.core.prefs");
		try {
			if (Files.exists(mavenSettingfilePath)) {
				File file = mavenSettingfilePath.toFile();
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(file));
					String tmpLineStr = null;
					final String lineHead= "eclipse.m2.userSettingsFile=";
					while ((tmpLineStr = reader.readLine()) != null) {
						if (tmpLineStr.startsWith(lineHead)) {
							mavenUserSettingXML = tmpLineStr.substring(lineHead.length());
							break;
						}
		            }
				} catch (Exception e) {
				} finally {
		            if (reader != null) {
		                try {
		                    reader.close();
		                } catch (IOException e1) {
		                }
		            }
		        }
			}
		} catch (Exception e) {
		}
		Preference.setMavenUserSettingXML(mavenUserSettingXML);
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
