package ctop.v3.vendor.authrization.interfaces;

import ctop.v3.vendor.authrization.data.VendorAuthMap;

/**
 * Design to Persistent Vendor Authorization Map, (Store Format Independence)
 * 
 * @author DreamInSun
 * 
 */
public interface IVendorAuthPersistence {

	public String getProjectRoot();

	public void createVendorAuthMap(String vendorId, String appId, VendorAuthMap vendorAuthMap);

	public void updateVendorAuthMap(String vendorId, String appId, VendorAuthMap vendorAuthMap);

	public VendorAuthMap retrieveVendorAuthMap(String vendorId, String appId);

	public void deleteVendorAuthMap(String vendorId, String appId);
}
