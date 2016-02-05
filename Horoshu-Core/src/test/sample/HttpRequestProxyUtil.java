package orange.core.horoshu.http.sample;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.*;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

public class HttpRequestProxyUtil {

	static Logger logger = Logger.getLogger(HttpRequestProxyUtil.class
			.getName());

	/**
	 * 使用get的方式访问URL
	 *
	 * @param url
	 *            访问地址
	 * @return 请求结果
	 * @throws Exception
	 * @throws IOException
	 */
	public static String doGet(String url,String accessToken) throws Exception {
		if (url == null) {
			return null;
		}
		String response = null;
		HttpClient httpClient = new HttpClient();
		GetMethod get = new GetMethod(url);
		try {
			/*String authorization = ProConfig.getProValues("authorization");*/

			get.setRequestHeader("accessToken", accessToken);
			get.setDoAuthentication(true);

			int result = httpClient.executeMethod(get);
			logger.debug("get method execRequest code:" + result);
			if (result > 200) {
				return null;
			}
			response = get.getResponseBodyAsString();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			get.releaseConnection();
			httpClient.getHttpConnectionManager().closeIdleConnections(0);
		}
		return response;
	}

	/**
	 * 使用get的方式访问URL
	 *
	 * @param url
	 *            访问地址
	 * @return 请求结果
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String doGet(String url) throws Exception {
		if (url == null) {
			return null;
		}
		String response = null;
		HttpClient httpClient = new HttpClient();
		GetMethod get = new GetMethod(url);
		try {
			String authorization = ProConfig.getProValues("authorization");

			get.setRequestHeader("Authorization", authorization);
			get.setDoAuthentication(true);

			int result = httpClient.executeMethod(get);
			logger.debug("get method execRequest code:" + result);
			if (result > 200) {
				return null;
			}
			response = get.getResponseBodyAsString();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			get.releaseConnection();
			httpClient.getHttpConnectionManager().closeIdleConnections(0);
		}
		return response;
	}

	/**
	 * 使用delete的方式访问URL
	 *
	 * @param url
	 *            访问地址
	 * @return 请求结果
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String doDelete(String url) throws Exception {
		if (url == null) {
			return null;
		}
		String response = null;
		HttpClient httpClient = new HttpClient();
		DeleteMethod delete = new DeleteMethod(url);
		try {
			String authorization = ProConfig.getProValues("authorization");

			delete.setRequestHeader("Authorization", authorization);
			delete.setDoAuthentication(true);

			int result = httpClient.executeMethod(delete);
			logger.debug("delete method execRequest code:" + result);
			if (result > 200) {
				return null;
			}
			response = delete.getResponseBodyAsString();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			delete.releaseConnection();
			httpClient.getHttpConnectionManager().closeIdleConnections(0);
		}
		return response;
	}

	/**
	 * 根据accessToken 获取(采用post 方法)
	 *
	 * @param url
	 * @param jsonParam
	 * @param accessToken
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String doPost(String url, String jsonParam,String accessToken) {
		if (StringUtils.isBlank(url) || StringUtils.isBlank(jsonParam)) {
			return null;
		}
		String strRequest = null;
		HttpClient httpClient = new HttpClient();
		PostMethod post = new PostMethod(url);
		HttpPost httpPost = new HttpPost(url);
		try {

			post.addRequestHeader(HTTP.CONTENT_TYPE,
					"application/json;charset=UTF-8");
			//post.setRequestHeader("accessToken", value.replace("\r\n",""));
			post.addRequestHeader("accessToken", accessToken);
			httpPost.addHeader("Content-Type", "application/json");
			// httpPost.addR("accesstoken", accessToken);
			post.setRequestBody(jsonParam.toString());
			// post.releaseConnection();
			// httpPost.addHeader("AccessToken", token);
			int result = httpClient.executeMethod(post);
			logger.info("doPost--------result--------------------------------:"
					+ result);
			System.out
					.println("doPost--------result--------------------------------:"
							+ result);
			if (result > 200) {
				logger.info(String.valueOf(result) + ":"
						+ post.getResponseBodyAsString());
			}
			strRequest = post.getResponseBodyAsString();
			logger.info("doPost--------result--------------------------------:"
					+ String.valueOf(result) + ":"
					+ post.getResponseBodyAsString());
			System.out
					.println("doPost--------result--------------------------------:"
							+ String.valueOf(result)
							+ ":"
							+ post.getResponseBodyAsString());

		} catch (Exception e) {
			System.out.println(e.getMessage());
			logger.debug(e.getMessage());
			e.printStackTrace();
		} finally {
			post.releaseConnection();
			httpClient.getHttpConnectionManager().closeIdleConnections(0);
		}
		System.out.println("doPost-----------------------end-----------------");
		return strRequest;

	}

	/**
	 * 根据accessToken 获取(采用put 方法)
	 *
	 * @param url
	 * @param jsonParam
	 * @param accessToken
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String doPut(String url, String jsonParam,String accessToken) {
		if (StringUtils.isBlank(url) || StringUtils.isBlank(jsonParam)) {
			return null;
		}
		String strRequest = null;
		HttpClient httpClient = new HttpClient();
		PutMethod put = new PutMethod(url);
		HttpPut httpPut = new HttpPut(url);
		try {

			put.addRequestHeader(HTTP.CONTENT_TYPE,
					"application/json;charset=UTF-8");
			//put.setRequestHeader("accessToken", value.replace("\r\n",""));
			put.addRequestHeader("accessToken", accessToken);
			httpPut.addHeader("Content-Type", "application/json");
			// httpPost.addR("accesstoken", accessToken);
			put.setRequestBody(jsonParam.toString());
			// put.releaseConnection();
			// httpPost.addHeader("AccessToken", token);
			int result = httpClient.executeMethod(put);
			logger.info("doPut--------result--------------------------------:"
					+ result);
			System.out
					.println("doPut--------result--------------------------------:"
							+ result);
			if (result > 200) {
				logger.info(String.valueOf(result) + ":"
						+ put.getResponseBodyAsString());
			}
			strRequest = put.getResponseBodyAsString();
			logger.info("doPut--------result--------------------------------:"
					+ String.valueOf(result) + ":"
					+ put.getResponseBodyAsString());
			System.out
					.println("doPut--------result--------------------------------:"
							+ String.valueOf(result)
							+ ":"
							+ put.getResponseBodyAsString());

		} catch (Exception e) {
			System.out.println(e.getMessage());
			logger.debug(e.getMessage());
			e.printStackTrace();
		} finally {
			put.releaseConnection();
			httpClient.getHttpConnectionManager().closeIdleConnections(0);
		}
		System.out.println("doPut-----------------------end-----------------");
		return strRequest;

	}

}
