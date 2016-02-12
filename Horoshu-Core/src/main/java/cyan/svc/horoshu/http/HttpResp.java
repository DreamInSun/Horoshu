package cyan.svc.horoshu.http;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;

/**
 * HttpResp是对HttpResponse的工具封装，便于链式调用时的快速操作。
 * <p/>
 * Created by DreamInSun on 2016/2/13.
 */
public class HttpResp {

    /*========== Constant ==========*/

    /*========== Properties ==========*/
    private HttpResponse m_httpResponse;

    /*========== Constructor ==========*/
    public HttpResp(HttpResponse httpResponse) {
        m_httpResponse = httpResponse;
    }

    /*========== Export Function ==========*/
    HttpResponse getRawResponse() {
        return this.m_httpResponse;
    }

    public <T> T getEntity(Class<T> clazz) {
        return HttpRespUtil.getEntity(m_httpResponse, clazz);
    }

    public JSONObject getJSONObject() {
        return HttpRespUtil.getJSONObject(m_httpResponse);
    }

    public JsonObject getJsonObject() {
        return HttpRespUtil.getJsonObject(m_httpResponse);
    }

    public void print() {
        HttpRespUtil.printResp(m_httpResponse);
    }
}
