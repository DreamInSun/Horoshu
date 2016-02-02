package ctop.v3.msb.bridge;

import java.util.HashMap;
import java.util.Map;

import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.msbu.data.MsbuInfo;
import ctop.v3.msb.proxy.MsbProxy;
import ctop.v3.msb.usrm.data.MsbRouteItem;

/**
 * Bridge Unit Can Map Source Services in Source MSB to DestProxy, <br />
 * One Bridge Connect Two MSB Together
 * 
 * 
 * MSbBridge Life Circle: <br />
 * 1. On initialization, load bridge maps.<br />
 * 2. Register virtual services on USRM Keeper, with connect word point to bridge unit. <br />
 * <br />
 * 3. On Receiving Bridge Access Request, bridge unit will access real service via destProxy with same Request. <br />
 * 4. <br />
 * 
 * @author DreamInSun
 * 
 */
public class MsbBridge {
	/*========== Constant ==========*/

	/*========== Properties ==========*/
	/*  Access Service from Source MSB */
	MsbuInfo m_SrcMsbuInfo;
	MsbProxy m_SrcProxy;
	/* Publish Service to Destination MSB */
	MsbuInfo m_DestMsbuInfo;
	MsbProxy m_DestProxy;

	Map<MsbUrn, MsbRouteItem> m_ConvertMap = new HashMap<MsbUrn, MsbRouteItem>();

	/** Key : MsbDomain, Value : MsbProxy */
	//Map<String, UsrmSvcRouter> m_CallerMap = new HashMap<Strng, UsrmCaller>;

	/*========== Constructor ==========*/
	public MsbBridge(MsbuInfo srcMsbuInfo, MsbuInfo destMsbuInfo) {
		/* Get Proxy */

	}

	/*========== Inject Proxy ==========*/
	public void setSrcProxy(MsbProxy proxy) {
		m_SrcProxy = proxy;
	}

	public void setDestProxy(MsbProxy proxy) {
		m_DestProxy = proxy;
	}

	/*==========  ==========*/
	public MsbRouteItem getBridgeServicesMap() {
		return null;

	}
	/*==========  ==========*/
}
