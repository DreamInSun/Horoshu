package orange.core.horoshu.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Http链式请求构造器。
 * 使用"HTTP 1.1"通用请求规范，支持GET/POST/PUT/DELETE/HEAD/OPTIONS/TRACE/PATCH
 * <p/>
 * 注意事项：
 * <ol>
 * <li>必须使用HttpSvc.build()或者HttpSvc.build()创建。</li>
 * <li>必须执行setURI</li>
 * <li>其他函数可选执行</li>
 * <li>最后执行动作函数，返回HttpResponse</li>
 * <li>如果设置了FutureCallback,则使用异步调用，不再返回同步结果</li>
 * </ol>
 * Created by DreamInSun on 2016/2/3.
 */
public class HttpReqChain {
    /*===== Constants =====*/

    /*===== Chain Properties =====*/
    /* Fast JSON Config */
    public static SerializeConfig g_jsonSerializeConfig = new SerializeConfig() {

    };
    /*========== Static Properties ==========*/
    private static Logger g_logger = org.slf4j.LoggerFactory.getLogger(HttpReqChain.class.getName());
    /*========== Execute Request ==========*/
    private static long g_RequestIdCnt = 0;
    /*===== Chain Properties =====*/
    final private HttpSvc m_httpSvc;
    public long m_timestamp;
    /*========== Properties ==========*/
    /*===== Configuration =====*/
    private String m_dfltCharset = HttpRequest.CONTENT_CHARSET_UTF8;
    private NameValuePair m_dfltCharsetPair = new BasicNameValuePair(HttpRequest.CONTENT_CHARSET_KEY, HttpRequest.CONTENT_CHARSET_UTF8);
    private long m_reqestId;
    private URIBuilder m_uriBuilder;
    private HttpRequest m_req;
    private FutureCallback<? super HttpResponse> m_respFutureClbk;
    //TODO Optimize this error dealer
    private List<Throwable> m_errors;

    /*===== Constructor =====*/
    public HttpReqChain(HttpSvc httpSvc) {
        /*===== Input Protection =====*/
        if (null == httpSvc) {
            pushError(new Exception("HttpSvc is Null"));
        }
        /*=====  =====*/

        /*=====  =====*/
        m_httpSvc = httpSvc;
        m_uriBuilder = new URIBuilder();
        m_req = new HttpRequest();
    }

    private static long getReqID() {
        return g_RequestIdCnt++;
    }

    /*===== Assistant Function =====*/
    public long getReqId() {
        return m_reqestId;
    }

    /*===== Assistant Function =====*/
    private boolean isLastOpFail() {
        return !(m_errors == null || m_errors.isEmpty());
    }

    private void pushError(Exception e) {
        if (m_errors == null) {
            m_errors = new ArrayList<>();
        }
        m_errors.add(e);
    }

    /*===== Getter & Setter =====*/
    public HttpRequest getReq() {
        return m_req;
    }


    /*===== Export Function =====*/

    public URIBuilder getUriBuilder() {
        return m_uriBuilder;
    }

    public HttpSvc getHttpSvc() {
        return m_httpSvc;
    }

    /**
     * 直接使用字符串设置资源名（URI），输入字符串必须符合URI规范
     *
     * @param uriStr 合法的URI字符串
     * @return Http构造链
     */
    public HttpReqChain setURI(String uriStr) {
        if (isLastOpFail()) return this;
        /*===== Input Protection =====*/
        if (null == uriStr || uriStr.isEmpty()) {
            pushError(new NullPointerException("uriStr is Null"));
        }
        /*===== Convert =====*/
        try {
            m_uriBuilder = new URIBuilder(uriStr);
        } catch (URISyntaxException e) {
            pushError(e);
        }
        return this;
    }

    /**
     * 直接设置创建好的URI
     *
     * @param uri 合法的URI
     * @return Http构造链
     */
    public HttpReqChain setURI(final URI uri) {
        if (isLastOpFail()) return this;
        m_uriBuilder = new URIBuilder(uri);
        return this;
    }

    /**
     * 设置URI使用的协议
     *
     * @param scheme http/https
     * @return Http构造链
     */
    public HttpReqChain setScheme(final String scheme) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setScheme(scheme);
        return this;
    }

    /**
     * 设置用户信息
     *
     * @param username 用户名
     * @param passwd   授权码，""则留空
     * @return Http构造链
     */
    public HttpReqChain setUser(final String username, final String passwd) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setUserInfo(username, passwd);
        return this;
    }

    /**
     * 设置主机和端口
     *
     * @param hostName
     * @param port     为0则不制定
     * @return Http构造链
     */
    public HttpReqChain setHost(final String hostName, final int port) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setHost(hostName);
        m_uriBuilder.setPort(port);
        return this;
    }

    /**
     * 设置请求主机
     *
     * @param hostName 支持IP，域名，服务名(服务DNS功能)
     * @return Http构造链
     */
    public HttpReqChain setHost(final String hostName) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setHost(hostName);
        return this;
    }

    /*========== Param ==========*/

    /**
     * 设置请求端口
     *
     * @param port 端口，0-65536整数，0为不指定，根据协议默认。
     * @return Http构造链
     */
    public HttpReqChain setPort(final int port) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setPort(port);
        return this;
    }

    /**
     * 设置资源路径,需符合路径规范。
     *
     * @param path 格式："/abc/xyz.kkk"
     * @return Http构造链
     */
    public HttpReqChain setPath(final String path) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setPath(path);
        return this;
    }

    /**
     * 设置查询参数，若Key相同，则会覆盖，会被UrlEncode。
     *
     * @param key Query的键
     * @param val 必须支持toString
     * @return Http构造链
     */
    public HttpReqChain setParam(String key, String val) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setParameter(key, val);
        return this;
    }

    /*========== Header ==========*/

    /**
     * 设置查询参数，若Key相同，则会覆盖，会被UrlEncode。
     *
     * @param paramMap Map的Value必须支持toString
     * @return Http构造链
     */
    public HttpReqChain setParams(Map<String, Object> paramMap) {
        if (isLastOpFail()) return this;
        List<NameValuePair> nvps = new ArrayList<>();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
        }
        m_uriBuilder.setParameters(nvps);
        return this;
    }

    /*========== Fragment ==========*/
    public HttpReqChain setFragment(String frag) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setFragment(frag);
        return this;
    }

    /*========== Content ==========*/

    /**
     * 设置请求头，如key相同则会覆盖。
     *
     * @param key Query的键
     * @param val 必须支持toString
     * @return Http构造链
     */
    public HttpReqChain setHeader(String key, Object val) {
        if (isLastOpFail()) return this;
        m_req.setHeader(key, val.toString());
        return this;
    }

    /**
     * 批量设置请求头，如key相同则会覆盖。
     *
     * @param headerMap Map的value必须支持toString
     * @return Http构造链
     */
    public HttpReqChain setHeaders(Map<String, Object> headerMap) {
        if (isLastOpFail()) return this;
        /* */
        Header[] headers = new Header[headerMap.size()];
        int i = 0;
        for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
            headers[i++] = new BasicHeader(entry.getKey(), entry.getValue().toString());
        }
        m_req.setHeaders(headers);
        return this;
    }

    /**
     * 设置Ｈttp请求内容，默认"text/plain"编码
     *
     * @param bodyStr Http请求体 字符串
     * @return Http构造链
     */
    public HttpReqChain setContent(String bodyStr) {
        this.setContent(bodyStr, HttpRequest.CONTENT_TYPE_PLAIN);
        return this;
    }

    /**
     * 设置Ｈttp请求内容，默认"application/json"编码
     *
     * @param bodyObj Http请求体 普通对象
     * @return Http构造链
     */
    public HttpReqChain setContent(Object bodyObj) {
        this.setContent(bodyObj, HttpRequest.CONTENT_TYPE_JSON);
        return this;
    }

    public HttpReqChain setContent(Object bodyObj, String encode) {
        this.setContent(bodyObj, encode, null);
        return this;
    }

    /**
     * 设置Http请求体，编码格式
     *
     * @param bodyObj 　要传输的请求体
     * @param encode  可使用参数<ul>
     *                <li>HttpRequest.CONTENT_TYPE_XML，暂不启用</li>
     *                <li>HttpRequest.CONTENT_TYPE_JSON_UTF8</li>
     *                <li>HttpRequest.CONTENT_TYPE_JSON</li>
     *                <li>HttpRequest.CONTENT_TYPE_PROTOBUF，暂不启用</li>
     *                <li>HttpRequest.CONTENT_TYPE_MULTIPART, bodyObj必须为Multipart类型</li>
     *                <li>HttpRequest.CONTENT_TYPE_PLAIN，bodyObj及其子项必须支持或自定义"toString"函数</li>
     *                </ul>
     * @return Http构造链
     */
    public HttpReqChain setContent(Object bodyObj, String encode, String charset) {
        if (isLastOpFail()) return this;
        /*===== STEP 1. SetHeader=====*/
        if (null == charset) {
            m_req.setHeader(HTTP.CONTENT_TYPE, encode);
        } else {
            m_req.setHeader(HTTP.CONTENT_TYPE, encode + "; charset=" + charset);
        }
        /*===== STEP 1. Obj to Json =====*/
        try {
            switch (encode) {
                case HttpRequest.CONTENT_TYPE_XML:
                    //TODO : Implements it !
                    //XStream xstream = new XStream();
                    break;
                case HttpRequest.CONTENT_TYPE_JSON:
                    m_req.setEntity(new StringEntity(JSON.toJSONString(bodyObj, g_jsonSerializeConfig)));
                    break;
                case HttpRequest.CONTENT_TYPE_PROTOBUF:
                    //TODO Binary Serilization
                    m_req.setEntity(new StringEntity(bodyObj.toString()));
                    break;
                case HttpRequest.CONTENT_TYPE_MULTIPART:
                    MultipartEntityBuilder multiBuilder = MultipartEntityBuilder.create();
                    //TODO : Parse Multipart Form Defiition.
                    multiBuilder.addTextBody("file", bodyObj.toString());
                    m_req.setEntity(multiBuilder.build());
                    break;
                case HttpRequest.CONTENT_TYPE_PLAIN:
                default:
                    m_req.setHeader(HTTP.CONTENT_TYPE, HttpRequest.CONTENT_TYPE_MULTIPART);
                    m_req.setEntity(new StringEntity(bodyObj.toString()));
                    break;
            }
        } catch (UnsupportedEncodingException e) {
            pushError(e);
        }
        return this;
    }

    /*========== Execute Request ==========*/
    public HttpResponse get() {
        m_req.setMethod(HttpRequest.METHOD_GET);
        return genericInvoke();
    }

    public HttpResponse post() {
        m_req.setMethod(HttpRequest.METHOD_POST);
        return genericInvoke();
    }

    public HttpResponse put() {
        m_req.setMethod(HttpRequest.METHOD_PUT);
        return genericInvoke();
    }

    public HttpResponse delete() {
        m_req.setMethod(HttpRequest.METHOD_DELETE);
        return genericInvoke();
    }

    public HttpResponse options() {
        m_req.setMethod(HttpRequest.METHOD_OPTIONS);
        return genericInvoke();

    }

    public HttpResponse head() {
        m_req.setMethod(HttpRequest.METHOD_HEAD);
        return genericInvoke();
    }

    @Deprecated
    public HttpResponse trace() {
        m_req.setMethod(HttpRequest.METHOD_TRACE);
        return genericInvoke();
    }

    @Deprecated
    public HttpResponse patch() {
        m_req.setMethod(HttpRequest.METHOD_PATCH);
        return genericInvoke();
    }

    private HttpResponse genericInvoke() {
        if (isLastOpFail()) {
            for (Throwable error : m_errors) {
                // TODO get ErrorLog
                g_logger.error(error.getMessage());
            }
            return null;
        }
        /*===== Hook : Prepare Request =====*/
        m_reqestId = HttpReqChain.getReqID();

        HttpResponse resp = null;
        HttpSvc.IHook hooks = m_httpSvc.getHooks();
        /*===== Hook : preInvoke =====*/
        if (hooks != null) hooks.preInvoke(this);
        /*===== Translate =====*/
        try {
            m_req.setURI(m_httpSvc.translateDNS(m_uriBuilder).build());
        } catch (URISyntaxException e) {
            pushError(e);
        }
        /*===== Hook : postDns =====*/
        if (hooks != null) hooks.postDns(this);
        /* Execute Request */
        if (m_respFutureClbk != null) {
            m_httpSvc.asyncRequest(m_req, (FutureCallback<HttpResponse>) m_respFutureClbk);
        } else {
            resp = m_httpSvc.async2syncRequest(m_req);
        }
        /*===== Hook : postInvoke =====*/
        if (hooks != null) hooks.postInvoke(this);
        /*===== Return =====*/
        return resp;
    }


    /*========== Public Request ==========*/

    /**
     * 玩真的Future回调模式，需自己实现。<br/>
     * 注意：一定要自己处理HttpClient的关闭，否则容易发生内存泄漏。
     * 一般情况建议使用setRespHandler；
     *
     * @param respFutureClbk 需要按照Java Future规范实现回调函数
     * @return
     */
    private HttpReqChain setRespFutureClbk(FutureCallback<HttpResponse> respFutureClbk) {
        m_req.setHeader(HttpSvc.HEADER_FIELD_CONNECTION, HttpSvc.CONN_STAT_CLOSE);
        m_respFutureClbk = respFutureClbk;
        return this;
    }

    public HttpReqChain setRespHandler(final IHttpRespHandler respHandler) {
        FutureCallback<HttpResponse> futureClbk = new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                respHandler.procHttpResponse(m_reqestId, result);
            }

            @Override
            public void failed(Exception ex) {
                g_logger.error(ex.getMessage());
            }

            @Override
            public void cancelled() {
                g_logger.error(m_req + " is Cancelled.");
            }
        };
        setRespFutureClbk(futureClbk);
        return this;
    }

    /*========== Assistant Function ==========*/
    public long markTime() {
        m_timestamp = System.currentTimeMillis();
        return m_timestamp;
    }

    public long getElapsedTime() {
        long et = System.currentTimeMillis() - m_timestamp;
        m_timestamp = System.currentTimeMillis();
        return et;
    }

}
