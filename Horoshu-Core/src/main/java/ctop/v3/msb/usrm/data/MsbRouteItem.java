package ctop.v3.msb.usrm.data;

import java.io.Serializable;

import ctop.v3.msb.common.urn.MsbUrn;

/**
 * 
 * @author DreamInSun
 * 
 */
public class MsbRouteItem implements Serializable {
	private static final long serialVersionUID = 1L;

	/*========== Constant ==========*/

	/*========== Services Properties ==========*/
	/** Service Information : Readable Service Information */
	public MsbUrn msbUrn;
	/** Primary Identity : Should be Global Unique */
	protected String domain;
	protected String proxyID;
	protected String svcName;

	/*========== Route Properties ===========*/
	/**
	 * 
	 */

	/**
	 * Connection Word : an String represent how to connect <br />
	 * e.g. Message Queue (JMS) / RMI / Socket / R-OSGi / Others / JDBC <br />
	 * JMS : MQ://usrname@IP:Port/Proxy?key=xxxxxx <br />
	 * Bridge : BRIDGE :// <br />
	 * JDBC : jdbc:oracle:thin:@127.0.0.1:1521/ORCL <br />
	 * 
	 * */
	//public TYPE ; 
	/*
	public String connectWord;
	public String usrname;
	public String password;
	*/

	/*========== Constructor ===========*/
	/**
	 * 
	 * @param msbUrn
	 * @param proxyID
	 */
	public MsbRouteItem(MsbUrn msbUrn, String proxyID) {
		this.msbUrn = msbUrn;
		this.proxyID = proxyID;
	}

	/*========== Transformation ==========*/

	/*========== Externalizable ==========*/

	/*========== Getter & Setter ==========*/
	public MsbUrn getMsbUrn(){
		return this.msbUrn;
	}
	public String getProxyID() {
		return proxyID.toString();
	}

}
