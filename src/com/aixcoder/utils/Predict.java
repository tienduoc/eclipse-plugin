package com.aixcoder.utils;

import java.util.List;

import com.aixcoder.core.PredictContext;
import com.aixcoder.lib.HttpRequest;
import com.aixcoder.lib.JSON;
import com.aixcoder.lib.Preference;
import com.aixcoder.utils.shims.CollectionUtils;
import com.aixcoder.utils.shims.DigestUtils;

public class Predict {
	public static class PredictResult {
		public String[] tokens;
		public String current;
		public String[] rCompletions;

		public PredictResult(String[] tokens, String current, String[] rCompletions) {
			super();
			this.tokens = tokens;
			this.current = current;
			this.rCompletions = rCompletions;
		}

		public String toString() {
			return "PredictResult: (" + current + ")[" + CollectionUtils.join(" ", tokens) + "]";
		}
	}

	public final static int TIME_OUT = 2500;

	public static PredictResult predict(PredictContext predictContext, String remainingText) {
		try {
			String fileid = predictContext.filename;
			String uuid = "eclipse-" + Preference.getUUID();
			String proj = predictContext.proj;
			String text = predictContext.prefix;
			text = DataMasking.mask(text);
			remainingText = DataMasking.mask(remainingText);
			int offset = CodeStore.getInstance().getDiffPosition(fileid, text);
			String md5 = DigestUtils.getMD5(text);

			HttpRequest httpRequest = HttpRequest.post(Preference.getEndpoint() + "predict").connectTimeout(TIME_OUT)
					.readTimeout(TIME_OUT).useCaches(false).contentType("x-www-form-urlencoded", "UTF-8")
					.form("text", text.substring(offset)).form("uuid", uuid).form("project", proj)
					.form("ext", Preference.getModel()).form("fileid", DigestUtils.getMD5(fileid))
					.form("remaining_text", remainingText).form("offset", String.valueOf(offset)).form("md5", md5);
			String string = httpRequest.body();
			httpRequest.disconnect();
			List<JSON> list = JSON.decode(string).getList();
			if (list.size() > 0) {
				JSON json = list.get(0);
				String[] tokens = JSON.getStringList(json.getList("tokens"));
				String current = json.getString("current");
				String[] rCompletion = JSON.getStringList(json.getList("r_completion"));
				return new PredictResult(tokens, current, rCompletion);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new PredictResult(new String[0], "", null);
	}
}
