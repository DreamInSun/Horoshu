package ctop.v3.msb.proxy.constants;

/**
 * This Enumerate Define the JMS Type using in MSB System
 * 
 * @author DreamInSun
 * 
 */
public enum EMsbMsgType {

	/*========== Remote Procedure Call : Synchronize ==========*/
	/** Receiving this type of message, Receiver will send output direct to the sender as soon as processed. */
	MSB_RPC_REQ(5),
	/** Receiving this type of message, Receiver find callback in callback list and execute it. */
	MSB_RPC_RES(6),

	/*========== Remote Procedure Call : Asynchronize ==========*/
	/** Receiving this type of message, Receiver will send output direct to the sender as soon as processed. */
	//MSB_RPC_ASYNC_REQ(7),
	/** Receiving this type of message, Receiver will send output direct to the sender as soon as processed. */
	//MSB_RPC_ASYNC_RES(8),

	/*========== Remote Procedure Call : Synchronize Platform Special ==========*/
	/** Receiving this type of message, Receiver will send output direct to the sender as soon as processed. */
	MSB_PLATFORM_REQ(9),
	/** Receiving this type of message, Receiver find callback in callback list and execute it. */
	MSB_PLATFORM_RES(10),

	/*========== Remote Procedure Call : Asynchronize Platform Special ==========*/
	/** Receiving this type of message, Receiver will send output direct to the sender as soon as processed. */
	//MSB_RPC_ASYNC_REQ_V3(11),
	/** Receiving this type of message, Receiver will send output direct to the sender as soon as processed. */
	//MSB_RPC_ASYNC_RES_V3(12),

	/*========== Message ==========*/
	/** Receiving this type of message, Receiver will look up next receiver and send to it after message processed */
	MSB_MSG_ASYNC(9);

	/*=========================================================*/
	/*==================== Enumerate Value ====================*/
	/*=========================================================*/

	private int value;

	EMsbMsgType(int val) {
		this.value = val;
	}

	@Override
	public String toString() {
		return this.name() + Integer.toString(value);
	}

}
