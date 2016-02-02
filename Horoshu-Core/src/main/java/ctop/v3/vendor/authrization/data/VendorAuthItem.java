package ctop.v3.vendor.authrization.data;

import java.io.Serializable;

import ctop.v3.msb.common.urn.MsbUrn;

/**
 * 
 * @author DreamInSun
 * 
 */
public class VendorAuthItem implements Serializable, Comparable<VendorAuthItem> {
	private static final long serialVersionUID = 1L;

	/*========== Properties ==========*/
	private MsbUrn msbUrn;
	private Boolean isPermit;

	/*========== Constructor ==========*/
	/**
	 * Only for Default Creation
	 * 
	 * @param msbUrn
	 */
	@Deprecated
	public VendorAuthItem() {

	}

	public VendorAuthItem(MsbUrn msbUrn) {
		this(msbUrn, false);
	}

	public VendorAuthItem(MsbUrn msbUrn, boolean isPermit) {
		this.msbUrn = msbUrn;
		this.isPermit = isPermit;
	}

	/*========== Getter & Setter ==========*/
	public MsbUrn getMsbUrn() {
		return msbUrn;
	}

	public void setMsbUrn(MsbUrn msbUrn) {
		this.msbUrn = msbUrn;
	}

	public Boolean getIsPermit() {
		return isPermit;
	}

	public void setIsPermit(Boolean isPermit) {
		this.isPermit = isPermit;
	}

	/*========== Hash Code ==========*/
	@Override
	public int hashCode() {
		return msbUrn.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (!(obj instanceof VendorAuthItem)) {
			return false;
		}
		return ((VendorAuthItem) obj).msbUrn.equals(this.msbUrn);
	}

	@Override
	public String toString() {
		String tmpRet = null;
		if (isPermit) {
			tmpRet = "[" + msbUrn + "] is Permitted";
		} else {
			tmpRet = "[" + msbUrn + "] is Denied";
		}
		return tmpRet;
	}

	@Override
	public int compareTo(VendorAuthItem o) {
		return msbUrn.fullUrn.compareTo(o.msbUrn.fullUrn);
	}

}
