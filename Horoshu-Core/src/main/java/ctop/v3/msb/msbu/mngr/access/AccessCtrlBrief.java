package ctop.v3.msb.msbu.mngr.access;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Description of Access Control from specified domain. <br/>
 * Always for USRM Command Load & Configuration.
 * 
 * @author DreamInSun
 * 
 */
public class AccessCtrlBrief implements Serializable {
	private static final long serialVersionUID = 1L;

	/*========== Constant ==========*/

	/*========== Enumerate ==========*/
	public enum ETYPE {
		/** Depend for the Master Node Configuration. */
		NODE_CONFIG,
		/** Default Permission is passed, the access control item specified critical permission, always denied. */
		BLACK_LIST,
		/** Default Permission is denied, the access control item specified critical permission, always passed. */
		WHITE_LIST,
	}

	/*========== Properties ==========*/
	private String domain;
	private ETYPE type;
	private List<AccessCtrlItem> acItemsList = new ArrayList<AccessCtrlItem>();

	/*========== Constructor ==========*/
	@Deprecated
	public AccessCtrlBrief(){
		
	}
	
	public AccessCtrlBrief(String domain) {
		this(domain, ETYPE.BLACK_LIST, null);
	}

	public AccessCtrlBrief(String domain, ETYPE type) {
		this(domain, type, null);
	}

	public AccessCtrlBrief(String domain, ETYPE type, List<AccessCtrlItem> acItemList) {
		this.domain = domain;
		this.type = type;
		if (acItemList != null) {
			this.acItemsList = acItemList;
		}
	}

	/*========== Getter & Setter ==========*/
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public ETYPE getType() {
		return type;
	}

	public void setType(ETYPE type) {
		this.type = type;
	}

	public List<AccessCtrlItem> getAcItemsList() {
		return acItemsList;
	}

	public void setAcItemsList(List<AccessCtrlItem> acItemsList) {
		this.acItemsList = acItemsList;
	}

	/*==========  ===========*/
	public void addAccessCrtlItem(AccessCtrlItem acItem) {
		acItemsList.add(acItem);
	}

	/*========== toString ==========*/
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("\r\n/*========== AccessCtrlBrief ===========*/\r\n").append("Domain : " + this.domain + "\t Type : " + this.type.name() + "\r\n");
		for (AccessCtrlItem acItem : this.acItemsList) {
			sb.append(acItem.toString() + "\r\n");
		}
		sb.append("/*========== AccessCtrlBrief ===========*/\r\n");
		return sb.toString();
	}

}
