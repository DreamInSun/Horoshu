package ctop.v3.msb.msbu.mngr.access;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import ctop.v3.msb.common.exception.MsbException;
import ctop.v3.msb.common.urn.MsbUrn;

/**
 * Access Item described an service access control in black-list or white-list.
 * 
 * @author DreamInSun
 * 
 */
public class AccessCtrlItem implements Externalizable {

	/*========== Constant ===========*/
	/**
	 * Access Control Item Type
	 * 
	 * @author DreamInSun
	 * 
	 */
	public enum ECtrlType {
		/** Deny the MSBUs in specified domain to access the MsbUrn Resource */
		DOMAIN,
		/** Deny the specified MSBU to access the MsbUrn Resource */
		MSBU,
	}

	/*========== Properties ==========*/
	/** Service Name */
	private MsbUrn msbUrn;
	/** Control Type */
	private ECtrlType callerType;
	/** the Name of caller, defined by type, can be domain or MSBU name. */
	private String callerName;
	/** */
	private boolean isPermit;

	/*========== Constructor ==========*/
	/**
	 * Only for Interface Serializable
	 */
	@Deprecated
	public AccessCtrlItem() {
	}

	public AccessCtrlItem(String strMsbUrn, ECtrlType type, String callerName, boolean isPermit) throws MsbException {
		this(MsbUrn.parse(strMsbUrn), type, callerName, isPermit);
	}

	/**
	 * Constructor of Access Control Item.
	 * 
	 * @param msbUrn
	 *            MsbUrn of Service aimed to Control
	 * @param type
	 *            AccessCtrlItem.ECtrlType
	 * @param callerName
	 *            canBe Domain or MSBU Name
	 */
	public AccessCtrlItem(MsbUrn msbUrn, ECtrlType type, String callerName, boolean isPermit) {
		this.msbUrn = msbUrn;
		this.callerType = type;
		this.callerName = callerName;
		this.isPermit = isPermit;
	}

	/*========== Getter & Setter ==========*/
	public MsbUrn getMsbUrn() {
		return msbUrn;
	}

	public void setMsbUrn(MsbUrn msbUrn) {
		this.msbUrn = msbUrn;
	}

	public ECtrlType getCallerType() {
		return callerType;
	}

	public void setCallerType(ECtrlType type) {
		this.callerType = type;
	}

	public String getCallerName() {
		return callerName;
	}

	public void setCallerName(String callerName) {
		this.callerName = callerName;
	}

	public void setPermit(boolean isPermit) {
		this.isPermit = isPermit;
	}

	public boolean isPermit() {
		return isPermit;
	}

	/*======================================================================*/
	/*==================== Externalizable : Constructor ====================*/
	/*======================================================================*/
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(msbUrn);
		out.writeUTF(callerType.name());
		out.writeUTF(callerName);
		out.writeBoolean(isPermit);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.msbUrn = (MsbUrn) in.readObject();
		this.callerType = ECtrlType.valueOf(in.readUTF());
		this.callerName = in.readUTF();
		this.isPermit = in.readBoolean();
	}

	/*======================================================================*/
	/*==================== Externalizable : Constructor ====================*/
	/*======================================================================*/

	/*========== getComplexIndex ==========*/
	public String getComplexPrimary() {
		return msbUrn + ":" + callerName;
	}

	/*========== hashCode ==========*/
	@Override
	public int hashCode() {
		return this.getComplexPrimary().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (!(obj instanceof AccessCtrlItem)) {
			return false;
		}
		return ((AccessCtrlItem) obj).getComplexPrimary().equals(this.getComplexPrimary());
	}

	/*========== toString ==========*/
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("{" + msbUrn + "} ").append(callerType.name()).append(" [" + callerName + "] ");
		if (this.isPermit) {
			sb.append("PERMITED ;");
		} else {
			sb.append("DENIED ;");
		}
		return sb.toString();
	}

}
