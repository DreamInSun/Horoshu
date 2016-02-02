package ctop.v3.msb.proxy.conn;

/**
 * Connection Informations introducing MSBU how to communicate with MSB media & other MSBU.<br />
 * 
 * 
 * 
 * @author DreamInSun
 * 
 */
public class MsbConnParam {
	/*========== Constant ==========*/
	public static final String USER_NAME = null;
	public static final String PASS_WORD = null;
	public static final String CONNECT_WORD = "tcp://localhost:61616";

	public enum EType {
		/** */
		JMS_MQ,
		/** */
		RMI,
		/** */
		HTTP_RPC,
		/** */
		WEB_SERVICES,
		/** */
		CTOP_REST,
	}

	/*========== Properties ==========*/
	public EType type;
	public String userName;
	public String passWord;
	public String connectWord;

	/*========== Constructor ==========*/
	public MsbConnParam(EType type, String usrname, String passwd, String connectwd) {
		this.type = type;
		this.userName = usrname;
		this.passWord = passwd;
		this.connectWord = connectwd;
	}

	public MsbConnParam(String type, String usrname, String passwd, String connectwd) {
		this.type = EType.valueOf(type);
		this.userName = usrname;
		this.passWord = passwd;
		this.connectWord = connectwd;
	}

	/* Defaults */
	public MsbConnParam() {
		this.type = EType.JMS_MQ;
		userName = MsbConnParam.USER_NAME;
		passWord = MsbConnParam.PASS_WORD;
		connectWord = MsbConnParam.CONNECT_WORD;
	}

	/*========== toString ===========*/
	@Override
	public String toString() {
		return "Connector: " + this.connectWord + "	User: " + this.userName + "	Password: " + this.passWord;
	}
}
