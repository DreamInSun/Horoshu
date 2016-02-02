package ctop.v3.msb.usrm.node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ctop.v3.msb.common.urn.MsbDomain;
import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.usrm.data.MsbRouteItem;
import ctop.v3.msb.usrm.data.UsrmNodeInfo;
import ctop.v3.msb.usrm.data.UsrmNodeInfo.EState;

/**
 * 
 * @author DreamInSun
 * 
 */
public class UsrmNodeManager {
	/* ========== Constant ========== */

	/* ========== Properties ========== */
	/** SPECIAL : USRM Node Management, Key USRM NodeName */
	private Map<String, UsrmNodeInfo> m_UsrmNodeMap = new HashMap<String, UsrmNodeInfo>();
	/** For Bridge RouteItem Index */
	private Map<String, UsrmNodeInfo> m_BridgeMap = new HashMap<String, UsrmNodeInfo>();

	/* ========== Constructor ========== */
	public UsrmNodeManager() {
		loadNodeInfo();
	}

	/* ========== ========== */
	/**
	 * 1. If the Node is First Time Declaring it self. Generate a key for
	 * 
	 * @param nodeInfo
	 * @return The Registered Node Info or Illegal informations
	 */
	public UsrmNodeInfo registerNode(UsrmNodeInfo nodeInfo) {
		/* Mark Timestamp */
		nodeInfo.tsUpdate = nodeInfo.tsModified = getCurrentTimeStamp();
		/* If already exist update it */
		UsrmNodeInfo tmpNodeInfo = m_UsrmNodeMap.get(nodeInfo.getProxyId());
		if (tmpNodeInfo == null) {
			nodeInfo.setState(EState.LEGAL);
			m_UsrmNodeMap.put(nodeInfo.getProxyId(), nodeInfo);
			updateBridgeMap(nodeInfo);
		} else {
			if (false == isNodeInfoConflict(nodeInfo)) {
				nodeInfo.setState(EState.LEGAL);
				m_UsrmNodeMap.put(nodeInfo.getProxyId(), nodeInfo);
				updateBridgeMap(nodeInfo);
			} else {
				nodeInfo.setState(EState.ILLEGAL);
			}
		}
		return nodeInfo;
	}

	public UsrmNodeInfo unregisterNode(UsrmNodeInfo nodeInfo) {
		/* Mark Timestamp */
		nodeInfo.tsModified = getCurrentTimeStamp();

		nodeInfo.setState(EState.UNRGISTERED);

		return nodeInfo;
	}

	/*===================================================================*/
	/*==================== USRM Node State Functions ====================*/
	/*===================================================================*/

	public void updateNodeInfo(UsrmNodeInfo nodeInfo) {
		UsrmNodeInfo tmpNodeInfo = m_UsrmNodeMap.get(nodeInfo.getProxyId());
		if (tmpNodeInfo == null) {
			this.registerNode(nodeInfo);
		} else {
			tmpNodeInfo.tsUpdate = getCurrentTimeStamp();
		}
	}

	/**
	 * Update Bridge Informations, not change the Node Info Map
	 * 
	 * @param nodeInfo
	 */
	private final void updateBridgeMap(UsrmNodeInfo nodeInfo) {
		for (String domain : nodeInfo.bridgeDomain) {
			m_BridgeMap.put(domain, nodeInfo);
		}
	}

	public void markDeadNode(long tsDead) {
		for (UsrmNodeInfo nodeInfo : m_UsrmNodeMap.values()) {
			if (nodeInfo.tsUpdate < tsDead) {
				nodeInfo.setState(EState.OFFLINE);
			}
		}
	}

	/*============================================================================*/
	/*==================== USRM Node Authentication Functions ====================*/
	/*============================================================================*/
	/* ========== Authentication : isNodeInfoConflict ========== */
	/**
	 * 
	 * @param nodeInfo
	 * @return
	 */
	final private boolean isNodeInfoConflict(UsrmNodeInfo nodeInfo) {
		// TODO add Node Conflict asert control
		if (m_UsrmNodeMap.containsKey(nodeInfo.getProxyId())) {

		}
		return false;
	}

	/*====================================================================*/
	/*==================== USRM Node Bridge Management ===================*/
	/*====================================================================*/

	/* ========== Bridge Management : searchMatchDomainBridge ========== */
	/**
	 * 
	 * @param domain
	 * @return
	 */
	public UsrmNodeInfo searchMatchDomainBridge(MsbDomain domain) {
		// TODO 
		return null;
	}

	public MsbRouteItem getBridgeRouteItem(MsbUrn msbUrn) {
		MsbRouteItem tmpRet = null;
		UsrmNodeInfo nodeInfo = m_BridgeMap.get(msbUrn.getDomain());
		if (nodeInfo != null) {
			tmpRet = new MsbRouteItem(msbUrn, nodeInfo.getProxyId());
		}
		return tmpRet;
	}

	/*=====================================================================*/
	/*==================== USRM Node  Data Persistence ====================*/
	/*=====================================================================*/
	public void saveNodeInfo() {

	}

	public void loadNodeInfo() {

		try {
			/* Load it From DB */
		} catch (Exception exp) {

		}

	}

	/* =========================================================== */
	/* ==================== Monitor Functions ==================== */
	/* =========================================================== */

	public UsrmNodeInfo[] getUsrmNodeInfoList() {
		UsrmNodeInfo[] tmpRet = null;
		Collection<UsrmNodeInfo> usrmNodeInfoList = this.m_UsrmNodeMap.values();
		if (usrmNodeInfoList.size() > 0) {
			tmpRet = new UsrmNodeInfo[usrmNodeInfoList.size()];
			usrmNodeInfoList.toArray(tmpRet);
		}
		return tmpRet;
	}

	public UsrmNodeInfo getUsrmNodeInfo(String nodeName) {
		UsrmNodeInfo tmpRet = this.m_UsrmNodeMap.get(nodeName);
		return tmpRet;
	}

	/* =========================================================== */
	/* ==================== Display Functions ==================== */
	/* =========================================================== */

	/* ========== toString ========== */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		for (UsrmNodeInfo nodeInfo : this.m_UsrmNodeMap.values()) {
			sb.append("Name : " + nodeInfo.getName() + "\tProxy : " + nodeInfo.getProxyId() + "\t Master : " + nodeInfo.isMaster + "\t Publisher : " + nodeInfo.isPublisher + "\t Bridge £º"
					+ nodeInfo.isBridge + "[");
			if (nodeInfo.bridgeDomain != null) {
				for (String domain : nodeInfo.bridgeDomain) {
					sb.append(domain).append(" ");
				}
			}
			sb.append("]\r\n");
		}
		return sb.toString();
	}

	/* ========== Assistant Function ========== */
	protected long getCurrentTimeStamp() {
		/* Get Current Timestamp */
		return System.currentTimeMillis();
	}
}
