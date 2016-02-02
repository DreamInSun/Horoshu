package ctop.v3.msb.proxy.routemap;

import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.usrm.data.MsbRouteBulletin;
import ctop.v3.msb.usrm.data.MsbRouteItem;

public interface IRouteMap {

	/**
	 * 
	 */
	void addRouteItem(MsbRouteItem routeItem);

	/**
	 * Delete Service Route By MsbUrn
	 */
	void removeRouteItem(MsbUrn msbUrn);

	/**
	 * Form An Service Route Bulletin to Broadcast.
	 * 
	 * @return
	 */
	public MsbRouteBulletin toMsbRouteBulletin();

	/**
	 * Update Services Map with Input Bulletin
	 * 
	 * @param bulletin
	 */
	public void updateServicesMap(MsbRouteBulletin bulletin);

	/**
	 * 
	 * @param urn
	 * @return
	 */
	public MsbRouteItem searchRouteItem(MsbUrn urn);

	/**
	 * 
	 */
	public void reset();
}
