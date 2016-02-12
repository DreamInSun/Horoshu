package cyan.svc.mngm.consul;

import cyan.core.config.BasicConfig;
import cyan.core.config.IConfig;
import cyan.svc.horoshu.http.HttpSvc;
import cyan.svc.mngm.consul.vo.ServiceDesc;
import cyan.svc.mngm.consul.vo.Services;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by DreamInSun on 2016/2/10.
 */
public class ConsulClient {

    /*========== Constant ==========*/
    /* API Module */
    public static final String API_MOD_AGENT = "/v1/agent/";
    public static final String API_MOD_CATALOG = "/v1/catalog/";
    public static final String API_MOD_EVENT = "/v1/event/";
    public static final String API_MOD_HEALTH = "/v1/health/";
    /* */

    /*========== Factory ==========*/
    HttpSvc m_httpSvc;
    IConfig m_httpSvcConfig;

    /*========== Properties ==========*/
    private URI m_consulNode;

    /*========== Constructor ==========*/
    public ConsulClient(String url) throws URISyntaxException {
        url = url.toLowerCase();
        URIBuilder uriBuilder = new URIBuilder(url);
        switch (uriBuilder.getScheme()) {
            case "consul":
                uriBuilder.setScheme("http");
            case "http":
            case "https":
                m_consulNode = uriBuilder.build();
                break;
            default:
                new URISyntaxException(url, "Consul URL mast be start with consul/http/https.");
                break;
        }
        /*=====  =====*/
        m_httpSvcConfig = new BasicConfig().set("invoker", ConsulClient.class.getName());
        m_httpSvc = HttpSvc.getInstance(m_httpSvcConfig);
    }

    /*========== Assistant Function : Rest API ==========*/


    /*========== Export Function : Catalog ==========*/
    public Services listServices() {
        Services services = m_httpSvc.start(m_consulNode).setPath(API_MOD_CATALOG + "services").get().getEntity(Services.class);
        return services;
    }

    public ServiceDesc[] getServiceDescArr(String serviceName) {
        ServiceDesc[] serviceDescArr = m_httpSvc.start(m_consulNode).setPath(API_MOD_CATALOG + "service/" + serviceName).get().getEntity(ServiceDesc[].class);
        return serviceDescArr;
    }
}
