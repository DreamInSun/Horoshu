package ctop.v3.msb.usrm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import ctop.v3.msb.MSB;
import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.msbu.config.IAccessCtrlConfigProvider;
import ctop.v3.msb.msbu.data.MsbuInfo;
import ctop.v3.msb.msbu.mngr.access.AccessCtrlBrief;
import ctop.v3.msb.msbu.mngr.access.AccessCtrlItem;
import ctop.v3.msb.proxy.MsbProxy;
import ctop.v3.msb.proxy.data.ServiceEntry;
import ctop.v3.msb.proxy.data.ServiceEntry.EModifyType;
import ctop.v3.msb.usrm.constants.EUsrmCmdCode;
import ctop.v3.msb.usrm.data.MsbRouteBulletin;
import ctop.v3.msb.usrm.data.MsbRouteItem;
import ctop.v3.msb.usrm.data.MsbSvcBulletin;
import ctop.v3.msb.usrm.data.UsrmCommand;
import ctop.v3.msb.usrm.data.UsrmNodeInfo;
import ctop.v3.msb.usrm.interfaces.IUsrmCmdListener;
import ctop.v3.msb.usrm.node.UsrmNode;
import ctop.v3.msb.usrm.node.UsrmNodeManager;

/**
 * USRM Manager
 * 
 * @author DreamInSun
 * 
 */
public class UsrmSvcMaster extends UsrmNode {
	private static Logger g_Logger = Logger.getLogger(UsrmSvcMaster.class);

	/*========== Constant ==========*/
	public static final String ATTR_NODE_LIFE_TIME = "node-lifetime";
	public static final String ATTR_SVC_LIFE_TIME = "service-lifetime";

	/** Time window to deal Heart Beat Message. */
	public static final int HEART_BEAT_WINDOW = 300000;

	/* ========== Properties ========== */
	/** MsbID to define USRM master managing */
	protected String m_DomainMsbID;
	/**  */
	protected String m_MsbName;
	/*===== Global Managers =====*/
	/** Store the Node Informations in local MSB domain */
	private UsrmNodeManager m_NodeManager = new UsrmNodeManager();
	/** Store the Services in local MSB domain */
	private Map<MsbUrn, ServiceEntry> m_GlobalSvcMap = new ConcurrentHashMap<MsbUrn, ServiceEntry>();

	/*===== Centralized Configurations =====*/
	/** Bridge Access Control Configurations */
	private IAccessCtrlConfigProvider m_AccessConfigProvider = null;
	/** */

	/*===== ======*/
	private int m_NodeLifeTime = UsrmSvcPublisher.DEFAULT_HEART_BEAT_INTERVAL * 2;
	private int m_SvcLifeTime = UsrmSvcPublisher.DEFAULT_HEART_BEAT_INTERVAL * 2;

	/* ========== Constructor ========== */
	/**
	 * 
	 * @param chn
	 */
	public UsrmSvcMaster(MsbProxy msbProxy) {
		/* UsrmSlaver */
		super(msbProxy);
		g_Logger.debug("Initializing UsrmSvcMaster.");

		/*===== STEP 2. Register Command Listeners =====*/
		m_UsrmChannel.registerMasterCmdListener(m_onHeartBeat);
		m_UsrmChannel.registerMasterCmdListener(m_onDecleraUsrmNode);
		m_UsrmChannel.registerMasterCmdListener(m_onModifyService);
		m_UsrmChannel.registerMasterCmdListener(m_onDiscoverRoute);

		m_UsrmChannel.registerMasterCmdListener(m_onReplyRequestRoute);
		m_UsrmChannel.registerMasterCmdListener(m_onReplyRequestSvcDeclare);
		m_UsrmChannel.registerMasterCmdListener(m_onReplyDispatchAccessCtrl);

		/*===== STEP 3. Register self as Master =====*/
		m_NodeManager.registerNode(m_UsrmNodeInfo);

		/*===== STEP 4. Load MSB Extend Configurations =====*/
		//TODO ??

		/*===== STEP 5. Node & Service Life Cycle Control Thread =====*/
		int heartbeatInterval = Integer.parseInt(m_MsbuInfo.publisherParams.get(UsrmSvcPublisher.ATTR_HEART_BEAT));
		int nodeLifetime = Integer.parseInt(m_MsbuInfo.masterParams.get(ATTR_NODE_LIFE_TIME));
		if (nodeLifetime > 0) {
			if (nodeLifetime < heartbeatInterval) {
				nodeLifetime = 2 * heartbeatInterval;
			}
			m_NodeLifeTime = nodeLifetime;
			new Thread(m_NodeLifeCircleThread).start();
		}

		int svcLifetime = Integer.parseInt(m_MsbuInfo.masterParams.get(ATTR_SVC_LIFE_TIME));
		if (svcLifetime > 0) {
			if (svcLifetime < heartbeatInterval) {
				svcLifetime = 2 * heartbeatInterval;
			}
			m_SvcLifeTime = nodeLifetime;
			new Thread(m_SvcLifeCircleThread).start();
		}
	}

	/* ========================================================== */
	/* ==================== Export Functions ==================== */
	/* ========================================================== */
	/**
	 * 
	 */
	public void broadcastSvcBulletin() {
		MsbRouteBulletin bulletin = m_RouteMap.toMsbRouteBulletin();
		UsrmCommand cmd = new UsrmCommand(this.m_UsrmNodeInfo, EUsrmCmdCode.BROADCAST_SERVICES, bulletin);
		this.m_UsrmChannel.broadcastCommand(cmd);
	}

	/**
	 * 
	 * @param destNode
	 * @param msbUrnList
	 */
	private void requestSvcReport(UsrmNodeInfo destNode, MsbUrn[] msbUrnList) {
		UsrmCommand cmd = new UsrmCommand(this.m_UsrmNodeInfo, EUsrmCmdCode.REQUEST_SVC_DECLARE, msbUrnList);
		this.m_UsrmChannel.sendCmdToNode(destNode, cmd);
	}

	public void setAccessControlItem(AccessCtrlItem acItem) {
		this.m_AccessConfigProvider.setAccessCtrlItem(acItem.getCallerName(), acItem);
	}

	/**
	 * Dispatch the Access Control Item to Specified USRM NodeMaintainc. <br />
	 * the receiver will modified the Access Control Manager, if related.
	 * 
	 * @param destNode
	 * @param acItem
	 */
	public void dispatchAccessControlList(UsrmNodeInfo destNode, AccessCtrlBrief acMngr) {
		UsrmCommand cmd = new UsrmCommand(this.m_UsrmNodeInfo, EUsrmCmdCode.DISPATCH_ACCESS_CONTROL, acMngr);
		this.m_UsrmChannel.sendCmdToNode(destNode, cmd);
	}

	/**
	 * 
	 * @param acConfigProvider
	 */
	public void setAccessCtrlConfigProvider(IAccessCtrlConfigProvider acConfigProvider) {
		this.m_AccessConfigProvider = acConfigProvider;
		broadcastAccessControlList();
	}

	private final void broadcastAccessControlList() {
		for (UsrmNodeInfo nodeInfo : this.m_NodeManager.getUsrmNodeInfoList()) {
			if (nodeInfo.isBridge) {
				String[] bridgeDomains = nodeInfo.bridgeDomain;
				for (String domain : bridgeDomains) {
					AccessCtrlBrief acMngr = m_AccessConfigProvider.getDomainAccessBrief(domain);
					dispatchAccessControlList(nodeInfo, acMngr);
				}
			}
		}
	}

	/* =============================================================== */
	/* ==================== USRM Command Handlers ==================== */
	/* =============================================================== */

	/* ========= USRM Command Handler : HEART_BEAT ========== */
	private IUsrmCmdListener m_onHeartBeat = new IUsrmCmdListener() {
		@Override
		public EUsrmCmdCode getInterestCommand() {
			return EUsrmCmdCode.HEART_BEAT;
		}

		@Override
		public void onReceiveUsrmCmd(UsrmCommand cmd) {

			/*===== STEP 1. Skip Expired HeartBeat =====*/
			long tsNow = getCurrentTimeStamp();
			/* To avoid Heart Beat Overflow */
			if (tsNow - cmd.getTimestamp() > HEART_BEAT_WINDOW) {
				g_Logger.info(tsNow + "Skip Heart Beat." + cmd.getTimestamp());
				return;
			}

			/* ===== STEP 2. Get Living MsbUrn List ===== */
			Object load = cmd.getLoad();

			/* ===== STEP 3. Update Sender =====*/
			UsrmNodeInfo senderInfo = cmd.getSender();
			UsrmSvcMaster.this.m_NodeManager.updateNodeInfo(senderInfo);

			/* ===== STEP 4. Update Global Service Map ===== */
			if (load != null) {
				MsbUrn[] livingMsbUrn = (MsbUrn[]) load;
				List<MsbUrn> missingUrnList = new ArrayList<MsbUrn>();
				for (MsbUrn urn : livingMsbUrn) {
					ServiceEntry tmpEntry = m_GlobalSvcMap.get(urn);
					if (tmpEntry != null) {
						tmpEntry.tsUpdate = tsNow;
						tmpEntry.setModifyType(ServiceEntry.EModifyType.SERVICE_VALID);
					} else {
						/* Request Missing Service Description */
						if (urn.domain.equals(m_MsbuInfo.getDomain())) {
							g_Logger.debug("Missing Service : " + urn.fullUrn);
							missingUrnList.add(urn);
						}
					}
				}
				/* Require the Missing MSBURN */
				if (missingUrnList.size() > 0) {
					MsbUrn[] msbUrnArray = new MsbUrn[missingUrnList.size()];
					missingUrnList.toArray(msbUrnArray);
					UsrmSvcMaster.this.requestSvcReport(cmd.getSender(), msbUrnArray);
				}
			}
		}
	};

	/* ========= USRM Command Handler : REPLY_REQUEST_SVC_DECLARE ========== */
	private IUsrmCmdListener m_onReplyRequestSvcDeclare = new IUsrmCmdListener() {
		@Override
		public EUsrmCmdCode getInterestCommand() {
			return EUsrmCmdCode.REPLY_REQUEST_SVC_DECLARE;
		}

		@Override
		public void onReceiveUsrmCmd(UsrmCommand cmd) {
			ServiceEntry[] tmpSvcEntryList = (ServiceEntry[]) cmd.getLoad();
			long tsNow = getCurrentTimeStamp();
			for (ServiceEntry svcEntry : tmpSvcEntryList) {
				svcEntry.tsModified = svcEntry.tsUpdate = tsNow;
				modifyGlobalService(ServiceEntry.EModifyType.REGISTERING, svcEntry, cmd.getSender().getProxyId());
			}
		}
	};

	/* ========= USRM Command Handler : REPLY_DISCOVER_ROUTE ========== */
	private IUsrmCmdListener m_onReplyRequestRoute = new IUsrmCmdListener() {

		@Override
		public EUsrmCmdCode getInterestCommand() {
			return EUsrmCmdCode.REPLY_REQUEST_ROUTE;
		}

		@Override
		public void onReceiveUsrmCmd(UsrmCommand cmd) {
			MsbRouteItem[] routeItemArray = (MsbRouteItem[]) cmd.getLoad();
			for (MsbRouteItem routeItem : routeItemArray) {
				UsrmSvcMaster.this.m_RouteMap.addRouteItem(routeItem);
			}
		}
	};

	/* ========= USRM Command Handler : DISCOVER_ROUTE ========== */
	private IUsrmCmdListener m_onDiscoverRoute = new IUsrmCmdListener() {
		@Override
		public EUsrmCmdCode getInterestCommand() {
			return EUsrmCmdCode.DISCOVER_ROUTE;
		}

		@Override
		public void onReceiveUsrmCmd(UsrmCommand cmd) {

			/* ===== STEP 1. Get Required MsbUrn List ===== */
			MsbUrn msbUrn = (MsbUrn) cmd.getLoad();
			/* ===== STEP 2. Search Required Route In Master Map ===== */
			MsbRouteItem tmpRoute = null;
			if (msbUrn.isLocalDomain(UsrmSvcMaster.this.m_MsbuInfo.domain)) {
				/**
				 * if the Request MSBURN is local domain, search it in local RouteMap
				 */
				tmpRoute = UsrmSvcMaster.this.m_RouteMap.searchRouteItem(msbUrn);
			} else {
				/**
				 * if the Request MSBURN is cross domain, search it in Node Map, and find the most matches domain
				 */
				tmpRoute = UsrmSvcMaster.this.m_NodeManager.getBridgeRouteItem(msbUrn);
			}

			/* ==== STEP 3. Send Reply Command ===== */
			UsrmCommand cmdReply = new UsrmCommand(UsrmSvcMaster.this.m_UsrmNodeInfo, EUsrmCmdCode.REPLY_DISCOVER_ROUTE, tmpRoute);
			UsrmSvcMaster.this.m_UsrmChannel.sendCmdToNode(cmd.getSender(), cmdReply);
		}
	};

	/* ========= USRM Command Handler : REGISTER_SERVICES ========== */
	private IUsrmCmdListener m_onModifyService = new IUsrmCmdListener() {
		@Override
		public EUsrmCmdCode getInterestCommand() {
			return EUsrmCmdCode.MODIFY_SERVICES;
		}

		@Override
		public void onReceiveUsrmCmd(UsrmCommand cmd) {
			/* ===== STEP 1. Get Service Bulletin ===== */
			MsbSvcBulletin svcBulletin = (MsbSvcBulletin) cmd.getLoad();
			/* ===== STEP 2. Execute Service Modification ===== */
			ArrayList<MsbUrn> tmpReply = new ArrayList<MsbUrn>();
			long tsNow = getCurrentTimeStamp();
			for (Entry<MsbUrn, ServiceEntry> svcMapEntry : svcBulletin.getModifiedServices().entrySet()) {
				ServiceEntry svcEntry = svcMapEntry.getValue();
				svcEntry.tsModified = svcEntry.tsUpdate = tsNow;
				modifyGlobalService(svcEntry.getModifyType(), svcEntry, svcBulletin.m_UsrmNodeInfo.getProxyId());
				tmpReply.add(svcMapEntry.getKey());
			}
			/* ===== Reply Service Confirm ===== */
			UsrmCommand cmdReply = new UsrmCommand(UsrmSvcMaster.this.m_UsrmNodeInfo, EUsrmCmdCode.REPLY_MODIFY_SERVICES, tmpReply);
			UsrmSvcMaster.this.m_UsrmChannel.sendCmdToNode(cmd.getSender(), cmdReply);
		}
	};

	/*========== Assistant : modifyGlobalService ==========*/
	private final boolean modifyGlobalService(ServiceEntry.EModifyType type, ServiceEntry svcEntry, String proxyID) {
		boolean tmpRet = false;
		MsbUrn msbUrn = svcEntry.msbUrn;
		/* Collection */
		/* Modify the Global Register Map */
		switch (type) {
		case REGISTERING:
			g_Logger.debug("Global Register Service: " + msbUrn + " in [" + proxyID + "]");
			/* Change Service Entry Map */
			svcEntry.setModifyType(ServiceEntry.EModifyType.SERVICE_VALID);
			m_GlobalSvcMap.put(msbUrn, svcEntry);
			/* Change Route Map */
			MsbRouteItem routeItem = new MsbRouteItem(msbUrn, proxyID);
			UsrmSvcMaster.this.m_RouteMap.addRouteItem(routeItem);
			tmpRet = true;
			break;
		case UNREGISTERING:
			g_Logger.debug("Global Unregister Service: " + msbUrn + " in [" + proxyID + "]");
			/* Change Service Entry Map */
			svcEntry.setModifyType(ServiceEntry.EModifyType.SERVICE_INVALID);
			m_GlobalSvcMap.put(msbUrn, svcEntry);
			/* Change Route Map */
			UsrmSvcMaster.this.m_RouteMap.removeRouteItem(msbUrn);
			tmpRet = true;
			break;
		case NONE:
		default:
			break;
		}
		return tmpRet;
	}

	/* ========= USRM Command Handler : DECLEAR_USRMNODE ========== */
	/**
	 * On Usrm Node Initializing, broadcast or directly sending to Master the informations about self description.
	 * Master gets the declaration and validation
	 */
	private IUsrmCmdListener m_onDecleraUsrmNode = new IUsrmCmdListener() {
		@Override
		public EUsrmCmdCode getInterestCommand() {
			return EUsrmCmdCode.DECLEAR_USRM_NODE;
		}

		@Override
		public void onReceiveUsrmCmd(UsrmCommand cmd) {
			UsrmNodeInfo nodeInfo = (UsrmNodeInfo) cmd.getSender();
			nodeInfo = m_NodeManager.registerNode(nodeInfo);
			UsrmCommand cmdReply = new UsrmCommand(UsrmSvcMaster.this.m_UsrmNodeInfo, EUsrmCmdCode.REPLY_DECLEAR_USRM_NODE, nodeInfo);
			UsrmSvcMaster.this.m_UsrmChannel.sendCmdToNode(nodeInfo, cmdReply);

			/* If the Node is A Bridge Node, update the Bridge Access Control */
			if (nodeInfo.isBridge == true) {
				for (String domain : nodeInfo.bridgeDomain) {
					/* Get Related Access Control Items */
					AccessCtrlBrief acMngr = UsrmSvcMaster.this.m_AccessConfigProvider.getDomainAccessBrief(domain);
					//AccessCtrlItem[] acItems = acMngr.getAccessCtrlItems(domain);
					/* Dispatch Access Control Command */
					dispatchAccessControlList(nodeInfo, acMngr);
				}
			}
		}
	};

	/* ========= USRM Command Handler : DECLEAR_USRMNODE ========== */
	/**
	 * On Usrm Node Initializing, broadcast or directly sending to Master the informations about self description.
	 * Master gets the declaration and validation
	 */
	private IUsrmCmdListener m_onReplyDispatchAccessCtrl = new IUsrmCmdListener() {
		@Override
		public EUsrmCmdCode getInterestCommand() {
			return EUsrmCmdCode.REPLY_DISPATCH_ACCESS_CONTROL;
		}

		@Override
		public void onReceiveUsrmCmd(UsrmCommand cmd) {
			UsrmNodeInfo nodeInfo = (UsrmNodeInfo) cmd.getSender();
			nodeInfo = m_NodeManager.registerNode(nodeInfo);
			UsrmCommand cmdReply = new UsrmCommand(UsrmSvcMaster.this.m_UsrmNodeInfo, EUsrmCmdCode.REPLY_DECLEAR_USRM_NODE, nodeInfo);
			UsrmSvcMaster.this.m_UsrmChannel.sendCmdToNode(nodeInfo, cmdReply);
		}
	};

	/* ======================================================= */
	/* ==================== Master Thread ==================== */
	/* ======================================================= */

	private Runnable m_NodeLifeCircleThread = new Runnable() {

		@Override
		public void run() {
			while (true) {

				try {
					/* Sleep Interval */
					Thread.sleep(m_NodeLifeTime);
					/* Update Modified Services */
					long tsDeadTime = getCurrentTimeStamp() - m_NodeLifeTime;
					m_NodeManager.markDeadNode(tsDeadTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	};

	private Runnable m_SvcLifeCircleThread = new Runnable() {

		@Override
		public void run() {
			while (true) {
				try {
					/* Sleep Interval */
					Thread.sleep(m_SvcLifeTime);
					/* Update Modified Services */
					long tsDeadTime = getCurrentTimeStamp() - m_SvcLifeTime;
					for (ServiceEntry svcEntry : m_GlobalSvcMap.values()) {
						if (svcEntry.tsUpdate < tsDeadTime) {
							svcEntry.setModifyType(EModifyType.SERVICE_INVALID);
						}
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	};

	/* ============================================================ */
	/* ==================== Assistant Function ==================== */
	/* ============================================================ */

	/* ========== USRM Management : getServiceMap ========== */
	/**
	 * Only for UsrmManagement.
	 * 
	 * @return
	 */
	public Map<MsbUrn, ServiceEntry> getServiceMap() {
		return m_GlobalSvcMap;
	}

	/**
	 * 
	 * @param msbUrn
	 * @return
	 */
	public ServiceEntry getSvcEntry(MsbUrn msbUrn) {
		return m_GlobalSvcMap.get(msbUrn);
	}

	public String printGlobalServiceMap() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("\r\n");
		for (ServiceEntry svcEntry : this.m_GlobalSvcMap.values()) {
			sb.append(svcEntry.msbUrn + "\t" + svcEntry.tsModified + "\t" + svcEntry.tsUpdate + "\r\n");
		}
		return sb.toString();
	}

	/*========== USRM Management : getNodeSummary ==========*/
	public UsrmNodeInfo[] getUsrmNodeInfoList() {
		return this.m_NodeManager.getUsrmNodeInfoList();
	}

	public UsrmNodeInfo getMsbuInfo(String nodeName) {
		return this.m_NodeManager.getUsrmNodeInfo(nodeName);
	}

	/*========== USRM Management : getAccessControl ==========*/
	public AccessCtrlBrief[] getAccessCtrlBriefs() {
		return this.m_AccessConfigProvider.getAccessCtrlBriefs();
	}

	public boolean updateAccessCtrlBirefs(List<AccessCtrlBrief> acBreifs) {
		for (AccessCtrlBrief acBrief : acBreifs) {
			this.m_AccessConfigProvider.setAccessCtrlBiref(acBrief);
		}
		/* */
		this.m_AccessConfigProvider.saveConfigFile();
		/* */
		this.broadcastAccessControlList();
		return true;
	}

	public UsrmNodeInfo getUsrmMasterInfo() {
		return this.m_UsrmNodeInfo;
	}

	public MsbuInfo[] getMsbuInfoList() {
		return MSB.getMsbConfigProvider().getMsbuInfoList();
	}
}
