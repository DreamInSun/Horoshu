package ctop.v3.msb.usrm.constants;

/**
 * USRM Command Code will Determine the type of object the UsrmCommand loading.
 * 
 * @author DreamInSun
 * 
 */
public enum EUsrmCmdCode {

	/*========== Simple Command ==========*/
	UNKNOWN(0x0000),
	/** MSBU Maintains : MSBU Client/Provider => MSBU Master */
	HEART_BEAT(0x0001),

	/*========== USRM Router : Route Discovery ==========*/
	/** USRM Router Services Discovery : MSBU Client => MSBU Master */
	DISCOVER_ROUTE(0x0011),
	/** MSBU Router Services Discovery : MSBU Master => MSBU Client */
	REPLY_DISCOVER_ROUTE(0x0091),

	/*========== USRM Router : Route Discovery ==========*/
	/** USRM Master Services Request : MSBU Master => MSBU Nodes */
	REQUEST_ROUTE(0x0012),
	/** USRM Master Services Request : MSBU Nodes => MSBU Master */
	REPLY_REQUEST_ROUTE(0x0092),

	/*========== USRM Master : Request Service Declare  ==========*/
	/** USRM Master Services Request : Master => Publisher */
	REQUEST_SVC_DECLARE(0x0020),
	/** USRM Master Services Request : Publisher => Master */
	REPLY_REQUEST_SVC_DECLARE(0x00A0),

	/*========== USRM Slaver : Service Declaration ==========*/
	/** USRM Node Declaration Self on MSB : MSBU Slaver => MSBU Master */
	DECLEAR_USRM_NODE(0x0021),
	/** USRM Node Declaration Self on MSB : MSBU Master => MSBU Slaver */
	REPLY_DECLEAR_USRM_NODE(0x00A1),

	/*========== USRM NodeMaintain : Access Control Configuration ==========*/
	/** USRM Router Services Discovery : MSBU Master => MSBU NodeMaintain */
	DISPATCH_ACCESS_CONTROL(0x0025),
	/** USRM Router Services Discovery : MSBU NodeMaintain => MSBU Master */
	REPLY_DISPATCH_ACCESS_CONTROL(0x0095),

	/*========== USRM Slaver : Service Modification ==========*/
	/** USRM Slaver Service Modification : MSBU Slaver => MSBU Master ( register ) */
	MODIFY_SERVICES(0x0022),
	/** USRM Slaver Service Modification : MSBU Master => MSBU Slaver ( register ) */
	REPLY_MODIFY_SERVICES(0x00A2),

	/*========== USRM Master :  ==========*/
	/** Broadcast Service Bulletin : MSBU Master => MSBU Client/Provider */
	BROADCAST_SERVICES(0x00F0),

	/*========== USRM Master : Seek Service ==========*/
	/** USRM seek specified service in local MSB domain : MSBU Master => MSBU Provider */
	SEEK_SERVICE(0x0071),
	/** USRM seek specified service in local MSB domain : MSBU Provider => MSBU Master */
	REPLY_SEEK_SERVICE(0x00F1);

	/*=========================================================*/
	/*==================== Enumerate Value ====================*/
	/*=========================================================*/

	private int value;

	private EUsrmCmdCode(int val) {
		this.value = val;
	}

	/*==========  ==========*/
	public int getValue() {
		return this.value;
	}

	public static EUsrmCmdCode getEnum(int value) {
		switch (value) {
		case 0x0001:
			return HEART_BEAT;
		case 0x0011:
			return DISCOVER_ROUTE;
		case 0x0091:
			return REPLY_DISCOVER_ROUTE;
		case 0x0020:
			return REQUEST_SVC_DECLARE;
		case 0x0021:
			return DECLEAR_USRM_NODE;
		case 0x00A1:
			return REPLY_DECLEAR_USRM_NODE;
		case 0x0022:
			return MODIFY_SERVICES;
		case 0x00A0:
			return REPLY_REQUEST_SVC_DECLARE;
		case 0x00A2:
			return REPLY_MODIFY_SERVICES;
		case 0x0025:
			return DISPATCH_ACCESS_CONTROL;
		case 0x00A5:
			return REPLY_DISPATCH_ACCESS_CONTROL;
		case 0x00F0:
			return BROADCAST_SERVICES;
		case 0x0071:
			return SEEK_SERVICE;
		case 0x00F1:
			return REPLY_SEEK_SERVICE;
		case 0x0000:
		default:
			return UNKNOWN;
		}
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}

}
