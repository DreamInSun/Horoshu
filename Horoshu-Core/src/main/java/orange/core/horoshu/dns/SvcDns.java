package orange.core.horoshu.dns;

import cyan.core.config.Config;
import cyan.core.config.IConfig;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by DreamInSun on 2016/2/2.
 */
public class SvcDns {

    /*========== Factory ==========*/
    private static SvcDns g_svcDns;
    /*========== Properties ==========*/
    private Map<String, DnsItem> m_DnsMap = new ConcurrentHashMap<String, DnsItem>();
    /* Fresh DNS Item Interval, in Second */
    private Integer m_freshInterval = 15;

    /*========== Constructor ==========*/
    private SvcDns() {
        test();
    }

    public static SvcDns getInstance() {
        return SvcDns.getInstance(Config.getEmptyConfig());
    }

    public static SvcDns getInstance(IConfig config) {
        if (null == g_svcDns) {
            g_svcDns = new SvcDns();
        }
        return g_svcDns;
    }

    /*========== Export Functions ==========*/
    public URIBuilder translateAddr(URIBuilder uriBuilder) throws URISyntaxException {
        DnsItem realAddr = this.lookup(uriBuilder.getHost());
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
    private DnsItem lookup(String svcName) {
        /*==== Input Protection =====*/
        if (null == svcName || svcName.isEmpty()) return null;
        /*==== LookUp From Map =====*/
        return m_DnsMap.get(svcName);
    }

    private void test() {
        addDnsItem("cyan.core.Test", "dreaminsun.ngrok.natapp.cn", 0, null );
    }

    /*====================================================*/
    /*========== DNS Synchronization Management ==========*/
    /*====================================================*/
    public void addDnsItem(DnsItem dnsItem) {
        m_DnsMap.put(dnsItem.svcName, dnsItem);
    }

    public void addDnsItem(String svcName, String host, int port, String pathBase) {
        DnsItem dnsItem = new DnsItem();
        dnsItem.svcName = svcName;
        dnsItem.host = host;
        dnsItem.port = port;
        dnsItem.pathBase = pathBase;
        m_DnsMap.put(svcName, dnsItem);
    }

    public void fresh() {
        /*===== STEP 1. Remove Expired DNS =====*/

        /*===== STEP 1. Remove Expired DNS =====*/
    }

    public void cleanCache() {
        this.m_DnsMap.clear();
    }
}
