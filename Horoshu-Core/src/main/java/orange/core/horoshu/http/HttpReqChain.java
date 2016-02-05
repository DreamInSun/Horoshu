package orange.core.horoshu.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.cyan.arsenal.Console;
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
 * Created by DreamInSun on 2016/2/3.
 */
public class HttpReqChain {
    /*===== Constants =====*/
    public static final String DFLT_URI_SCHEME = "http";
    public static final String DFLT_URI_HOST = "localhost";
    public static final String DFLT_URI_PATH = "/";
    public static final String DFLT_URI_FRAGMET = "";

    /*===== Chain Properties =====*/
    public static SerializeConfig g_jsonSerializeConfig = new SerializeConfig() {

    };

    /*===== Chain Properties =====*/
    private HttpSvc m_httpSvc;
    private HttpRequest m_req;
    private URIBuilder m_uriBuilder;
    private List<Throwable> m_errors;

    /*===== Chain Properties =====*/
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

    /*===== Export Function =====*/
    public HttpReqChain setURI(String uriStr) {
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

    public HttpReqChain setURI(final URI uri) {
        if (isLastOpFail()) return this;
        m_uriBuilder = new URIBuilder(uri);
        return this;
    }

    public HttpReqChain setScheme(final String scheme) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setScheme(scheme);
        return this;
    }

    public HttpReqChain setUser(final String username, final String passwd) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setUserInfo(username, passwd);
        return this;
    }

    public HttpReqChain setHost(final String hostName, final int port) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setHost(hostName);
        m_uriBuilder.setPort(port);
        return this;
    }

    public HttpReqChain setHost(final String hostName) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setHost(hostName);
        return this;
    }

    public HttpReqChain setPort(final int port) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setPort(port);
        return this;
    }

    public HttpReqChain setPath(final String path) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setPath(path);
        return this;
    }

    /*========== Param ==========*/
    public HttpReqChain setParam(String key, String val) {
        if (isLastOpFail()) return this;
        m_uriBuilder.setParameter(key, val);
        return this;
    }

    public HttpReqChain setParams(Map<String, Object> paramMap) {
        if (isLastOpFail()) return this;
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
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


    /*========== Header ==========*/
    public HttpReqChain setHeader(String key, Object val) {
        if (isLastOpFail()) return this;
        m_req.setHeader(key, val.toString());
        return this;
    }

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

    /*========== Content ==========*/
    public HttpReqChain setContent(String bodyStr) {
        this.setContent(bodyStr, HttpRequest.CONTENT_TYPE_PLAIN);
        return this;
    }

    public HttpReqChain setContent(Object bodyObj) {
        this.setContent(bodyObj, HttpRequest.CONTENT_TYPE_JSON);
        return this;
    }

    public HttpReqChain setContent(Object bodyObj, String encode) {
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
        Console.info(m_uriBuilder);
        /* Translate */
        try {
            m_req.setURI(m_httpSvc.translateDNS(m_uriBuilder).build());
        } catch (URISyntaxException e) {
            pushError(e);
        }
        /* */
        Console.info(m_req);
        return m_httpSvc.doRequest(m_req);
    }
}
