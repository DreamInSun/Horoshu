package ctop.v3.msb.msbu;

import javax.jms.Message;
import org.apache.log4j.Logger;
import org.jdom.Element;

import com.hztech.util.ComInput;
import com.hztech.util.ComOutput;

import ctop.v3.msb.MSB;
import ctop.v3.msb.common.exception.MsbException;
import ctop.v3.msb.common.urn.MsbDomain;
import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.managment.UsrmManager;
import ctop.v3.msb.msbu.config.IAccessCtrlConfigProvider;
import ctop.v3.msb.msbu.data.MsbuInfo;
import ctop.v3.msb.MSB.*;
import ctop.v3.msb.msbu.mngr.access.AccessCtrlManagerMixMap;
import ctop.v3.msb.msbu.mngr.access.IAccessCtrlManager;

import ctop.v3.msb.port.IPlatformPort;
import ctop.v3.msb.proxy.MsbProxy;
import ctop.v3.msb.proxy.data.ServiceEntry;

import ctop.v3.msb.usrm.*;

import ctop.v3.msb.usrm.data.MsbRouteItem;
import ctop.v3.msb.usrm.data.SvcDescriptor;

/**
 * 
 * @author DreamInSun
 * 
 */
public class Msbu {
	private final static Logger g_Logger = Logger.getLogger(Msbu.class);

	/* ========== Constants ========== */
	/** Type of MSB Unit */
	public enum EType {
		/** Connect MSB as a service caller : MsbProxy + UsrmSvcRouter */
		CALLER,
		/** Connect MSB as a service provider : MsbProxy + UsrmSvcRouter + UsrmSvcKeeper */
		PROVIDER,
		/** Connect MSB as a service manager : MsbProxy + UsrmSvcRouter + UsrmSvcKeeper + UsrmSvcMaster */
		MANAGER,
		/** Connect MSB as a bridge */
		BRIDGE,
	}

	/* ========== Properties ========== */

	/* ===== MSBU Component ===== */
	/** For Communication */
	private MsbProxy m_MsbProxy = null;
	/** Cross Domain Control */
	private IAccessCtrlManager m_AcMngr = null;
	/** USRM Role */
	private UsrmNodeMaintain m_UsrmSvcBase = null;
	private UsrmSvcRouter m_UsrmSvcRouter = null;
	private UsrmSvcPublisher m_UsrmSvcPublisher = null;
	private UsrmSvcMaster m_UsrmSvcMaster = null;
	/* Platform Port */
	private IPlatformPort m_PlatformPort = null;
	/** Management Class */
	private UsrmManager m_UsrmMngr = null;

	/* ========================================================= */
	/* ==================== Setter & Getter ==================== */
	/* ========================================================= */
	public MsbuInfo getMsbuInfo() {
		return this.m_MsbProxy.getMsbuInfo();
	}

	public MsbProxy getProxy() {
		return this.m_MsbProxy;
	}

	/* ========== Constructor ========== */
	/**
	 * Echo Domain will create one MSBU
	 * 
	 * @param msbuInfo
	 */
	public Msbu(MsbuInfo msbuInfo) {

		/* ===== STEP 1. Parse MSBU Info ===== */
		/* Print MSBU Configuration Informations */
		if (msbuInfo != null) {
			g_Logger.info(msbuInfo.toString());
		}

		/* ===== STEP 2. Create MSB proxy for communication ===== */
		// TODO Waiting for ActiveMQ Started. or deal with requirement later
		m_MsbProxy = MsbProxy.newInstance(msbuInfo);

		/* ===== STEP 3. Access Control Manager ===== */
		m_AcMngr = m_MsbProxy.getAccessCtrlManager();

		/* ===== STEP 4. Create USRM Services ===== */

		/**
		 * UsrmSvcBase Should be Initialized before other module for get configurations
		 */
		m_UsrmSvcBase = new UsrmNodeMaintain(m_MsbProxy);

		if (m_UsrmSvcBase.isNodePrepared()) {
			if (msbuInfo.isManagerEnable) {
				m_UsrmSvcMaster = new UsrmSvcMaster(m_MsbProxy);
				m_UsrmMngr = new UsrmManager(m_UsrmSvcMaster);
				/* Central Control Configurations */
				//TODO
				IAccessCtrlConfigProvider acConfigProvider = MSB.getMsbConfigProvider().getAccessControlProvicer();
				m_UsrmSvcMaster.setAccessCtrlConfigProvider(acConfigProvider);
			}
			if (msbuInfo.isPublisherEnable) {
				m_UsrmSvcPublisher = new UsrmSvcPublisher(m_MsbProxy);
			}
			if (msbuInfo.isRouterEnable) {
				m_UsrmSvcRouter = new UsrmSvcRouter(m_MsbProxy);
			}
		}

		/*===== STEP 4. Establish MSB Bridge =====*/
		//MsbBridgeMap.
	}

	/* ========================================================== */
	/* ==================== Bridge Functions ==================== */
	/* ========================================================== */

	/**
	 * For MSB Bridge to inject bridge MSBU.
	 * 
	 * @param bridgeMsbu
	 *            the destination MSBU.
	 */
	public void addBridgeMsbu(Msbu bridgeMsbu) {
		m_MsbProxy.addBridgeMsbu(bridgeMsbu);
		m_UsrmSvcBase.updateUsrmNode();
	}

	/**
	 * For MSB Bridge to obtain the destination MSBU.
	 * 
	 * @param domain
	 * @return
	 */
	public Msbu getBridgeMsbu(String domain) {
		return m_MsbProxy.obtainBridgeMsbu(domain);
	}

	/**
	 * Check Cross Domain Permission, when one Bridge Proxy Try to obtain the other domain MSBU, this function should be
	 * invoked to determine the destination MSBURN is open to this domain.
	 * 
	 * @return
	 */
	public boolean checkCrossDomainPermission(MsbUrn msbUrn, String callerDomain) {
		boolean tmpRet = false;
		/*===== STEP 1. Check Parent Domain =====*/
		/*
		MsbDomain domainSelf = new MsbDomain(this.m_MsbProxy.getMsbuInfo().getDomain());
		MsbDomain domainCaller = new MsbDomain(callerDomain);
		if (domainSelf.isChildDomainOf(domainCaller)) {
			g_Logger.debug("Caller From " + domainCaller + " is parent domain of local domain " + domainSelf);
			return true;
		}  
		*/
		/*===== STEP 2. if is not parent domain =====*/
		if (m_AcMngr != null) {
			tmpRet = m_AcMngr.isPermit(msbUrn, callerDomain);
		}
		return tmpRet;
	}

	/* ================================================================== */
	/* ==================== USRM Services Management ==================== */
	/* ================================================================== */

	/* =========== USRM Services Initialization ========== */
	/** Assistant Function for Services Binding */
	private final MsbUrn getLocalMsbUrn(String svcUrn) {
		return new MsbUrn(this.m_MsbProxy.getMsbuInfo().getDomain(), null, svcUrn, null);
	}

	/** */
	public void bindPlatformService(String svcUrn, SvcDescriptor desc) throws MsbException {
		if (m_UsrmSvcPublisher != null) {
			MsbUrn msbUrn = getLocalMsbUrn(svcUrn);
			/* Fill SceDescription with MsbUrn */
			if (desc != null) {
				desc.setSvcType(SvcDescriptor.SVC_TYPE_PLATFORM);
			}
			ServiceEntry svcEntry = ServiceEntry.createPlatformSvcEntry(msbUrn, desc);
			m_UsrmSvcPublisher.bindService(svcEntry);
		} else {
			throw new MsbException("This MSBU is not an Service Provider.");
		}
	}

	/** */
	public void bindRpcService(String svcUrn, SvcDescriptor desc, IMsbRpcHandler handler) throws MsbException {
		if (m_UsrmSvcPublisher != null) {
			MsbUrn msbUrn = getLocalMsbUrn(svcUrn);
			/* Fill SceDescription with MsbUrn */
			if (desc != null) {
				desc.setSvcType(SvcDescriptor.SVC_TYPE_RPC);
			}
			/* Create Service Entry and Add it to Map */
			ServiceEntry svcEntry = ServiceEntry.createRpcSvcEntry(msbUrn, handler, desc);
			m_UsrmSvcPublisher.bindService(svcEntry);
		} else {
			throw new MsbException("This MSBU is not an Service Provider.");
		}
	}

	/** */
	public void bindMsgService(String svcUrn, SvcDescriptor desc, IMsbMsgHandler handler) throws MsbException {
		if (m_UsrmSvcPublisher != null) {
			MsbUrn msbUrn = getLocalMsbUrn(svcUrn);
			/* Fill SceDescription with MsbUrn */
			if (desc != null) {
				desc.setSvcType(SvcDescriptor.SVC_TYPE_MSG);
			}
			ServiceEntry svcEntry = ServiceEntry.createMsgSvcEntry(msbUrn, handler, desc);
			m_UsrmSvcPublisher.bindService(svcEntry);
		} else {
			throw new MsbException("This MSBU is not an Service Provider.");
		}
	}

	/** */
	public void unbindService(String svcUrn) {
		if (m_UsrmSvcPublisher != null) {
			MsbUrn msbUrn = getLocalMsbUrn(svcUrn);
			m_UsrmSvcPublisher.unbindService(msbUrn);
		}
	}

	/**
	 * Confirm Services Changes
	 */
	public void commitServicesChange() {
		m_UsrmSvcPublisher.commitSvcModification();
	}

	/* ====================================================================== */
	/* ==================== MSB Services Access Entrance ==================== */
	/* ====================================================================== */

	/* ========== MSBSAE : Service RPC ========== */
	public void invokeMethodAsync(MsbUrn msbUrn, ComInput ci, IMsbRpcCallback callback) throws MsbException {
		if (isLocalDomain(msbUrn)) {
			MsbRouteItem routeItem = m_UsrmSvcRouter.findServiceRoute(msbUrn);
			if (routeItem != null) {
				m_MsbProxy.invokeRemoteAsync(routeItem, ci, callback);
			} else {
				g_Logger.error("invokeMethodSync ==> Cannot Find RouteItem ��\"" + msbUrn + "\" in [" + this.m_MsbProxy.getMsbuInfo().getName() + "].");
			}
		} else {
			/* Invoke Service via Cross Domain Bridge */
			if (this.m_MsbProxy.getMsbuInfo().isBridgeEnable == true) {
				Msbu destDomainMsbu = this.getCrossDomainMsbu(msbUrn);
				if (destDomainMsbu != null) {
					destDomainMsbu.invokeMethodAsync(msbUrn, ci, callback);
				}
			} else {
				/* Request Bridge MSBU from Master */
				g_Logger.error("invokeMethodSync : \"" + msbUrn + "\" cannot find suitable domain.");
			}
		}
	}

	/* ========== MSBSAE : invokeMethodSync ========== */
	/**
	 * Not thread Safe, to be modified.
	 */
	public ComOutput invokeMethodSync(MsbUrn msbUrn, ComInput ci) throws MsbException {
		ComOutput tmpOutput = null;
		/* Get RouteItem */
		MsbRouteItem routeItem = null;
		if (m_UsrmSvcRouter != null) {
			routeItem = m_UsrmSvcRouter.findServiceRoute(msbUrn);
		} else {
			throw new MsbException(MsbException.ErrCode.USRM_ROUTE_ERROR, this.m_MsbProxy.getMsbuInfo().getName() + " USRM Service Router is Has Not Initilized.");
		}
		/* Invoke */
		if (routeItem != null) {
			if (isLocalMsbu(routeItem)) {
				if (isLocalDomain(msbUrn)) {
					/* Invoke Service local MSBU */
					tmpOutput = m_MsbProxy.invokeRemoteSync(routeItem, ci);
				} else {
					/* Invoke Service local Cross Domain Bridge */
					Msbu destDomainMsbu = this.getCrossDomainMsbu(msbUrn);
					if (destDomainMsbu != null) {
						tmpOutput = destDomainMsbu.invokeMethodSync(msbUrn, ci);
					} else {
						throw new MsbException(MsbException.ErrCode.BRIDGE_MSBU_NOT_FOUND, "invokeMethodSync : \"" + msbUrn + "\" cannot find suitable Bridge MSBU.");
					}
				}
			} else {
				tmpOutput = m_MsbProxy.invokeRemoteSync(routeItem, ci);
			}
		} else {
			throw new MsbException(MsbException.ErrCode.USRM_ROUTE_ERROR, "invokeMethodSync ==> Cannot Find RouteItem ��\"" + msbUrn + "\" in [" + this.m_MsbProxy.getMsbuInfo().getName() + "].");
		}

		return tmpOutput;
	}

	/* ========== MSBSAE : invokePlatformMethodAsync ========== */
	/**
	 * 
	 * @param msbUrn
	 * @param ci
	 * @param callback
	 * @throws MsbException
	 */
	public void invokePlatformMethodAsync(MsbUrn msbUrn, ComInput ci, IMsbRpcCallback callback) throws MsbException {
		if (isLocalDomain(msbUrn)) {
			MsbRouteItem routeItem = m_UsrmSvcRouter.findServiceRoute(msbUrn);
			if (routeItem != null) {
				m_MsbProxy.invokeRemoteAsyncV3(routeItem, ci, callback);
			} else {
				g_Logger.error("invokeMethodSync ==> Cannot Find RouteItem ��\"" + msbUrn + "\" in [" + this.m_MsbProxy.getMsbuInfo().getName() + "].");
			}
		} else {
			/* Invoke Service via Cross Domain Bridge */
			Msbu destDomainMsbu = this.getCrossDomainMsbu(msbUrn);
			if (destDomainMsbu != null) {
				destDomainMsbu.invokePlatformMethodAsync(msbUrn, ci, callback);
			} else {
				g_Logger.error("invokeMethodSync : \"" + msbUrn + "\" cannot find suitable domain.");
			}
		}
	}

	/* ========== MSBSAE : invokePlatformMethodSync ========== */
	/**
	 * Not thread Safe, to be modified.
	 */
	public ComOutput invokePlatformMethodSync(MsbUrn msbUrn, ComInput ci) throws MsbException {
		ComOutput tmpOutput = null;
		if (isLocalDomain(msbUrn)) {
			MsbRouteItem routeItem = m_UsrmSvcRouter.findServiceRoute(msbUrn);
			if (routeItem != null) {
				tmpOutput = m_MsbProxy.invokeRemoteSyncV3(routeItem, ci);
			} else {
				g_Logger.error("invokeMethodSync ==> Cannot Find RouteItem ��\"" + msbUrn + "\" in [" + this.m_MsbProxy.getMsbuInfo().getName() + "].");
			}
		} else {
			/* Invoke Service via Cross Domain Bridge */
			Msbu destDomainMsbu = this.getCrossDomainMsbu(msbUrn);
			if (destDomainMsbu != null) {
				tmpOutput = destDomainMsbu.invokePlatformMethodSync(msbUrn, ci);
			} else {
				g_Logger.error("invokeMethodSync : \"" + msbUrn + "\" cannot find suitable domain.");
			}
		}
		return tmpOutput;
	}

	/* ========== MSBSAE : Message Service ========== */
	public boolean sendMessage(MsbUrn msbUrn, Message msg) {
		return false;
	}

	/* ============================================================ */
	/* ==================== MSBU Role Judgment ==================== */
	/* ============================================================ */
	public boolean isSvcManager() {
		return (m_UsrmSvcMaster != null);
	}

	public boolean isSvcProvider() {
		return (m_UsrmSvcPublisher != null);
	}

	public boolean isSvcBridge() {
		return this.getProxy().isBridgeEnable();
	}

	/* ===================================================================== */
	/* ==================== MSBU Cross Domain Functions ==================== */
	/* ===================================================================== */
	/**
	 * 
	 * @param msburn
	 * @return
	 */
	private final boolean isLocalDomain(MsbUrn msburn) {
		// TODO Maybe NULL Point
		return msburn.domain.equals(this.m_MsbProxy.getMsbuInfo().getDomain());
	}

	/**
	 * Check if the RouteItem point to an local domain Proxy.
	 * 
	 * @param routeItem
	 * @return
	 */
	private final boolean isLocalMsbu(MsbRouteItem routeItem) {
		boolean tmpRet = false;
		if (routeItem != null) {
			tmpRet = routeItem.getProxyID().equals(m_MsbProxy.getMsbuInfo().getProxyID());
		}
		return tmpRet;
	}

	/**
	 * Find the Most Match Domain Bridge MSBU, and return it; <br />
	 * Permission Check will be performed in MsbProxy getCrossDomainMsbu();
	 * 
	 * @param routeItem
	 * @return
	 */
	public Msbu getCrossDomainMsbu(MsbUrn msbUrn) throws MsbException {
		return m_MsbProxy.getCrossDomainMsbu(msbUrn);
	}

	/* ============================================================ */
	/* ==================== MSBU Role Judgment ==================== */
	/* ============================================================ */

	/* ========== toString ========== */
	@Override
	public String toString() {
		return super.toString();
	}

	/* =================================================================== */
	/* ==================== MSBU Management Interface ==================== */
	/* =================================================================== */
	/**
	 * Get the USRM Manager Interface to perform USRM Management via Commands.
	 * 
	 * @return
	 */
	public UsrmManager getUsrmMngr() {
		return m_UsrmMngr;
	}

	/* =============================================================== */
	/* ==================== CTOP V3 Platform Port ==================== */
	/* =============================================================== */
	public void setPlatformPort(IPlatformPort platformPort) {
		m_PlatformPort = platformPort;
		this.m_MsbProxy.setPlatformPort(platformPort);
		/* Auto Update */
		updatePlatformServices();
	}

	public void updatePlatformServices() {
		if (this.isSvcProvider()) {
			if (m_PlatformPort != null) {
				String[] svcList = m_PlatformPort.getServiceList();
				if (svcList != null) {
					for (String svcName : svcList) {
						Element emltDesc = m_PlatformPort.getServiceDesc(svcName, null);
						SvcDescriptor desc = new SvcDescriptor(emltDesc);
						try {
							this.bindPlatformService(svcName, desc);
						} catch (MsbException ignore) {
							/* Ignore the never happen Exception */
						}
					}
				}
			}
		}
	}
}
