package ctop.v3.msb.proxy.routemap;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.common.urn.MsbUrnVer;
import ctop.v3.msb.msbu.data.MsbuInfo;
import ctop.v3.msb.usrm.data.MsbRouteBulletin;
import ctop.v3.msb.usrm.data.MsbRouteItem;

/**
 * MSB resource router based on HashMap. <br />
 * 
 * @author DreamInSun
 * 
 */
public class RouteMapHash extends RouteMapBase {

	/*========== Constant ==========*/
	static MsbUrnVer g_minVer = new MsbUrnVer(0, 0);
	static MsbUrnVer g_maxVer = new MsbUrnVer(9999, 9999);

	/*========== Properties ==========*/
	/** Using double HashMap to Realize RouteMap */
	private Map<MsbUrn, MsbRouteItem> m_RouteMap = Collections.synchronizedMap(new HashMap<MsbUrn, MsbRouteItem>()); // TODO Notice the ConCurrentHashMap is right Choice

	/*===========================================================*/
	/*==================== Singleton Factory ====================*/
	/*===========================================================*/
	/** Key MSB Domain Name */
	static Map<String, RouteMapHash> g_RouteMapCache = new LinkedHashMap<String, RouteMapHash>();

	/**
	 * Get Singleton RouteMap, Every UsrmNode with same UsrmNodeInfo share the same RouteMap. <br />
	 * 
	 * @param nodeInfo
	 * @return
	 */
	public static RouteMapHash getSingleton(MsbuInfo msbuInfo) {
		RouteMapHash tmpRetMap = g_RouteMapCache.get(msbuInfo.domain);
		if (tmpRetMap == null) {
			tmpRetMap = new RouteMapHash(msbuInfo);
			g_RouteMapCache.put(msbuInfo.domain, tmpRetMap);
		}
		return tmpRetMap;
	}

	/*========== Constructor ==========*/
	public RouteMapHash(MsbuInfo msbuInfo) {
		super(msbuInfo);
	}

	/*=================================================================*/
	/*==================== Interface : IUrsmRouter ====================*/
	/*=================================================================*/

	/*========== IUrsmRouter : getRoute ==========*/
	/**
	 * USRM Master will execute Discover Route if this function return an null route item.
	 */
	@Override
	public MsbRouteItem searchRouteItem(MsbUrn urn) {
		MsbRouteItem tmpRouteItem = m_RouteMap.get(urn);
		return tmpRouteItem;
	}

	/*==============================================================*/
	/*==================== Route Map Management ====================*/
	/*==============================================================*/

	/*========== IUrsmRouter : addRouteItem ==========*/
	@Override
	public void addRouteItem(MsbRouteItem routeItem) {
		m_RouteMap.put(routeItem.msbUrn, routeItem);
	}

	/*========== IUrsmRouter : deleteRouteItem ==========*/
	@Override
	public void removeRouteItem(MsbUrn msbUrn) {
		m_RouteMap.remove(msbUrn);
	}

	/*========== IUrsmRouter : updateServicesMap ==========*/
	@Override
	public void updateServicesMap(MsbRouteBulletin bulletin) {
		for (MsbRouteItem routeItem : bulletin) {
			m_RouteMap.put(routeItem.msbUrn, routeItem);
		}
	}

	/*========== IUrsmRouter : toMsbRouteBulletin ==========*/
	@Override
	public MsbRouteBulletin toMsbRouteBulletin() {
		/* Temporary Bulletin */
		MsbRouteBulletin routebulletin = new MsbRouteBulletin(m_UsrmNodeInfo);
		for (Entry<MsbUrn, MsbRouteItem> entry : m_RouteMap.entrySet()) {
			routebulletin.add(entry.getValue());
		}
		return routebulletin;
	}

	@Override
	protected MsbRouteItem discoverRoute(MsbUrn svUrn, String param) {
		return null;
	}

	@Override
	public void reset() {
		this.m_RouteMap.clear();
	}

}
