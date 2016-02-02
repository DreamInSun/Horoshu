package ctop.v3.msb.proxy.constants;

/**
 * Constant Definition
 * 
 * @author DreamInSun
 * 
 */
public interface CMsgField {

	/* Sender Information */
	String SENDER_DOMAIN = "SenderDomain";
	String SENDER_PROXY = "SenderProxy";

	String SENDER_NAME = "SenderName";

	/* Authentication Information */
	String APP_ID = "AppID";
	String LICENSE_KEY = "LincenseKey";
	String SESSION_ID = "SessionID";

	/* Destination Information */
	String MSBURN = "SvcName";

	/* Message Type */
	String MessageType = "";

	/* Service Orchestration */

	/* Service Choreography */

	/* Load */
	String MSG_LOAD = "ComInput";

	/* Callback ID  */
	String CALLBACK_ID = "CallbackID";
}
