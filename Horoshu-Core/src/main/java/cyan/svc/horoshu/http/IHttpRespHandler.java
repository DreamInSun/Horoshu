package cyan.svc.horoshu.http;

/**
 * Created by DreamInSun on 2016/2/6.
 */
public interface IHttpRespHandler {
    void procHttpResponse(long reqId, HttpResp resp);
}
