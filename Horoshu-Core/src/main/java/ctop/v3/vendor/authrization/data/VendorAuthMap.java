package ctop.v3.vendor.authrization.data;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/* Serializable */
import org.dom4j.Element;

/**
 * 
 * @author DreamInSun
 * 
 */
public class VendorAuthMap implements Serializable {
	private static final long serialVersionUID = 1L;

	/*========== Properties ==========*/
	private AppInfo appInfo;
	public Set<VendorAuthItem> vendorAuthMap;

	/*========== Constructor ==========*/
	public VendorAuthMap() {

	}

	public VendorAuthMap(AppInfo appInfo) {
		this.appInfo = appInfo;
		vendorAuthMap = new TreeSet<VendorAuthItem>();
	}

	/*========== Getter & Setter ==========*/
	public AppInfo getAppInfo() {
		return appInfo;
	}

	public void setAppInfo(AppInfo appInfo) {
		this.appInfo = appInfo;
	}

	/*========== Map Management ==========*/

	public void attachVendorAuthItem(VendorAuthItem vendorAuthItem) {
		vendorAuthMap.add(vendorAuthItem);
	}

	public void updateVendorAuthItem(VendorAuthItem vendorAuthItem) {
		vendorAuthMap.remove(vendorAuthItem);
		vendorAuthMap.add(vendorAuthItem);
	}

	/*========== XML Formatter ==========*/
	public Element getElement() {
		Element elmtAuthMap = null;
		//TODOb full fill it
		return elmtAuthMap;
	}

	public static VendorAuthMap parseXML(Element elmt) {
		VendorAuthMap tmpAuthMap = null;
		//TODO full fill it
		return tmpAuthMap;
	}

	/*========== JSON Formatter ==========*/

	@Override
	public String toString() {
		return super.toString();
	}

}
