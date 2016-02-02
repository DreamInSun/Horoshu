package ctop.v3.vendor.authrization.interfaces;

import java.util.List;

import ctop.v3.vendor.authrization.data.VendorInfo;

public interface IVendorManage {

	public String[] getVendorIdList();

	public List<VendorInfo> getVendorInfoList();

	public String createVendor(VendorInfo vendorAuthMap);

	public boolean updateVendor(String vendorId, VendorInfo vendorAuthMap);

	public VendorInfo retrieveVendor(String vendorId);

	public boolean deleteVendor(String vendorId);
}
