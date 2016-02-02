package ctop.v3.msb.msbu.mngr.access;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.usrm.data.UsrmNodeInfo;
import ctop.v3.msb.util.HashSetMap;

/**
 * Not Used Right now
 * 
 * @author DreamInSun
 * 
 */
public class AccessCtrlManagerMixMap implements IAccessCtrlManager {

	/*========== Constant ==========*/

	/*========== Properties ==========*/
	/** Local Domain */
	private String localDomain;
	private boolean m_DefaultPermission = true;
	/** Key : Domain, Value : Default Access */
	private Map<String, AccessCtrlBrief.ETYPE> m_AccessCtrlDomainDefault = Collections.synchronizedMap(new HashMap<String, AccessCtrlBrief.ETYPE>());
	/** Key AccessCtrlItem description string */
	private Map<String, AccessCtrlItem> m_AccessCtrlMap = Collections.synchronizedMap(new HashMap<String, AccessCtrlItem>());

	/*===== Index =====*/
	/** Index of MSBU, for further optimize */
	private HashSetMap<String, AccessCtrlItem> m_MsbuIndex = new HashSetMap<String, AccessCtrlItem>();
	/** Index of Domain, for further optimize */
	private HashSetMap<String, AccessCtrlItem> m_DomainIndex = new HashSetMap<String, AccessCtrlItem>();
	/**  */
	private HashMap<String, Boolean> m_DomainDefault = new HashMap<String, Boolean>();

	/*========== Constructor ==========*/
	public AccessCtrlManagerMixMap() {
	}

	/*========== Deconstructor ==========*/
	public void finalize() {

	}

	/*========== Getter & Setter ==========*/
	public String getLocalDomain() {
		return localDomain;
	}

	public void setLocalDomain(String domain) {
		this.localDomain = domain;
	}

	/*========== Advanced Getter & Setter ==========*/
	/**
	 * 
	 * @param domain
	 * @param isPermit
	 */
	public void setDomainDefault(String domain, boolean isPermit) {
		m_DomainDefault.put(domain, isPermit);
	}

	/**
	 * 
	 * @param domain
	 * @return
	 */
	public boolean getDomainDefault(String domain) {
		boolean tmpRet = false;
		Boolean isPermit = m_DomainDefault.get(domain);
		if (isPermit != null) {
			tmpRet = isPermit;
		}
		return tmpRet;
	}

	/*========================================================================*/
	/*==================== Interface : IAccessCtrlManager ====================*/
	/*========================================================================*/

	/*========== Access Control Domain Brief Management ==========*/
	@Override
	public void updateAccessDomainBrief(AccessCtrlBrief acBrief) {
		/*===== STEP 1. Get Type =====*/
		String domain = acBrief.getDomain();
		this.m_AccessCtrlDomainDefault.put(domain, acBrief.getType());

		/*===== STEP 2. Set Access Control Items =====*/
		List<AccessCtrlItem> acItemList = acBrief.getAcItemsList();
		for (AccessCtrlItem acItem : acItemList) {
			this.addAccessCrtlItem(acItem);
		}
	}

	/*========== Assertion of Access Services ==========*/

	@Override
	public boolean isPermit(MsbUrn msbUrn, String callerDomain) {
		boolean tmpRet = m_DefaultPermission;
		/*===== STEP 1. Get Domain Default Setting =====*/
		AccessCtrlBrief.ETYPE type = this.m_AccessCtrlDomainDefault.get(callerDomain);
		switch (type) {
		case BLACK_LIST:
			tmpRet = true;
			break;
		case WHITE_LIST:
			tmpRet = false;
			break;
		case NODE_CONFIG:
		default:
			tmpRet = m_DefaultPermission;
			break;
		}
		/*===== STEP 2. Create Complex Primary =====*/
		String complexKey = msbUrn.fullUrn + ":" + callerDomain;
		/* STEP 3. Find Access Control Item */
		AccessCtrlItem acItems = this.m_AccessCtrlMap.get(complexKey);
		if (acItems != null) {
			tmpRet = acItems.isPermit();
		}
		return tmpRet;
	}

	@Override
	public boolean isPermit(MsbUrn msbUrn, UsrmNodeInfo callerInfo) {
		boolean tmpRet = m_DefaultPermission;
		/* STEP 1. Create Complex Primary */
		String complexKey = msbUrn.fullUrn + ":" + callerInfo.getProxyId();
		/* STEP 2. Find Access Control Item */
		AccessCtrlItem acItems = this.m_AccessCtrlMap.get(complexKey);
		if (acItems != null) {
			tmpRet = acItems.isPermit();
		}
		return tmpRet;
	}

	@Override
	public boolean isPermit(String callerDomain) {
		return m_DefaultPermission;
	}

	@Override
	public boolean isPermit(UsrmNodeInfo callerInfo) {
		return m_DefaultPermission;
	}

	/*=======================================================================*/
	/*==================== Access Control Map Management ====================*/
	/*=======================================================================*/

	/**
	 * 
	 */
	public void addAccessCrtlItem(AccessCtrlItem item) {
		/*===== STEP 1. Put Item to Major Map =====*/
		m_AccessCtrlMap.put(item.getComplexPrimary(), item);

		/*===== STEP 2. Update the Index =====*/
		switch (item.getCallerType()) {
		case DOMAIN:
			m_DomainIndex.putValue(item.getCallerName(), item);
			break;
		case MSBU:
			m_MsbuIndex.putValue(item.getCallerName(), item);
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @param msbUrn
	 * @param callerName
	 */
	public void removeAccessCtrlItem(MsbUrn msbUrn, String callerName) {
		/*===== STEP 1. Get Item if Exist =====*/
		String complexPrimary = msbUrn.fullUrn + ":" + callerName;
		AccessCtrlItem tmpItem = m_AccessCtrlMap.get(complexPrimary);

		/*===== STEP 2. Remove Item from Map & Index =====*/
		if (tmpItem != null) {
			m_AccessCtrlMap.remove(complexPrimary);
			switch (tmpItem.getCallerType()) {
			case DOMAIN:
				m_DomainIndex.remove(tmpItem.getCallerName());
				break;
			case MSBU:
				m_MsbuIndex.remove(tmpItem.getCallerName());
				break;
			default:
				break;
			}
		}
	}

	/*====================================================================*/
	/*==================== USRM Management Interfaces ====================*/
	/*====================================================================*/

	/**
	 * for Master to Push Access Control Map.
	 */
	public void updateAccessCtrlMap(AccessCtrlItem[] acItems) {
		for (AccessCtrlItem acItem : acItems) {
			/* If the Complex Primary Key is same, it will override the old Setting. */
			this.addAccessCrtlItem(acItem);
		}
	}

	/**
	 * Get The Access Control Item related to Domain.
	 * 
	 * @param domain
	 * @return
	 */
	public AccessCtrlItem[] getAccessCtrlItems(String domain) {
		AccessCtrlItem[] tmpAcItems = null;
		Set<AccessCtrlItem> tmpAcItemSet = this.m_DomainIndex.get(domain);
		if (tmpAcItemSet != null && tmpAcItemSet.size() > 0) {
			tmpAcItems = new AccessCtrlItem[tmpAcItemSet.size()];
			tmpAcItemSet.toArray(tmpAcItems);
		}
		return tmpAcItems;
	}

	/**
	 * 
	 * @return
	 */
	public AccessCtrlItem[] getAccessCtrlItemList() {
		AccessCtrlItem[] tmpAcItems = null;
		if (m_AccessCtrlMap != null && m_AccessCtrlMap.size() > 0) {
			tmpAcItems = new AccessCtrlItem[m_AccessCtrlMap.size()];
			m_AccessCtrlMap.values().toArray(tmpAcItems);
		}
		return tmpAcItems;
	}

	/**
	 * 
	 * @return
	 */
	public int getCount() {
		return m_AccessCtrlMap.size();
	}

}
