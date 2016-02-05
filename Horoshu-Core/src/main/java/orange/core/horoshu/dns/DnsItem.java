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
     * Serivce real host in IP or Domain
     */
    public String host;
    /**
     * Service read port
     */
    public Integer port;
    /**
     * Service Protocol
     */
    public String protocol;
    /**
     * must be execRequest with '/'
     */
    public String pathBase;

    /*========== Construcotr ==========*/
    public DnsItem(String svcName, String host, int port, String pathBase) {
        this(svcName, host, port, pathBase, DnsItem.SVC_TYPE_HTTP);
    }

    public DnsItem(String svcName, String host, int port, String pathBase, String protocol) {
        this.svcName = svcName;
        this.host = host;
        this.port = port;
        this.pathBase = pathBase;
        this.protocol = protocol;
    }

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
