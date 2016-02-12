package cyan.svc.horoshu.rest;

import cyan.svc.horoshu.http.HttpRespUtil;
import cyan.svc.horoshu.http.HttpSvc;
import org.apache.http.HttpResponse;

import java.net.URI;

/**
 * Created by DreamInSun on 2016/2/10.
 */
public class Rest {

    /*========== Export Function ==========*/
    public Rest() {

    }

    /*========== Properties ==========*/


    /*========== Constructor ==========*/

    /*========== Factory==========*/
    public static Rest getEndpoint(URI endpoint) {
        return null;
    }

    /*========== Export Function : CURDL ==========*/
    public Object C(String endpoint, Object request) {
        HttpResponse res = HttpSvc.build().setPath(endpoint).setContent(request).post().getEntity(null);
        return HttpRespUtil.getEntity(res);
    }

    public Object R(String endpoint, Object request) {
        HttpResponse res = HttpSvc.build().setPath(endpoint).setContent(request).get().getEntity(null);
        return HttpRespUtil.getEntity(res);
    }

    public Object U(String endpoint, Object request) {
        HttpResponse res = HttpSvc.build().setPath(endpoint).setContent(request).put().getEntity(null);
        return HttpRespUtil.getEntity(res);
    }

    public Object D(String endpoint, Object request) {
        HttpResponse res = HttpSvc.build().setPath(endpoint).setContent(request).delete().getEntity(null);
        return HttpRespUtil.getEntity(res);
    }

    public Object[] L(String endpoint, Object request) {
        return null;
    }
}
