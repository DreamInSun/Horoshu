package ctop.v3.msb.msbu.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ctop.v3.msb.common.exception.MsbException;
import ctop.v3.msb.msbu.Msbu.EType;
import ctop.v3.msb.proxy.conn.MsbConnParam;

/**
 * Value Object for initializing one MSBU.
 * 
 * @author DreamInSun
 * 
 */
public class MsbuInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	/*========== Properties ==========*/
	public String domain;
	public String name;
	public EType type;

	/*===== MSBU Service =====*/
	/** in Millisecond */
	public int NodeDeclearTimeout = 5000;

	public boolean isRouterEnable = false;
	public boolean isManagerEnable = false;
	public boolean isPublisherEnable = false;
	public boolean isBridgeEnable = false;
	public List<String> bridgeDomain = new ArrayList<String>();

	/* Connection Params */
	public MsbConnParam msbConnParam = null;
	/* */
	public Map<String, String> masterParams = new HashMap<String, String>();
	public Map<String, String> publisherParams = new HashMap<String, String>();

	/*========== Constructor ==========*/
	/**
	 * 
	 * @param domain
	 * @param name
	 * @param type
	 * @throws MsbException
	 */
	public MsbuInfo(String domain, String name, String type) throws MsbException {
		this.domain = domain;
		this.name = name;
		try {
			this.type = EType.valueOf(type);
		} catch (Exception exp) {
			throw new MsbException("Cannot Parse Type MSBU Type" + type);
		}
	}

	/**
	 * 
	 * @param domain
	 * @param name
	 * @param type
	 */
	public MsbuInfo(String domain, String name, EType type) {
		this.domain = domain;
		this.name = name;
		this.type = type;
	}

	/*========== Getter ==========*/
	/**
	 * 
	 * @return
	 */
	public String getDomain() {
		return this.domain;
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 
	 * @return MSB Unique ID to locate MSBU.
	 */
	public String getProxyID() {
		return this.name;
	}

	/**
	 * 
	 * @return
	 */
	public void setMsbConnParam(MsbConnParam connParam) {
		this.msbConnParam = connParam;
	}

	/**
	 * 
	 * @return
	 */
	public MsbConnParam getMsbConnParam() {
		return this.msbConnParam;
	}

	/*========== toString ==========*/
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("\r\n/*========== MSBU Information ==========*/ \r\n");
		sb.append("Domain : ").append(this.domain).append("\r\n");
		sb.append("Name : ").append(this.name).append("\r\n");
		sb.append("Features: \r\n");
		if (isRouterEnable)
			sb.append("	Service Router Activated.\r\n");
		if (isManagerEnable)
			sb.append("	Service Manager Activated.\r\n");
		if (isPublisherEnable)
			sb.append("	Service Provider Activated. \r\n");
		if (isBridgeEnable)
			sb.append("	Service Bridge Activated. \r\n");
		sb.append("/*========== Connection Params ==========*/\r\n");
		sb.append(msbConnParam.toString() + "\r\n");
		sb.append("/*======================================*/\r\n");
		return sb.toString();
	}

}
