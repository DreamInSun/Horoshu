package ctop.v3.msb.bridge.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ctop.v3.msb.MSB;
import ctop.v3.msb.msbu.Msbu;

public class MsbBridgeMap {
	private static final Logger g_Logger = Logger.getLogger(MsbBridgeMap.class);

	/*========== Constant ============*/

	/*==========  ==========*/
	public class BridgeLink {
		/** MSB Domain */
		public String dependingDomain;
		/** MSB Domain */
		public String dependedDomain;
		/** Flag to Identify the Bridge State */
		public boolean isEstablieshed = false;

		BridgeLink(String root, String depend) {
			dependingDomain = root;
			dependedDomain = depend;
		}
	}

	/*========== Properties ============*/
	private String m_DefaultMsbuDomain = null;
	private String m_DefaultMsbuName = null;

	private static List<BridgeLink> g_BridgeDependList = new ArrayList<BridgeLink>();

	/*========== Constructor ============*/
	public MsbBridgeMap() {
	}

	public void establishBridges(Msbu curMsbu) {
		/*  */
		g_Logger.debug("Establish MSBU Bridge when creating £º" + curMsbu.getMsbuInfo().getName());
		/*  */
		String currentMsbDomain = curMsbu.getProxy().getMsbuInfo().getDomain();
		/* Loop to Establish Bridge */
		for (BridgeLink bridgeLink : g_BridgeDependList) {
			if (bridgeLink.isEstablieshed == false) {
				/* Add Link to Current MSBU */
				if (bridgeLink.dependingDomain.equals(currentMsbDomain)) {
					if (curMsbu.getBridgeMsbu(bridgeLink.dependedDomain) == null) {
						Msbu dependedMsbu = MSB.getGlobalMsbuMap().get(bridgeLink.dependedDomain);
						if (dependedMsbu != null) {
							curMsbu.addBridgeMsbu(dependedMsbu);
							bridgeLink.isEstablieshed = true;
							g_Logger.debug("Inject MSBU{" + bridgeLink.dependedDomain + "} to MSBU{" + currentMsbDomain + "}");
						}
					}
				}
				/* Add Current MSBU link to Depending */
				if (bridgeLink.dependedDomain.equals(currentMsbDomain)) {
					Msbu dependingMsbu = MSB.getGlobalMsbuMap().get(bridgeLink.dependingDomain);
					if (dependingMsbu != null && dependingMsbu.getBridgeMsbu(bridgeLink.dependingDomain) == null) {
						if (dependingMsbu != null) {
							dependingMsbu.addBridgeMsbu(curMsbu);
							bridgeLink.isEstablieshed = true;
							g_Logger.debug("Inject MSBU{" + currentMsbDomain + "} to MSBU{" + bridgeLink.dependingDomain + "}");

						}
					}
				}
			}
		}
	}

	private final boolean isDuplicated(String dependingDomain, String dependedDOmain) {
		boolean tmpRet = false;
		for (BridgeLink link : g_BridgeDependList) {
			if (link.dependedDomain.equals(dependedDOmain) && link.dependingDomain.equals(dependingDomain)) {
				tmpRet = true;
			}
		}
		return tmpRet;
	}

	/*==========  ===========*/
	public void addLink(String dependingDomain, String dependedDomain) {
		if (isDuplicated(dependingDomain, dependedDomain) == false) {
			g_BridgeDependList.add(new BridgeLink(dependingDomain, dependedDomain));
		}
	}

	public void addBiLink(String domainName1, String domainName2) {
		if (isDuplicated(domainName1, domainName2) == false) {
			g_BridgeDependList.add(new BridgeLink(domainName1, domainName2));
		}
		if (isDuplicated(domainName2, domainName1) == false) {
			g_BridgeDependList.add(new BridgeLink(domainName2, domainName1));
		}
	}

	/*========== Getter & Setter ==========*/
	public String getDefaultMsbuName() {
		return m_DefaultMsbuName;
	}

	public void setDefaultMsbuName(String msbuName) {
		m_DefaultMsbuName = msbuName;
	}

	public String getDefaultMsbuDomain() {
		return m_DefaultMsbuDomain;
	}

	public void setDefaultMsbuDomain(String msbuDomain) {
		m_DefaultMsbuDomain = msbuDomain;
	}

	/*========== toString ==========*/
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(512).append("\r\n/*========== MSB Bridge Depending Map ===========*/\r\n");
		for (BridgeLink bridgeLink : g_BridgeDependList) {
			sb.append(bridgeLink.dependingDomain + "=>" + bridgeLink.dependedDomain + "\r\n");
		}
		return sb.toString();
	}

}
