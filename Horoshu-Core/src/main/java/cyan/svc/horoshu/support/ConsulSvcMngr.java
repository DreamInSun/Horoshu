package cyan.svc.horoshu.support;

import cyan.svc.horoshu.dns.SvcRouteMap;
import cyan.svc.horoshu.dns.vo.SvcDns;
import cyan.svc.horoshu.svcmngr.BasicSvcMngr;
import cyan.svc.mngm.consul.ConsulClient;
import cyan.svc.mngm.consul.vo.ServiceDesc;
import cyan.svc.mngm.consul.vo.Services;

import java.net.URISyntaxException;

/**
 * Created by DreamInSun on 2016/2/11.
 */
public class ConsulSvcMngr extends BasicSvcMngr {
    /*========== Constant ===========*/


    /*========== Properties ===========*/
    private ConsulClient m_consul;

    /*========== Constructor ===========*/
    public ConsulSvcMngr(SvcRouteMap svcRouteMap , String consulUri) throws URISyntaxException {
        super(svcRouteMap);
        m_consul = new ConsulClient(consulUri);
    }

    /*========== Interface : ISvcMngr ===========*/
    @Override
    public void registerSvc(String svcName) {

    }

    @Override
    public void unregisterSvc() {

    }

    @Override
    public void discoverSvc() {

    }

    @Override
    public void refreshSvcRoute() {
        Services services = m_consul.listServices();
        for (String key : services.keySet()) {
            ServiceDesc[] serviceDescArr = m_consul.getServiceDescArr(key);
            for (ServiceDesc svc : serviceDescArr) {
                SvcDns svcDns = new SvcDns(key, svc.ServiceAddress, svc.ServicePort, null);
                m_SvcRouteMap.addDnsItem(svcDns);
            }
        }
        m_SvcRouteMap.printSvcRouteMap();
    }
}
