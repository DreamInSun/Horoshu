package cyan.svc.horoshu.dns.vo;

import java.io.Serializable;

public class SvcDesc implements Serializable {
    /*========== Constant ==========*/
    public static final int SVC_TYPE_UNKOWN = 0;
    public static final int SVC_TYPE_RPC = 1;
    public static final int SVC_TYPE_MSG = 2;
    public static final int SVC_TYPE_PLATFORM = 3;
    private static final long serialVersionUID = 1L;
    /*========== Properties ==========*/
    /* Basic Info */
    private int SvcType = SVC_TYPE_UNKOWN; // TODO remove it
    private String brief = null;

	/* Input Parameters */

	/* Output Parameters */

	/*========== Constructors ==========*/


	/*========== Getter & Setter ==========*/


}
