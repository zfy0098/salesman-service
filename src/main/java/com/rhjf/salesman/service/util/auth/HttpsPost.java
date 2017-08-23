package com.rhjf.salesman.service.util.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * @ClassName HttpsPost 
 * @Description httpsPost请求�?
 */
public class HttpsPost {


	/**
	 * @Title post 
	 * @Description 请求方法
	 * @param url 
	 * @param params
	 * @param timeout
	 * @param characterSet
	 * @return HttpSendResult
	 */
	public static HttpSendResult post(String url, Map<String, String> params, int timeout, String characterSet) {
		if (characterSet == null || "".equals(characterSet)) {
			characterSet = "UTF-8";
		}
		HttpSendResult result = new HttpSendResult();

		SSLConnectionSocketFactory sslsf = null;
		try {
			sslsf = getSSLConnectionSocketFactory();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e1) {
			e1.printStackTrace();
			return result;
		}
		CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();

		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Connection", "close");
		httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=" + characterSet);

		httpPost.setConfig(config);


		// 建立NameValuePair数组，用于存储欲传的参数
		List<NameValuePair> postParam = new ArrayList<NameValuePair>();
		for (String key : params.keySet()) {
			postParam.add(new BasicNameValuePair(key, params.get(key)));
		}
		// 添加参数
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(postParam, characterSet));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return result;
		}

		CloseableHttpResponse resp = null;
		try {
			resp = client.execute(httpPost);
			result.setStatus(resp.getStatusLine().getStatusCode());
			result.setResponseBody(EntityUtils.toString(resp.getEntity(), characterSet));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			return result;
		} finally {
			if (resp != null) {
				try {
					resp.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}

		return result;
	}

	/**
	 * @Title getSSLConnectionSocketFactory
	 * @Description 创建没有证书的SSL链接工厂�?
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws KeyManagementException
	 * @return SSLConnectionSocketFactory
	 */
	public static SSLConnectionSocketFactory getSSLConnectionSocketFactory()
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		SSLContextBuilder context = new SSLContextBuilder();
		context.loadTrustMaterial(null, new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		});
		return new SSLConnectionSocketFactory(context.build());
	}
}
