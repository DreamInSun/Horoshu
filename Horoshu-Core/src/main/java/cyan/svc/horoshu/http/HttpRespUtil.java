package cyan.svc.horoshu.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.cyan.arsenal.Console;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;


/**
 * Created by DreamInSun on 2016/2/6.
 */
public class HttpRespUtil {
    private static final Logger g_logger = LoggerFactory.getLogger(HttpRespUtil.class);

    private static final ParserConfig g_parserConfig = new ParserConfig();

    @Nullable
    public static <T> T getEntity(HttpResponse resp, Class<T> outputClazz, String mime, String charset) {
        Object ret = null;
        /*===== Input Protection =====*/
        if (resp == null) return null;
        /*===== Status Code =====*/
        HttpEntity entity = resp.getEntity();
        if (entity == null) return null;
        /*===== Get Content Type =====*/
        if (200 == getStatusCode(resp)) {
            /*===== Parse Content Type =====*/
            Header contentType = entity.getContentType();
            /* Determine MIME and Charset */
            if (contentType != null) {
                if (null == mime) {
                    mime = getMime(contentType);
                    charset = getCharset(contentType);
                    if (null == mime) {
                        mime = HttpRequest.CONTENT_TYPE_PLAIN;
                        charset = HttpRequest.CONTENT_CHARSET_UTF8;
                    }
                } else {
                    if (null == charset) {
                        charset = HttpRequest.CONTENT_CHARSET_UTF8;
                    }
                }
            }

            //Console.debug("MIME = " + mime + " Charset = " + charset);

            /*===== Parse Content =====*/
            try {
                switch (mime) {
                    case HttpRequest.CONTENT_TYPE_JSON:
                        String retStr = IOUtils.toString(entity.getContent(), charset);

                        switch (outputClazz.getName()) {
                            case "com.google.gson.JsonObject":
                                //InputStreamReader reader = new InputStreamReader(entity.getContent());
                                //JsonElement elmt = new Gson().fromJson(reader).
                                ret = new JsonParser().parse(retStr).getAsJsonObject();
                                break;
                            case "com.alibaba.fastjson.JSONObject":
                                ret = JSON.parseObject(retStr);
                                break;
                            default:
                                ret = JSON.parseObject(retStr, outputClazz);
                                break;
                        }
                            /*===== Default Change To JsonObject =====*/
                        break;
                    case HttpRequest.CONTENT_TYPE_HTML:

                        break;
                    case HttpRequest.CONTENT_TYPE_PLAIN:
                    default:
                            /* Return String */
                        ret = IOUtils.toString(entity.getContent(), charset);
                        break;
                }
            } catch (IOException e) {
                g_logger.error(e.getStackTrace().toString());
            }

            /*===== Parse Content =====*/
            try {
                EntityUtils.consume(entity);
            } catch (IOException e) {
                g_logger.error(e.getMessage());
            }
        }
                /*===== Return =====*/
        return (T) ret;
    }

    public static JSONObject getJSONObject(HttpResponse resp) {
        return getEntity(resp, JSONObject.class, HttpRequest.CONTENT_TYPE_JSON, HttpRequest.CONTENT_CHARSET_UTF8);
    }

    public static JsonObject getJsonObject(HttpResponse resp) {
        return getEntity(resp, JsonObject.class, HttpRequest.CONTENT_TYPE_JSON, HttpRequest.CONTENT_CHARSET_UTF8);
    }

    public static <T> T getEntity(HttpResponse resp, Class<T> outputClazz) {
        return (T) getEntity(resp, outputClazz, HttpRequest.CONTENT_TYPE_JSON, HttpRequest.CONTENT_CHARSET_UTF8);
    }

    public static <T> T getEntity(HttpResponse resp) {
        return (T) getEntity(resp, null, null, null);
    }

    public static void printResp(HttpResponse resp) {
        /*===== Input Protection =====*/
        if (resp == null) {
            Console.info("Response is Null");
            return;
        }
        Console.info(resp);
        /*===== Parse Header =====*/
        HttpEntity entity = resp.getEntity();
        if (entity == null) {
            Console.info("Response Entuty is Null");
            return;
        }
        int status = getStatusCode(resp);
        String mime = getMime(entity.getContentType());
        String charset = getCharset(entity.getContentType());
        long contentLen = entity.getContentLength();
        Console.info("{ Status Code:" + status + ", MIME:" + mime + ", Charset:" + charset + ", ContentLen:" + contentLen + "}");
        /*===== Parse Content =====*/

    }


    public static int getStatusCode(HttpResponse resp) {
        if (null == resp || null == resp.getStatusLine()) return 0;
        return resp.getStatusLine().getStatusCode();
    }

    public static String getMime(Header contentType) {
        String mime = null;
        if (contentType != null) {
            mime = contentType.getElements()[0].getName();
        }
        return mime;
    }

    public static String getCharset(Header contentType) {
        String charset = null;
        if (contentType != null) {
            HeaderElement elmt = contentType.getElements()[0];
            if (null != elmt.getParameterByName("charset")) {
                charset = elmt.getParameterByName("charset").getValue();
            }
        }
        return charset;
    }

    public static String getClassName(HttpResponse resp) {
        Header header = resp.getFirstHeader("ClassName");
        if (null != header) {
            return header.getValue();
        }
        return null;
    }

    public static Class getClass(HttpResponse resp) {
        Class entityCls = null;
        String className = getClassName(resp);
        if (className != null) {
            try {
                entityCls = Class.forName(className);
            } catch (ClassNotFoundException e) {
                g_logger.error(e.getMessage());
            }
        }
        return entityCls;
    }

    /*========== Assistant Function ==========*/

}
