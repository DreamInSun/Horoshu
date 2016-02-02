package ctop.v3.vendor.authrization.data;

import java.io.Serializable;
import java.util.List;

public class VendorInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	/*========== Properties ==========*/
	private String id;
	private String name;
	private String openId;
	private String desc;
	private List<AppInfo> appInfoList;

	/* Organization Informations */
	private String businessLicense;
	private String organizationCode;
	private String homePage;
	/* Contact */
	private String contact;
	private String phone;
	private String email;

	/*========== Constructor ===========*/
	@Deprecated
	public VendorInfo() {

	}

	/**
	 * 
	 * @param name
	 */
	public VendorInfo(String name) {
		this(name, null);
	}

	/**
	 * 
	 * @param name
	 * @param desc
	 */
	public VendorInfo(String name, String desc) {
		this.name = name;
		this.desc = desc;
	}

	/*========== Getter & Setter ===========*/
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void addAppInfo(AppInfo appInfo) {
		this.appInfoList.add(appInfo);
	}

	public void removeAppInfo(AppInfo appInfo) {
		this.appInfoList.remove(appInfo);
	}

	/*========== Getter & Setter ===========*/
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getBusinessLicense() {
		return businessLicense;
	}

	public void setBusinessLicense(String businessLicense) {
		this.businessLicense = businessLicense;
	}

	public String getOrganizationCode() {
		return organizationCode;
	}

	public void setOrganizationCode(String organizationCode) {
		this.organizationCode = organizationCode;
	}

	public String getHomePage() {
		return homePage;
	}

	public void setHomePage(String homePage) {
		this.homePage = homePage;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<AppInfo> getAppInfoList() {
		return appInfoList;
	}

	public void setAppInfoList(List<AppInfo> appIdList) {
		this.appInfoList = appIdList;
	}

	/*========= toString ==========*/
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("\"VendorInfo\":{");
		if (id != null) {
			sb.append("\"id\":\"" + id + "\",");
		}
		if (name != null) {
			sb.append("\"name\":\"" + name + "\",");
		}
		if (desc != null) {
			sb.append("\"desc\":\"" + desc + "\",");
		}
		if (businessLicense != null) {
			sb.append("\"businessLicense\":\"" + businessLicense + "\",");
		}
		if (organizationCode != null) {
			sb.append("\"organizationCode\":\"" + organizationCode + "\",");
		}
		if (contact != null) {
			sb.append("\"contact\":\"" + contact + "\",");
		}
		if (phone != null) {
			sb.append("\"phone\":\"" + phone + "\",");
		}
		if (email != null) {
			sb.append("\"email\":\"" + email + "\",");
		}
		sb.append("}");
		return sb.toString();
	}

}
