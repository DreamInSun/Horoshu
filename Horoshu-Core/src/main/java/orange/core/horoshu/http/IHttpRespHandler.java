package orange.core.horoshu.http;

import org.apache.http.HttpResponse;

/**
 * Created by DreamInSun on 2016/2/5.
 */
public interface IHttpRespHandler {
    void parseResp(HttpResponse resp);
}
