package ctop.v3.msb.proxy.base;

import com.hztech.util.ComOutput;

import ctop.v3.msb.MSB.IMsbRpcCallback;

public class SyncCallback implements IMsbRpcCallback {

	/*========== Properties ==========*/
	public ComOutput output;
	private Object thrObj;

	/*========== Constructor ==========*/
	public SyncCallback() {

	}

	/*==========  ==========*/
	/**
	 * Block Mode£¬ Retrieve Message on Get Reply Message.
	 * 
	 * @return
	 */
	public ComOutput getOutput() {
		return output;
	}

	/*========== Interface : IMsbProxyCallback ==========*/
	@Override
	public void onReplyMsg(ComOutput coRes) {
		output = coRes;
	}

	public Object getThreadObject() {
		return thrObj;
	}

	public void setThreadObject(Object obj) {
		this.thrObj = obj;
	}

}
