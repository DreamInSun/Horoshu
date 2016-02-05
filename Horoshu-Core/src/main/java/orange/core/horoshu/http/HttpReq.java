package orange.core.horoshu.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

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
 * <li>必须使用HttpSvc.request()或者HttpSvc.build()创建。</li>
 * <li>必须执行setURI</li>
 * <li>其他函数可选执行</li>
 * <li>最后执行动作函数，返回HttpResponse</li>
 * </ol>
 * Created by DreamInSun on 2016/2/3.
 */
public class HttpReq {
    /*===== Constants =====*/
    public static final String DFLT_URI_SCHEME = "http";
    public static final String DFLT_URI_HOST = "localhost";
    public static final String DFLT_URI_PATH = "/";
    public static final String DFLT_URI_FRAGMET = "";

    /*===== Chain Properties =====*/
    /* Fast JSON Config */
    public static SerializeConfig g_jsonSerializeConfig = new SerializeConfig() {

    };

    public static ParserConfig g_jsonParserConfig = new ParserConfig() {

    };

    /*===== Chain Properties =====*/
    private HttpSvc m_httpSvc;
    private HttpRequest m_req;
    private IHttpRespHandler m_respHandler;
    private URIBuilder m_uriBuilder;
    private List<Throwable> m_errors;

    /*===== Chain Properties =====*/
    public HttpReq(HttpSvc httpSvc) {
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

    public URIBuilder getUriBuilder() {
        return m_uriBuilder;
    }

    public HttpSvc getHttpSvc() {
        return m_httpSvc;
    }


    /*===== Export Function =====*/

    /**
     * 直接使用字符串设置资源名（URI），输入字符串必须符合URI规范
     *
     * @param uriStr
     * @return
     */
    public HttpReq setURI(String uriStr) {
        if (isLastOpFail()) return this;
        /*===== Input Protection =====*/
        if (null == uriStr || "" == uriStr) {
            //m_expLog.setStackTrace(  );
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
     * @param uri
     * @return
     */
    public HttpReq setURI(final URI uri) {
        if (isLastOpFail()) return this;
        m_uriBuilder = new URIBuilder(uri);
        return this;
    }

    /**
     * 设置URI使用的协议
     *
     * @param scheme http/https
     * @return
     */
    public HttpReq setScheme(final String scheme) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setScheme(scheme);
        return this;
    }

    /**
     * 设置用户信息
     *
     * @param username
     * @param passwd   ""则留空
     * @return
     */
    public HttpReq setUser(final String username, final String passwd) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setUserInfo(username, passwd);
        return this;
    }

    /**
     * 设置主机和端口
     *
     * @param hostName
     * @param port     为0则不制定
     * @return
     */
    public HttpReq setHost(final String hostName, final int port) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setHost(hostName);
        m_uriBuilder.setPort(port);
        return this;
    }

    /**
     * 设置请求主机
     *
     * @param hostName 支持IP，域名，服务名(服务DNS功能)
     * @return
     */
    public HttpReq setHost(final String hostName) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setHost(hostName);
        return this;
    }

    /**
     * 设置请求端口
     *
     * @param port
     * @return
     */
    public HttpReq setPort(final int port) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setPort(port);
        return this;
    }

    /**
     * 设置资源路径,需符合路径规范。
     *
     * @param path 格式："/abc/xyz.kkk"
     * @return
     */
    public HttpReq setPath(final String path) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setPath(path);
        return this;
    }

    /*========== Param ==========*/

    /**
     * 设置查询参数，若Key相同，则会覆盖，会被UrlEncode。
     *
     * @param key
     * @param val 必须支持toString
     * @return
     */
    public HttpReq setParam(String key, String val) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setParameter(key, val);
        return this;
    }

    /**
     * 设置查询参数，若Key相同，则会覆盖，会被UrlEncode。
     *
     * @param paramMap Map的Value必须支持toString
     * @return
     */
    public HttpReq setParams(Map<String, Object> paramMap) {
        if (isLastOpFail()) return this;
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
        }
        m_uriBuilder.setParameters(nvps);
        return this;
    }

    /*========== Fragment ==========*/
    public HttpReq setFragment(String frag) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setFragment(frag);
        return this;
    }

    /*========== Header ==========*/

    /**
     * 设置请求头，如key相同则会覆盖。
     *
     * @param key
     * @param val 必须支持toString
     * @return
     */
    public HttpReq setHeader(String key, Object val) {
        if (isLastOpFail()) return this;
        m_req.setHeader(key, val.toString());
        return this;
    }

    /**
     * 批量设置请求头，如key相同则会覆盖。
     *
     * @param headerMap Map的value必须支持toString
     * @return
     */
    public HttpReq setHeaders(Map<String, Object> headerMap) {
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

    /*========== Content ==========*/

    /**
     * 设置Ｈttp请求内容，默认"text/plain"编码
     *
     * @param bodyStr
     * @return HttpReq
     */
    public HttpReq setContent(String bodyStr) {
        this.setContent(bodyStr, HttpRequest.CONTENT_TYPE_PLAIN);
        return this;
    }

    /**
     * 设置Ｈttp请求内容，默认"application/json"编码
     *
     * @param bodyObj
     * @return HttpReq
     */
    public HttpReq setContent(Object bodyObj) {
        this.setContent(bodyObj, HttpRequest.CONTENT_TYPE_JSON);
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
     * @return
     */
    public HttpReq setContent(Object bodyObj, String encode) {
        if (isLastOpFail()) return this;
        /*===== STEP 1. Obj to Json =====*/
        try {
            m_req.setHeader(HTTP.CONTENT_TYPE, encode);
            switch (encode) {
                case HttpRequest.CONTENT_TYPE_XML:
                    //TODO : Implements it !
                    //XStream xstream = new XStream();
                    break;
                case HttpRequest.CONTENT_TYPE_JSON_UTF8:
                    m_req.setEntity(new StringEntity(JSON.toJSONString(bodyObj, g_jsonSerializeConfig)));
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

    public HttpResponse trace() {
        m_req.setMethod(HttpRequest.METHOD_TRACE);
        return genericInvoke();
    }

    public HttpResponse patch() {
        m_req.setMethod(HttpRequest.METHOD_PATCH);
        return genericInvoke();
    }

    /*========== Execute Request ==========*/
    private HttpResponse genericInvoke() {
        if (isLastOpFail()) ; //throw m_errors;
        HttpSvc.IHooks hooks = m_httpSvc.getHooks();
        /* Hook : preInvoke */
        if (hooks != null) hooks.preInvoke(this);
        /* Translate */
        try {
            m_req.setURI(m_httpSvc.translateDNS(m_uriBuilder).build());
        } catch (URISyntaxException e) {
            pushError(e);
        }
        /* Hook : postDns */
        if (hooks != null) hooks.postDns(this);
        /* Execute Request */
        HttpResponse resp = m_httpSvc.execRequest(m_req);
        /* Handler : Parse response */
        if (m_respHandler != null) m_respHandler.parseResp(resp);
        /* Hook : postInvoke */
        if (hooks != null) hooks.postInvoke(this);
        /* Return */
        return resp;
    }


    /*========== Public Request ==========*/
    public HttpReq setRespHandler(IHttpRespHandler respHanler) {
        m_respHandler = respHanler;
        return this;
    }
}