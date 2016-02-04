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

    public static SvcDns getInstance() {
        return SvcDns.getInstance(Config.getEmptyConfig());
    }

    public static SvcDns getInstance(IConfig config) {
        if (null == g_svcDns) {
            g_svcDns = new SvcDns();
        }
        return g_svcDns;
    }

    /*========== Properties ==========*/
    /**  */
    private Map<String, DnsItem> m_DnsMap = new ConcurrentHashMap<String, DnsItem>();
    /** Fresh DNS Item Interval in Second */
    private Integer m_freshInterval = 15;

    /*========== Export Functions ==========*/
    public DnsItem lookup(String svcName) {
        /*==== Input Protection =====*/
        if (null == svcName || svcName.isEmpty()) return null;
        /*==== LookUp From Map =====*/
        return m_DnsMap.get(svcName);
    }

    public URI translateAddr(URI uri) throws URISyntaxException {
        DnsItem realAddr = this.lookup(uri.getHost());
        if (realAddr != null) {
            URIBuilder uriBuilder = new URIBuilder(uri);
            uriBuilder.setHost(realAddr.realHost);
            uriBuilder.setPort(realAddr.realPort);
            return uriBuilder.build();
        }
        return uri;
    }

    /*====================================================*/
    /*========== DNS Synchronization Management ==========*/
    /*====================================================*/
    public void fresh() {
        /*===== STEP 1. Remove Expired DNS =====*/

        /*===== STEP 1. Remove Expired DNS =====*/
    }

    public void cleanCache() {
        this.m_DnsMap.clear();
    }
}
