package com.aixcoder.lib;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.aixcoder.extension.Activator;
import com.aixcoder.extension.AiXPreInitializer;
import com.aixcoder.i18n.Localization;
import com.aixcoder.utils.HttpHelper;
import com.aixcoder.utils.shims.Consumer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
	public static final String ENDPOINT = "ENDPOINT";
	public static final String ENTERPRISE_PORT = "ENTERPRISE_PORT";
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

	public static final String id = Activator.PLUGIN_ID + ".preferences.page";
	public static ScopedPreferenceStore preferenceManager = new ScopedPreferenceStore(InstanceScope.INSTANCE, id);

	private static boolean isProfessional = false;
	public static boolean isProfessionalError = true;
	public static boolean isProfessionalFetched = false;

	public static boolean isActive() {
		new AiXPreInitializer().initializeDefaultPreferences();
		return preferenceManager.getBoolean(ACTIVE);
	}

	public static String remoteVersionUrl;
	public static String group;
	public static String endpoint;
	public static String installPage;
	public static Map<String, String> defaultModel = new HashMap<String, String>();
	static {
		try {
			String configFile = FilenameUtils.concat(FilenameUtils.concat(System.getProperty("user.home"), "aiXcoder"), "aix-enterprise-config.json");
			String text = FileUtils.readFileToString(new File(configFile), "utf-8");
			JsonObject jo = new Gson().fromJson(text, JsonObject.class);
			group = jo.get("group").getAsString();
			installPage = jo.get("installPage").getAsString();
			remoteVersionUrl = jo.get("remoteVersionUrl").getAsString();
			endpoint = jo.get("endpoint").getAsString();
			JsonObject model = jo.get("model").getAsJsonObject();
			for (Entry<String, JsonElement> entry : model.entrySet()) {
				defaultModel.put(entry.getKey(), entry.getValue().getAsString());
			}
		} catch (Exception e) {
			// TODO: handle exception
		} 
	}

	public static String getEndpoint() {
		String endpoint = preferenceManager.getString(ENDPOINT);
		if (endpoint == null || endpoint.isEmpty()) {
			endpoint = Preference.endpoint;
		}
		if (!endpoint.endsWith("/")) {
			endpoint = endpoint + "/";
		}
		return endpoint;
	}

	public static int getEnterprisePort() {
		return preferenceManager.getInt(ENTERPRISE_PORT);
	}

	public static String getModel() {
		String model = preferenceManager.getString(MODEL);
		if (model.equals("java(Java)") && defaultModel.containsKey("java")) {
			model = defaultModel.get("java");
		}
		return model;
	}

	public static String getUUID() {
		LoginInfo info = getUUIDFromFile();
		synchronized (id) {
			preferenceManager.setValue(P_UUID, info.uuid);
		}
		if (preferenceManager.getString(P_UUID) == null || preferenceManager.getString(P_UUID).isEmpty()) {
			synchronized (id) {
				if (preferenceManager.getString(P_UUID) == null || preferenceManager.getString(P_UUID).isEmpty()) {
					preferenceManager.setValue(P_UUID, "eclipse-" + UUID.randomUUID().toString());
				}
			}
		}
		String corpUser = System.getProperty("user.name");
		String uuid = preferenceManager.getString(P_UUID);
		if (corpUser != null && uuid.indexOf(corpUser + "=>") < 0) {
			uuid = corpUser + "=>" + uuid;
			preferenceManager.setValue(P_UUID, uuid);
		}
		if (group == null) {
			return preferenceManager.getString(P_UUID);
		}
		return group + "-" + preferenceManager.getString(P_UUID);
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
		return preferenceManager.getBoolean(SELF_LEARN);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

}
