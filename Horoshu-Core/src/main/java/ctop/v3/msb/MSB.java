package ctop.v3.msb;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Message;

import org.apache.log4j.Logger;

import com.hztech.util.ComInput;
import com.hztech.util.ComOutput;

import ctop.v3.msb.bridge.data.MsbBridgeMap;
import ctop.v3.msb.msbu.Msbu;
import ctop.v3.msb.msbu.config.IMsbConfigProvider;
import ctop.v3.msb.msbu.data.MsbuInfo;

/**
 * MSB is an Static Class
 * 
 * @author DreamInSun
 * 
 */
public class MSB {
	private final static Logger g_Logger = Logger.getLogger(MSB.class);

	/* ========== Static Properties ========== */
	/** Each Domain Can Get One MSBU */
	private static Map<String, Msbu> g_MsbuMap = new HashMap<String, Msbu>();
	/* Informations */
	private static IMsbConfigProvider g_configProvider;

	/* =========================================================== */
	/* ==================== Singleton Factory ==================== */
	/* =========================================================== */
	/**
	 * Initializing by XML configuration.
	 * 
	 * @param configPath
	 *            configuration file path & name from container root
	 * @return
	 */
	public static Msbu intialize(IMsbConfigProvider configProvider) {
		Msbu tmpMsbu = null;
		if (configProvider != null) {
			/*========== STEP 0. Store Configuration Provider ==========*/
			g_configProvider = configProvider;
			/*========== STEP 1. Get Bridge Map  ==========*/
			MsbBridgeMap bridgeMap = configProvider.getMsbBridgeMap();

			/*========== STEP 2. Initialize Necessary MSBU ==========*/
			if (bridgeMap == null) {
				/* Create MSBU without Bridge */
				String domain = configProvider.getMsbuInfo().getDomain();
				tmpMsbu = g_MsbuMap.get(domain);
				if (tmpMsbu == null) {
					g_Logger.info("Initialize MSBU : " + domain);
					tmpMsbu = new Msbu(configProvider.getMsbuInfo());
					g_MsbuMap.put(domain, tmpMsbu);
				}
			} else {
				/* Create All MSBU in the Bridge List */
				MsbuInfo[] msbuInfoList = configProvider.getMsbuInfoList();

				for (MsbuInfo msbuInfo : msbuInfoList) {
					String domain = msbuInfo.getDomain();
					tmpMsbu = g_MsbuMap.get(domain);
					/* Create MSBU */
					if (tmpMsbu == null) {
						g_Logger.info("Initialize MSBU" + domain);
						tmpMsbu = new Msbu(configProvider.getMsbuInfo(domain));
						g_MsbuMap.put(domain, tmpMsbu);
					}
					/* establish Bridges */
					bridgeMap.establishBridges(tmpMsbu);
					/* */
					tmpMsbu = g_MsbuMap.get(bridgeMap.getDefaultMsbuDomain());
				}
			}
		} else {
			g_Logger.error("Error on Creating MSBU Instance, Input MsbConfigProvider is Null.");
		}
		return tmpMsbu;
	}

	/*========== Getter & Setter ===========*/
	public static Map<String, Msbu> getGlobalMsbuMap() {
		return g_MsbuMap;
	}

	public static IMsbConfigProvider getMsbConfigProvider() {
		return g_configProvider;
	}

	/*===========  ===========*/
	public static MsbuInfo getMsbuInfo(String domain) {
		return g_configProvider.getMsbuInfo(domain);
	}

	/* =========================================================== */
	/* ==================== Interfaces ==================== */
	/* =========================================================== */
	public interface IMsbRpcHandler {
		/**
		 * Default Service Invoke Method
		 * 
		 * @param msgIn
		 * @return
		 */
		ComOutput onInvokeSvr(ComInput msgIn);
	}

	public interface IMsbRpcCallback {
		/**
		 * Execute the callback on receiving reply message. (Callback Mode)
		 * 
		 * @param message
		 */
		void onReplyMsg(ComOutput co);
	}

	public interface IMsbMsgHandler {
		void onReceiveMessage(Message msg);
	}

}
