package orange.core.horoshu.http;

import org.apache.http.client.methods.*;

/**
 * Created by DreamInSun on 2016/2/5.
 */
public class HttpRequest extends HttpEntityEnclosingRequestBase {

    /*========== Constant ==========*/
     /* REquest Method */
    public static final String METHOD_GET = HttpGet.METHOD_NAME;
    public static final String METHOD_POST = HttpPost.METHOD_NAME;
    public static final String METHOD_PUT = HttpPut.METHOD_NAME;
    public static final String METHOD_DELETE = HttpDelete.METHOD_NAME;
    public static final String METHOD_OPTIONS = HttpOptions.METHOD_NAME;
    public static final String METHOD_HEAD = HttpHead.METHOD_NAME;
    public static final String METHOD_TRACE = HttpTrace.METHOD_NAME;
    public static final String METHOD_PATCH = HttpPatch.METHOD_NAME;

   /* More Method : CONNECT, PROPFIND, PROPPATCH, MKCOL, COPY, MOVE, LOCK, UNLOCK */

    /* Content-Type */
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=utf-8";
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String CONTENT_TYPE_PLAIN = "text/plain";
    public static final String CONTENT_TYPE_PROTOBUF = "application/octet-stream";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    /*========== Properties ==========*/
    private String m_method = HttpRequest.METHOD_GET;

    /*========== Constructor ==========*/
    public HttpRequest() {
        super();
    }

    /*========== Override ==========*/
    @Override
    public String getMethod() {
        return m_method;
    }

    /*========== Export Function ==========*/
    public void setMethod(String method) {
        switch (method) {
            case HttpRequest.METHOD_DELETE:
            case HttpRequest.METHOD_PUT:
            case HttpRequest.METHOD_POST:
            case HttpRequest.METHOD_GET:
            case HttpRequest.METHOD_OPTIONS:
            case HttpRequest.METHOD_HEAD:
            case HttpRequest.METHOD_TRACE:
            case HttpRequest.METHOD_PATCH:
                m_method = method;
                break;
        }
    }
}
