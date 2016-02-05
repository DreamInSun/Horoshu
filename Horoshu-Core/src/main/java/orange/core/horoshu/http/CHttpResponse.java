package orange.core.horoshu.http;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.ReasonPhraseCatalog;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;

import java.util.Locale;

/**
 * Created by DreamInSun on 2016/2/6.
 */
public class CHttpResponse extends BasicHttpResponse implements HttpResponse {

    /*==========  ==========*/
    public CHttpResponse(StatusLine statusline, ReasonPhraseCatalog catalog, Locale locale) {
        super(statusline, catalog, locale);
    }

    public CHttpResponse(StatusLine statusline) {
        super(statusline);
    }

    public CHttpResponse(ProtocolVersion ver, int code, String reason) {
        super(ver, code, reason);
    }


}
