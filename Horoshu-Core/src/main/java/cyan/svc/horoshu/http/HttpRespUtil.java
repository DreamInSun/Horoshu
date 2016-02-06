package cyan.svc.horoshu.http;

import com.cyan.arsenal.Console;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by DreamInSun on 2016/2/6.
 */
public class HttpRespUtil {

    public static <T> T getEntity(HttpResponse resp) {
        /*===== Input Protection =====*/
        if (resp == null) return null;
        /*===== Status Code =====*/
        int status = resp.getStatusLine().getStatusCode();
        HttpEntity entity = resp.getEntity();
        if (entity == null) return null;
        long contentLen = entity.getContentLength();
        Console.debug(contentLen);
        /*===== Get Content Type =====*/
        if (200 == getStatusCode(resp)) {
            /*===== Parse Content Type =====*/
            Header contentType = entity.getContentType();
            if (contentType != null) {
                String mime = getMime(contentType);
                String charset = getCharset(contentType);
                Console.debug("MIME = " + mime + " Charset = " + charset);

                /*===== Parse Content =====*/
                try {
                    String outputStr;
                    switch (mime) {
                        case HttpRequest.CONTENT_TYPE_JSON:
                            outputStr = EntityUtils.toString(entity);
                            //Console.info(outputStr);
                            break;
                        case HttpRequest.CONTENT_TYPE_HTML:
                            outputStr = EntityUtils.toString(entity);
                            //Console.info(outputStr);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /*===== Parse Content =====*/
                try {
                    EntityUtils.consume(entity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
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
        Console.info("{ Status Code:" + status + ", MIME:" + mime + ", Charset:" + charset + ", ContentLen:"+ contentLen +"}");
        /*===== Parse Content =====*/

    }

    public static int getStatusCode(HttpResponse resp) {
        if (null != resp && null != resp.getStatusLine()) return 0;
        return resp.getStatusLine().getStatusCode();
    }

    /*========== Assistant Function ==========*/
    private static String getMime(Header contentType) {
        String mime = null;
        if (contentType != null) {
            mime = contentType.getElements()[0].getName();
        }
        return mime;
    }

    private static String getCharset(Header contentType) {
        String charset = null;
        if (contentType != null) {
            HeaderElement elmt = contentType.getElements()[0];
            if (null != elmt.getParameterByName("charset")) {
                charset = elmt.getParameterByName("charset").getValue();
            }
        }
        return charset;
    }
}
