package com.aixcoder.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.aixcoder.extension.Activator;
import com.aixcoder.lib.HttpRequest;
import com.aixcoder.utils.shims.Consumer;

public class HttpHelper {

	public enum HTTPMethod {
		POST, GET
	}

	public static String urlBuilder(String url, Map<String, String> params) {
		StringBuilder paramsSB = new StringBuilder(url);
		if (params != null && params.size() > 0) {
			paramsSB.append("?");
			boolean first = true;
			try {
				for (Entry<String, String> entry : params.entrySet()) {
					if (!first) {
						paramsSB.append("&");
					}
					paramsSB.append(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "utf-8"));
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return paramsSB.toString();
	}

	public final static int TIME_OUT = 2500;

	public static IProxyData lastSuccessfulProxy;

	public static boolean proxyDataEquals(IProxyData a, IProxyData b) {
		return a.getType().equals(b.getType()) && a.getHost().equals(b.getHost()) && a.getPort() == b.getPort()
				&& a.isRequiresAuthentication() == b.isRequiresAuthentication() && (!a.isRequiresAuthentication()
						|| (a.getUserId().equals(b.getUserId()) && a.getPassword().equals(b.getPassword())));
	}

	private static String requestRaw(HTTPMethod method, String url, IProxyData proxy, Consumer<HttpRequest> prepare)
			throws URISyntaxException {
		// step 1: build request
		HttpRequest httpRequest = method == HTTPMethod.POST ? HttpRequest.post(url) : HttpRequest.get(url);

		// step 2: check proxy
		if (proxy == null) {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference<IProxyService> psReference = context
					.getServiceReference(org.eclipse.core.net.proxy.IProxyService.class);
			IProxyService ps = context.getService(psReference);
			IProxyData[] pp = ps.select(new URI(url));
			if (pp.length > 0) {
				if (lastSuccessfulProxy != null) {
					for (IProxyData iProxyData : pp) {
						if (proxyDataEquals(lastSuccessfulProxy, iProxyData)) {
							proxy = lastSuccessfulProxy;
							break;
						}
					}
				}
				if (proxy == null) {
					// no successful proxy
					for (IProxyData iProxyData : pp) {
						String pr = requestRaw(method, url, iProxyData, prepare);
						if (pr != null) {
							lastSuccessfulProxy = iProxyData;
							return pr;
						}
					}
					return null;
				}
			}
		}

		// set proxy
		if (proxy != null) {
			httpRequest.useProxy(proxy.getHost(), proxy.getPort());
			if (proxy.isRequiresAuthentication()) {
				httpRequest.proxyBasic(proxy.getUserId(), proxy.getPassword());
			}
		}
		httpRequest.connectTimeout(TIME_OUT).readTimeout(TIME_OUT).useCaches(false);
		prepare.apply(httpRequest);
		try {
			String r = httpRequest.body();
			return r;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			httpRequest.disconnect();
		}
	}

	public static String request(HTTPMethod method, String url, Map<String, String> params,
			Consumer<HttpRequest> prepare) throws URISyntaxException {
		url = HttpHelper.urlBuilder(url, params);
		return requestRaw(method, url, null, prepare);
	}

	public static String post(String url, Consumer<HttpRequest> prepare) throws URISyntaxException {
		return request(HTTPMethod.POST, url, null, prepare);
	}

	public static String post(String url, Map<String, String> params, Consumer<HttpRequest> prepare)
			throws URISyntaxException {
		return request(HTTPMethod.POST, url, params, prepare);
	}

	public static String get(String url) throws URISyntaxException {
		return request(HTTPMethod.GET, url, null, new Consumer.ConsumerAdapter<HttpRequest>());
	}

	public static String get(String url, Map<String, String> params) throws URISyntaxException {
		return request(HTTPMethod.GET, url, params, new Consumer.ConsumerAdapter<HttpRequest>());
	}
}
