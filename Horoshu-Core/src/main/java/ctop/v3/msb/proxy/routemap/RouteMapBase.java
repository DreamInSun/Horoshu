package ctop.v3.msb.proxy.routemap;

import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.msbu.data.MsbuInfo;
import ctop.v3.msb.usrm.channel.UsrmChannel;
import ctop.v3.msb.usrm.data.MsbRouteItem;
import ctop.v3.msb.usrm.data.UsrmNodeInfo;

public abstract class RouteMapBase implements IRouteMap {

	/*========== Properties ==========*/
	/** USRM Management Info */
	protected UsrmNodeInfo m_UsrmNodeInfo;
	/** USRM Communication Channel */
	protected UsrmChannel m_CommChannel;

	/*========== Constructor ==========*/
	public RouteMapBase(MsbuInfo msbuInfo) {
		m_UsrmNodeInfo = UsrmNodeInfo.getInstance(msbuInfo);
	}

	/**
	 * Discover Service Resource in the MSB by Request Master.
	 * 
	 * @param svUrn
	 * @param param
	 * @return
	 */
	protected abstract MsbRouteItem discoverRoute(MsbUrn svUrn, String param);

}
