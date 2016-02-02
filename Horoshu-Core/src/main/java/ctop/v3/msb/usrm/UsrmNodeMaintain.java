package ctop.v3.msb.usrm;

import org.apache.log4j.Logger;

import ctop.v3.msb.msbu.mngr.access.AccessCtrlBrief;
import ctop.v3.msb.msbu.mngr.access.IAccessCtrlManager;
import ctop.v3.msb.proxy.MsbProxy;
import ctop.v3.msb.usrm.constants.EUsrmCmdCode;
import ctop.v3.msb.usrm.data.UsrmCommand;
import ctop.v3.msb.usrm.data.UsrmNodeInfo;
import ctop.v3.msb.usrm.interfaces.IUsrmCmdListener;
import ctop.v3.msb.usrm.node.UsrmNode;

/**
 * 
 * @author DreamInSun
 * 
 */
public class UsrmNodeMaintain extends UsrmNode {
	private static final Logger g_Logger = Logger.getLogger(UsrmNodeMaintain.class);

	/* ========== Properties ========== */
	/* Assistant */
	private static Object m_PendingObj = null;

	/* ========== Properties ========== */
	/**  */
	private IAccessCtrlManager m_AcMngr;

	/** */
	//private 

	/* ========== Constructor ========== */
	/**
	 * 
	 * @param chn
	 */
	public UsrmNodeMaintain(MsbProxy msbProxy) {
		/* UsrmSlaver */
		super(msbProxy);
		g_Logger.debug("Initializing UsrmSvcBase.");
		m_AcMngr = msbProxy.getAccessCtrlManager();
		/* Register USRM Command Listener */
		m_UsrmChannel.registerCmdListener(m_onReplyDeclearUsrmNode);
		m_UsrmChannel.registerCmdListener(m_onDispatchAccessCtr);
		/* if Node is node not LEGAL */
		if (m_UsrmNodeInfo.isMaster) {
			g_Logger.debug(this.m_MsbuInfo.name + " is an Manager Node, it can be initialized directly.");
			m_UsrmNodeInfo.setState(UsrmNodeInfo.EState.LEGAL);
			this.m_UsrmMasterInfo = m_UsrmNodeInfo;
		} else {
			declareUsrmNode();
		}
	}

	/* ========== Ability : declareUsrmNode ========== */
	public void declareUsrmNode() {

		if (m_UsrmNodeInfo.getState() != UsrmNodeInfo.EState.LEGAL) {
			try {
				/* ===== Wait ===== */
				while (m_PendingObj != null) {
					synchronized (m_PendingObj) {
						m_PendingObj.wait(m_MsbuInfo.NodeDeclearTimeout);
					}
				}
				/* ===== Block Other Declaration ===== */
				if (m_PendingObj == null) {
					m_PendingObj = this;
				}
				/* ===== STEP 2. Try to Declare Again ===== */
				synchronized (m_PendingObj) {
					while (m_UsrmNodeInfo.getState() != UsrmNodeInfo.EState.LEGAL && m_UsrmNodeInfo.getState() != UsrmNodeInfo.EState.ILLEGAL) {
						//g_Logger.debug("Current Node Information : \r\n" + m_UsrmNodeInfo);
						/* Set USRM Node State to INITAILIZING */
						m_UsrmNodeInfo.setState(UsrmNodeInfo.EState.INITIALIZING);
						/* Generate Declare Command */
						UsrmCommand cmd = new UsrmCommand(m_UsrmNodeInfo, EUsrmCmdCode.DECLEAR_USRM_NODE, null);
						m_UsrmChannel.sendCmdToMaster(cmd);
						/* Wait For Declaration Confirm */
						m_PendingObj.wait(m_MsbuInfo.NodeDeclearTimeout);
						g_Logger.debug("Initializing USRM Node Base : Suspend USRM Node [" + m_PendingObj + "].");

					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/* ========== Ability : updateUsrmNode ========== */
	public void updateUsrmNode() {
		if (m_UsrmNodeInfo.getState() == UsrmNodeInfo.EState.LEGAL) {
			m_UsrmNodeInfo.updateFrom(m_MsbuInfo);
			/* Generate Declare Command */
			UsrmCommand cmd = new UsrmCommand(m_UsrmNodeInfo, EUsrmCmdCode.DECLEAR_USRM_NODE, null);
			m_UsrmChannel.sendCmdToMaster(cmd);
		}
	}

	/* =============================================================== */
	/* ==================== USRM Command Handlers ==================== */
	/* =============================================================== */

	/* ========= USRM Command Handler : REPLY_DECLEAR_USRMNODE ========== */
	protected IUsrmCmdListener m_onReplyDeclearUsrmNode = new IUsrmCmdListener() {
		@Override
		public EUsrmCmdCode getInterestCommand() {
			return EUsrmCmdCode.REPLY_DECLEAR_USRM_NODE;
		}

		@Override
		public void onReceiveUsrmCmd(UsrmCommand cmd) {

			/* Get Reply Command with Authorized UsrmNodeInfo */
			UsrmNodeInfo replyNodeInfo = (UsrmNodeInfo) cmd.getLoad();
			UsrmNodeInfo selfNodeInfo = m_UsrmNodeInfo;
			g_Logger.debug("Reply Node Information : \r\n" + replyNodeInfo + "\r\n From Master " + cmd.getSender().getName());
			/* Remember Master Informations */
			m_UsrmMasterInfo = cmd.getSender();
			/* Update USRM Info */
			if (selfNodeInfo.getName().equals(replyNodeInfo.getName())) {
				// TODO more management control here
				m_UsrmNodeInfo = replyNodeInfo;
			}
			/* Weak Self */
			if (m_PendingObj != null) {
				synchronized (m_PendingObj) {
					m_PendingObj.notify();
					g_Logger.debug("Weak USRM Node : " + m_PendingObj);
				}
				m_PendingObj = null;
			}
			return;
		}
	};

	/* ========= USRM Command Handler : REPLY_DECLEAR_USRMNODE ========== */
	protected IUsrmCmdListener m_onDispatchAccessCtr = new IUsrmCmdListener() {
		@Override
		public EUsrmCmdCode getInterestCommand() {
			return EUsrmCmdCode.DISPATCH_ACCESS_CONTROL;
		}

		@Override
		public void onReceiveUsrmCmd(UsrmCommand cmd) {
			/* Get Reply Command with Authorized UsrmNodeInfo */
			AccessCtrlBrief acDomainBreif = (AccessCtrlBrief) cmd.getLoad();
			g_Logger.info(acDomainBreif.toString());
			m_AcMngr.updateAccessDomainBrief(acDomainBreif);
			/* Reply */
		}
	};

	/* ========== ========== */
	public boolean isNodePrepared() {
		if (this.m_UsrmNodeInfo.getState() == UsrmNodeInfo.EState.LEGAL) {
			return true;
		} else {
			return false;
		}

	}
}
