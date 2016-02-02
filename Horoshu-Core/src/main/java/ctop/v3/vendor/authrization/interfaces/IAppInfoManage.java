package ctop.v3.vendor.authrization.interfaces;

import java.util.List;

import ctop.v3.vendor.authrization.data.AppInfo;

public interface IAppInfoManage {

	public String[] getAppIdList(String vendorId);

	public List<AppInfo> getAppInfoList();

	public List<AppInfo> getAppInfoListByVendor(String vendorId);

	public String createAppInfo(AppInfo appinfo);

	public boolean updateAppInfo(String appId, AppInfo appinfo);

	public AppInfo retrieveAppInfo(String appId);

	public boolean deleteAppInfo(String appId);

}
