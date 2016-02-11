package cyan.svc.horoshu.support;

import com.cyan.arsenal.Console;
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
    public ConsulSvcMngr(String consulUri) throws URISyntaxException {
        m_consul = new ConsulClient(consulUri);
    }

    /*========== Interface : ISvcMngm ===========*/
    @Override
    public void registerSvc(String svcName) {

    }

    @Override
    public void unregisterSvc() {

    }

    @Override
    public void discoverSvc() {
        Services services = m_consul.listServices();
        for (String key : services.keySet() ) {
            ServiceDesc[] serviceDescArr = m_consul.getServiceDescArr(key);
            for (ServiceDesc svc : serviceDescArr) {
                Console.info(svc);
            }
        }
    }
}
