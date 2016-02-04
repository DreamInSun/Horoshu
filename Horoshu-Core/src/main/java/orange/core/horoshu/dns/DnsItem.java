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
    public String realHost;
    /**
     * Service read port
     */
    public Integer realPort;
    /**
     * must be request with '/'
     */
    public String projBase;
}
