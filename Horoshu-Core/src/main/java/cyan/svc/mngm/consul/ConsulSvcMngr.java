package cyan.svc.mngm.consul;

import cyan.svc.horoshu.http.HttpRespUtil;
import cyan.svc.horoshu.http.HttpSvc;
import cyan.svc.mngm.SvcMngr;
import cyan.svc.mngm.consul.vo.ServiceDesc;
import cyan.svc.mngm.consul.vo.Services;
import org.apache.http.HttpResponse;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by DreamInSun on 2016/2/10.
 */
public class ConsulSvcMngr extends SvcMngr {

    /*========== Constant ==========*/
    /* API Module */
    public static final String API_MOD_AGENT = "/v1/agent/";
    public static final String API_MOD_CATALOG = "/v1/catalog/";
    public static final String API_MOD_EVENT = "/v1/event/";
    public static final String API_MOD_HEALTH = "/v1/health/";
    /* */

    /*========== Factory ==========*/


    /*========== Properties ==========*/
    private URI m_consulNode;

    /*========== Constructor ==========*/
    public ConsulSvcMngr() throws URISyntaxException {
        m_consulNode = new URI("http://lord.17orange.com:8500/");
    }

    /*========== Assistant Function : Rest API ==========*/


    /*========== Export Function : Catalog ==========*/
    public Services listServices() {
        HttpResponse resp = HttpSvc.build(m_consulNode).setPath(API_MOD_CATALOG + "services").get();
        //JSONObject jsonObj = HttpRespUtil.getJSONObject(resp);
        //Services services = new Services(jsonObj);
        Services services = HttpRespUtil.getEntity(resp, Services.class);
        return services;
    }

    public ServiceDesc[] getServiceDescArr(String serviceName) {
        HttpResponse resp = HttpSvc.build(m_consulNode).setPath(API_MOD_CATALOG + "service/" + serviceName).get();
        ServiceDesc[] serviceDescArr = HttpRespUtil.getEntity(resp, ServiceDesc[].class);
        return serviceDescArr;
    }

    /*========== Interface : ISvcMngr ==========*/
    @Override
    public void registerSvc(String svcName) {

    }

    @Override
    public void unregisterSvc() {

    }

    @Override
    public void discoverSvc() {

    }
}
