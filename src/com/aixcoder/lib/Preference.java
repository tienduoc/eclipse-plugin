package com.aixcoder.lib;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import java.awt.Desktop;
import java.net.URI;

import com.aixcoder.core.LocalServerStatus;
import com.aixcoder.core.LocalService;
import com.aixcoder.extension.Activator;
import com.aixcoder.extension.AiXPreInitializer;
import com.aixcoder.i18n.Localization;
import com.aixcoder.utils.HttpHelper;
import com.aixcoder.utils.PromptUtils;
import com.aixcoder.utils.shims.Consumer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class LoginInfo {
	String uuid;
	String token;

	public LoginInfo(String uuid, String token) {
		super();
		this.uuid = uuid;
		this.token = token;
	}

}

public class Preference {

	public static final String ACTIVE = "ACTIVE";
	public static final String SELF_LEARN = "SELF_LEARN";
	public static final String MODEL = "MODEL";
	public static final String P_UUID = "UUID";
	public static final String PARAMS = "PARAMS";
	public static final String SEARCH_ENDPOINT = "SEARCH_ENDPOINT";
	public static final String AUTO_IMPORT = "AUTO_IMPORT";
	public static final String SORT_ONLY = "SORT_ONLY";
	public static final String LANGUAGE = "LANGUAGE";
	public static final String ASKED_LANGUAGE = "ASKED_LANGUAGE";
	public static final String ALLOW_TELEMETRY = "ALLOW_TELEMETRY";
	public static final String ASKED_TELEMETRY = "ASKED_TELEMETRY";
	public static final String LONG_RESULT_RANK = "LONG_RESULT_RANK";
	public static final String LONG_RESULT_CUT = "LONG_RESULT_CUT";
	public static final String LONG_RESULT_CUT_SORT = "LONG_RESULT_CUT_SORT";
	public static final String ASKED_LOCAL_INITIALIZING = "ASKED_LOCAL_INITIALIZING";
	public static final String ALLOW_LOCAL_INCOMPLETE = "ALLOW_LOCAL_INCOMPLETE";
	public static final String USE_LOCAL_SERVICE = "USE_LOCAL_SERVICE";

	public static final String id = Activator.PLUGIN_ID + ".preferences.page";
	public static ScopedPreferenceStore preferenceManager = new ScopedPreferenceStore(InstanceScope.INSTANCE, id);

	private static boolean isProfessional = false;
	public static boolean isProfessionalError = false;
	public static boolean isProfessionalFetched = true;
	
	public static final String urlLearnMoreLocalService = "https://www.aixcoder.com/#/versionInfo";

	public static boolean isActive() {
		new AiXPreInitializer().initializeDefaultPreferences();
		return preferenceManager.getBoolean(ACTIVE);
	}

	public static String getModel() {
		return preferenceManager.getString(MODEL);
	}

	public static String getUUID() {
		LoginInfo info = getUUIDFromFile();
		if (info.uuid != null) {
			synchronized (id) {
				preferenceManager.setValue(P_UUID, info.uuid);
			}
		}
		if (preferenceManager.getString(P_UUID) == null || preferenceManager.getString(P_UUID).isEmpty()) {
			synchronized (id) {
				if (preferenceManager.getString(P_UUID) == null || preferenceManager.getString(P_UUID).isEmpty()) {
					preferenceManager.setValue(P_UUID, "eclipse-" + UUID.randomUUID().toString());
				}
			}
		}
		return preferenceManager.getString(P_UUID);
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

	public static boolean sortOnly() {
		new AiXPreInitializer().initializeDefaultPreferences();
		return preferenceManager.getBoolean(SORT_ONLY);
	}

	public static boolean allowTelemetry() {
		new AiXPreInitializer().initializeDefaultPreferences();
		return preferenceManager.getBoolean(ALLOW_TELEMETRY);
	}

	public static boolean askedTelemetry() {
		return preferenceManager.getBoolean(ASKED_TELEMETRY);
	}

	public static String getLanguage() {
		return preferenceManager.getString(LANGUAGE);
	}

	public static boolean askedLanguage() {
		return preferenceManager.getBoolean(ASKED_LANGUAGE);
	}

	public static int getLongResultRank() {
		return preferenceManager.getInt(LONG_RESULT_RANK);
	}

	/**
	 * -1 : auto
	 * 
	 * @return
	 */
	public static int getLongResultCuts() {
		new AiXPreInitializer().initializeDefaultPreferences();
		String cuts = preferenceManager.getString(LONG_RESULT_CUT);
		if (cuts.equals(Localization.longResultCutAuto)) {
			return -1;
		} else if (cuts.equals(Localization.longResultCut0)) {
			return 0;
		} else if (cuts.equals(Localization.longResultCut1)) {
			return 1;
		} else if (cuts.equals(Localization.longResultCut2)) {
			return 2;
		} else if (cuts.equals(Localization.longResultCut3)) {
			return 3;
		} else if (cuts.equals(Localization.longResultCut4)) {
			return 4;
		} else if (cuts.equals(Localization.longResultCut5)) {
			return 5;
		}
		return -1;
	}

	public static String getLongResultCutsOrder() {
		return preferenceManager.getString(LONG_RESULT_CUT_SORT);
	}

	public static boolean getSelfLearn() {
		return false;
	}

	static LoginInfo loginInfo;

	public static LoginInfo getUUIDFromFile() {
		if (loginInfo == null) {
			String homedir = System.getProperty("user.home");
			String loginFile = FilenameUtils.concat(FilenameUtils.concat(homedir, "aiXcoder"), "login");
			String token = null;
			String uuid = null;
			try {
				String content = FileUtils.readFileToString(new File(loginFile), "utf-8");
				JsonObject o = new Gson().fromJson(content, JsonObject.class);
				token = o.get("token").getAsString();
				uuid = o.get("uuid").getAsString();
			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
			} catch (IOException e) {
//				e.printStackTrace();
			}
			loginInfo = new LoginInfo(uuid, token);
		}
		return loginInfo;
	}

	public static boolean isProfessional() {
		if (isProfessionalFetched) {
			return isProfessional;
		}
		try {
			final LoginInfo info = getUUIDFromFile();
			String r = HttpHelper.post("https://aixcoder.com/aixcoderutil/plug/checkToken",
					new Consumer<HttpRequest>() {
						@Override
						public void apply(HttpRequest httpRequest) {
							// send request
							httpRequest.contentType("x-www-form-urlencoded", "UTF-8").form("token", info.token);
						}
					});
			if (r == null) {
				throw new Exception();
			}
			JsonObject o = new Gson().fromJson(r, JsonObject.class);
			isProfessional = o.get("level").getAsInt() == 2 || true;
			isProfessionalError = false;
		} catch (Exception e) {
			e.printStackTrace();
			isProfessionalError = true;
		}
		isProfessionalFetched = true;
		return isProfessional;
	}

	private static String lastLocalEndpoint = null;
	private static long lastLocalEndpointTimestamp = 0;

	/**
	 * 获取本地服务地址
	 *
	 * @return
	 */
	public static String getDefaultLocalEndpoint() {
		if (System.currentTimeMillis() - lastLocalEndpointTimestamp > 10 * 1000) {
			// 检查 localconfig.json
			lastLocalEndpointTimestamp = System.currentTimeMillis();
			String localConfigPath = FilenameUtils
					.concat(FilenameUtils.concat(System.getProperty("user.home"), "aiXcoder"), "localconfig.json");
			String localConfig;
			try {
				localConfig = FileUtils.readFileToString(new File(localConfigPath), Charset.forName("UTF-8"));
			} catch (IOException e) {
				lastLocalEndpoint = "http://localhost:8787"; 
				return lastLocalEndpoint;
			}
			boolean lastLocalEndpointSet = false;
			if (localConfig != null) {
				JsonObject jsonObject = new Gson().fromJson(localConfig, JsonObject.class);
				if (jsonObject != null) {
					Integer port = jsonObject.get("port").getAsInt();
					if (port != null) {
						lastLocalEndpoint = String.format("http://localhost:%d", port);
						lastLocalEndpointSet = true;
					}
				}
			}
			if (!lastLocalEndpointSet) {
				lastLocalEndpoint = "http://localhost:8787";
			}
		}
		return lastLocalEndpoint;
	}

	public static boolean hasLoginFile = false;
	public static boolean useLocalConfigFile = false;

	/**
	 * 启动的时候检查login文件，如果不存在就用本地版
	 */
	public static void detectLocalOrOnline() {
		String loginPath = FilenameUtils.concat(FilenameUtils.concat(System.getProperty("user.home"), "aiXcoder"),
				"login");
		String login;
		try {
			login = FileUtils.readFileToString(new File(loginPath), Charset.forName("UTF-8"));
		} catch (IOException e) {
			login = null;
		}
		if (login != null && login.length() > 0) {
			// 可能登录过校验 uuid
			JsonObject json = new Gson().fromJson(login, JsonObject.class);
//			String token = json.get("token").getAsString();
			String uuid = json.get("uuid").getAsString();
			if (uuid.startsWith("local-")) {
				// 本地服务生成的假uuid，继续使用本地版
				hasLoginFile = false;
				useLocalConfigFile = false;
			} else {
				// 线上版
				hasLoginFile = true;
				useLocalConfigFile = true;
			}
		} else {
			// 没有登录过
			hasLoginFile = false;
			useLocalConfigFile = false;
		}
	}

	public static ConcurrentHashMap<String, LocalServerStatus> models = new ConcurrentHashMap<String, LocalServerStatus>();

	static long lastCheckLocalTime = 0;
	public static String localserver = FilenameUtils
			.concat(FilenameUtils.concat(System.getProperty("user.home"), "aiXcoder"), "localserver.json");

	static void readFile() {
		try {
			if (System.currentTimeMillis() - lastCheckLocalTime < 1000 * 5) {
				return;
			}
			lastCheckLocalTime = System.currentTimeMillis();
			String text = FileUtils.readFileToString(new File(localserver), Charset.forName("UTF-8"));
			JsonObject _j = new Gson().fromJson(text, JsonObject.class);
			if (!_j.has("models")) {
				return;
			}
			JsonArray jo = _j.get("models").getAsJsonArray();
			models.clear();
			for (int i = 0; i < jo.size(); i++) {
				JsonObject j = jo.get(i).getAsJsonObject();
				String name = j.has("name") ? j.get("name").getAsString() : null;
				boolean active = j.has("active") ? j.get("active").getAsBoolean() : true;
				models.put(j.get("name").getAsString(), new LocalServerStatus(name, active));
			}
		} catch (IOException e) {
//			e.printStackTrace();
		}
	}

	static void initWatch() {
		try {
			String dir = FilenameUtils.concat(System.getProperty("user.home"), "aiXcoder");
			FileAlterationObserver observer = new FileAlterationObserver(dir);
			FileAlterationMonitor monitor = new FileAlterationMonitor();
			FileAlterationListener listener = new FileAlterationListenerAdaptor() {
				@Override
				public void onFileCreate(File file) {
					if (file.getName().endsWith("localserver.json")) {
						System.out.println("localserver.json has changed");
						readFile();
					}
				}

				@Override
				public void onFileDelete(File file) {
					if (file.getName().endsWith("localserver.json")) {
						System.out.println("localserver.json has changed");
						readFile();
					}
				}

				@Override
				public void onFileChange(File file) {
					if (file.getName().endsWith("localserver.json")) {
						System.out.println("localserver.json has changed");
						readFile();
					}
				}
			};
			observer.addListener(listener);
			monitor.addObserver(observer);
			monitor.start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static {
		try {
			detectLocalOrOnline();
			new Thread("periodicReadLocalServer") {
				public void run() {
					while (true) {
						try {
							readFile();
							Thread.sleep(1000 * 60 * 5);
						} catch (InterruptedException e) {
							break;
						}
					}
				};
			}.start();
			initWatch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean useLocalEndpoint(String ext) {
		LocalServerStatus model = models.get(ext);
		if (hasLoginFile) {
			if (model != null && model.active) {
				// 使用本地版地址
				return true;
			} else {
				// 使用线上版默认地址
				return false;
			}
		} else {
			return true;
		}
	}

	static String getEndpointBasedonModelConfig(ConcurrentHashMap<String, LocalServerStatus> mc, String ext) {
		boolean localActive = false;
		if (mc.containsKey(ext)) {
			localActive = mc.get(ext).active;
		}
		if (!localActive) {
			return getRemoteEndpoint();
		} else {
			return getDefaultLocalEndpoint();
		}
	}

	public static boolean isInstallAiXcoderApp() {
		String osName = System.getProperty("os.name");
		if (osName.contains("Win")) {
			String aiXcoderAppPath = FilenameUtils.concat(System.getenv("localappdata"), "aixcoderinstaller");
			if (new File(aiXcoderAppPath).exists()) {
				return true;
			} else {
				return false;
			}
		} else {// mac
			String aiXcoderAppPath = "/Applications/aiXcoder.app";
			if (new File(aiXcoderAppPath).exists()) {
				return true;
			} else {
				return false;
			}
		}
	}
	static boolean localOnlineSwitchWindow = false;
	public static String getEndpoint(String ext) {
		String endpoint;
		if (hasLoginFile) {
			ConcurrentHashMap<String, LocalServerStatus> mc = Preference.models;
			if (mc.size() == 0) {
				// prompt for switching to local
				if (!localOnlineSwitchWindow) {
					localOnlineSwitchWindow = true;
					PromptUtils.promptQuestion("Local Online Switch", null, Localization.switchToLocal,
							new String[] { Localization.yes, Localization.no }, new Consumer<String>() {
								@Override
								public void apply(String choice) {
									if (choice != null) {
										if (choice.equals(Localization.yes)) {
											LocalService.switchToLocal(true);
										} else if (choice.equals(Localization.no)) {
											LocalService.switchToLocal(false);
										} else if (choice.equals(Localization.learnMoreLocalService)) {
											try {
										        Desktop desktop = Desktop.getDesktop();
										        if (Desktop.isDesktopSupported()
										                && desktop.isSupported(Desktop.Action.BROWSE)) {
										            URI uri = new URI(Preference.urlLearnMoreLocalService);
										            desktop.browse(uri);
										        }
										    } catch (Exception e) {
										    	e.printStackTrace();
										    }
										}
									}
									localOnlineSwitchWindow = false;
								}
							});
				}
				endpoint = getRemoteEndpoint();
			} else {
				endpoint = getEndpointBasedonModelConfig(mc, ext);
			}
		} else {
			if (isInstallAiXcoderApp()) {
				ConcurrentHashMap<String, LocalServerStatus> mc = Preference.models;
				if (mc.size() == 0) {
					// prompt for switching to local
					if (!localOnlineSwitchWindow) {
						localOnlineSwitchWindow = true;
						PromptUtils.promptQuestion("Local Online Switch", null, Localization.switchToOnline,
								new String[] { Localization.login, Localization.continueToUseLocal }, new Consumer<String>() {
									@Override
									public void apply(String choice) {
										if (choice != null) {
											if (choice.equals(Localization.login)) {
												PromptUtils.promptMessage("Login", null, Localization.promptToLogin);
											} else if (choice.equals(Localization.continueToUseLocal)) {
												LocalService.switchToLocal(true);
											} else if (choice.equals(Localization.learnMoreLocalService)) {
												try {
											        Desktop desktop = Desktop.getDesktop();
											        if (Desktop.isDesktopSupported()
											                && desktop.isSupported(Desktop.Action.BROWSE)) {
											            URI uri = new URI(Preference.urlLearnMoreLocalService);
											            desktop.browse(uri);
											        }
											    } catch (Exception e) {
											    	e.printStackTrace();
											    }
											}
										}
										localOnlineSwitchWindow = false;
									}
								});
					}
					endpoint = getDefaultLocalEndpoint();
				} else {
					endpoint = getEndpointBasedonModelConfig(mc, ext);
				}
			} else {
				endpoint = getDefaultLocalEndpoint();
			}
		}
		if (!endpoint.endsWith("/")) {
			endpoint += "/";
		}
		return endpoint;
	}

	public static String getRemoteEndpoint() {
		return "https://api.aixcoder.com/";
	}

}
