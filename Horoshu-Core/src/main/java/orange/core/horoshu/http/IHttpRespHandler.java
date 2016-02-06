package orange.core.horoshu.http;

import org.apache.http.HttpResponse;

/**
 * Created by DreamInSun on 2016/2/6.
 */
public interface IHttpRespHandler {
    void procHttpResponse(long reqId, HttpResponse resp);
}
