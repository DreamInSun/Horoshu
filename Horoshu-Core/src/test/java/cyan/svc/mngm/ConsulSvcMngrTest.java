package cyan.svc.mngm;

import com.cyan.arsenal.Console;
import cyan.svc.mngm.consul.ConsulClient;
import cyan.svc.mngm.consul.vo.ServiceDesc;
import cyan.svc.mngm.consul.vo.Services;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 * ConsulClient Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>02/11/2016</pre>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConsulSvcMngrTest extends TestCase {
    /*========== Static Properties ==========*/
    private static ConsulClient g_consul;
    private static Services g_services;
    public ConsulSvcMngrTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ConsulSvcMngrTest.class);
    }

    /*========== Prepare ==========*/
    public void setUp() throws Exception {
        super.setUp();
        //HttpSvc.config(new BasicConfig().set(HttpSvc.CONFIG_HOOKS, HttpSvc.defaultHooks));
        if (null == g_consul) {
            g_consul = new ConsulClient("consul://lord.17orange.com:8500");
        }

    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /*========== Test ==========*/
    public void test1listServices() throws Exception {
        g_services = g_consul.listServices();
        Assert.assertNotNull(g_services);
        Console.info(g_services);
    }

    public void test2GetService() throws Exception {
        for (String key : g_services.keySet()) {
            ServiceDesc[] serviceDescArr = g_consul.getServiceDescArr(key);
            for (ServiceDesc svc : serviceDescArr) {
                Console.info(svc);
            }
        }
    }
}
