package ctop.v3.msb.usrm.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ctop.v3.msb.msbu.data.MsbuInfo;

/**
 * Informations to described an USRM Node
 * 
 * @author DreamInSun
 * 
 */
public class UsrmNodeInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	/*========== Constant ==========*/
	/**
	 * Running State of an UsrmNode.
	 * 
	 * @author DreamInSun
	 * 
	 */
	public enum EState {
		/** Default Start Value */
		UNRGISTERED,
		/** Node Has Declared an WAiting for Response */
		INITIALIZING,
		/** Register Succeed */
		ILLEGAL,
		/** Register Failed */
		LEGAL,
		/** */
		OFFLINE,
	}

	/*========== Properties ==========*/
	/* Basic Information */
	private String name;
	private String domain;
	/* Node State*/
	private EState state;
	/** Current is same to Name, will be change to UUID */
	private String proxyId;

	/** Key must not be null */
	private String key = "";

	/** Timestamp of USRM Node Modified */
	public long tsModified;
	/** Timestamp of USRM Node Updated */
	public long tsUpdate;

	/* Control Info */
	public boolean isMaster;
	public boolean isPublisher;
	public boolean isBridge;
	public String[] bridgeDomain;

	/*========== Factory ==========*/
	private static Map<String, UsrmNodeInfo> g_UsrmNodeInfoMap = new HashMap<String, UsrmNodeInfo>();

	public static UsrmNodeInfo getInstance(MsbuInfo msbuInfo) {
		UsrmNodeInfo tmpUsrmNodeInfo = null;
		/* Get From Singleton Map */
		if (msbuInfo != null) {
			tmpUsrmNodeInfo = g_UsrmNodeInfoMap.get(msbuInfo.domain);

		}
		/* Create One if Not Exist */
		tmpUsrmNodeInfo = new UsrmNodeInfo(msbuInfo);
		g_UsrmNodeInfoMap.put(msbuInfo.domain, tmpUsrmNodeInfo);
		/* Return */
		return tmpUsrmNodeInfo;
	}

	/*========== Constructor ==========*/
	public UsrmNodeInfo() {

	}

	protected UsrmNodeInfo(MsbuInfo msbuInfo) {
		/* Copy Basic Informations */
		this.name = msbuInfo.getName();
		this.domain = msbuInfo.getDomain();
		this.proxyId = msbuInfo.getProxyID();
		/* Default Value */
		this.state = EState.UNRGISTERED;
		/* USRM Module Informations */
		isMaster = msbuInfo.isManagerEnable;
		isPublisher = msbuInfo.isPublisherEnable;
		isBridge = msbuInfo.isBridgeEnable;
		bridgeDomain = new String[msbuInfo.bridgeDomain.size()];
		msbuInfo.bridgeDomain.toArray(bridgeDomain);
	}

	/**
	 * Update USRM Node Info from MSBU Info. keep the USRM own properties not changed.
	 * 
	 * @param msbuInfo
	 */
	public void updateFrom(MsbuInfo msbuInfo) {
		isMaster = msbuInfo.isManagerEnable;
		isPublisher = msbuInfo.isPublisherEnable;
		isBridge = msbuInfo.isBridgeEnable;
		bridgeDomain = new String[msbuInfo.bridgeDomain.size()];
		msbuInfo.bridgeDomain.toArray(bridgeDomain);
	}

	/*========== Getter & Setter ==========*/
	public String getProxyId() {
		return proxyId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public EState getState() {
		return state;
	}

	public void setState(EState state) {
		this.state = state;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	/*========== Hash Code ==========*/
	/**
	 * For Identifying Different Usrm Node
	 */
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	/*========== print USRM Node Information ==========*/
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(512);
		sb.append("/*========= USRM Node ==========*/\r\n");
		sb.append("NAME  : ").append(name).append("\r\n");
		sb.append("STATE : ").append(state).append("\r\n");
		sb.append("PROXY : ").append(proxyId).append("\r\n");
		sb.append("KEY   : ").append(key).append("\r\n\r\n");
		if (isMaster) {
			sb.append("Master Enable.\r\n");
		}
		if (isBridge) {
			sb.append("Bridge Enable.\r\n");
			for (String domain : bridgeDomain) {
				sb.append("		Bridge Domain : ").append(domain).append("\r\n");
			}
		}
		if (isPublisher) {
			sb.append("Publisher Enable.\r\n");
		}
		sb.append("/*========== USRM Node ==========*/");
		/* Control Info */
		return sb.toString();
	}
}
