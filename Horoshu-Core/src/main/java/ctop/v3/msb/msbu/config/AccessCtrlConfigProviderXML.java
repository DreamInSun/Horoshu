package ctop.v3.msb.msbu.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import ctop.v3.msb.common.exception.MsbException;
import ctop.v3.msb.common.urn.MsbDomain;
import ctop.v3.msb.msbu.mngr.access.AccessCtrlBrief;
import ctop.v3.msb.msbu.mngr.access.AccessCtrlItem;

public class AccessCtrlConfigProviderXML implements IAccessCtrlConfigProvider, Serializable {
	private static final long serialVersionUID = 1L;

	/*===== Access Control Item XML Tags & Attributions Name =====*/
	public static final String ACCESS_CTRL_ROOT = "AccessControl";
	public static final String ATTR_ACCESS_CTRL_SLEF_DOMAIN = "domain";

	/* Access Domain Definition */
	public static final String TAG_ACCESS_DOMAIN = "AccessDomain";
	public static final String ATTR_ACCESS_DOMAIN_TYPE = "type";
	public static final String ATTR_ACCESS_DOMAIN_NAME = "domain";

	/* Access Control Item */
	public static final String TAG_ACCESS_ITEM = "AcItem";
	public static final String ATTR_ACCESS_ITEM_MSBURN = "msburn";
	public static final String ATTR_ACCESS_ITEM_PERMIT = "permit";

	/*========== Properties ==========*/
	protected MsbDomain m_SelfDomain;
	protected Element m_elmtAcConfig = null;
	/** KEY : domain, Access Control Manager here used to Store all the items, not for quick search. */
	protected Map<String, AccessCtrlBrief> m_AcMngrMap = new HashMap<String, AccessCtrlBrief>();

	/*========== Constructor ==========*/
	protected AccessCtrlConfigProviderXML() {
	}

	public AccessCtrlConfigProviderXML(Element elmt) throws MsbException {
		setConfigElemnt(elmt);
	}

	public void setConfigElemnt(Element elmt) throws MsbException {
		/*===== SETP 1. Input Protection =====*/
		if (elmt == null) {
			throw new MsbException(MsbException.ErrCode.CONFIG_LOAD_ERROR, "Access Control Configuration NullPoint.");
		}
		if (!elmt.getName().equals(ACCESS_CTRL_ROOT)) {
			throw new MsbException(MsbException.ErrCode.CONFIG_PARSE_ERROR, "Access Control Configuration Error : " + elmt);
		}
		m_elmtAcConfig = elmt;

		/*===== STEP 2. Get Self Domain =====*/
		m_SelfDomain = new MsbDomain(m_elmtAcConfig.attributeValue(ATTR_ACCESS_CTRL_SLEF_DOMAIN));

		/*===== STEP 3. Parsing Configurations =====*/
		@SuppressWarnings("unchecked")
		List<Element> elmt_MsbuInfoList = m_elmtAcConfig.elements(TAG_ACCESS_DOMAIN);
		for (Element elmtAccessDomain : elmt_MsbuInfoList) {
			parseAccessDomain(elmtAccessDomain);
		}
	}

	/**
	 * 
	 * @param elmt
	 * @return
	 */
	private final void parseAccessDomain(Element elmt) throws MsbException {

		/*===== Domain Control =====*/
		String domain = elmt.attributeValue(ATTR_ACCESS_DOMAIN_NAME);
		String strType = elmt.attributeValue(ATTR_ACCESS_DOMAIN_TYPE);
		AccessCtrlBrief acDomainBrief = m_AcMngrMap.get(domain);
		if (acDomainBrief == null) {
			acDomainBrief = new AccessCtrlBrief(domain);
			m_AcMngrMap.put(domain, acDomainBrief);
		}

		/*===== Type Control =====*/
		AccessCtrlBrief.ETYPE type = AccessCtrlBrief.ETYPE.valueOf(strType);
		acDomainBrief.setType(type);

		/*==== Parse Access Control Items =====*/
		@SuppressWarnings("unchecked")
		List<Element> elmtAccessCtrlItems = elmt.elements(TAG_ACCESS_ITEM);

		for (Element elmtAcItem : elmtAccessCtrlItems) {
			AccessCtrlItem acItem = parseAccessCtrlItem(elmtAcItem, domain);
			acDomainBrief.addAccessCrtlItem(acItem);
		}
	}

	/**
	 * 
	 * @param elmt
	 * @return
	 */
	private final AccessCtrlItem parseAccessCtrlItem(Element elmt, String domain) throws MsbException {
		AccessCtrlItem tmpAcItem = null;
		String msburn = elmt.attributeValue(ATTR_ACCESS_ITEM_MSBURN);
		String permit = elmt.attributeValue(ATTR_ACCESS_ITEM_PERMIT);
		boolean isPermit = Boolean.parseBoolean(permit);

		tmpAcItem = new AccessCtrlItem(msburn, AccessCtrlItem.ECtrlType.DOMAIN, domain, isPermit);
		return tmpAcItem;
	}

	/*==============================================================*/
	/*==================== Persistence Function ====================*/
	/*==============================================================*/

	@Override
	public boolean setAccessCtrlBiref(AccessCtrlBrief acBrief) {
		String domain = acBrief.getDomain();
		this.m_AcMngrMap.put(domain, acBrief);
		/*===== Save it in ELement =====*/
		for (AccessCtrlItem acItem : acBrief.getAcItemsList()) {
			updateAccessCtrlConfig(domain, acItem);
		}
		return true;
	}

	public void saveAccessCtrlConfig() {
		if (m_elmtAcConfig != null) {

		}
	}

	public void updateAccessCtrlConfig(String domain, AccessCtrlItem acItem) {

		if (m_elmtAcConfig != null) {
			String xpathAcDomain = String.format("./%s[@%s=%s]", TAG_ACCESS_DOMAIN, ATTR_ACCESS_DOMAIN_NAME, domain);
			List<?> elmtAcDomains = m_elmtAcConfig.selectNodes(xpathAcDomain);
			if (elmtAcDomains != null & elmtAcDomains.size() > 0) {
				Element elmtAcDomain = (Element) elmtAcDomains.get(0);

				String xpathAcItem = String.format("./%s[@%s=\"%s\"]", TAG_ACCESS_ITEM, ATTR_ACCESS_ITEM_MSBURN, acItem.getMsbUrn());
				List<?> elmtAcItems = elmtAcDomain.selectNodes(xpathAcItem);
				if (elmtAcItems != null & elmtAcDomains.size() > 0) {
					//TODO some BUG may cause here.
					for (Object elmtAcItem : elmtAcItems) {
						elmtAcDomain.remove((Element) elmtAcItem);
					}
				}
				addAccessCtrlItem(elmtAcDomain, acItem);
			} else {
				/*===== Create New Access Domain =====*/
				Element elmtAcDomain = m_elmtAcConfig.addElement(TAG_ACCESS_DOMAIN);
				elmtAcDomain.addAttribute(ATTR_ACCESS_DOMAIN_NAME, domain);
				addAccessCtrlItem(elmtAcDomain, acItem);
			}
		}
	}

	private final void addAccessCtrlItem(Element elmtAcDomain, AccessCtrlItem acItem) {
		Element elmtAcItem = elmtAcDomain.addElement(TAG_ACCESS_ITEM);
		elmtAcItem.addAttribute(ATTR_ACCESS_ITEM_MSBURN, acItem.getMsbUrn().fullUrn);
		elmtAcItem.addAttribute(ATTR_ACCESS_ITEM_PERMIT, Boolean.toString(acItem.isPermit()));

	}

	/*===========================================================*/
	/*====================  ====================*/
	/*===========================================================*/
	/**
	 * 
	 * @param domain
	 * @return
	 */
	@Override
	public AccessCtrlBrief getDomainAccessBrief(String domain) {
		return this.m_AcMngrMap.get(domain);
	}

	@Override
	public AccessCtrlItem[] getAccessCtrlList() {
		AccessCtrlItem[] tmpAcItems = null;
		List<AccessCtrlItem> tmpAcItemList = new ArrayList<AccessCtrlItem>();
		for (AccessCtrlBrief acMngr : m_AcMngrMap.values()) {
			for (AccessCtrlItem acItem : acMngr.getAcItemsList()) {
				tmpAcItemList.add(acItem);
			}
		}
		tmpAcItems = new AccessCtrlItem[tmpAcItemList.size()];
		tmpAcItemList.toArray(tmpAcItems);
		return tmpAcItems;
	}

	/*========== Getter & Setter ==========*/
	public MsbDomain getSelfDomain() {
		return this.m_SelfDomain;
	}

	@Override
	public AccessCtrlBrief[] getAccessCtrlBriefs() {
		AccessCtrlBrief[] tmpAcMngrs = null;
		if (m_AcMngrMap != null) {
			int count = m_AcMngrMap.values().size();
			tmpAcMngrs = new AccessCtrlBrief[count];
			m_AcMngrMap.values().toArray(tmpAcMngrs);
		}
		return tmpAcMngrs;
	}

	@Override
	public void setAccessCtrlItem(String domain, AccessCtrlItem acItem) {
		// TODO Auto-generated method stub
		AccessCtrlBrief acMngr = m_AcMngrMap.get(domain);
		if (acMngr == null) {
			acMngr = new AccessCtrlBrief(domain);
			m_AcMngrMap.put(domain, acMngr);
		}
		acMngr.addAccessCrtlItem(acItem);
		/*===== Update Configuration File =====*/
		this.updateAccessCtrlConfig(domain, acItem);

	}

	/*==========  ==========*/
	public interface ISaveConfigFile {
		void saveConfigFile(Element elmtAcConfig);
	}

	private ISaveConfigFile m_saveconfigFile = null;

	public void setSaveConfigFile(ISaveConfigFile saveconfigFile) {
		this.m_saveconfigFile = saveconfigFile;
	}

	public void saveConfigFile() {
		if (m_saveconfigFile != null) {
			m_saveconfigFile.saveConfigFile(m_elmtAcConfig);
		}
	};

}
