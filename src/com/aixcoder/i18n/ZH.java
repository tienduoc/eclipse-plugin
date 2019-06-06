package com.aixcoder.i18n;

import java.util.HashMap;
import java.util.Map;

public class ZH extends Localization {
	public static final String id = "zh-cn";
	static Map<String, String> m = new HashMap<String, String>();
	static {
		m.put(enableAiXCoder, "启用aiXcoder(&e)");
		m.put(serverURL, "预测服务器地址(&s)");
		m.put(searchURL, "搜索服务器地址(&h)");
		m.put(autoImportClasses, "自动导入类引用(&i)");
		m.put(sortOnly, "仅显示排序（会稍微降低延迟）(&o)");
		m.put(allowTelemetry, "允许发送使用数据(&t)");
		m.put(language, "语言🌏（需要重新打开设置页面）(&l)");
		m.put(model, "模型(&m)");
		m.put(additionalParameters, "额外参数(&p)");
		m.put(description, "AiXcoder是一个AI驱动的代码补全服务。访问 https://aixcoder.com 获得更多信息。");
	}

	public static String R(String input) {
		return m.getOrDefault(input, input);
	}
}
