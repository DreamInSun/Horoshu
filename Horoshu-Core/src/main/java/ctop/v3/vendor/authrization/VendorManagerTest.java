package ctop.v3.vendor.authrization;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import ctop.v3.vendor.authrization.data.AppInfo;
import ctop.v3.vendor.authrization.data.VendorInfo;

public class VendorManagerTest {
	private static Logger g_Logger = Logger.getLogger(VendorManagerTest.class);

	@Test
	public void testVendorManager() {
		@SuppressWarnings("deprecation")
		VendorManager vendorMngr = new VendorManager();
		//DEBUG
		VendorInfo vendor1 = new VendorInfo("Vendor1", "Vendor for Testing.");
		VendorInfo vendor2 = new VendorInfo("Vendor2", "Vendor for Vanish.");

		vendor2.setBusinessLicense("BA123423587921");
		vendor2.setContact("@$RKJ");
		vendor2.setPhone("12387421");

		String vendor1_id = vendorMngr.createVendor(vendor1);
		String vendor2_id = vendorMngr.createVendor(vendor2);

		vendor1.setDesc("Vendor for Update.");
		vendorMngr.updateVendor(vendor1_id, vendor1);

		VendorInfo vendor3 = vendorMngr.retrieveVendor(vendor1_id);
		VendorInfo vendor4 = vendorMngr.retrieveVendor(vendor2_id);

		assertEquals(vendor1_id, vendor3.getId());
		assertEquals(vendor2_id, vendor4.getId());

		String[] VendorList = vendorMngr.getVendorIdList();
		List<VendorInfo> vendorInfoList = vendorMngr.getVendorInfoList();

		g_Logger.info(VendorList);
		g_Logger.info(vendorInfoList);

		AppInfo app1 = new AppInfo(vendor1_id, "VENDOR_1_APP_1");
		AppInfo app2 = new AppInfo(vendor1_id, "VENDOR_1_APP_2");
		AppInfo app3 = new AppInfo(vendor2_id, "VENDOR_3_APP_3");

		String appId1 = vendorMngr.createAppInfo(app1);
		String appId2 = vendorMngr.createAppInfo(app2);
		String appId3 = vendorMngr.createAppInfo(app3);

		g_Logger.info(appId1 + appId2 + appId3);

		app2.setDesc("NEW");
		vendorMngr.updateAppInfo(appId2, app2);

		String[] appIdList = vendorMngr.getAppIdList(vendor2_id);
		List<AppInfo> appInfoList = vendorMngr.getAppInfoList();

		g_Logger.info(appIdList);
		g_Logger.info(appInfoList);

	}

}
