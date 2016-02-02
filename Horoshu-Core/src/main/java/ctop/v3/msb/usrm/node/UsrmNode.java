package ctop.v3.msb.usrm.node;

import ctop.v3.msb.msbu.data.MsbuInfo;
import ctop.v3.msb.proxy.MsbProxy;
import ctop.v3.msb.proxy.routemap.*;

import ctop.v3.msb.usrm.channel.UsrmChannel;
import ctop.v3.msb.usrm.data.*;

/**
 * Usrm Node Base: <br />
 * 1. Declare node informations on MSB ; ( DECLEAR_USRMNODE ) <br />
 * 
 * @see UsrmNode
 * 
 * @author DreamInSun
 * 
 */
public abstract class UsrmNode {

	/* ========== Constant ========== */

	/* ========== Properties ========== */
	/** All this Properties Should be Reference */
	protected MsbuInfo m_MsbuInfo;
	/** All this Properties Should be Reference */
	protected UsrmNodeInfo m_UsrmNodeInfo;
	/** All this Properties Should be Reference */
	protected UsrmNodeInfo m_UsrmMasterInfo;
	/** All this Properties Should be Reference */
	protected UsrmChannel m_UsrmChannel;
	/** For MSB Route Management */
	protected IRouteMap m_RouteMap;

	/* ========== Constructor ========== */
	/**
	 * Set Configuration to Initialize Behavior Type & Remember Itself. <br />
	 * Set Communication Channel for Communicating between USRMs. <br />
	 * 
	 * @param configProvider
	 * @param chn
	 */
	public UsrmNode(MsbProxy msbProxy) {
		/* Store MSBU Informations */
		m_MsbuInfo = msbProxy.getMsbuInfo();
		/* Create USRM Node Informations for USRM System */
		m_UsrmNodeInfo = UsrmNodeInfo.getInstance(m_MsbuInfo);
		/* Store Communication Channel */
		m_UsrmChannel = msbProxy.getUsrmChannel();
		/* Get Route Map */
		m_RouteMap = msbProxy.getRouteMap();
	}

	/* ========== Assistant Function ========== */
	protected long getCurrentTimeStamp() {
		/* Get Current Timestamp */
		return System.currentTimeMillis();
	}
}
