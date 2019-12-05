package com.aixcoder.core;

import static com.aixcoder.i18n.Localization.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Version;

import com.aixcoder.extension.Activator;
import com.aixcoder.i18n.Localization;
import com.aixcoder.lib.HttpRequest;
import com.aixcoder.lib.HttpRequest.HttpRequestException;
import com.aixcoder.lib.Preference;
import com.aixcoder.utils.CodeStore;
import com.aixcoder.utils.CompletionOptions;
import com.aixcoder.utils.DataMasking;
import com.aixcoder.utils.HttpHelper;
import com.aixcoder.utils.HttpHelper.HTTPMethod;
import com.aixcoder.utils.Predict;
import com.aixcoder.utils.Predict.PredictResult;
import com.aixcoder.utils.Predict.SortResult;
import com.aixcoder.utils.Rescue;
import com.aixcoder.utils.shims.CollectionUtils;
import com.aixcoder.utils.shims.Consumer;
import com.aixcoder.utils.shims.DigestUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class LocalServerStatus {
	String name;
	boolean active;
	String url;

	public LocalServerStatus(String name, boolean active, String url) {
		super();
		this.name = name;
		this.active = active;
		this.url = url;
	}
}

public class API {
	public static long timestamp;
	static long lastCheckLocalTime = 0;
	static ConcurrentHashMap<String, LocalServerStatus> models = new ConcurrentHashMap<String, LocalServerStatus>();

	static void readFile() {
		try {
			String localserver = FilenameUtils.concat(FilenameUtils.concat(System.getProperty("user.home"), "aiXcoder"), "localserver.json");
			if (System.currentTimeMillis() - lastCheckLocalTime < 1000 * 5) {
				return;
			}
			lastCheckLocalTime = System.currentTimeMillis();
			String text = FileUtils.readFileToString(new File(localserver), Charset.forName("UTF-8"));
			JsonArray jo = new Gson().fromJson(text, JsonObject.class).get("models").getAsJsonArray();
			models.clear();
			for (int i = 0; i < jo.size(); i++) {
				JsonObject j = jo.get(i).getAsJsonObject();
				models.put(j.get("name").getAsString(), new LocalServerStatus(j.get("name").getAsString(),
						j.get("active").getAsBoolean(), j.get("url").getAsString()));
			}
		} catch (IOException e) {
			e.printStackTrace();
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
	}

	public static String[] getModels() {
		String body;
		try {
			if (Preference.getEndpoint().isEmpty())
				return null;
			body = HttpHelper.get(Preference.getEndpoint() + "getmodels");
			JsonArray jo = new Gson().fromJson(body, JsonElement.class).getAsJsonArray();
			String[] models = CollectionUtils.getStringList(jo);
			return models;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	static boolean local = false;
	static boolean firstLocalRequestAttempt = true;

	public static PredictResult predict(PredictContext predictContext, String remainingText, String UUID) {
		timestamp = Calendar.getInstance().getTimeInMillis();
		String ext = Preference.getModel();
		String endpoint;
		if (models.containsKey(ext) && models.get(ext).active && models.get(ext).url != null) {
			endpoint = models.get(ext).url;
			System.out.println("LOCAL!");
			local = true;
		} else {
			endpoint = Preference.getEndpoint();
			local = false;
		}
		PredictResult r = predict(true, predictContext, remainingText, UUID, endpoint);
		System.out.println("API.predict took " + (Calendar.getInstance().getTimeInMillis() - timestamp) + "ms");
		return r;
	}

	public static void report(ReportType type, int tokenNum, int charNum) {
		if (Preference.allowTelemetry() && !local) {
			System.out.println("API.report " + type.name());
			final String uuid = Preference.getUUID();
			Map<String, String> m = new HashMap<String, String>();
			m.put("type", String.valueOf(type.getValue()));
			m.put("area", Preference.getModel());
			m.put("plugin_version", Platform.getBundle(Activator.PLUGIN_ID).getVersion().toString());
			m.put("ide_version", Platform.getBundle("org.eclipse.platform").getVersion().toString());
			m.put("ide_type", "eclipse");
			m.put("uuid", uuid);
			m.put("token_num", String.valueOf(tokenNum));
			m.put("char_num", String.valueOf(charNum));
			try {
				HttpHelper.post(Preference.getEndpoint() + "user/predict/userUseInfo", m);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (HttpRequestException e) {
				e.printStackTrace();
			}
		}
	}

	private static Rescue[] readRescues(JsonArray jsonRescues) {
		if (jsonRescues != null && jsonRescues.size() > 0) {
			Rescue[] rescues = new Rescue[jsonRescues.size()];
			for (int rescue_i = 0; rescue_i < jsonRescues.size(); rescue_i++) {
				JsonObject jsonRescue = jsonRescues.get(rescue_i).getAsJsonObject();
				Rescue rescue = new Rescue();
				rescue.type = jsonRescue.get("type").getAsString();
				rescue.value = jsonRescue.get("value").getAsString();
				rescues[rescue_i] = rescue;
			}
			return rescues;
		}
		return null;
	}

	static int localError = 0;
	static boolean localAutoStart = false;
	static boolean askedLocalAutoStart = false;

	static void startLocalServer() {
		String url_open = "aixcoder://localserver";
		LocalService.openurl(url_open);
	}

	static boolean isProfessionalErrorShown = false;
	static boolean notProfessionalErrorShown = false;
	static Learner learner;

	public static void learn(String ext, String fileid) {
		if (Preference.getSelfLearn()) {
			boolean professional = Preference.isProfessional();
			if (Preference.isProfessionalError && !isProfessionalErrorShown) {
				isProfessionalErrorShown = true;
				new UIJob("Prompt aiXcoder unable to login") {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						boolean login = MessageDialog.openQuestion(null, R(Localization.unableToLoginTitle),
								R(Localization.unableToLogin));
						if (login) {
							String url_open = "aixcoder://login";
							try {
								java.awt.Desktop.getDesktop().browse(java.net.URI.create(url_open));
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						return Status.OK_STATUS;
					}
				}.schedule();
			} else if (professional) {
				if (learner == null) {
					learner = new Learner();
				}
				learner.learn(ext, fileid);
			} else {
				isProfessionalErrorShown = true;
				new UIJob("Prompt aiXcoder not professional") {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						boolean login = MessageDialog.openQuestion(null, R(Localization.notProfessionalTitle),
								R(Localization.notProfessional));
						if (login) {
							String url_open = "https://www.aixcoder.com/#/Product?tab=0";
							try {
								java.awt.Desktop.getDesktop().browse(java.net.URI.create(url_open));
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		}
	}

	public static PredictResult predict(boolean allowRetry, final PredictContext predictContext,
			final String remainingText, final String UUID, String endpoint) {
		try {
			final String fileid = predictContext.filename;
			final String ext = Preference.getModel();
			learn(ext, fileid);
			final String uuid = Preference.getUUID();
			final String proj = predictContext.proj;
			final String text = predictContext.prefix;
			final String maskedText = DataMasking.mask(text);
			final String maskedRemainingText = DataMasking.mask(remainingText);
			final int offset = CodeStore.getInstance().getDiffPosition(fileid, maskedText);
			final String md5 = DigestUtils.getMD5(maskedText);
			final int longResultCuts = Preference.getLongResultCuts();
			if (!endpoint.endsWith("/")) {
				endpoint = endpoint + "/";
			}
			//local true
			final Map<String,Object> localParameter = new HashMap<String, Object>();
			if(true) {
				String laterFileId = fileid + ".later";
				StringBuilder laterCode = new StringBuilder(predictContext.suffix);
				StringBuilder laterCodeReverse = laterCode.reverse();
				int laterOffset = CodeStore.getInstance().getDiffPosition(laterFileId, laterCodeReverse.toString());
				String laterMd5 = DigestUtils.getMD5(laterCodeReverse.toString());
				laterCode = new StringBuilder(laterCodeReverse.substring(laterOffset)).reverse();
				if (true){
					localParameter.put("laterCode", laterCode);
					localParameter.put("laterOffset", String.valueOf(laterOffset));
					localParameter.put("laterMd5", laterMd5);
				}

			}


			String string = HttpHelper.post(endpoint + "predict", new Consumer<HttpRequest>() {
				@Override
				public void apply(HttpRequest httpRequest) {
					// send request
					httpRequest.contentType("x-www-form-urlencoded", "UTF-8").form("queryUUID", UUID)
							.form("text", maskedText.substring(offset)).form("uuid", uuid).form("project", proj)
							.form("projectRoot", predictContext.projRoot)
							.form("ext", ext).form("fileid", fileid).form("remaining_text", maskedRemainingText)
							.form("offset", String.valueOf(offset)).form("md5", md5).form("sort", 1)
							.form("long_result_cuts", longResultCuts);
					if (Preference.sortOnly()) {
						httpRequest.form("ngen", 1);
					}

					String params = Preference.getParams();
					for (String param : params.split("&")) {
						if (param.length() == 0 || params.indexOf("=") == -1)
							continue;
						String[] paramParts = param.split("=");
						String paramKey = paramParts[0];
						String paramValue = paramParts[1];
						httpRequest.form(paramKey, paramValue);
					}

					// localParameter
					if (localParameter.size() > 0) {
						httpRequest.form(localParameter, "UTF-8");
					}
				}
			});
			if (string == null) {
				return null;
			}

			if (string.contains("Conflict")) {
				CodeStore.getInstance().invalidateFile(proj, fileid);
				if (localParameter.size() > 0) {
					String laterFileId = fileid + ".later";
					CodeStore.getInstance().invalidateFile(proj, laterFileId);
				}
				if (allowRetry) {
					return predict(false, predictContext, maskedRemainingText, UUID, endpoint);
				}
			} else {
				System.out.println(string);
				JsonArray list;
				try {
					JsonObject jo = new Gson().fromJson(string, JsonObject.class);
					list = jo.get("data").getAsJsonArray();
				} catch (com.google.gson.JsonSyntaxException e) {
					list = new Gson().fromJson(string, JsonArray.class);
				}
				if (list.size() > 0) {
					CodeStore.getInstance().saveLastSent(proj, fileid, maskedText);
					if (localParameter.size() > 0) {
						String laterFileId = fileid + ".later";
						StringBuilder laterCode = new StringBuilder(predictContext.suffix);
						StringBuilder laterCodeReverse = laterCode.reverse();
						CodeStore.getInstance().saveLastSent(proj, laterFileId,laterCodeReverse.toString());
					}
				}
				Predict.LongPredictResult[] longPredicts = new Predict.LongPredictResult[list.size()];
				SortResult[] sortResults = null;
				for (int j = 0; j < list.size(); j++) {
					JsonObject json = list.get(j).getAsJsonObject();

					JsonElement jsonTokens = json.get("tokens");
					String[] tokens = CollectionUtils
							.getStringList(jsonTokens != null ? jsonTokens.getAsJsonArray() : null);

					JsonElement jsonCurrent = json.get("current");
					String current = jsonCurrent != null ? jsonCurrent.getAsString() : "";

					JsonElement jsonRCompletion = json.get("r_completion");
					String[] rCompletion = CollectionUtils
							.getStringList(jsonRCompletion != null ? jsonRCompletion.getAsJsonArray() : null);

					JsonArray sortList = json.getAsJsonArray("sort");
					if (sortList != null && sortList.size() > 0) {
						sortResults = new SortResult[sortList != null ? sortList.size() : 0];
						for (int i = 0; i < sortResults.length; i++) {
							JsonArray asJsonArray = sortList.get(i).getAsJsonArray();
							double prob = asJsonArray.get(0).getAsDouble();
							String word = asJsonArray.get(1).getAsString();
							CompletionOptions options = null;
							if (asJsonArray.size() >= 3) {
								options = new CompletionOptions();
								JsonObject jsonOptions = asJsonArray.get(2).getAsJsonObject();
								if (jsonOptions.get("forced") != null && jsonOptions.get("forced").getAsBoolean()) {
									options.forced = true;
								}
								if (jsonOptions.get("rescues") != null) {
									JsonArray rescues = jsonOptions.get("rescues").getAsJsonArray();
									options.rescues = readRescues(rescues);
								}
								if (jsonOptions.get("filters") != null) {
									JsonArray filters = jsonOptions.get("filters").getAsJsonArray();
									options.filters = CollectionUtils.getStringList(filters);
								}
							}
							sortResults[i] = new SortResult(prob, word, options);
						}
					}
					JsonElement jsonRescues = json.get("rescues");
					Rescue[] rescues = readRescues(jsonRescues != null ? jsonRescues.getAsJsonArray() : null);
					longPredicts[j] = new Predict.LongPredictResult(tokens, current, rescues, rCompletion);
				}
				localError = 0;
				return new PredictResult(longPredicts, sortResults);
			}
		} catch (HttpRequest.HttpRequestException e) {
			if (e.getMessage().contains("Read timed out")) {
				System.out.println("time out");
			} else {
				e.printStackTrace();
			}
			if (local) {
				localError++;
				if (localError >= 5) {
					startLocalServer();
					localError = 0;
				} else if (firstLocalRequestAttempt) {
					startLocalServer();
					firstLocalRequestAttempt = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return new PredictResult(new Predict.LongPredictResult[0], new SortResult[0]);
	}

	public static String[] getTrivialLiterals() {
		try {
			final String uuid = Preference.getUUID();
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("uuid", uuid);
			params.put("ext", Preference.getModel());
			String string = HttpHelper.get(Preference.getEndpoint() + "trivial_literals", params);
			JsonArray jo = new Gson().fromJson(string, JsonArray.class);
			String[] literals = CollectionUtils.getStringList(jo);
			return literals;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void zipFile(String project, String[] data) {
		if (data.length == 0)
			return;
		try {
			final String uuid = Preference.getUUID();
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("uuid", uuid);
			params.put("project", project);
			params.put("ext", Preference.getModel());

			ByteArrayOutputStream wr = new ByteArrayOutputStream();
			DeflaterOutputStream zipOut = new DeflaterOutputStream(wr);
			String str = new Gson().toJson(data);
			zipOut.write(Charset.forName("utf-8").encode(str).array());
			zipOut.close();
			wr.close();
			final byte[] arr = wr.toByteArray();
			HttpHelper.post(Preference.getEndpoint() + "zipfile2", params, new Consumer<HttpRequest>() {
				@Override
				public void apply(HttpRequest t) {
					t.connectTimeout(60 * 1000).readTimeout(60 * 1000);
					t.send(arr);
				}
			});
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static double parseVersion(String v) {
		if (v.isEmpty()) {
			return 0;
		}
		Pattern p = Pattern.compile("^(\\D*)(\\d*)(\\D*)$");
		Matcher m = p.matcher(v);
		m.find();
		if (m.group(2).isEmpty()) {
			// v1.0.0.[preview]
			return -1;
		}
		double i = Integer.parseInt(m.group(2));
		if (!m.group(3).isEmpty()) {
			// v1.0.[0b]
			i -= 0.1;
		}
		return i;
	}

	public static int versionCompare(String str1, String str2) {
		String[] v1 = str1.split("\\.");
		String[] v2 = str2.split("\\.");
		int i = 0;
		for (; i < v1.length && i < v2.length; i++) {
			double iv1 = parseVersion(v1[i]);
			double iv2 = parseVersion(v2[i]);

			if (iv1 != iv2) {
				return iv1 - iv2 < 0 ? -1 : 1;
			}
		}
		if (i < v1.length) {
			// "1.0.1", "1.0"
			double iv1 = parseVersion(v1[i]);
			return iv1 < 0 ? -1 : (int) Math.ceil(iv1);
		}
		if (i < v2.length) {
			double iv2 = parseVersion(v2[i]);
			return -iv2 < 0 ? -1 : (int) Math.ceil(iv2);
		}
		return 0;
	}

	public static void checkUpdate(Version version) {
		try {
			String updateURL = "https://api.github.com/repos/aixcoder-plugin/localservice/releases/latest";
			String versionJson = HttpHelper.request(HTTPMethod.GET, updateURL, null, new Consumer<HttpRequest>() {

				@Override
				public void apply(HttpRequest t) {
					t.header("User-Agent", "aiXcoder-eclipse-plugin");
				}

			});
			JsonObject newVersions = new Gson().fromJson(versionJson, JsonElement.class).getAsJsonObject();
			String v = newVersions.get("tag_name").getAsString();
			String localVersion = LocalService.getVersion();
			boolean doUpdate = false;
			if (versionCompare(localVersion, v) < 0) {
				System.out.println("New aiXCoder version is available: " + v);
				doUpdate = true;
			} else {
				System.out.println("AiXCoder is up to date");
				doUpdate = false;
			}
			if (doUpdate) {
				LocalService.forceUpdate();
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
