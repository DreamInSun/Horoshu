package orange.core.horoshu.dns;

/**
 * Created by DreamInSun on 2016/2/2.
 */
public class DnsItem {
     /*========== Constant ==========*/
    public static final String SVC_TYPE_HTTP = "HTTP";
    public static final String SVC_TYPE_EJB = "EJB";

    /*========== Properties ==========*/
    public String host;
    public String realHost;
    public Integer realPort;

    /*========== Getter & Setter ==========*/
}
