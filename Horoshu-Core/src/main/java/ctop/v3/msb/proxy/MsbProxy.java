package ctop.v3.msb.proxy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import com.hztech.util.ComInput;
import com.hztech.util.ComOutput;
import com.hztech.util.RSUtil;

import ctop.v3.msb.MSB.IMsbRpcCallback;
import ctop.v3.msb.common.exception.MsbException;
import ctop.v3.msb.common.urn.MsbDomain;
import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.msbu.Msbu;
import ctop.v3.msb.msbu.config.IMsbConfigProvider;
import ctop.v3.msb.msbu.data.MsbuInfo;
import ctop.v3.msb.msbu.mngr.access.AccessCtrlManagerMixMap;
import ctop.v3.msb.msbu.mngr.access.IAccessCtrlManager;
import ctop.v3.msb.msbu.mngr.callback.MsbCallbackManager;

import ctop.v3.msb.port.IPlatformPort;
import ctop.v3.msb.proxy.base.*;
import ctop.v3.msb.proxy.conn.MsbConnParam;
import ctop.v3.msb.proxy.constants.*;
import ctop.v3.msb.proxy.data.ServiceEntry;
import ctop.v3.msb.proxy.mngr.IMsbuSvcManager;
import ctop.v3.msb.proxy.mngr.MsbSvcManager;
import ctop.v3.msb.proxy.routemap.IRouteMap;
import ctop.v3.msb.proxy.routemap.RouteMapHash;

import ctop.v3.msb.usrm.channel.UsrmChannel;
import ctop.v3.msb.usrm.data.MsbRouteItem;

/**
 * MsbProxy is the main entrance of the distribute service communication framework, providing multiple type of
 * 
 * @author DreamInSun
 * 
 */
public class MsbProxy {
	private static Logger g_Logger = Logger.getLogger(MsbProxy.class);

	/*========== Constant ==========*/
	protected static String CONSUMER_MSG_RECEIVER = "MsgReceiver";

	// TODO : timeout will be load from configuration
	/** Timeout in RPC Synchronize mode,in millisecond */
	public static int TIMEOUT_RPC_SYNC = 30000;

	/** Timeout in RPC Asynchronize mode,in millisecond */
	public static int TIMEOUT_RPC_ASYNC = 60000;

	/*===============================================================*/
	/*====================== Singleton Factory ======================*/
	/*===============================================================*/

	/*========== Global variables ==========*/
	/** Each MSB Domain will have an MSB Proxy */
	protected static Map<String, MsbProxy> g_MsbProxyMap = new LinkedHashMap<String, MsbProxy>();

	/*========== Factory ==========*/
	/**
	 * Get Global Static Instance
	 * 
	 * @param proxyID
	 *            if is null, return the global proxy.
	 * @return
	 */
	public static MsbProxy getInstance(MsbuInfo msbuInfo) {
		MsbProxy tmpProxy = g_MsbProxyMap.get(msbuInfo.domain);
		if (tmpProxy == null) {
			tmpProxy = new MsbProxy(msbuInfo);
			g_MsbProxyMap.put(msbuInfo.domain, tmpProxy);
		}
		return tmpProxy;
	}

	/** Recommended Function */
	public static MsbProxy getInstance(IMsbConfigProvider configProvider) {
		MsbuInfo msbuInfo = configProvider.getMsbuInfo();
		return MsbProxy.getInstance(msbuInfo);
	}

	/**
	 * Only for Debug Uses
	 * 
	 * @param proxyID
	 * @param mqConnParam
	 * @return
	 */
	public static MsbProxy newInstance(MsbuInfo msbuInfo) {
		return new MsbProxy(msbuInfo);
	}

	/*===========================================================*/
	/*====================== MsbProxy Body ======================*/
	/*===========================================================*/

	/*========== Properties ==========*/
	/* attribute */
	private MsbuInfo m_MsbuInfo;
	/** RouteMap Sharing Every Proxy to USRM Feature */
	private IRouteMap m_RouteMap;
	/** USRM Communication Channel */
	private UsrmChannel m_UsrmChannel;
	/** Manager of Local MSB Service Registered in local MSBU */
	private MsbCallbackManager<String> m_CallbackMngr = new MsbCallbackManager<String>();
	/** */
	protected IMsbuSvcManager m_SvcHandlerMngr;
	/** Access Control Manager */
	private AccessCtrlManagerMixMap m_AcMngr;
	/** Bridge MSBU : Key Domain, Value MSBU */
	private Map<String, Msbu> m_BridgeMsbuMap = new HashMap<String, Msbu>();
	/* Callback */

	/* Platform Port */
	private IPlatformPort m_PlatformPort = null;

	/*===== JMS =====*/
	/* Self Destination Cache */
	private Destination m_SelfDestination;
	/* JMS Factory */
	private ConnectionFactory m_ConnectionFactory;
	private Connection m_Connection;
	private Session m_MqSession;
	/* JMS Producer */
	private Map<String, MessageProducer> m_ProducerMap;
	/* JMS Consumer */
	private Map<String, MessageConsumer> m_ConsumerMap;

	/*========== Constructor ==========*/
	private MsbProxy(MsbuInfo msbuInfo) {
		/*===== STEP 1. Load Params From Configuration  =====*/
		m_MsbuInfo = msbuInfo;
		m_SvcHandlerMngr = new MsbSvcManager();
		m_AcMngr = new AccessCtrlManagerMixMap();

		/*===== STEP 2. Input Protection =====*/
		MsbConnParam connectParam = m_MsbuInfo.getMsbConnParam();
		if (connectParam == null || msbuInfo.name == null)
			return;

		/*===== STEP 3. Remember Proxy ID =====*/
		this.connectMq();
	}

	public Connection connectMq() {
		MsbConnParam connectParam = m_MsbuInfo.getMsbConnParam();
		/* Create JMS Properties */
		try {
			m_ConnectionFactory = new ActiveMQConnectionFactory(connectParam.userName, connectParam.passWord, connectParam.connectWord);
			/* Start Connection */
			m_Connection = m_ConnectionFactory.createConnection();
			m_Connection.start();
			m_MqSession = m_Connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			/* Start Producer */
			m_ProducerMap = new WeakHashMap<String, MessageProducer>();
			/* Start Consumer */
			m_ConsumerMap = new WeakHashMap<String, MessageConsumer>();
			/* Setup Proxy Receiving Channel */
			setupConsumerChannel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return m_Connection;
	}

	/*========== Deconstructor ==========*/
	protected void finalize() {
		try {
			super.finalize();
			/* Close Producer */
			Iterator<Entry<String, MessageProducer>> itPro = m_ProducerMap.entrySet().iterator();
			while (itPro.hasNext()) {
				itPro.next().getValue().close();
			}
			/* Close Consumer */

			Iterator<Entry<String, MessageConsumer>> itCon = m_ConsumerMap.entrySet().iterator();
			while (itCon.hasNext()) {
				itCon.next().getValue().close();
			}
			/* Close Session */
			if (null != m_MqSession)
				m_MqSession.close();
			/* Close Connection */
			if (null != m_Connection)
				m_Connection.close();
		} catch (Throwable ignore) {
			/* Ignore Close MQ Connection Error */
		}
	}

	/*==========  ==========*/
	/**
	 * Get Services Manager for USRM Keeper to bind Services
	 * 
	 * @return
	 */
	public IMsbuSvcManager getSvcManager() {
		return m_SvcHandlerMngr;
	}

	/*========== Producer Cache Factory ==========*/
	/**
	 * 
	 * @param type
	 * @param regionName
	 *            region (Queue / Topic) Name
	 * @return
	 * @throws MsbException
	 * @throws JMSException
	 */
	private MessageProducer getProducer(EProducerType type, String regionName) throws MsbException, JMSException {
		MessageProducer tmpProducer = null;
		/* Load From Cache */
		if (m_ProducerMap.containsKey(regionName)) {
			tmpProducer = m_ProducerMap.get(regionName);
		} else {
			/* Session Protection */
			if (m_MqSession == null)
				throw new MsbException("Producer Session Not Exist");
			/* Create Destination */
			Destination dest = null;
			switch (type) {
			case PTP_TX:
				dest = m_MqSession.createQueue(regionName);
				break;
			case PNS_PUB:
				dest = m_MqSession.createTopic(regionName);
				break;
			case PTP_TX_TMP:
				dest = m_MqSession.createTemporaryQueue();
				break;
			case PNS_PUB_TMP:
				dest = m_MqSession.createTemporaryTopic();
				break;
			}
			/* Create Producer */
			if (dest != null) {
				tmpProducer = m_MqSession.createProducer(dest);
				/* Add to Cache */
				m_ProducerMap.put(regionName, tmpProducer);
			}
		}
		/* Return */
		return tmpProducer;
	}

	/*========== Consumer Cache Factory ==========*/
	/**
	 * 
	 * @param type
	 * @param regionName
	 *            if regionName is Self, will store it in <code>m_SlefDestination</code>
	 * @return
	 * @throws MsbProxyException
	 * @throws JMSException
	 */
	public MessageConsumer getConsumer(EConsumerType type, String regionName) throws MsbException, JMSException {
		MessageConsumer tmpConsumer = null;
		/* Load From Cache */
		if (m_ConsumerMap.containsKey(regionName)) {
			tmpConsumer = m_ConsumerMap.get(regionName);
		} else {
			if (m_MqSession == null)
				throw new MsbException("Producer Session Not Exist");
			/* Create Destination */
			Destination dest = null;
			switch (type) {
			case PTP_RX:
				dest = m_MqSession.createQueue(regionName);
				break;
			case PNS_SUB:
				dest = m_MqSession.createTopic(regionName);
				break;
			case PTP_RX_TMP:
				dest = m_MqSession.createTemporaryQueue();
				break;
			case PNS_SUB_TMP:
				dest = m_MqSession.createTemporaryTopic();
				break;
			}
			/* Store Self Destination */
			if (regionName.equals(m_MsbuInfo.getProxyID()))
				this.m_SelfDestination = dest;
			/* Create Producer */
			if (dest != null) {
				tmpConsumer = m_MqSession.createConsumer(dest);
				/* Add to Cache */
				m_ConsumerMap.put(regionName, tmpConsumer);
			}
		}
		/* Return */
		return tmpConsumer;
	}

	/*====================================================================*/
	/*==================== USRM Communication Channel ====================*/
	/*====================================================================*/
	public Connection getMqConnection() {
		return this.m_Connection;
	}

	public UsrmChannel getUsrmChannel() {
		if (m_UsrmChannel == null) {
			try {
				m_UsrmChannel = new UsrmChannel(this);
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		return m_UsrmChannel;
	}

	public IRouteMap getRouteMap() {
		if (m_RouteMap == null) {
			m_RouteMap = new RouteMapHash(m_MsbuInfo);
		}
		return m_RouteMap;
	}

	/* ========================================================== */
	/* ==================== Bridge Functions ==================== */
	/* ========================================================== */

	/**
	 * For Establish Bridge Link,
	 * 
	 * @param bridgeMsbu
	 */
	public void addBridgeMsbu(Msbu bridgeMsbu) {
		// TODO : Here may cause some bug by default MsbuInfo.
		String domain = bridgeMsbu.getMsbuInfo().getDomain();
		m_BridgeMsbuMap.put(domain, bridgeMsbu);
		/* Edit the local MSBU Informations */
		m_MsbuInfo.isBridgeEnable = true;
		m_MsbuInfo.bridgeDomain.add(domain);
	}

	/**
	 * For Establish Bridge Link, quick search MSBU by MSB domain
	 * 
	 * @param domain
	 * @return
	 * @throws MsbException
	 */
	public Msbu obtainBridgeMsbu(String domain) {
		return m_BridgeMsbuMap.get(domain);
	}

	/**
	 * Find the Most Match Domain Bridge MSBU, and return it;
	 * 
	 * @param routeItem
	 * @return
	 */
	public Msbu getCrossDomainMsbu(MsbUrn msbUrn) throws MsbException {
		Msbu retMsbu = null;
		if (m_BridgeMsbuMap != null & m_BridgeMsbuMap.size() > 0) {
			/*===== STEP 1. Get Destination Domain =====*/
			MsbDomain domainDest = new MsbDomain(msbUrn.getDomain());
			/*===== STEP 2. Find the most Matches Bridge =====*/
			int matchLevel = 0;
			Msbu destMsbu = null;
			for (Entry<String, Msbu> bridgeEntry : m_BridgeMsbuMap.entrySet()) {
				// TODO Matches Domain, Optimize it here.
				int tmpMatches = domainDest.matcheLevel(bridgeEntry.getKey());
				if (tmpMatches > matchLevel) {
					matchLevel = tmpMatches;
					destMsbu = bridgeEntry.getValue();
				}
			}
			/*===== STEP 3. Check Permission =====*/
			// TODO : Maybe an Permission Leak here
			if (true == destMsbu.checkCrossDomainPermission(msbUrn, this.m_MsbuInfo.getDomain())) {
				retMsbu = destMsbu;
			} else {
				throw new MsbException(MsbException.ErrCode.ACCESS_NOT_PERMITTED, "MSBU in domain \"" + m_MsbuInfo.getDomain() + "\" has no permission to access {" + msbUrn + "}. ");
			}
		}
		return retMsbu;
	}

	/**
	 * Check if this MsbProxy Support Bridge to other domain.( Contains other cross domain MSBU )
	 * 
	 * @return if this MsbProxy Support Bridge
	 */
	public boolean isBridgeEnable() {
		return (m_BridgeMsbuMap.size() > 0);
	}

	/**
	 * Check if the MSBURN is in local domain.
	 * 
	 * @param msburn
	 *            the destination MSBURN.
	 * @return if the MSBURN is in local domain
	 */
	private final boolean isLocalDomain(MsbUrn msburn) {
		boolean tmpRet = false;
		if (msburn != null) {
			tmpRet = msburn.domain.equals(m_MsbuInfo.getDomain());
		}
		return tmpRet;
	}

	/*=============================================================*/
	/*==================== Consumer Management ====================*/
	/*=============================================================*/
	/**
	 * Setup Message Receiver <br />
	 * 
	 * @throws MsbProxyException
	 * @throws JMSException
	 */
	public void setupConsumerChannel() throws MsbException, JMSException {
		/* Setup Message Receiver */
		MessageConsumer msgReceiver = this.getConsumer(EConsumerType.PTP_RX, m_MsbuInfo.getProxyID());
		m_ConsumerMap.put(MsbProxy.CONSUMER_MSG_RECEIVER, msgReceiver);
		msgReceiver.setMessageListener(m_MsgListener);
	}

	/*========== Message Handler ===========*/
	/**
	 * NOTE : In Listener Mode, the process will be performed in thread pool automatically.
	 */
	private MessageListener m_MsgListener = new MessageListener() {
		@Override
		public void onMessage(Message message) {

			try {
				/*===== STEP 1. Prepare Necessary Objects =====*/
				ObjectMessage inMsg;
				ObjectMessage outMsg;
				String destMsbProxyID;
				/*===== STEP 2. Execute Message by JMS Type =====*/
				EMsbMsgType msgType = EMsbMsgType.valueOf(message.getJMSType());
				switch (msgType) {

				/*===== Message =====*/
				case MSB_MSG_ASYNC:
					/* Get Input Message */
					inMsg = (ObjectMessage) message;
					/* Process & Create Output Message */
					outMsg = MsbProxy.this.delegateInvokePlatform(inMsg);
					/* Destination MsbProxy ID */
					destMsbProxyID = inMsg.getStringProperty(CMsgField.SENDER_PROXY);
					/* Send Message Back */
					MsbProxy.this.getProducer(EProducerType.PTP_TX, destMsbProxyID).send(outMsg);
					break;

				/*===== Platform RPC Synchronized =====*/
				case MSB_PLATFORM_REQ:
					inMsg = (ObjectMessage) message;
					outMsg = MsbProxy.this.delegateInvokePlatform(inMsg);
					/* Send Message Back */
					destMsbProxyID = inMsg.getStringProperty(CMsgField.SENDER_PROXY);
					MsbProxy.this.getProducer(EProducerType.PTP_TX, destMsbProxyID).send(outMsg);
					break;
				case MSB_PLATFORM_RES:
					MsbProxy.this.m_CallbackMngr.executeCallback(message, false);
					break;

				/*===== RPC Synchronized =====*/
				case MSB_RPC_REQ:
					outMsg = MsbProxy.this.delegateInvoke((ObjectMessage) message);
					/* Send Message Back */
					destMsbProxyID = message.getStringProperty(CMsgField.SENDER_PROXY);
					MsbProxy.this.getProducer(EProducerType.PTP_TX, destMsbProxyID).send(outMsg);
					break;
				case MSB_RPC_RES:
					MsbProxy.this.m_CallbackMngr.executeCallback(message, false);
					break;

				/*===== Default =====*/
				default:
					g_Logger.error("Receiving Not Support Message Type." + msgType + "\r\n");
					break;
				}
			} catch (JMSException exp) {
				g_Logger.error("JMS Error in [MsbProxy.m_MsgListener] : \r\n" + exp.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/*====================================================================*/
	/*==================== Proxy Communication Access ====================*/
	/*====================================================================*/

	/*==========  ==========*/
	public void invokeRemoteAsync(MsbRouteItem routeItem, ComInput ci, IMsbRpcCallback callback) throws MsbException {
		/*===== STEP 1. Shortcut Invoke =====*/
		if (true == invokeShortcutAsync(routeItem, ci, callback))
			return;

		/*===== STEP 2. Send Message via MSB =====*/
		if (ci != null) {
			/* Callback Identified by this MessageID */
			String msgID = generateCallbackID();
			g_Logger.debug("Invoke Remote Async {" + routeItem.msbUrn + "} in [" + routeItem.getProxyID() + "] with ID : " + msgID);
			try {
				/* Create Message */
				ObjectMessage msgReq = this.m_MqSession.createObjectMessage();
				fillRpcMsgHeader(msgReq, EMsbMsgType.MSB_RPC_REQ, msgID, routeItem.msbUrn);
				msgReq.setObject(ci);
				/* Register Callback */
				m_CallbackMngr.registerCallBack(msgID, callback, true);
				/* Send Message */
				this.getProducer(EProducerType.PTP_TX, routeItem.getProxyID()).send(msgReq);
			} catch (JMSException e) {
				e.printStackTrace();
			}
			/* Return the Message ID */
		}
		return;
	}

	public ComOutput invokeRemoteSync(MsbRouteItem routeItem, ComInput ci) throws MsbException {
		ComOutput co = null;
		/*===== STEP 1. Shortcut Invoke =====*/
		try {
			co = invokeShortcutSync(routeItem, ci);
			if (co != null)
				return co;
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*===== STEP 2. Send Message via MSB =====*/
		try {

			/* Generate Application Unique Message ID */
			String msgID = generateCallbackID();
			g_Logger.debug("[" + m_MsbuInfo.name + "] Invoke Remote Sync {" + routeItem.msbUrn + "} in [" + routeItem.getProxyID() + "] with ID : " + msgID);
			/* Generate Invoke Message */
			ObjectMessage msgReq = this.m_MqSession.createObjectMessage();
			fillRpcMsgHeader(msgReq, EMsbMsgType.MSB_RPC_REQ, msgID, routeItem.msbUrn);
			msgReq.setObject(ci);
			/* Add Standard Callback to Thread Wait List */
			SyncCallback syncCallBack = new SyncCallback();
			this.m_CallbackMngr.registerCallBack(msgID, syncCallBack, false);
			/* Send Message & Suspend Thread */
			this.getProducer(EProducerType.PTP_TX, routeItem.getProxyID()).send(msgReq);
			this.m_CallbackMngr.waitRpcResponse(msgID, syncCallBack, MsbProxy.TIMEOUT_RPC_SYNC);
			/* Extract Object */
			co = syncCallBack.getOutput();
		} catch (JMSException exp) {
			throw new MsbException(MsbException.ErrCode.PROXY_ERROR_JMS, exp.getMessage());
		} catch (InterruptedException ignore) {
		}
		return co;
	}

	/*========== Platform Interface ==========*/
	public void invokeRemoteAsyncV3(MsbRouteItem routeItem, ComInput ci, IMsbRpcCallback callback) throws MsbException {
		String msgID = generateCallbackID();
		/* Input Protection */
		if (ci != null) {
			/*===== STEP 1. Shortcut Invoke =====*/
			if (true == invokeShortcutAsync(routeItem, ci, callback))
				return;
			/*===== STEP 2. Send Message via MSB =====*/
			try {
				/* Create Message */
				ObjectMessage msgReq = m_MqSession.createObjectMessage();
				fillRpcMsgHeader(msgReq, EMsbMsgType.MSB_PLATFORM_REQ, msgID, routeItem.msbUrn);
				msgReq.setObject(ci);
				/* Regist Callback */
				m_CallbackMngr.registerCallBack(msgID, callback, true);
				/* Send Message */
				this.getProducer(EProducerType.PTP_TX, routeItem.getProxyID()).send(msgReq);
			} catch (JMSException e) {
				e.printStackTrace();
			}
			/* Return the Message ID */
		}
		return;
	}

	public ComOutput invokeRemoteSyncV3(MsbRouteItem routeItem, ComInput ci) throws MsbException {
		ComOutput co = null;
		/* Generate Application Unique Message ID */
		String msgID = generateCallbackID();
		try {
			/* Create Message */
			ObjectMessage msgReq = this.m_MqSession.createObjectMessage();
			fillRpcMsgHeader(msgReq, EMsbMsgType.MSB_PLATFORM_REQ, msgID, routeItem.msbUrn);
			msgReq.setJMSCorrelationID(msgID);
			msgReq.setObject(ci);
			/* Add Standard Callback to Thread Wait List */
			SyncCallback syncCallBack = new SyncCallback();
			this.m_CallbackMngr.registerCallBack(msgID, syncCallBack, false);
			this.m_CallbackMngr.waitRpcResponse(msgID, syncCallBack, MsbProxy.TIMEOUT_RPC_SYNC);
			/* Extract Object */
			co = syncCallBack.getOutput();
		} catch (JMSException exp) {
			exp.printStackTrace();
		} catch (InterruptedException exp) {
			exp.printStackTrace();
		}
		return co;
	}

	/*============================================================*/
	/*==================== Assistant Function ====================*/
	/*============================================================*/

	/*============ Assistant : fillRpcMsgHeader ===========*/
	/**
	 * 
	 * @param msg
	 * @param type
	 * @param msgID
	 * @param destUrn
	 */
	private final void fillRpcMsgHeader(Message msg, EMsbMsgType type, String msgID, MsbUrn destUrn) {
		/* Fill Header */
		try {
			/* Fill Basic */
			msg.setJMSType(type.name());
			msg.setJMSCorrelationID(msgID);
			msg.setJMSReplyTo(m_SelfDestination);
			/* Fill Extension */
			msg.setStringProperty(CMsgField.SENDER_PROXY, m_MsbuInfo.getProxyID());
			msg.setStringProperty(CMsgField.MSBURN, destUrn.fullUrn);
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

	/*============ Assistant : generateCallbackID ===========*/
	/**
	 * Generate Callback ID of an callback;
	 * 
	 * @return
	 */
	private final String generateCallbackID() {
		return UUID.randomUUID().toString();
	}

	/*=========================================================*/
	/*==================== Process Message ====================*/
	/*=========================================================*/

	/*========== Process Message : delegateInvokePlatform ===========*/
	/**
	 * 
	 * @param msgIn
	 * @return
	 * @throws Exception
	 */
	private ObjectMessage delegateInvokePlatform(ObjectMessage msgIn) throws Exception {
		ObjectMessage msgOut = null;
		ComOutput co = null;
		/* Get URN & ComInput & Session Key */
		MsbUrn msbUrn = MsbUrn.parse(msgIn.getStringProperty(CMsgField.MSBURN));
		String urn = msbUrn.getSvcName();
		ComInput ci = (ComInput) msgIn.getObjectProperty(CMsgField.MSG_LOAD);
		//TODO where is Session Key
		co = m_SvcHandlerMngr.invokePlatformService(urn, ci);

		/* Fill Return Message */
		msgOut = MsbProxy.this.m_MqSession.createObjectMessage();
		msgOut.setJMSType(EMsbMsgType.MSB_PLATFORM_RES.name());
		msgOut.setJMSCorrelationID(msgIn.getJMSCorrelationID());
		// Maybe Mark Performer with MSBURN
		msgOut.setObject(co);
		/* */
		return msgOut;
	}

	/*========== Process Message : delegateInvoke ===========*/
	/**
	 * Proxy Internal Method Invoke.
	 * 
	 * @param msgIn
	 * @return
	 * @throws JMSException
	 */
	private ObjectMessage delegateInvoke(ObjectMessage msgIn) {
		ObjectMessage msgOut = null;
		ComOutput co = null;
		try {
			try {
				MsbUrn msbUrn = MsbUrn.parse(msgIn.getStringProperty(CMsgField.MSBURN));
				if (isLocalDomain(msbUrn)) {
					String svUrn = msbUrn.getSvcName();
					// TODO Optimize here.
					ComInput ci = (ComInput) msgIn.getObject();
					co = m_SvcHandlerMngr.invokeRpcService(svUrn, ci);
					// TODO Optimize here.
					g_Logger.debug("Process Reomote Invoke Service {" + svUrn + "} whith ID : " + msgIn.getJMSCorrelationID());
				} else {
					Msbu destMsbu = this.getCrossDomainMsbu(msbUrn);
					ComInput ci = (ComInput) msgIn.getObject();
					co = destMsbu.invokeMethodSync(msbUrn, ci);
				}
			} catch (MsbException exp) {
				co = RSUtil.setOutput(-1, exp.getMessage());
				g_Logger.info(exp.getMessage());
			}
			/* Fill Return Message */
			msgOut = m_MqSession.createObjectMessage();
			if (msgIn.getJMSType().equals(EMsbMsgType.MSB_RPC_REQ.name())) {
				msgOut.setJMSType(EMsbMsgType.MSB_RPC_RES.name());
			}
			msgOut.setJMSCorrelationID(msgIn.getJMSCorrelationID());
			// Maybe Mark Performer with MSBURN
			msgOut.setObject(co);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return msgOut;
	}

	/*===========================================================*/
	/*==================== Shortcut Invoking ====================*/
	/*===========================================================*/

	/*========== Shortcut Invoking : invokeShortcutAsync ==========*/
	private final boolean invokeShortcutAsync(MsbRouteItem routeItem, ComInput ci, IMsbRpcCallback callback) {
		boolean tmpRet = false;
		if (true == isShortcut(routeItem)) {
			ComOutput co = null;
			callback.onReplyMsg(co);
			tmpRet = true;
		}
		return tmpRet;
	}

	/*========== Shortcut Invoking : invokeShortcutSync ==========*/
	private final ComOutput invokeShortcutSync(MsbRouteItem routeItem, ComInput ci) throws MsbException {
		ComOutput co = null;
		if (true == isShortcut(routeItem)) {
			g_Logger.debug("Invoke Shutcut : " + routeItem.msbUrn);
			ServiceEntry svcEntry = this.m_SvcHandlerMngr.getServiceEntry(routeItem.msbUrn);
			if (svcEntry != null) {
				switch (svcEntry.getType()) {
				case PLATFORM:
					if (m_PlatformPort != null) {
						co = m_PlatformPort.invokePlatformService(routeItem.msbUrn.svcName, ci);
					}
					break;
				case RPC:
					co = svcEntry.getRpcHandler().onInvokeSvr(ci);
					break;
				default:
					break;
				}
			} else {
				throw new MsbException("Not Find local Service : " + routeItem.msbUrn);
			}
		}
		return co;
	}

	/*=========== Assistant : isShortcut ==========*/
	/**
	 * Assert if the aim resource is in the same proxy.
	 * 
	 * @param routeItem
	 * @return
	 */
	private final boolean isShortcut(MsbRouteItem routeItem) {
		String destProxyID = routeItem.getProxyID();
		String srcProxyID = m_MsbuInfo.getProxyID();
		return (destProxyID.equals(srcProxyID));
	}

	/*==================================================*/
	/*==================== Shortcut ====================*/
	/*==================================================*/

	/*========= Getter & Setter ==========*/
	public MsbuInfo getMsbuInfo() {
		return m_MsbuInfo;
	}

	public IAccessCtrlManager getAccessCtrlManager() {
		return m_AcMngr;
	}

	public void setPlatformPort(IPlatformPort platformPort) {
		this.m_PlatformPort = platformPort;
	}

}
