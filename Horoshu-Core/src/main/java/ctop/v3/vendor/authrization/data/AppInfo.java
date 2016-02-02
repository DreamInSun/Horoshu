package ctop.v3.vendor.authrization.data;

import java.io.Serializable;

public class AppInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	/*========= Properties ==========*/
	private String vendorId;
	private String appId;
	private String name;
	private String namespace;
	private String desc;
	private String iconPath;
	private String licenseKey;

	/*========== Constructor ==========*/
	/**
	 * Only for Debug
	 */
	@Deprecated
	public AppInfo() {

	}

	public AppInfo(String vendorId, String name) {
		this(vendorId, name, null);
	}

	public AppInfo(String vendorId, String name, String desc) {
		this.vendorId = vendorId;
		this.name = name;
		this.desc = desc;
	}

	/*========== Getter & Setter =========*/

	public String getVendorId() {
		return vendorId;
	}

	public void setVendorId(String vendorInfo) {
		this.vendorId = vendorInfo;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getLicenseKey() {
		return licenseKey;
	}

	public void setLicenseKey(String licenseKey) {
		this.licenseKey = licenseKey;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	/*========= toString ==========*/
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(512);
		sb.append("\"AppInfo\":{");
		if (appId != null) {
			sb.append("\"appId\":\"" + appId + "\",");
		}
		if (vendorId != null) {
			sb.append("\"vendorId\":\"" + vendorId + "\",");
		}
		if (name != null) {
			sb.append("\"appName\":\"" + name + "\",");
		}
		if (desc != null) {
			sb.append("\"desc\":\"" + desc + "\",");
		}
		if (licenseKey != null) {
			sb.append("\"licenseKey\":\"" + licenseKey + "\",");
		}
		sb.append("}");
		return sb.toString();
	}

}
