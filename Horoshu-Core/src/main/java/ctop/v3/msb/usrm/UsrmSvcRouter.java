package ctop.v3.msb.usrm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.proxy.MsbProxy;
import ctop.v3.msb.usrm.constants.EUsrmCmdCode;
import ctop.v3.msb.usrm.data.MsbRouteItem;
import ctop.v3.msb.usrm.data.UsrmCommand;
import ctop.v3.msb.usrm.interfaces.IUsrmCmdListener;
import ctop.v3.msb.usrm.node.UsrmNode;

/**
 * Usrm Service Router : <br />
 * 1. Discover Service Route; ( DISCOVER_ROUTE & REPLY_DISCOVER_ROUTE ) <br />
 * 
 * @see UsrmNode
 * 
 * @author DreamInSun
 * 
 */
public class UsrmSvcRouter extends UsrmNode {
	private static final Logger g_Logger = Logger.getLogger(UsrmSvcRouter.class);
	/* ========== Constant ========== */
	/** in Millisecond */
	private static final int TIMEOUT_DISCOVER_ROUTE = 3000;
	/* ========== Properties ========== */
	public Map<MsbUrn, List<Object>> m_WaitRouteMap = new LinkedHashMap<MsbUrn, List<Object>>();

	/* ========== Constructor ========== */
	public UsrmSvcRouter(MsbProxy msbProxy) {
		/* UsrmSlaver */
		super(msbProxy);
		g_Logger.debug("Initializing UsrmSvcRouter.");
		/* Register Command Listeners */
		m_UsrmChannel.registerCmdListener(m_onReplyDiscoverServices);

	}

	/* ========== Ability : dicoverServices ========== */
	public void dicoverServices(MsbUrn msbUrn) {
		UsrmCommand cmd = new UsrmCommand(UsrmSvcRouter.this.m_UsrmNodeInfo, EUsrmCmdCode.DISCOVER_ROUTE, msbUrn);
		m_UsrmChannel.sendCmdToMaster(cmd);
	}

	/* =============================================================== */
	/* ==================== USRM Command Handlers ==================== */
	/* =============================================================== */

	/* ========= USRM Command Handler : REPLY_DISCOVER_ROUTE ========== */
	IUsrmCmdListener m_onReplyDiscoverServices = new IUsrmCmdListener() {
		@Override
		public EUsrmCmdCode getInterestCommand() {
			return EUsrmCmdCode.REPLY_DISCOVER_ROUTE;
		}

		@Override
		public void onReceiveUsrmCmd(UsrmCommand cmd) {
			MsbRouteItem routeItem = (MsbRouteItem) cmd.getLoad();
			if (routeItem != null) {
				/* Print Route Item */
				g_Logger.debug("Receive Reply Discover Route :{" + routeItem.msbUrn + "} on [" + routeItem.getProxyID() + "]");
				/* Add Route Item */
				UsrmSvcRouter.this.m_RouteMap.addRouteItem(routeItem);
				/* Get the Threads Pending for the Same MSBURN Route */
				List<Object> threadObjList = m_WaitRouteMap.get(routeItem.msbUrn);
				if (threadObjList != null) {
					for (Object obj : threadObjList) {
						synchronized (obj) {
							obj.notify();
						}
					}
					/* Clear Pending Route Object List */
					m_WaitRouteMap.remove(routeItem.msbUrn);
				}
			}
		}
	};

	/* ========================================================== */
	/* ==================== Export Functions ==================== */
	/* ========================================================== */
	/**
	 * 
	 * @param msbUrn
	 * @return
	 */
	public MsbRouteItem findServiceRoute(MsbUrn msbUrn) {
		MsbRouteItem tmpRouteItem = m_RouteMap.searchRouteItem(msbUrn);
		if (tmpRouteItem == null) {
			/* Send Discover Command */
			this.dicoverServices(msbUrn);
			/* Get the RouteItem Again */
			tmpRouteItem = m_RouteMap.searchRouteItem(msbUrn);
			if (tmpRouteItem == null) { // To Fast to Sleep;
				try {
					/* Get the Threads Pending for the Same MSBURN Route */
					List<Object> threadObjList = m_WaitRouteMap.get(msbUrn);
					if (threadObjList == null) {
						threadObjList = new ArrayList<Object>();
						m_WaitRouteMap.put(msbUrn, threadObjList);
					}
					/* Add Self Pending for the Same MSBURN */
					threadObjList.add(this);
					synchronized (this) {
						g_Logger.debug("Router Sleep" + this);
						this.wait(TIMEOUT_DISCOVER_ROUTE);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				/* Get the RouteItem Again */
				tmpRouteItem = m_RouteMap.searchRouteItem(msbUrn);
			}

		}
		return tmpRouteItem;
	}

}
