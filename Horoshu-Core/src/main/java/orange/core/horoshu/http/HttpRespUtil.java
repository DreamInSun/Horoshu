package orange.core.horoshu.http;

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
        if (200 == status) {
            /*===== Parse Content Type =====*/
            String mimeName = "";
            String charSet = "";
            Header contentType = entity.getContentType();
            if (contentType != null) {
                HeaderElement elmt = entity.getContentType().getElements()[0];
                mimeName = elmt.getName();
//                charSet = elmt.getParameterByName("charset").getValue();
                Console.debug(mimeName + charSet);
            }

            /*===== Parse Content =====*/
//            try {
//                String outputStr;
//                switch (arr[0]) {
//                    case CHttpRequest.CONTENT_TYPE_JSON:
//                        outputStr = EntityUtils.toString(entity);
//                        //Console.info(outputStr);
//                        break;
//                    case CHttpRequest.CONTENT_TYPE_JSON_UTF8:
//                        outputStr = EntityUtils.toString(entity);
//                        //Console.info(outputStr);
//                        break;
//                    case CHttpRequest.CONTENT_TYPE_HTML:
//                        outputStr = EntityUtils.toString(entity);
//                        //Console.info(outputStr);
//                        break;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }

        try {
            EntityUtils.consume(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void printResp(HttpResponse resp) {
        /*=====  =====*/
        Console.info("Status Code:" + resp.getStatusLine().getStatusCode());
    }

}
