package ctop.v3.msb.usrm.channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.log4j.Logger;

import ctop.v3.msb.msbu.data.MsbuInfo;
import ctop.v3.msb.proxy.MsbProxy;
import ctop.v3.msb.usrm.constants.EUsrmCmdCode;
import ctop.v3.msb.usrm.data.UsrmCommand;
import ctop.v3.msb.usrm.data.UsrmNodeInfo;
import ctop.v3.msb.usrm.interfaces.IUsrmCmdListener;

/**
 * Create four Communication Channel by Proxy, and Deliver them to Usrm Client <br />
 * 1. Directly Communication : <br />
 * [Reporter] Node => PTP( UsrmMasterRx ) => Master <br />
 * [Receiver] Master => PTP( UsrmNodeRx_ + NodeName ) => Node <br />
 * 
 * 2. Broadcast Communication : <br />
 * [declaration] Node => Broadcast ( USRM_NODE_DECALRATION ) => Master <br />
 * [broadcastOberver] Master => Broadcast ( USRM_MASTER_BROADCAST ) => Node <br />
 * 
 * @author DreamInSun
 * 
 */
public final class UsrmChannel {
	private static Logger g_Logger = Logger.getLogger(UsrmChannel.class);

	/*========== Constant ==========*/
	public static final String JMS_TYPE_USRM_CMD = "UsrmCmd";

	/*===== Client Channel =====*/
	/** Every USRM Node Create an Message Queue to Receive Messages */
	public static final String PREFIX_RX = "UsrmNodeRx_";
	/** For MSBU Bootstrap : Broadcast Service Info to MSB boot topic, only USRM Master subscribe this Topic */
	public static final String USRM_NODE_DECALRATION = "Usrm_Bootstrap";

	/*===== Master Channel =====*/
	/** Usrm Master Create this channel to Receive Require Command from USRM Nodes */
	public static final String USRM_MASTER_RX = "UsrmMasterRx";
	/**
	 * In Decentration Mode, Every MSB Proxy Observe this Topic and broadcast Services to Others. <br />
	 * In Master/Slaver Mode, Only Master can publish Service Bulletin to this Topic.
	 */
	public static final String USRM_MASTER_BROADCAST = "UsrmMasterBroadcast";

	/*========== Properties ==========*/
	private MsbProxy m_MsbProxy;
	private MsbuInfo m_MsbuInfo;
	private Connection m_MqConn;
	public Session m_MqSession;

	boolean isLostConnect = true;

	/*===== Destination =====*/
	/* NOTE :  */
	private Destination m_destMasterRx;
	private Destination m_destNodeRx;
	private Destination m_destDeclaration;
	private Destination m_destBroadcast;

	/*===== Client Special Channel =====*/
	/** Direct Communicate to USRM Master, Send Require Message */
	private MessageProducer m_cmdReporter;
	/** Direct Communicate to USRM Master, Receive Messages from USRM Master */
	private MessageConsumer m_cmdReceiver;

	/** Client Declare Self Information to MSB Network */
	private MessageProducer m_declarater;
	/** Client Listening to Master Broadcast Commands */
	private MessageConsumer m_broadcastOberver;

	/*===== Master Special Channel =====*/
	/** Master Broadcast Commands to All the Client in MSB Network */
	private MessageProducer m_broadcaster;
	/** Direct Communicate to USRM Node, Send Reply Message */
	private MessageConsumer m_cmdMasterRx;
	/** Master Listening to Client */
	private MessageConsumer m_declarationOberver;

	/*===== Command Listener  =====*/
	private Map<EUsrmCmdCode, IUsrmCmdListener> m_NodeListenerMap = new ConcurrentHashMap<EUsrmCmdCode, IUsrmCmdListener>();
	private Map<EUsrmCmdCode, IUsrmCmdListener> m_MasterListenerMap = new ConcurrentHashMap<EUsrmCmdCode, IUsrmCmdListener>();
	private Map<EUsrmCmdCode, IUsrmCmdListener> m_BroadcastListenerMap = new HashMap<EUsrmCmdCode, IUsrmCmdListener>();

	/*===== Cache =====*/
	//private Map<String, MessageProducer> m_sendToClient = new WeakHashMap<String, MessageProducer>();

	/*========== Constructor ==========*/
	/**
	 * Setup An Basic USRM Client Communication Channel As Declaration, Report, ReceiveBroadcast. Note: Once Get Parent
	 * USRM ProxyID, invoke setupReportChannel() to setup report channel <br />
	 * Note: If it plays a role as USRM Master, invoke setupMasterChannel(). <br />
	 * if the USRM Node is an Bridge Node, it will get 2 MsbuInfo.
	 * 
	 * @param proxyID
	 * @param mqConn
	 * @throws JMSException
	 */
	public UsrmChannel(MsbProxy msbPRoxy) throws JMSException {
		/* Store Necessary Values */
		m_MsbProxy = msbPRoxy;
		this.m_MsbuInfo = msbPRoxy.getMsbuInfo();
		setupMqChannels();

		/* */
	}

	private final void setupMqChannels() throws JMSException {
		try {
			m_MqConn = this.m_MsbProxy.connectMq();
			m_MqSession = m_MqConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			/*=====  =====*/
			setupDestination();
			/*=====  =====*/
			this.setupClientChannel();
			/*=====  =====*/
			if (m_MsbProxy.getMsbuInfo().isManagerEnable) {
				this.setupMasterChannel();
			}
		} catch (JMSException e) {
			g_Logger.error("Cannot Reconnect JMS Server... Try Again Later.");
		}
	}

	private final void resetChennls() {
		m_destMasterRx = null;
		m_destNodeRx = null;
		m_destDeclaration = null;
		m_destBroadcast = null;
		m_cmdReporter = null;
		m_declarater = null;
		m_cmdReceiver = null;
		m_broadcastOberver = null;
		m_broadcaster = null;
		m_cmdMasterRx = null;
		m_declarationOberver = null;
	}

	private final void reInitChannels() {
		g_Logger.info("Try Reconnect MQ");
		try {
			this.resetChennls();
			this.setupMqChannels();
			isLostConnect = false;
		} catch (JMSException ignore) {
			g_Logger.info("Try Reconnect MQ Failed");
		}
	}

	/*======================================================================*/
	/*==================== Setup Communication Channels ====================*/
	/*======================================================================*/

	/*==========  ==========*/
	/**
	 * 
	 */
	public void setupDestination() {
		try {
			/* Directly Sending to Master */
			m_destMasterRx = m_MqSession.createQueue(USRM_MASTER_RX);
			/* Node Declaration */
			m_destDeclaration = m_MqSession.createTopic(UsrmChannel.USRM_NODE_DECALRATION);
			/* Direct Command Listener */
			m_destNodeRx = m_MqSession.createQueue(UsrmChannel.PREFIX_RX + m_MsbuInfo.name);
			/* Broadcast Listener */
			m_destBroadcast = m_MqSession.createTopic(UsrmChannel.USRM_MASTER_BROADCAST);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	/*========== Function : setupClientChannle ==========*/
	public void setupClientChannel() {
		try {
			/*===== Tx Channel =====*/
			/* Directly Sending to Master */
			if (m_cmdReporter == null)
				this.m_cmdReporter = m_MqSession.createProducer(m_destMasterRx);
			/* Node Declaration */
			if (m_declarater == null)
				this.m_declarater = m_MqSession.createProducer(this.m_destDeclaration);

			/*===== Rx Channel =====*/
			/* Direct Command Listener */
			if (m_cmdReceiver == null)
				this.m_cmdReceiver = m_MqSession.createConsumer(m_destNodeRx);
			setReplyListener();
			/* Broadcast Listener */
			if (m_broadcastOberver == null)
				this.m_broadcastOberver = m_MqSession.createConsumer(this.m_destBroadcast);
			setBroadcastListener();

		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	/*========== Function : setupMasterChannle ==========*/
	public void setupMasterChannel() {
		try {
			/*===== Tx Channel =====*/
			/* Create Send to USRM Node Dynamically */
			//this.m_cmdAssigner = m_MqSession.createProducer(m_destMasterRx);
			/* Broadcast */
			if (m_broadcaster == null)
				this.m_broadcaster = m_MqSession.createProducer(this.m_destBroadcast);

			/*===== Rx Channel =====*/
			/* Declaration */
			if (m_declarationOberver == null)
				this.m_declarationOberver = m_MqSession.createConsumer(m_destDeclaration);
			setDeclarationListener();
			/* Directly Command */
			if (m_cmdMasterRx == null)
				this.m_cmdMasterRx = m_MqSession.createConsumer(m_destMasterRx);
			setRequestListener();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	/*=================================================================*/
	/*==================== Communication Functions ====================*/
	/*=================================================================*/

	/*========= Communication Function : declareCommand ==========*/
	public boolean declareCommand(UsrmCommand cmd) {
		if (this.m_declarater != null) {
			Message msg = this.packMessage(cmd);
			if (msg != null) {
				try {
					g_Logger.debug(m_MsbuInfo.getName() + " Declare Self [" + cmd.getCode().name() + "] to all the other USRM Node.");
					m_cmdReporter.send(msg);
					return true;
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/*========== Communication Function : sendCommand ==========*/
	/**
	 * Send Command to parent USRM; <br />
	 * Master Can Only Exist One. Isolating it for optimization.
	 * 
	 * @param cmd
	 * @return if send UsrmCommand succeed.
	 */
	public boolean sendCmdToMaster(UsrmCommand cmd) {
		g_Logger.debug(cmd.getSender().getName() + " Send [" + cmd.getCode().name() + "] to Master");
		if (m_cmdReporter != null) {
			Message msg = this.packMessage(cmd);
			if (msg != null) {
				try {
					m_cmdReporter.send(msg);
					return true;
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/*========== Communication Function : sendCmdToNode ==========*/
	/**
	 * Send Command to Specified USRM Node ( not only master can use it, but not recommended ).
	 * 
	 * @param destNode
	 * @param cmd
	 * @return
	 */
	public boolean sendCmdToNode(UsrmNodeInfo destNode, UsrmCommand cmd) {
		g_Logger.debug(cmd.getSender().getName() + " Send [" + cmd.getCode().name() + "] to " + destNode.getName());
		Message msg = packMessage(cmd);
		if (msg != null) {
			try {
				Destination dest = m_MqSession.createQueue(PREFIX_RX + destNode.getName());
				MessageProducer producer = m_MqSession.createProducer(dest);
				producer.send(msg);
				return true;
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/*========== Communication Function : broadcastCommand ==========*/
	/**
	 * Broadcast Command to Every USRM Node, ( not only master can use it, but not recommended ). <br />
	 * NOTE : not only USRM Master can use this method, but not recommended other disturb.
	 */
	public boolean broadcastCommand(UsrmCommand cmd) {
		if (m_broadcaster != null) {
			Message msg = packMessage(cmd);
			if (msg != null) {
				try {
					m_broadcaster.send(msg);
					m_MqSession.commit();
					return true;
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/*============================================================================*/
	/*==================== Communication Listeners Management ====================*/
	/*============================================================================*/

	/*========== Function : setReplyListener =========*/
	private boolean setReplyListener() {
		if (m_cmdReceiver != null) {
			try {
				m_cmdReceiver.setMessageListener(new MessageListener() {
					@Override
					public void onMessage(Message message) {
						invokeNodeListener(unpackMessage(message));
					}
				});
				return true;
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/*========== Function : setBroadcastListener =========*/
	private boolean setBroadcastListener() {
		if (m_cmdReceiver != null) {
			try {
				m_cmdReceiver.setMessageListener(new MessageListener() {
					@Override
					public void onMessage(Message message) {
						invokeNodeListener(unpackMessage(message));
					}
				});
				return true;
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/*========== Function : setDeclarationListener =========*/
	private boolean setDeclarationListener() {
		if (this.m_declarationOberver != null) {
			try {
				m_declarationOberver.setMessageListener(new MessageListener() {
					@Override
					public void onMessage(Message message) {
						invokeMasterListener(unpackMessage(message));
					}
				});
				return true;
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/*========== Function : setRequestListener =========*/
	private boolean setRequestListener() {
		if (this.m_cmdMasterRx != null) {
			try {
				m_cmdMasterRx.setMessageListener(new MessageListener() {
					@Override
					public void onMessage(Message message) {
						invokeMasterListener(unpackMessage(message));
					}
				});
				return true;
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/*===========================================================================*/
	/*==================== Node Command Listeners Management ====================*/
	/*===========================================================================*/

	final public void registerCmdListener(IUsrmCmdListener listener) {
		m_NodeListenerMap.put(listener.getInterestCommand(), listener);
	}

	final public void unregisterCmdListener(IUsrmCmdListener listener) {
		m_NodeListenerMap.remove(listener.getInterestCommand());
	}

	final private void invokeNodeListener(UsrmCommand cmd) {
		if (cmd != null) {
			IUsrmCmdListener listener = m_NodeListenerMap.get(cmd.getCode());
			if (listener != null) {
				listener.onReceiveUsrmCmd(cmd);
			}
		}
	}

	/*=============================================================================*/
	/*==================== Master Command Listeners Management ====================*/
	/*=============================================================================*/

	final public void registerMasterCmdListener(IUsrmCmdListener listener) {
		m_MasterListenerMap.put(listener.getInterestCommand(), listener);
	}

	final public void unregisterMasterCmdListener(IUsrmCmdListener listener) {
		m_MasterListenerMap.remove(listener.getInterestCommand());
	}

	final private void invokeMasterListener(UsrmCommand cmd) {
		if (cmd != null) {
			IUsrmCmdListener listener = m_MasterListenerMap.get(cmd.getCode());
			if (listener != null) {
				listener.onReceiveUsrmCmd(cmd);
			}
		}
	}

	/*=============================================================================*/
	/*==================== Broadcast Command Listeners Management ====================*/
	/*=============================================================================*/
	final public void registerBroadcastCmdListener(IUsrmCmdListener listener) {
		m_BroadcastListenerMap.put(listener.getInterestCommand(), listener);
	}

	final public void unregisterBroadcastCmdListener(IUsrmCmdListener listener) {
		m_BroadcastListenerMap.remove(listener.getInterestCommand());
	}

	@SuppressWarnings("unused")
	final private void invokeBroadcastListener(UsrmCommand cmd) {
		if (cmd != null) {
			IUsrmCmdListener listener = m_BroadcastListenerMap.get(cmd.getCode());
			if (listener != null) {
				listener.onReceiveUsrmCmd(cmd);
			}
		}
	}

	/*=========================================================*/
	/*==================== Message Packers ====================*/
	/*=========================================================*/

	/*========== Inline Function : packMessage ==========*/
	/**
	 * Pack Up UsrmCommand to Message
	 * 
	 * @param cmd
	 * @return
	 */
	final private Message packMessage(UsrmCommand cmd) {
		ObjectMessage msg = null;

		if (isLostConnect == true) {
			this.reInitChannels();
		}

		try {
			msg = m_MqSession.createObjectMessage();
			msg.setJMSType(UsrmChannel.JMS_TYPE_USRM_CMD);
			msg.setObject(cmd);
		} catch (JMSException e) {
			String message = e.getMessage();
			g_Logger.error("Cannot Connect JMS Server : The Session is closed.");
			if ("The Session is closed".equals(message)) {
				isLostConnect = true;
			}
		}
		return msg;
	}

	/*========== Inline Function : unpackMessage ==========*/
	/**
	 * Unpack UsrmCommand From Message
	 * 
	 * @param cmd
	 * @return
	 */
	final private UsrmCommand unpackMessage(Message msg) {
		UsrmCommand tmpCmd = null;
		try {
			if (msg.getJMSType().equals(UsrmChannel.JMS_TYPE_USRM_CMD)) {
				tmpCmd = (UsrmCommand) ((ObjectMessage) msg).getObject();
				g_Logger.debug(m_MsbuInfo.getName() + " Receive [" + tmpCmd.getCode().name() + "] from " + tmpCmd.getSender().getName());
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return tmpCmd;
	}

}
