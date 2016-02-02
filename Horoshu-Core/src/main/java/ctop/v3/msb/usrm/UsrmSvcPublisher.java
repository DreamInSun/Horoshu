package ctop.v3.msb.usrm;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.proxy.MsbProxy;
import ctop.v3.msb.proxy.data.ServiceEntry;
import ctop.v3.msb.proxy.mngr.IMsbuSvcManager;
import ctop.v3.msb.usrm.constants.EUsrmCmdCode;
import ctop.v3.msb.usrm.data.*;
import ctop.v3.msb.usrm.interfaces.IUsrmCmdListener;
import ctop.v3.msb.usrm.node.UsrmNode;

/**
 * USRM Slaver to register services on MSB domain.
 * 
 * @author DreamInSun
 * 
 */
public class UsrmSvcPublisher extends UsrmNode {
	private static Logger g_Logger = Logger.getLogger(UsrmSvcPublisher.class);
	/* ========================================================== */
	/* ==================== SingletonFactory ==================== */
	/* ========================================================== */

	/* ========================================== */
	/* ==================== ==================== */
	/* ========================================== */

	/* ========== Constant =========== */
	public static final String ATTR_HEART_BEAT = "heart-beat";

	public static final int DEFAULT_HEART_BEAT_INTERVAL = 20000;

	/* ========== Properties ========== */

	/** MsbID to define USRM client belonging */
	protected String m_MsbID;

	/* Register Numbers */
	protected String m_ParentProxyID;
	protected String m_ParentName;

	protected IMsbuSvcManager m_MsbSvcMngr;

	protected MsbSvcBulletin m_SvcBulletinDirty;

	/** TThread Configuration, in Millisecond */
	protected int m_HeartBeatInterval = DEFAULT_HEART_BEAT_INTERVAL;

	/* ========== Service Observer ========== */
	/**
	 * Subscribe to Service
	 */

	/* ========== Constructor ========== */
	/**
	 * 1. Keep Communication Channel; <br />
	 * 2. Register USRM command listener; <br />
	 * 3. Register USRM broadcast listener; <br />
	 */
	public UsrmSvcPublisher(MsbProxy msbProxy) {
		super(msbProxy);
		g_Logger.debug("Initializing UsrmSvcPublisher.");
		m_MsbSvcMngr = msbProxy.getSvcManager();
		/* Setup Temporary Bulletin */
		m_SvcBulletinDirty = new MsbSvcBulletin(this.m_UsrmNodeInfo);
		/* Register Command Listeners */
		m_UsrmChannel.registerCmdListener(m_onReplyModifyServices);
		m_UsrmChannel.registerCmdListener(m_onBroadcastBulletin);
		m_UsrmChannel.registerCmdListener(m_onRequestSvcDeclare);
		/* Run Heart beat thread */
		int HeartBeatInterval = Integer.parseInt(m_MsbuInfo.publisherParams.get(ATTR_HEART_BEAT));
		if (HeartBeatInterval > 0) {
			m_HeartBeatInterval = HeartBeatInterval;
			new Thread(m_HearbeatThread).start();
		}
	}

	/* ========== ========== */
	public void reportToUsrmMaster(Serializable load) {
		/* Make Up Command Message */
		// TODO Get Service Bulletin;
		/* Send Message to USRM Master */
		UsrmCommand cmd = new UsrmCommand(this.m_UsrmMasterInfo, EUsrmCmdCode.MODIFY_SERVICES, load);
		m_UsrmChannel.sendCmdToMaster(cmd);
	}

	/* ================================================================= */
	/* ==================== Interface : IUrsmClient ==================== */
	/* ================================================================= */

	/* ========== IUrsmClient : updateSvrRouteTable ========== */
	public void updateSvrRouteTable(MsbRouteBulletin svrBulletin) {
		for (MsbRouteItem routeItem : svrBulletin) {
			m_RouteMap.addRouteItem(routeItem);
		}
	}

	/* ========== IUrsmClient : getProxyID ========== */
	public String getProxyID(MsbUrn urn, String verRequire) {
		String tmpRet = null;
		MsbRouteItem routeItem = m_RouteMap.searchRouteItem(urn);
		if (routeItem != null) {
			tmpRet = routeItem.getProxyID();
		}
		return tmpRet;
	}

	/* ====================================================== */
	/* ==================== Event Handler =================== */
	/* ====================================================== */

	private IUsrmCmdListener m_onRequestSvcDeclare = new IUsrmCmdListener() {
		@Override
		public EUsrmCmdCode getInterestCommand() {
			return EUsrmCmdCode.REQUEST_SVC_DECLARE;
		}

		@Override
		public void onReceiveUsrmCmd(UsrmCommand cmd) {
			MsbUrn[] mssingUrns = (MsbUrn[]) cmd.getLoad();
			for (MsbUrn urn : mssingUrns) {
				g_Logger.info("Missing URN :" + urn.toString());
			}
			ServiceEntry[] tmpSvcEntryList = m_MsbSvcMngr.getSvcEntryList();

			UsrmCommand cmdReply = new UsrmCommand(m_UsrmNodeInfo, EUsrmCmdCode.REPLY_REQUEST_SVC_DECLARE, tmpSvcEntryList);
			m_UsrmChannel.sendCmdToMaster(cmdReply);
		}
	};

	/* ========== Handler : m_UsrmCmdListener ========== */
	private IUsrmCmdListener m_onReplyModifyServices = new IUsrmCmdListener() {

		@Override
		public EUsrmCmdCode getInterestCommand() {
			return EUsrmCmdCode.REPLY_MODIFY_SERVICES;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onReceiveUsrmCmd(UsrmCommand cmd) {
			ArrayList<MsbUrn> confirmedURN = (ArrayList<MsbUrn>) cmd.getLoad();
			for (MsbUrn msbUrn : confirmedURN) {
				m_SvcBulletinDirty.clearDirty(msbUrn);
				ServiceEntry svcEntry = m_MsbSvcMngr.getServiceEntry(msbUrn);
				if (svcEntry != null) {
					svcEntry.setModifyType(ServiceEntry.EModifyType.SERVICE_VALID);
					g_Logger.debug("[" + m_MsbuInfo.getName() + "] Confirm Service Global Register :" + msbUrn);
				}
			}
		}
	};

	/* ========== Handler : m_BulletinListener ========== */
	private IUsrmCmdListener m_onBroadcastBulletin = new IUsrmCmdListener() {
		@Override
		public EUsrmCmdCode getInterestCommand() {
			return EUsrmCmdCode.BROADCAST_SERVICES;
		}

		@Override
		public void onReceiveUsrmCmd(UsrmCommand cmd) {

		}

	};

	/* =================================================================== */
	/* ==================== Service PRovider Commands ==================== */
	/* =================================================================== */

	/* ========== ========== */
	/**
	 * Update the services life cycle state.
	 */
	private final void sendHeartBeat() {
		UsrmCommand cmd = new UsrmCommand(m_UsrmNodeInfo, EUsrmCmdCode.HEART_BEAT, m_MsbSvcMngr.getServiceMsburnList());
		m_UsrmChannel.sendCmdToMaster(cmd);
	}

	/* ========================================================== */
	/* ==================== HeartBeat Thread ==================== */
	/* ========================================================== */
	private Runnable m_HearbeatThread = new Runnable() {

		@Override
		public void run() {
			while (true) {
				/* ===== STEP 1. Interval ===== */
				try {
					Thread.sleep(m_HeartBeatInterval);
					/* Update Modified Services */

					/* ===== STEP 2. SendHeart Beat ===== */

					if (m_SvcBulletinDirty.size() > 0) {
						commitSvcModification();
					} else {
						sendHeartBeat();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	};

	/* ================================================================== */
	/* ==================== Export Service Management ==================== */
	/* ================================================================== */
	public boolean bindService(ServiceEntry svcEntry) {
		g_Logger.debug(m_UsrmNodeInfo.getName() + " Bind Service : " + svcEntry.msbUrn);

		/* ===== STEP 1. Bind Service to Service ===== */
		m_MsbSvcMngr.bindService(svcEntry);
		/* ===== STEP 2. Update RouteMap ==== */
		MsbRouteItem routeItem = new MsbRouteItem(svcEntry.msbUrn, UsrmSvcPublisher.this.m_UsrmNodeInfo.getProxyId());
		m_RouteMap.addRouteItem(routeItem);
		/* ===== STEP 3. Update Dirty Map ===== */
		m_SvcBulletinDirty.addService(svcEntry);
		return true;
	}

	/**
	 * 
	 * @param msbUrn
	 * @return
	 */
	public boolean unbindService(MsbUrn msbUrn) {
		g_Logger.debug(m_UsrmNodeInfo.getName() + " Unbind Service : " + msbUrn);

		ServiceEntry svcEntry = this.m_MsbSvcMngr.getServiceEntry(msbUrn);
		/* Remove From Standard Service Manager */
		m_MsbSvcMngr.unbindService(msbUrn);
		m_RouteMap.removeRouteItem(msbUrn);
		/* Put Service Entry to Dirty Bulletin */
		m_SvcBulletinDirty.deleteService(svcEntry);
		this.commitSvcModification();
		/* Check & Return */
		return true;
	}

	/**
	 * Synchronize Services with Usrm Master.
	 * 
	 * @return
	 */
	public void commitSvcModification() {

		if (m_SvcBulletinDirty.size() > 0) {
			UsrmCommand cmd = new UsrmCommand(m_UsrmNodeInfo, EUsrmCmdCode.MODIFY_SERVICES, m_SvcBulletinDirty);
			this.m_UsrmChannel.sendCmdToMaster(cmd);
		}
	}
}
