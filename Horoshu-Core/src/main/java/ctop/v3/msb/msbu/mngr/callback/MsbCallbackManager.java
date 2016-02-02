package ctop.v3.msb.msbu.mngr.callback;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.hztech.util.ComOutput;

import ctop.v3.msb.MSB.IMsbRpcCallback;
import ctop.v3.msb.common.exception.MsbException;
import ctop.v3.msb.proxy.base.SyncCallback;

public class MsbCallbackManager<TCbID extends Comparable<?>> {
	private static final Logger g_Logger = Logger.getLogger(MsbCallbackManager.class);

	/*========== Properties ===========*/
	private Map<TCbID, IMsbRpcCallback> m_CallbackMapAsync = new HashMap<TCbID, IMsbRpcCallback>();
	private Map<TCbID, IMsbRpcCallback> m_CallbackMapSync = new HashMap<TCbID, IMsbRpcCallback>();

	public boolean registerCallBack(TCbID callbackID, IMsbRpcCallback callback, boolean isAsync) throws MsbException {
		boolean tmpRet = false;
		/* Determine which Map */
		Map<TCbID, IMsbRpcCallback> callbackMap;
		if (isAsync) {
			callbackMap = m_CallbackMapAsync;
		} else {
			callbackMap = m_CallbackMapSync;
		}
		/* Input Protection */
		if (callbackMap != null) {
			/* Key Conflict Check */
			//if (m_CallbackMap.containsKey(msgID)) throw new MsbProxyException(String.format("CallBack Matches Message ID [%s] Conflict.", msgID.toString()));
			/* */
			callbackMap.put(callbackID, callback);
			tmpRet = true;
		}
		return tmpRet;
	}

	public void executeCallback(Message msg, boolean isAsync) {
		/* Determine which Map */
		Map<TCbID, IMsbRpcCallback> callbackMap;
		if (isAsync) {
			callbackMap = m_CallbackMapAsync;
		} else {
			callbackMap = m_CallbackMapSync;
		}

		try {
			String callbackID = msg.getJMSCorrelationID();
			/* Find Callback */
			IMsbRpcCallback callback = callbackMap.get(callbackID);
			/* Execute Callback */
			if (callback != null) {
				callbackMap.remove(callbackID);
				ObjectMessage ObjMsg = (ObjectMessage) msg;
				ComOutput co = (ComOutput) ObjMsg.getObject();
				callback.onReplyMsg(co);
				if (isAsync == false) {
					/* Weak the Pending Thread if it is sleeping. */
					Object thdObj = ((SyncCallback) callback).getThreadObject();
					if (thdObj != null) {
						synchronized (thdObj) {
							thdObj.notify();
						}
					}
				}
			} else {
				//throw new MsbProxyException(String.format("Cannot Find CallBack Matches Message ID: [%s].", callbackID));
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	/*==========  ==========*/
	/**
	 * for Sync Invoke
	 * 
	 * @param callbackID
	 * @param callback
	 * @param timeout
	 * @throws InterruptedException
	 */
	public void waitRpcResponse(String callbackID, SyncCallback callback, int timeout) throws InterruptedException {
		/*===== Block Thread and wait for response =====*/

		if (callback.output == null) { // To fast to block
			// TODO Optimize Here	
			callback.setThreadObject(this);
			synchronized (this) {
				this.wait(timeout);
			}
		}

		/* Timeout */
		if (null == callback.output) {
			g_Logger.debug("Remoce Callback " + callbackID);
			this.m_CallbackMapSync.remove(callbackID);
		}
	}
}
