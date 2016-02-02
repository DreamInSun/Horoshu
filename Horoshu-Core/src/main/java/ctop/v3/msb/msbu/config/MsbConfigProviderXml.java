package ctop.v3.msb.msbu.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.*;

import ctop.v3.msb.bridge.data.MsbBridgeMap;
import ctop.v3.msb.common.exception.MsbException;
import ctop.v3.msb.msbu.Msbu;
import ctop.v3.msb.msbu.data.MsbuInfo;
import ctop.v3.msb.proxy.conn.MsbConnParam;
import ctop.v3.msb.proxy.conn.MsbConnParam.EType;
import ctop.v3.msb.proxy.conn.MsbConnParamMap;

/**
 * This kind of configuration provider can only work on platform.
 * 
 * @author DreamInSun
 * 
 */
public class MsbConfigProviderXml implements IMsbConfigProvider, Serializable {
	private static final long serialVersionUID = 1L;

	private final static Logger g_Logger = Logger.getLogger(MsbConfigProviderXml.class);

	/*========== Constant ==========*/
	public static final String MSB_DOMAIN_DEFAULT = "LOCAL";
	public static final String MSB_SVC_GROUP_DEFAULT = "DEFAULT";

	/*===== Connection Params XML Tags & Attributions Name =====*/
	public static final String CONNECTIONS_ROOT = "Connections";

	public static final String TAG_COON_MSB_DOMAIN = "MsbDomain";
	public static final String ATTR_CONN_MSB_DOMAIN_NAME = "name";

	public static final String TAG_CONN_PARAM = "ConnectParam";

	/* MSBU Configurations */
	public static final String TAG_MSBU_INFO = "MsbuInfo";
	public static final String ATTR_MSB_INFO_DOMAIN = "domain";
	public static final String ATTR_MSB_INFO_NAME = "name";
	public static final String ATTR_MSB_INFO_TYPE = "type";

	/* MSBU Connection Parameters */
	public static final String TAG_MSBU_CONN_PARAM = "MsbConnParam";
	public static final String ATTR_CONN_PARAM_TYPE = "type";
	public static final String ATTR_COMM_PARAM_NAME = "name";
	public static final String ATTR_COMM_PARAM_PASSWD = "password";
	public static final String ATTR_COMM_PARAM_CONNWD = "connect-word";

	/* USRM Module */
	public static final String TAG_MSBU_USRM_MODULE = "UsrmModule";
	public static final String TAG_USRM_MODULE_ROUTER = "RouterConfig";
	public static final String TAG_USRM_MODULE_MANAGER = "ManagerConfig";
	public static final String TAG_USRM_MODULE_PUBLISHER = "PublisherConfig";

	/* MSB Bridge Map */
	public static final String TAG_MSB_BRIDGE = "MsbBridge";
	public static final String ATTR_MSB_BRIDGE_MSB_DOMAIN = "domain";
	public static final String ATTR_MSB_BRIDGE_MSBU_NAME = "name";
	public static final String TAG_MSB_BRIDGE_LINK = "BridgeLink";

	public static final String ATTR_USRM_MODULE_ENABLE = "enable";

	/*========== Properties ==========*/
	/* Store the XML Element */
	protected Element m_elmtConfig;
	/* Cache */
	protected Map<String, MsbuInfo> m_MsbuInfoMap = new LinkedHashMap<String, MsbuInfo>();
	protected Map<String, MsbConnParamMap> m_ConnectParamCache = new HashMap<String, MsbConnParamMap>();
	protected MsbBridgeMap m_MsbBridgeMap;

	protected IAccessCtrlConfigProvider m_AccessCtrlConfigProvider;

	/*========== Constructor ===========*/
	/**
	 * Dynamic Web Application, Load Configuration XML file takes WEB-INFO as root;
	 * 
	 * @param resName
	 */
	public MsbConfigProviderXml(Element elmt) {
		m_elmtConfig = elmt;
	}

	protected MsbConfigProviderXml() {
		m_elmtConfig = null;
	}

	public void setConfigElement(Element elmt) {
		m_elmtConfig = elmt;
		resetCache();
	}

	protected void resetCache() {
		m_ConnectParamCache.clear();
		m_MsbuInfoMap.clear();
		m_MsbBridgeMap = null;
	}

	/*========================================================================*/
	/*==================== Interface : IMsbConfigProvider ====================*/
	/*========================================================================*/

	@Override
	public MsbuInfo[] getMsbuInfoList() {
		if (m_elmtConfig != null) {
			@SuppressWarnings("unchecked")
			List<Element> elmt_MsbuInfoList = m_elmtConfig.elements(TAG_MSBU_INFO);
			for (Element elmt : elmt_MsbuInfoList) {
				MsbuInfo tmpMsbuInfo = parseMsbuInfo(elmt);
				m_MsbuInfoMap.put(tmpMsbuInfo.getDomain(), tmpMsbuInfo);
			}
		}
		MsbuInfo[] tmpRet = new MsbuInfo[m_MsbuInfoMap.size()];
		m_MsbuInfoMap.values().toArray(tmpRet);
		return tmpRet;
	}

	@Override
	public MsbuInfo getMsbuInfo() {
		MsbuInfo tmpRet = null;
		if (m_MsbuInfoMap == null || m_MsbuInfoMap.size() == 0) {
			getMsbuInfoList();
		}
		/* */
		tmpRet = (MsbuInfo) m_MsbuInfoMap.values().toArray()[0];
		return tmpRet;
	}

	/*========== IMsbConfig : getMsbuInfo ==========*/
	@Override
	public MsbuInfo getMsbuInfo(String domain) {
		MsbuInfo tmpMsbuInfo = m_MsbuInfoMap.get(domain);
		if (tmpMsbuInfo == null) {
			if (m_elmtConfig != null) {
				@SuppressWarnings("unchecked")
				List<Element> elmt_MsbuInfoList = m_elmtConfig.elements(TAG_MSBU_INFO);
				for (Element elmt : elmt_MsbuInfoList) {
					if (domain.equals(elmt.addAttribute(ATTR_MSB_INFO_DOMAIN, ""))) {
						tmpMsbuInfo = parseMsbuInfo(elmt);
						break;
					}
				}
			}
		}
		return tmpMsbuInfo;
	}

	/*========== Child Feature Configurations Parser ==========*/
	private final MsbuInfo parseMsbuInfo(Element elmntConfig) {
		MsbuInfo tmpMsbuInfo = null;

		if (elmntConfig != null) {
			/* MsbuBasic Info */
			String domain = elmntConfig.attributeValue(ATTR_MSB_INFO_DOMAIN, "");
			String name = elmntConfig.attributeValue(ATTR_MSB_INFO_NAME, "");
			String type = elmntConfig.attributeValue(ATTR_MSB_INFO_TYPE, Msbu.EType.CALLER.name());
			try {
				tmpMsbuInfo = new MsbuInfo(domain, name, type);
			} catch (MsbException exp) {
				g_Logger.error(exp.getMessage());
			}
			/* MSB Proxy Connection */
			tmpMsbuInfo.msbConnParam = this.parseMsbConnParam(elmntConfig.element(TAG_MSBU_CONN_PARAM));
			/* USRM Module Configuration */
			Element elmt_UsrmModule = elmntConfig.element(TAG_MSBU_USRM_MODULE);
			parseRouterConfig(tmpMsbuInfo, elmt_UsrmModule.element(TAG_USRM_MODULE_ROUTER));
			parseMasterConfig(tmpMsbuInfo, elmt_UsrmModule.element(TAG_USRM_MODULE_MANAGER));
			parsePublisherConfig(tmpMsbuInfo, elmt_UsrmModule.element(TAG_USRM_MODULE_PUBLISHER));
		} else {
			g_Logger.error("Get MSBU Info from ConfigProvader Error, Null Pointer.");
		}
		return tmpMsbuInfo;
	}

	private final void parseRouterConfig(MsbuInfo msbuInfo, Element elmntConfig) {
		if (elmntConfig != null) {
			if ("true".equals(elmntConfig.attributeValue(ATTR_USRM_MODULE_ENABLE, "false"))) {
				msbuInfo.isRouterEnable = true;
			}
		}
	}

	private final void parseMasterConfig(MsbuInfo msbuInfo, Element elmntConfig) {
		if (elmntConfig != null) {
			if ("true".equals(elmntConfig.attributeValue(ATTR_USRM_MODULE_ENABLE, "false"))) {
				msbuInfo.isManagerEnable = true;
				@SuppressWarnings("unchecked")
				Iterator<Attribute> attrs = elmntConfig.attributeIterator();
				while (attrs.hasNext()) {
					Attribute attr = attrs.next();
					String key = attr.getName();
					if (key != null) {
						String value = attr.getValue();
						msbuInfo.masterParams.put(key, value);
					}
				}
			}
		}
	}

	private final void parsePublisherConfig(MsbuInfo msbuInfo, Element elmntConfig) {
		if (elmntConfig != null) {
			if ("true".equals(elmntConfig.attributeValue(ATTR_USRM_MODULE_ENABLE, "false"))) {
				msbuInfo.isPublisherEnable = true;
				@SuppressWarnings("unchecked")
				Iterator<Attribute> attrs = elmntConfig.attributeIterator();
				while (attrs.hasNext()) {
					Attribute attr = attrs.next();
					String key = attr.getName();
					if (key != null) {
						String value = attr.getValue();
						msbuInfo.publisherParams.put(key, value);
					}
				}
			}
		}
	}

	private final MsbConnParam parseMsbConnParam(Element elmntConfig) {
		MsbConnParam tmpMsbConnParam = null;
		if (elmntConfig != null) {
			String strType = elmntConfig.attributeValue(ATTR_CONN_PARAM_TYPE);
			MsbConnParam.EType type = MsbConnParam.EType.valueOf(strType);
			String userName = elmntConfig.attributeValue(MsbConfigProviderXml.ATTR_COMM_PARAM_NAME, "");
			String passWord = elmntConfig.attributeValue(MsbConfigProviderXml.ATTR_COMM_PARAM_PASSWD, "");
			String connectWord = elmntConfig.attributeValue(MsbConfigProviderXml.ATTR_COMM_PARAM_CONNWD, "");
			/* Add to Cache */
			tmpMsbConnParam = new MsbConnParam(type, userName, passWord, connectWord);
		}
		return tmpMsbConnParam;
	}

	/*========== IMsbConfigProvider : getConnectionParamMap ===========*/
	@SuppressWarnings({ "unchecked" })
	@Override
	public MsbConnParamMap getConnectionParamMap(String msbDomain) {
		MsbConnParamMap tmpConnParamMap = null;
		/* Fill Default Value */
		if (msbDomain == null) {
			msbDomain = MSB_DOMAIN_DEFAULT;
		}
		/* Search Cache */
		tmpConnParamMap = m_ConnectParamCache.get(msbDomain);
		/* Cannot find, load from XML */
		if (tmpConnParamMap == null && m_elmtConfig != null) {
			/* Find Connections Domain */
			Iterator<Element> elmt_MsbDomaims = m_elmtConfig.element(CONNECTIONS_ROOT).elementIterator(TAG_COON_MSB_DOMAIN);
			while (elmt_MsbDomaims.hasNext()) {
				/* Separate Connect Param by MSB Domain */
				Element elmt_MsbDomaim = elmt_MsbDomaims.next();
				String domainName = elmt_MsbDomaim.attributeValue(ATTR_CONN_MSB_DOMAIN_NAME, "");
				/* Get Connect Params by parsing configuration file */
				if (msbDomain.equals(domainName)) {
					/* Parse Every Connection Parameters */
					Iterator<Element> elmt_ConnParams = elmt_MsbDomaim.elementIterator(TAG_CONN_PARAM);
					/* Create New Connect Parameters Map in m_ConnectParamMap */
					tmpConnParamMap = new MsbConnParamMap();
					m_ConnectParamCache.put(domainName, tmpConnParamMap);
					/* Parse all Connection Type */
					while (elmt_ConnParams.hasNext()) {
						Element elmt_ConnParam = elmt_ConnParams.next();
						/* Get Connections Params */
						String strType = elmt_ConnParam.attributeValue(ATTR_CONN_PARAM_TYPE);
						MsbConnParam.EType type = MsbConnParam.EType.valueOf(strType);
						String userName = elmt_ConnParam.attributeValue(MsbConfigProviderXml.ATTR_COMM_PARAM_NAME, "");
						String passWord = elmt_ConnParam.attributeValue(MsbConfigProviderXml.ATTR_COMM_PARAM_PASSWD, "");
						String connectWord = elmt_ConnParam.attributeValue(MsbConfigProviderXml.ATTR_COMM_PARAM_CONNWD, "");
						/* Add to Cache */
						MsbConnParam tmpConnParam = new MsbConnParam(type, userName, passWord, connectWord);
						tmpConnParamMap.put(type, tmpConnParam);
					}
					break;
				}
			}
		} else {
			g_Logger.error("Parsing Connection Parameters Error");
		}

		return tmpConnParamMap;
	}

	/*========== IMsbConfigProvider : getConnectionParam ==========*/
	@Override
	public MsbConnParam getConnectionParam(String msbDomain, EType type) {
		MsbConnParam tmpConnParam = null;
		Map<MsbConnParam.EType, MsbConnParam> tmpConnParamMap = getConnectionParamMap(msbDomain);
		if (tmpConnParamMap != null) {
			tmpConnParam = tmpConnParamMap.get(type);
		}
		return tmpConnParam;
	}

	/*==================================================================*/
	/*==================== MSB Bridge Map Functions ====================*/
	/*==================================================================*/

	/*========== IMsbConfigProvider : getMsbBridgeMap ==========*/
	@Override
	public MsbBridgeMap getMsbBridgeMap() {
		if (m_MsbBridgeMap == null) {
			if (m_elmtConfig != null) {
				Element elmt_MsbBridge = m_elmtConfig.element(TAG_MSB_BRIDGE);

				if (elmt_MsbBridge != null) {
					m_MsbBridgeMap = new MsbBridgeMap();
					/* Get Default MSBU Informations */
					String defaultDomain = elmt_MsbBridge.attributeValue(ATTR_MSB_BRIDGE_MSB_DOMAIN);
					m_MsbBridgeMap.setDefaultMsbuDomain(defaultDomain);
					String defaultMsbuName = elmt_MsbBridge.attributeValue(ATTR_MSB_BRIDGE_MSBU_NAME);
					m_MsbBridgeMap.setDefaultMsbuName(defaultMsbuName);
					/* Get Bridge Links */
					@SuppressWarnings("unchecked")
					List<Element> bridgeLinks = elmt_MsbBridge.elements(TAG_MSB_BRIDGE_LINK);
					for (Element link : bridgeLinks) {
						String linkDomain = link.attributeValue(ATTR_MSB_BRIDGE_MSB_DOMAIN);
						if (linkDomain != null) {
							m_MsbBridgeMap.addBiLink(defaultDomain, linkDomain);
						}
					}
				}
			}
		}
		return m_MsbBridgeMap;
	}

	/*================================================================================*/
	/*==================== Access Control Configuration Functions ====================*/
	/*================================================================================*/
	/**
	 * 
	 * @param elmt
	 */
	public void initAccessCtrlProvider(Element elmt) {
		try {
			m_AccessCtrlConfigProvider = new AccessCtrlConfigProviderXML(elmt);
		} catch (MsbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Override
	public IAccessCtrlConfigProvider getAccessControlProvicer() {
		return m_AccessCtrlConfigProvider;
	}
}
