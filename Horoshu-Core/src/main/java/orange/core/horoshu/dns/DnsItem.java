package orange.core.horoshu.dns;

/**
 * Created by DreamInSun on 2016/2/2.
 */
public class DnsItem {
    /*========== Constant ==========*/
    public static final String SVC_TYPE_HTTP = "HTTP";
    public static final String SVC_TYPE_EJB = "EJB";

    /*========== Properties ==========*/
    /**
     * Uniformed Service Name
     */
    public String svcName;
    /**
     * Service Protocol
     */
    public String protocol;
    /**
     * Serivce real host in IP or Domain
     */
    public String host;
    /**
     * Service read port
     */
    public Integer port;
    /**
     * must be doRequest with '/'
     */
    public String pathBase;

    /*========== toString ==========*/
    @Override
    public String toString() {
        return "DnsItem{" +
                "svcName='" + svcName + '\'' +
                ", protocol='" + protocol + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", pathBase='" + pathBase + '\'' +
                '}';
    }
}
