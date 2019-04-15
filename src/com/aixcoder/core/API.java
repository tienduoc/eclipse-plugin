package com.aixcoder.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

import com.aixcoder.lib.HttpRequest;
import com.aixcoder.lib.Preference;
import com.aixcoder.utils.CodeStore;
import com.aixcoder.utils.DataMasking;
import com.aixcoder.utils.HttpHelper;
import com.aixcoder.utils.Predict.PredictResult;
import com.aixcoder.utils.shims.CollectionUtils;
import com.aixcoder.utils.shims.Consumer;
import com.aixcoder.utils.shims.DigestUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class API {
	public static long timestamp;

	public static String[] getModels() {
		String body;
		try {
			body = HttpHelper.get(Preference.getEndpoint() + "getmodels");
			JsonArray jo = new Gson().fromJson(body, JsonElement.class).getAsJsonArray();
			String[] models = CollectionUtils.getStringList(jo);
			return models;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static PredictResult predict(PredictContext predictContext, String remainingText, String UUID) {
		timestamp = Calendar.getInstance().getTimeInMillis();
		PredictResult r = predict(true, predictContext, remainingText, UUID);
		System.out.println("API.predict took " + (Calendar.getInstance().getTimeInMillis() - timestamp) + "ms");
		return r;
	}

	public static void report(String type) {
		final String uuid = Preference.getUUID();
		Map<String, String> m = new HashMap<String, String>();
		m.put("uuid", uuid);
		try {
			HttpHelper.get(Preference.getEndpoint() + "user/predict/" + type, m);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public static PredictResult predict(boolean allowRetry, final PredictContext predictContext,
			final String remainingText, final String UUID) {
		try {
			final String fileid = predictContext.filename;
			final String uuid = Preference.getUUID();
			final String proj = predictContext.proj;
			final String text = predictContext.prefix;
			final String maskedText = DataMasking.mask(text);
			final String maskedRemainingText = DataMasking.mask(remainingText);
			final int offset = CodeStore.getInstance().getDiffPosition(fileid, maskedText);
			final String md5 = DigestUtils.getMD5(maskedText);
			String string = HttpHelper.post(Preference.getEndpoint() + "predict", new Consumer<HttpRequest>() {
				@Override
				public void apply(HttpRequest httpRequest) {
					// send request
					httpRequest.contentType("x-www-form-urlencoded", "UTF-8").form("queryUUID", UUID)
							.form("text", maskedText.substring(offset)).form("uuid", uuid).form("project", proj)
							.form("ext", Preference.getModel()).form("fileid", fileid)
							.form("remaining_text", maskedRemainingText).form("offset", String.valueOf(offset))
							.form("md5", md5);

					String params = Preference.getParams();
					for (String param : params.split("&")) {
						if (param.length() == 0 || params.indexOf("=") == -1)
							continue;
						String[] paramParts = param.split("=");
						String paramKey = paramParts[0];
						String paramValue = paramParts[1];
						httpRequest.form(paramKey, paramValue);
					}
				}
			});
			if (string == null) {
				return null;
			}

			if (string.contains("Conflict")) {
				CodeStore.getInstance().invalidateFile(proj, fileid);
				if (allowRetry) {
					return predict(false, predictContext, maskedRemainingText, UUID);
				}
			} else {
				System.out.println(string);
				JsonObject jo = new Gson().fromJson(string, JsonObject.class);
				JsonArray list = jo.get("data").getAsJsonArray();
				if (list.size() > 0) {
					CodeStore.getInstance().saveLastSent(proj, fileid, maskedText);

					JsonObject json = list.get(0).getAsJsonObject();
					String[] tokens = CollectionUtils.getStringList(json.get("tokens").getAsJsonArray());
					String current = json.get("current").getAsString();
					String[] rCompletion = CollectionUtils.getStringList(json.get("r_completion").getAsJsonArray());
					return new PredictResult(tokens, current, rCompletion);
				}
			}
		} catch (HttpRequest.HttpRequestException e) {
			if (e.getMessage().contains("Read timed out")) {
				System.out.println("time out");
			} else {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return new PredictResult(new String[0], "", null);
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
		try {
			final String uuid = Preference.getUUID();
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("uuid", uuid);
			params.put("project", project);
			params.put("ext", Preference.getModel());

			ByteArrayOutputStream wr = new ByteArrayOutputStream();
			DeflaterOutputStream zipOut = new DeflaterOutputStream(wr);
			for (String fileId : data) {
				byte[] bytes = fileId.getBytes(Charset.forName("UTF-8"));
				byte[] sizeBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(bytes.length).array();
				zipOut.write(sizeBytes);
				zipOut.write(bytes);
			}
			zipOut.close();
			wr.close();
			final byte[] arr = wr.toByteArray();
			HttpHelper.post(Preference.getEndpoint() + "zipfile", params, new Consumer<HttpRequest>() {
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
}
