package cyan.svc.horoshu.dns;

import com.cyan.arsenal.Console;
import cyan.core.config.BasicConfig;
import cyan.core.config.IConfig;
import cyan.svc.horoshu.dns.vo.SvcDns;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by DreamInSun on 2016/2/2.
 */
public class SvcRouteMap {

    /*========== Factory ==========*/
    private static SvcRouteMap g_svcDns;

    /*========== Properties ==========*/
    //private Multimap<String, SvcDns> m_DnsMap = HashMultimap.create();
    private Map<String, SvcDns> n_DnsRuntimeMap = new ConcurrentHashMap<>();

    /*========== Constructor ==========*/
    private SvcRouteMap() {

    }

    public static SvcRouteMap getInstance() {
        return SvcRouteMap.getInstance(BasicConfig.getEmptyConfig());
    }

    public static SvcRouteMap getInstance(IConfig config) {
        if (null == g_svcDns) {
            g_svcDns = new SvcRouteMap();
        }
        return g_svcDns;
    }

    /*========== Export Functions ==========*/
    public URIBuilder translateAddr(URIBuilder uriBuilder) throws URISyntaxException {
        SvcDns realAddr = this.lookup(uriBuilder.getHost());
        if (realAddr != null) {
            if (null != realAddr.host && !realAddr.host.isEmpty()) {
                uriBuilder.setHost(realAddr.host);
            }
            if (realAddr.port != 0) {
                uriBuilder.setPort(realAddr.port);
            }
            if (null != realAddr.pathBase && !realAddr.pathBase.isEmpty()) {
                uriBuilder.setPath(realAddr.pathBase + uriBuilder.getPath());
            }
        }
        return uriBuilder;
    }

    public URI translateAddr(URI uri) throws URISyntaxException {
        return translateAddr(new URIBuilder(uri)).build();
    }

    /*========== Assistant Functions ==========*/
    private SvcDns lookup(String svcName) {
        /*==== Input Protection =====*/
        if (null == svcName || svcName.isEmpty()) return null;
        /*==== LookUp From Map =====*/
        return n_DnsRuntimeMap.get(svcName);
    }

    public void printSvcRouteMap(){
        Console.info("==================== Runtime Service Route Map ====================");
        for( SvcDns svcDns : n_DnsRuntimeMap.values() ){
            Console.info(svcDns);
        }
    }

    private void test() {
        addDnsItem("cyan.core.Test", "dreaminsun.ngrok.natapp.cn", 0, null);
    }

    /*====================================================*/
    /*========== DNS Synchronization Management ==========*/
    /*====================================================*/
    public void addDnsItem(SvcDns svcDns) {
        n_DnsRuntimeMap.put(svcDns.svcName, svcDns);
    }

    public void addDnsItem(String svcName, String host, int port, String pathBase) {
        SvcDns svcDns = new SvcDns(svcName, host, port, pathBase);
        addDnsItem(svcDns);
    }

    public void clean() {
        //this.m_DnsMap.clear();
    }
}
