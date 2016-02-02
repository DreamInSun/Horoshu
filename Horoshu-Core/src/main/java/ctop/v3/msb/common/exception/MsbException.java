package ctop.v3.msb.common.exception;

public class MsbException extends Exception {
	private static final long serialVersionUID = 1L;

	/*========== Constant ==========*/

	/* Error Code */
	public enum ErrCode {
		/** */
		DEFAULT,
		/** */
		PROXY_ERROR_JMS,
		/** */
		USRM_ROUTE_ERROR,
		/** */
		BRIDGE_MSBU_NOT_FOUND,
		/** USRM */
		ACCESS_NOT_PERMITTED,
		/** */
		CONFIG_LOAD_ERROR,
		/** */
		CONFIG_PARSE_ERROR,
	}

	/*========== Properties ==========*/
	ErrCode errCode;

	/*========== Constructor ==========*/
	public MsbException(String desc) {
		this(ErrCode.DEFAULT, desc);
	}

	public MsbException(ErrCode err, String desc) {
		super(desc);
		this.errCode = err;
	}

	/*========== Override : getMessage ==========*/
	@Override
	public String getMessage() {
		return "MSBU Exception :" + errCode.name() + "\r\n" + super.getMessage();
	}
}
