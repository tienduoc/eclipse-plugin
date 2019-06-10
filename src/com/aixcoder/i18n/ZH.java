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
		m.put(description, "AiXCoder是一个AI驱动的代码补全服务。访问 https://aixcoder.com 获得更多信息。");;
		m.put(telemetryTitle, "AiXCoder用户使用信息收集");
		m.put(telemetryQuestion, "您愿意发送匿名的使用数据以提高未来的用户体验吗？您可以稍后在设置页里修改这个设置。");
		m.put(endpointEmptyTitle, "AiXCoder预测服务器地址为空!");
		m.put(endpointEmptyWarning, "AiXCoder预测服务器地址未被设置。 请在Window->Preferences->AiXCoder Preferences设置页面中手动设置。我们的公用地址是 https://api.aixcoder.com/");
	}

	public static String R(String input) {
		if (m.containsKey(input))
			return m.get(input);
		return input;
	}
}
