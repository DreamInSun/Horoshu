package ctop.v3.msb.common.urn;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import ctop.v3.msb.common.exception.MsbException;

/**
 * MSB Uniform Resource Name following URI Name Rule. <br />
 * Appeared like "MSBURN://MsbName:ProxyID/syssv/com/ctop/x/y/z#version" used in MSB cluster Resource Locating. <br />
 * An Internal UR format looks like <code>"SYSSV.COM.CTOP.X.Y.Z"</code> will be automatically change to
 * <code>"MSBURN://local/syssv/com/ctop/x/y/z"</code> <br />
 * <br/>
 * MsbUrn used to locate uniform resource in cross domain environment and compatible for multiple version coexistence. <br />
 * 
 * @author DreamInSun
 * 
 */
public final class MsbUrn implements Externalizable, Comparable<MsbUrn> {
	private static final long serialVersionUID = 1L;

	/*========== Constant ==========*/
	public static String LOCAL_MSB = "LOCAL";
	public static String SVC_DEFAULT = "DEFAULT";
	public static String MSBURN_HEADER = "MSBURN://";

	/*========== Static Properties ===========*/
	static StringBuffer g_MsnUrnBuilder = new StringBuffer(256).append(MsbUrn.MSBURN_HEADER);

	/*========== Properties ===========*/
	/** MSBURN://MsbDomain:svcGroup/syssv/com/ctop/x/y/z#version */
	/** => RouteItem */
	public String fullUrn;
	/** XXXXXX or LOCAL, Represent Tree Architecture, TODO replace String with MsbuDomain */
	public String domain;
	/** Specified Service Group or 'DEFAULT' */
	public String proxy;
	/** SYSSV.X.Y.Z */
	public String svcName;
	/** 1.0.A */
	public MsbUrnVer svcVersion;

	/*========== Constructor ==========*/
	/** Only for deserialization */
	@Deprecated
	public MsbUrn() {
	}

	public MsbUrn(String msbID, String proxyID, String svUrn, String svVersion) {
		this.domain = msbID;
		this.proxy = proxyID;
		this.svcName = svUrn;
		this.svcVersion = MsbUrnVer.parse(svVersion);
		updateMsbUrn();
	}

	/*========== Parser ==========*/
	public static MsbUrn parse(String urn) throws MsbException {
		MsbUrn tmpRet = new MsbUrn();
		tmpRet.parseUrn(urn);
		return tmpRet;
	}

	/*========== Parser ==========*/
	public void parseUrn(String urn) throws MsbException {
		this.fullUrn = urn;
		/* Divide Field */
		int posMsbStatrt = 0;
		if (urn.startsWith(MSBURN_HEADER)) {
			posMsbStatrt = MSBURN_HEADER.length();
		}
		int posMsbEnd = urn.indexOf(':', posMsbStatrt);
		int posProxyStart = posMsbEnd + 1;
		int posProxyEnd = urn.indexOf('/', posMsbStatrt);
		posMsbEnd = (posMsbEnd != -1) ? posMsbEnd : posProxyEnd;
		int posUrStart = posProxyEnd + 1;
		int posUrEnd = urn.indexOf('#', posProxyEnd);
		int posVersionStart = posUrEnd + 1;
		/* Get Field Value */
		this.domain = urn.substring(posMsbStatrt, posMsbEnd);
		this.proxy = null;
		this.svcName = null;
		this.svcVersion = null;

		if (posProxyStart != 0) {
			proxy = urn.substring(posProxyStart, posProxyEnd);
		}
		if (posVersionStart != 0) {
			svcName = urn.substring(posUrStart, posUrEnd).replace('/', '.');
			svcVersion = MsbUrnVer.parse(urn.substring(posUrEnd + 1));
		} else {
			svcName = urn.substring(posUrStart).replace('/', '.');
		}
	}

	/*========== toString ==========*/
	private void updateMsbUrn() {
		synchronized (g_MsnUrnBuilder) {
			/* Prepare String Builder */
			g_MsnUrnBuilder.delete(MsbUrn.MSBURN_HEADER.length(), g_MsnUrnBuilder.length());
			//TODO Warning String Builder is not thread safe, take care of it;
			/* MSB ID */
			if (domain == null)
				g_MsnUrnBuilder.append(MsbUrn.LOCAL_MSB);
			else
				g_MsnUrnBuilder.append(domain);
			/* Proxy ID */
			if (proxy != null)
				g_MsnUrnBuilder.append(":").append(proxy);
			/* Resource Name */
			g_MsnUrnBuilder.append("/");
			if (svcName != null)
				g_MsnUrnBuilder.append(svcName.replace('.', '/'));
			/* Version */
			if (svcVersion != null)
				g_MsnUrnBuilder.append("#").append(svcVersion);
			/* Store  */
			fullUrn = g_MsnUrnBuilder.toString();
		}
	}

	@Override
	public String toString() {
		if (fullUrn == null) {
			updateMsbUrn();
		}
		return fullUrn;
	}

	/*========== Object : HashCode ==========*/
	/**
	 * Assurance MSBURN Hash Code the Same as String MsbUrn, but in the same format will get the same value.
	 */
	@Override
	public int hashCode() {
		if (fullUrn == null) {
			updateMsbUrn();
		}
		return fullUrn.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (!(obj instanceof MsbUrn)) {
			return false;
		}
		return ((MsbUrn) obj).fullUrn.equals(this.fullUrn);
	}

	/*========== Getter & Setter ==========*/
	public String getDomain() {
		return this.domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getProxy() {
		return proxy;
	}

	public void setProxy(String proxy) {
		this.proxy = proxy;
	}

	public String getSvcName() {
		return svcName;
	}

	public void setSvcName(String svcName) {
		this.svcName = svcName;
	}

	public MsbUrnVer getSvcVersion() {
		return svcVersion;
	}

	public void setSvcVersion(MsbUrnVer svcVersion) {
		this.svcVersion = svcVersion;
	}

	/*==========  ==========*/
	public boolean isLocalDomain(String domain) {
		return this.domain.equals(domain);
	}

	/*========== Interface : Externalizable ==========*/
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(this.fullUrn);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		String msburn = in.readUTF();
		try {
			this.parseUrn(msburn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int compareTo(MsbUrn o) {
		return this.fullUrn.compareTo(o.fullUrn);
	}
}
