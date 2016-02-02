package ctop.v3.vendor.authrization;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

import com.alibaba.fastjson.JSON;

import ctop.v3.msb.msbu.Msbu;
import ctop.v3.msb.proxy.data.ServiceEntry;
import ctop.v3.vendor.authrization.data.AppInfo;
import ctop.v3.vendor.authrization.data.VendorAuthItem;
import ctop.v3.vendor.authrization.data.VendorAuthMap;
import ctop.v3.vendor.authrization.data.VendorInfo;
import ctop.v3.vendor.authrization.interfaces.IAppInfoManage;
import ctop.v3.vendor.authrization.interfaces.IVendorAuthPersistence;
import ctop.v3.vendor.authrization.interfaces.IVendorManage;

public class VendorManager implements IVendorManage, IAppInfoManage {
	private static final Logger g_Logger = Logger.getLogger(VendorManager.class);

	/*========== Constant ==========*/
	public static final String PARAM_VENDOR_ID = "vendor_id";
	public static final String PARAM_OPEN_ID = "open_id";
	public static final String PARAM_APP_ID = "app_id";
	public static final String PARAM_VENDOR_AUTH_MAP = "vendor_auth_map";
	public static final String PARAM_VENDOR_INFO = "vendor_info";
	public static final String PARAM_APP_INFO = "app_info";

	/* MemCached Constant */
	private static final String MEMCACHED_ADDR = "10.168.6.1:11211";

	/*========== Properties ==========*/
	/* */
	private Msbu m_Msbu;
	/* */
	//private Map<String, VendorInfo> m_VendorMap;
	/* Persistence */
	private IVendorAuthPersistence m_VendorAuthPersistence;

	/* Memcached */
	private MemcachedClientBuilder m_MemCachedClientBuilder;
	private MemcachedClient m_MemCachedClient;

	private Connection m_connDb;

	/*========== Constructor ==========*/
	/**
	 * Only for Module Testing.
	 */
	@Deprecated
	public VendorManager() {
		initDataBase(DB_CONNECT_WORD);
	}

	public VendorManager(Msbu msbu, IVendorAuthPersistence vendorAuthPersistence) throws Exception {
		/*===== STEP 0. Store Necessary Properties =====*/
		this.m_Msbu = msbu;
		this.m_VendorAuthPersistence = vendorAuthPersistence;

		/*===== STEP 1. Input Protection =====*/
		if (m_Msbu.getProxy().getMsbuInfo().isManagerEnable == false) {
			throw new Exception("Current MSBU is not an USRM Service Master. ");
		}

		/*===== STEP 3. Build Memcached =====*/
		List<InetSocketAddress> destMemCachedAddre = AddrUtil.getAddresses(MEMCACHED_ADDR);
		m_MemCachedClientBuilder = new XMemcachedClientBuilder(destMemCachedAddre);
		try {
			m_MemCachedClient = m_MemCachedClientBuilder.build();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*========== STEP 4. Connect Data Base ==========*/
		/**/
		//String connWord = DB_CONNECT_WORD_PROJECT.replace(DB_CONNECT_PROJECT_REPLACEMENT, m_VendorAuthPersistence.getProjectRoot() );
		initDataBase(DB_CONNECT_WORD);
	}

	/*========== XML API ==========*/
	public String[] getVendorNames() {
		String[] tmpVendorNames = null;
		if (m_VendorAuthPersistence != null) {
			//tmpVendorNames = m_VendorAuthPersistence.getVendorList();
		}
		return tmpVendorNames;
	}

	public VendorInfo getVendorInfo(String name) {
		VendorInfo tmpVendor = null;

		return tmpVendor;
	}

	/*========== getGlobalMsbUrnList ==========*/
	private final ServiceEntry[] getGlobalMsbUrnList() {
		return m_Msbu.getUsrmMngr().getMsbUrnList();
	}

	private final void addNewMsbUrn(VendorAuthMap vendorAuthMap) {
		ServiceEntry[] svcEntries = getGlobalMsbUrnList();
		//TODO Optimized it here.
		for (ServiceEntry svcEntry : svcEntries) {
			vendorAuthMap.attachVendorAuthItem(new VendorAuthItem(svcEntry.msbUrn));
		}

	}

	/*=====================================================================*/
	/*==================== JSON Format Transformation =====================*/
	/*=====================================================================*/

	public String getVendorInfolist(Map<String, String[]> params) {
		String tmpJsonRet = null;
		List<VendorInfo> vendorInfoList = this.getVendorInfoList();
		if (vendorInfoList != null && vendorInfoList.size() > 0) {
			tmpJsonRet = JSON.toJSONString(vendorInfoList);
		}
		return tmpJsonRet;
	}

	/*========== POST ==========*/
	public String postVendorInfo(Map<String, String[]> params) {
		String strVendorInfo = params.get(PARAM_VENDOR_INFO)[0];
		VendorInfo vendorInfo = JSON.parseObject(strVendorInfo, VendorInfo.class);
		String vendorId = this.createVendor(vendorInfo);
		return vendorId;
	}

	public String getVendorInfo(Map<String, String[]> params) {
		String tmpJsonRet = null;
		String vendorId = params.get(PARAM_VENDOR_ID)[0];
		VendorInfo vendorInfo = this.getVendorInfo(vendorId);
		if (vendorInfo != null) {
			tmpJsonRet = JSON.toJSONString(vendorInfo);
		}
		return tmpJsonRet;
	}

	public String verifyOpenId(Map<String, String[]> params) {
		//String tmpJsonRet = null;
		String openId = params.get(PARAM_OPEN_ID)[0];
		boolean isValid = this.verifyOpenId(openId);
		return Boolean.toString(isValid);
	}

	/*==========================================================*/
	/*==================== Application Info ====================*/
	/*==========================================================*/

	public String getAppInfoList(Map<String, String[]> params) {
		String tmpJsonRet = null;
		List<AppInfo> appInfo = this.getAppInfoList();
		if (appInfo != null) {
			tmpJsonRet = JSON.toJSONString(appInfo);
		}
		return tmpJsonRet;
	}

	public String getAppInfoListByVendor(Map<String, String[]> params) {
		String tmpJsonRet = null;
		try {
			String vendorId = params.get(PARAM_VENDOR_ID)[0];
			if (vendorId != null) {
				List<AppInfo> appInfo = this.getAppInfoListByVendor(vendorId);
				if (appInfo != null) {
					tmpJsonRet = JSON.toJSONString(appInfo);
				}
			}
		} catch (NullPointerException exp) {
			g_Logger.error("Request Vendor Id is Null.");
		}
		return tmpJsonRet;
	}

	/*========== POST ==========*/
	public String postAppInfo(Map<String, String[]> params) {
		String tmpJsonRet = null;
		String vendorId = params.get(PARAM_VENDOR_ID)[0];
		String strAppInfo = params.get(PARAM_APP_INFO)[0];
		if (strAppInfo != null) {
			AppInfo appInfo = JSON.parseObject(strAppInfo, AppInfo.class);
			appInfo.setVendorId(vendorId);
			tmpJsonRet = this.createAppInfo(appInfo);
		}
		return tmpJsonRet;
	}

	/*========== PUT ==========*/
	public String putAppInfo(Map<String, String[]> params) {
		String tmpJsonRet = null;
		String appId = params.get(PARAM_APP_ID)[0];
		String strAppInfo = params.get(PARAM_APP_INFO)[0];
		if (strAppInfo != null) {
			AppInfo appInfo = JSON.parseObject(strAppInfo, AppInfo.class);
			if (appId == null) {
				appId = appInfo.getAppId();
			}
			boolean res = this.updateAppInfo(appId, appInfo);
			tmpJsonRet = Boolean.toString(res);
		}
		return tmpJsonRet;
	}

	/*========== GET ==========*/
	public String getAppInfo(Map<String, String[]> params) {
		String tmpJsonRet = null;
		String appId = params.get(PARAM_APP_ID)[0];
		if (appId != null) {
			AppInfo appInfo = this.retrieveAppInfo(appId);
			if (appInfo != null) {
				tmpJsonRet = JSON.toJSONString(appInfo);
			}
		}
		return tmpJsonRet;
	}

	/*========== DELETE ==========*/
	public String deleteAppInfo(Map<String, String[]> params) {
		String tmpJsonRet = null;
		String vendorId = params.get(PARAM_VENDOR_ID)[0];
		if (vendorId != null) {
			List<AppInfo> appInfo = this.getAppInfoListByVendor(vendorId);
			if (appInfo != null) {
				tmpJsonRet = JSON.toJSONString(appInfo);
			}
		}
		return tmpJsonRet;
	}

	/*==================================================================*/
	/*==================== Vendor Authorization Map ====================*/
	/*==================================================================*/

	/*========== POST ===========*/
	public void postVendorAuthMap(Map<String, String[]> params) {
		/* Get Parameters */
		String vendorId = params.get(PARAM_VENDOR_ID)[0];
		String appId = params.get(PARAM_APP_ID)[0];
		String strVendorAuthMap = params.get(PARAM_VENDOR_AUTH_MAP)[0];
		VendorAuthMap vendorAuthMap = JSON.parseObject(strVendorAuthMap, VendorAuthMap.class);
		/* Add new MsbUrn */
		addNewMsbUrn(vendorAuthMap);
		/* Update cache */
		if (m_MemCachedClient != null) {
			String complexKey = generateCacheComplexKey(vendorId, appId);
			try {
				m_MemCachedClient.set(complexKey, 0, vendorAuthMap);
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MemcachedException e) {
				e.printStackTrace();
			}
		}
		/* Save File */
		if (m_VendorAuthPersistence != null) {
			m_VendorAuthPersistence.createVendorAuthMap(vendorId, appId, vendorAuthMap);
		}
	}

	/*========== GET ===========*/
	public String getVendorAuthMap(Map<String, String[]> params) {
		String tmpJsonRet = null;
		/* Get Parameters */
		String vendorId = params.get(PARAM_VENDOR_ID)[0];
		String appId = params.get(PARAM_APP_ID)[0];
		/* Load */
		if (m_VendorAuthPersistence != null) {
			VendorAuthMap vendorAuthMap = m_VendorAuthPersistence.retrieveVendorAuthMap(vendorId, appId);
			if (vendorAuthMap != null) {
				tmpJsonRet = JSON.toJSONString(vendorAuthMap);
			}
		}
		return tmpJsonRet;
	}

	/*========== PUT ==========*/
	public void putVendorAuthMap(Map<String, String[]> params) {
		/* Get Parameters */
		String vendorId = params.get(PARAM_VENDOR_ID)[0];
		String appId = params.get(PARAM_APP_ID)[0];
		String strVendorAuthMap = params.get(PARAM_VENDOR_AUTH_MAP)[0];
		VendorAuthMap vendorAuthMap = JSON.parseObject(strVendorAuthMap, VendorAuthMap.class);
		/* Save File */
		if (m_VendorAuthPersistence != null) {
			m_VendorAuthPersistence.updateVendorAuthMap(vendorId, appId, vendorAuthMap);
		}
	}

	/*========== DELETE ===========*/
	public void deleteVendorAuthMap(Map<String, String[]> params) {
		/* Get Parameters */
		String vendorId = params.get(PARAM_VENDOR_ID)[0];
		String appId = params.get(PARAM_APP_ID)[0];
		/* Save File */
		if (m_VendorAuthPersistence != null) {
			m_VendorAuthPersistence.deleteVendorAuthMap(vendorId, appId);
		}
	}

	/*========== Cache Functions ==========*/
	private final String generateCacheComplexKey(String vendorId, String appId) {
		return "VendorAuthMap@" + vendorId + ":" + "appId";
	}

	/*=============================================================*/
	/*==================== Database Definition ====================*/
	/*=============================================================*/

	/* Database Constant */
	public static final String DB_CONNECT_PROJECT_REPLACEMENT = "$project$";
	public static final String DB_CONNECT_WORD_PROJECT = "jdbc:h2:$project$/DataBase/VendorInfo";
	public static final String DB_CONNECT_WORD = "jdbc:h2:~/VendorInfo";
	final static String DB_USER_NAME = "SA";
	final static String DB_PASS_WORD = "";

	public static final String DB_TABLE_VENDOR = "T_VENDOR_INFO";
	public static final String DB_TABLE_APP = "T_APP_INFO";

	/* Create Table Command : Vendor Information */
	final static String SQL_CMD_INIT_TABLE_VENDOR = ////////////////////////////////////
	"CREATE TABLE IF NOT EXISTS T_VENDOR_INFO  ( " + ////////////////////////////////////
			"	vendor_id varchar(256) PRIMARY KEY, " + /////////////////////////////////
			"	open_id varchar(256), " + ///////////////////////////////////////////////
			"	name varchar(256), " + //////////////////////////////////////////////////
			"	desc varchar(4096), " + //////////////////////////////////////////////////
			"	biz_license varchar(256), " + ///////////////////////////////////////////
			"	org_code varchar(256), " + //////////////////////////////////////////////
			"	home_page varchar(256), " + /////////////////////////////////////////////
			"	contact varchar(256), " + ///////////////////////////////////////////////
			"	phone varchar(256), " + /////////////////////////////////////////////////
			"	email varchar(256), " + /////////////////////////////////////////////////		 
			"	update_time long, " + /////////////////////////////////////////////////// 
			"	create_time long" + /////////////////////////////////////////////////////
			");";

	/*===== CRUD =====*/
	final static String SQL_CMD_INSERT_VENDER_INFO = "INSERT INTO T_VENDOR_INFO (vendor_id, name, open_id, desc, biz_license, org_code, home_page, contact, phone, email, update_time, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	final static String SQL_CMD_UPDATE_VENDER_INFO = "UPDATE T_VENDOR_INFO SET name = ?, open_id = ?, desc = ?, biz_license = ?, org_code = ?, home_page = ?, contact = ?, phone = ?, email = ?, update_time = ? WHERE vendor_id = ?;";
	final static String SQL_CMD_SEARCH_VENDER_BY_ID = "SELECT * FROM T_VENDOR_INFO WHERE vendor_id = ?;";
	final static String SQL_CMD_SEARCH_VENDER_BY_NAME = "SELECT * FROM T_VENDOR_INFO WHERE name = ?;";
	final static String SQL_CMD_DELETE_VENDER_BY_ID = "DELETE FROM T_VENDOR_INFO WHERE vendor_id = ?;";
	final static String SQL_CMD_GET_VENDER_LIST = "SELECT vendor_id FROM T_VENDOR_INFO;";
	final static String SQL_CMD_GET_VENDER_INFO_LIST = "SELECT * FROM T_VENDOR_INFO;";
	final static String SQL_CMD_VERIFY_OPEN_ID = "SELECT * FROM T_VENDOR_INFO WHERE open_id = ?;";

	/*========== SQL Command : AppInfo ==========*/

	/* Create Table Command : Application Information */
	final static String SQL_CMD_INIT_TABLE_APPINFO = ///////////////////////////////////
	"CREATE TABLE IF NOT EXISTS T_APP_INFO  ( " + ///////////////////////////////////////
			"	app_id varchar(256) PRIMARY KEY, " + ////////////////////////////////////
			"	vendor_id varchar(256), " + /////////////////////////////////////////////
			"	name varchar(256), " + //////////////////////////////////////////////////
			"   namespace varchar(256), " + /////////////////////////////////////////////
			"   icon_path varchar(256), " + /////////////////////////////////////////////
			"	desc varchar(4096), " + /////////////////////////////////////////////////
			"	license_key varchar(256), " + ///////////////////////////////////////////
			"	update_time long, " + ///////////////////////////////////////////////////
			"	create_time long, " + ///////////////////////////////////////////////////
			"	FOREIGN KEY (vendor_id) REFERENCES T_VENDOR_INFO(vendor_id)" + //////////
			");";

	/*===== CRUD =====*/
	final static String SQL_CMD_INSERT_APP_INFO = "INSERT INTO T_APP_INFO (app_id, vendor_id, name, namespace, icon_path, desc, license_key, update_time, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
	final static String SQL_CMD_UPDATE_APP_INFO = "UPDATE T_APP_INFO SET vendor_id = ?, name = ?, namespace=?, icon_path=?, desc = ?, license_key = ?, update_time = ? WHERE app_id = ?";
	final static String SQL_CMD_SEARCH_APP_BY_ID = "SELECT * FROM T_APP_INFO WHERE app_id = ?;";
	final static String SQL_CMD_DELETE_APP_BY_ID = "DELETE FROM T_APP_INFO WHERE app_id = ?;";
	final static String SQL_CMD_SEARCH_APP_BY_VENDORID = "SELECT * FROM T_APP_INFO WHERE vendor_id = ?;";
	final static String SQL_CMD_DELETE_APP_BY_VENDORID = "DELETE FROM T_APP_INFO WHERE vendor_id = ?;";
	final static String SQL_CMD_GET_APP_ID_LIST = "SELECT app_id FROM T_APP_INFO;";
	final static String SQL_CMD_GET_APP_INFO_LIST = "SELECT * FROM T_APP_INFO;";

	/*=============================================================*/
	/*==================== DataBase Management ====================*/
	/*=============================================================*/

	private final void initDataBase(String connectWord) {
		g_Logger.info("Connecting DataBse : " + connectWord);
		try {

			org.h2.Driver.load();
			m_connDb = DriverManager.getConnection(connectWord, DB_USER_NAME, DB_PASS_WORD);
			/* Create Table if Not Exist*/
			createTables();

			/*==========  ==========*/

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private final boolean createTables() throws SQLException {
		int cnt = this.executeUpdate(SQL_CMD_INIT_TABLE_VENDOR);
		int cnt2 = this.executeUpdate(SQL_CMD_INIT_TABLE_APPINFO);
		return (cnt != 0 && cnt2 != 0);
	}

	/*========== Assistant Functions ==========*/
	private final long getNow() {
		return System.currentTimeMillis();
	}

	private final int executeUpdate(String command) {
		int cnt = 0;
		if (m_connDb != null) {
			try {
				/* Create Statement */
				Statement stm = m_connDb.createStatement();
				cnt = stm.executeUpdate(command);
				/* Close Statement */
				//stm.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return cnt;
	}

	public final ResultSet executeQuery(String command) {
		ResultSet rs = null;
		if (m_connDb != null) {
			Statement stm;
			try {
				/* Create Statement */
				stm = m_connDb.createStatement();
				rs = stm.executeQuery(command);
				/* Close Statement */
				stm.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return rs;
	}

	/*=====================================================================*/
	/*==================== Interface : IVendorManage =====================*/
	/*=====================================================================*/
	@Override
	public String[] getVendorIdList() {
		String[] tmpVendorList = null;
		if (m_connDb != null) {
			try {
				Statement stmt = m_connDb.createStatement();
				ResultSet rs = stmt.executeQuery(SQL_CMD_GET_VENDER_LIST);
				List<String> vendorList = new ArrayList<String>();
				while (rs.next()) {
					vendorList.add(rs.getString("vendor_id"));
				}
				/* Close Statement */
				stmt.close();
				/* Transform */
				tmpVendorList = new String[vendorList.size()];
				vendorList.toArray(tmpVendorList);

			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		return tmpVendorList;
	}

	@Override
	public List<VendorInfo> getVendorInfoList() {
		List<VendorInfo> tmpVendorList = null;
		if (m_connDb != null) {
			try {
				Statement stmt = m_connDb.createStatement();
				ResultSet rs = stmt.executeQuery(SQL_CMD_GET_VENDER_INFO_LIST);
				tmpVendorList = new ArrayList<VendorInfo>();
				while (rs.next()) {
					VendorInfo vendor = extractVendorInfo(rs);
					tmpVendorList.add(vendor);
				}
				/* Close Statement */
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tmpVendorList;
	}

	public boolean verifyOpenId(String openId) {
		boolean tmpRet = true;
		if (m_connDb != null) {
			try {
				PreparedStatement pstmt = m_connDb.prepareStatement(SQL_CMD_VERIFY_OPEN_ID);
				pstmt.setString(1, openId);
				ResultSet rs = pstmt.executeQuery();

				/* Get Object Form Result Set */
				if (rs.next()) {
					tmpRet = false;
				}
				/* Close Statement */
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tmpRet;
	}

	@Override
	public String createVendor(VendorInfo vendorInfo) {
		String vendorId = null;
		if (m_connDb != null) {
			/*===== STEP 1. Generate Vendor ID =====*/
			vendorId = generateVendorID();
			vendorInfo.setId(vendorId);

			/*===== STEP 2.  =====*/
			try {
				int idx = 1;
				PreparedStatement pstmt = m_connDb.prepareStatement(SQL_CMD_INSERT_VENDER_INFO);
				pstmt.setString(idx++, vendorInfo.getId());
				pstmt.setString(idx++, vendorInfo.getName());
				pstmt.setString(idx++, vendorInfo.getOpenId());
				pstmt.setString(idx++, vendorInfo.getDesc());
				pstmt.setString(idx++, vendorInfo.getBusinessLicense());
				pstmt.setString(idx++, vendorInfo.getOrganizationCode());
				pstmt.setString(idx++, vendorInfo.getHomePage());
				pstmt.setString(idx++, vendorInfo.getContact());
				pstmt.setString(idx++, vendorInfo.getPhone());
				pstmt.setString(idx++, vendorInfo.getEmail());
				pstmt.setLong(idx++, getNow());
				pstmt.setLong(idx++, getNow());
				int cnt = pstmt.executeUpdate();
				/* Close Statement */
				pstmt.close();
				/* Check Insert Result */
				if (cnt == 0) {
					vendorId = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		return vendorId;
	}

	@Override
	public boolean updateVendor(String vendorId, VendorInfo vendorInfo) {
		int cnt = 0;
		if (m_connDb != null) {
			try {
				int idx = 1;
				PreparedStatement pstmt = m_connDb.prepareStatement(SQL_CMD_UPDATE_VENDER_INFO);
				pstmt.setString(idx++, vendorInfo.getName());
				pstmt.setString(idx++, vendorInfo.getOpenId());
				pstmt.setString(idx++, vendorInfo.getDesc());
				pstmt.setString(idx++, vendorInfo.getBusinessLicense());
				pstmt.setString(idx++, vendorInfo.getOrganizationCode());
				pstmt.setString(idx++, vendorInfo.getHomePage());
				pstmt.setString(idx++, vendorInfo.getContact());
				pstmt.setString(idx++, vendorInfo.getPhone());
				pstmt.setString(idx++, vendorInfo.getEmail());
				pstmt.setLong(idx++, getNow());
				pstmt.setString(idx++, vendorId);
				cnt = pstmt.executeUpdate();
				/* Close Statement */
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return (cnt != 0);
	}

	@Override
	public VendorInfo retrieveVendor(String vendorId) {
		VendorInfo tmpVendorInfo = null;
		if (m_connDb != null) {
			try {
				PreparedStatement pstmt = m_connDb.prepareStatement(SQL_CMD_SEARCH_VENDER_BY_ID);
				pstmt.setString(1, vendorId);
				ResultSet rs = pstmt.executeQuery();

				/* Get Object Form Result Set */
				if (rs.next()) {
					tmpVendorInfo = extractVendorInfo(rs);
				}
				/* Close Statement */
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tmpVendorInfo;
	}

	@Override
	public boolean deleteVendor(String vendorId) {
		int cnt = 0;
		if (m_connDb != null) {
			try {
				PreparedStatement pstmt = m_connDb.prepareStatement(SQL_CMD_DELETE_VENDER_BY_ID);
				pstmt.setString(1, vendorId);
				cnt = pstmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return cnt != 0;
	}

	/*========== Assistant Functions ==========*/
	private final String generateVendorID() {
		String vendorId;
		VendorInfo vendorInfo = null;
		do {
			vendorId = UUID.randomUUID().toString();
			vendorInfo = retrieveVendor(vendorId);
		} while (vendorInfo != null);
		return vendorId;
	}

	private VendorInfo extractVendorInfo(ResultSet rs) throws SQLException {
		@SuppressWarnings("deprecation")
		VendorInfo tmpVendorInfo = new VendorInfo();
		tmpVendorInfo.setId(rs.getString("vendor_id"));
		tmpVendorInfo.setName(rs.getString("name"));
		tmpVendorInfo.setOpenId(rs.getString("open_id"));
		tmpVendorInfo.setDesc(rs.getString("desc"));
		tmpVendorInfo.setBusinessLicense(rs.getString("biz_license"));
		tmpVendorInfo.setOrganizationCode(rs.getString("org_code"));
		tmpVendorInfo.setHomePage(rs.getString("home_page"));
		tmpVendorInfo.setContact(rs.getString("contact"));
		tmpVendorInfo.setPhone(rs.getString("phone"));
		tmpVendorInfo.setEmail(rs.getString("email"));
		return tmpVendorInfo;
	}

	/*=====================================================================*/
	/*==================== Interface : IAppInfoManage =====================*/
	/*=====================================================================*/
	@Override
	public String[] getAppIdList(String vendorId) {
		String[] appIdList = null;
		if (m_connDb != null) {
			try {
				Statement stmt = m_connDb.createStatement();
				ResultSet rs = stmt.executeQuery(SQL_CMD_GET_APP_ID_LIST);
				List<String> strAppIdList = new ArrayList<String>();
				while (rs.next()) {
					strAppIdList.add(rs.getString("app_id"));
				}
				/* Close Statement */
				stmt.close();
				/* Transform */
				appIdList = new String[strAppIdList.size()];
				strAppIdList.toArray(appIdList);

			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		return appIdList;
	}

	@Override
	public List<AppInfo> getAppInfoList() {
		List<AppInfo> appInfoList = null;
		if (m_connDb != null) {
			try {
				Statement stmt = m_connDb.createStatement();
				ResultSet rs = stmt.executeQuery(SQL_CMD_GET_APP_INFO_LIST);
				appInfoList = new ArrayList<AppInfo>();
				while (rs.next()) {
					AppInfo vendor = extractAppInfo(rs);
					appInfoList.add(vendor);
				}
				/* Close Statement */
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return appInfoList;
	}

	@Override
	public List<AppInfo> getAppInfoListByVendor(String vendorId) {
		List<AppInfo> appInfoList = null;
		if (m_connDb != null) {
			try {
				PreparedStatement pstmt = m_connDb.prepareStatement(SQL_CMD_SEARCH_APP_BY_VENDORID);
				pstmt.setString(1, vendorId);
				ResultSet rs = pstmt.executeQuery();
				appInfoList = new ArrayList<AppInfo>();
				while (rs.next()) {
					AppInfo vendor = extractAppInfo(rs);
					appInfoList.add(vendor);
				}
				/* Close Statement */
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return appInfoList;
	}

	@Override
	public String createAppInfo(AppInfo appInfo) {
		String appId = null;
		if (m_connDb != null) {
			/*===== STEP 1. Generate Vendor ID =====*/
			appId = generateAppID();
			appInfo.setAppId(appId);

			/*===== STEP 2.  =====*/
			try {
				int idx = 1;
				PreparedStatement pstmt = m_connDb.prepareStatement(SQL_CMD_INSERT_APP_INFO);
				pstmt.setString(idx++, appInfo.getAppId());
				pstmt.setString(idx++, appInfo.getVendorId());
				pstmt.setString(idx++, appInfo.getName());
				pstmt.setString(idx++, appInfo.getNamespace());
				pstmt.setString(idx++, appInfo.getIconPath());
				pstmt.setString(idx++, appInfo.getDesc());
				pstmt.setString(idx++, appInfo.getLicenseKey());
				pstmt.setLong(idx++, getNow());
				pstmt.setLong(idx++, getNow());
				int cnt = pstmt.executeUpdate();
				/* Close Statement */
				pstmt.close();
				/* Check Insert Result */
				if (cnt == 0) {
					appId = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return appId;
	}

	@Override
	public boolean updateAppInfo(String appId, AppInfo appinfo) {
		int cnt = 0;
		if (m_connDb != null) {
			try {
				int idx = 1;
				PreparedStatement pstmt = m_connDb.prepareStatement(SQL_CMD_UPDATE_APP_INFO);
				pstmt.setString(idx++, appinfo.getVendorId());
				pstmt.setString(idx++, appinfo.getName());
				pstmt.setString(idx++, appinfo.getNamespace());
				pstmt.setString(idx++, appinfo.getIconPath());
				pstmt.setString(idx++, appinfo.getDesc());
				pstmt.setString(idx++, appinfo.getLicenseKey());
				pstmt.setLong(idx++, getNow());
				pstmt.setString(idx++, appinfo.getAppId());
				cnt = pstmt.executeUpdate();
				/* Close Statement */
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return (cnt != 0);
	}

	@Override
	public AppInfo retrieveAppInfo(String appId) {
		AppInfo tmpAppInfo = null;
		if (m_connDb != null) {
			try {
				PreparedStatement pstmt = m_connDb.prepareStatement(SQL_CMD_SEARCH_APP_BY_ID);
				pstmt.setString(1, appId);
				ResultSet rs = pstmt.executeQuery();

				/* Get Object Form Result Set */
				if (rs.next()) {
					tmpAppInfo = extractAppInfo(rs);
				}
				/* Close Statement */
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tmpAppInfo;
	}

	@Override
	public boolean deleteAppInfo(String appId) {
		int cnt = 0;
		if (m_connDb != null) {
			try {
				PreparedStatement pstmt = m_connDb.prepareStatement(SQL_CMD_DELETE_APP_BY_ID);
				pstmt.setString(1, appId);
				cnt = pstmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return (cnt != 0);
	}

	/*========== Assistant Functions ==========*/
	private final String generateAppID() {
		String appId;
		AppInfo appInfo = null;
		do {
			appId = UUID.randomUUID().toString();
			appInfo = retrieveAppInfo(appId);
		} while (appInfo != null);
		return appId;
	}

	private AppInfo extractAppInfo(ResultSet rs) throws SQLException {
		@SuppressWarnings("deprecation")
		AppInfo tmpAppInfo = new AppInfo();
		tmpAppInfo.setAppId(rs.getString("app_id"));
		tmpAppInfo.setVendorId(rs.getString("vendor_id"));
		tmpAppInfo.setName(rs.getString("name"));
		tmpAppInfo.setNamespace(rs.getString("namespace"));
		tmpAppInfo.setIconPath(rs.getString("icon_path"));
		tmpAppInfo.setDesc(rs.getString("desc"));
		tmpAppInfo.setLicenseKey(rs.getString("license_key"));
		return tmpAppInfo;
	}

	/*========== Assistant Function ==========*/
	public VendorAuthMap getDefaultVendorMap(String appId) {
		AppInfo appInfo = this.retrieveAppInfo(appId);
		VendorAuthMap vendorAuthMap = new VendorAuthMap(appInfo);
		ServiceEntry[] serviceEntryList = m_Msbu.getUsrmMngr().getMsbUrnList();

		for (ServiceEntry svcEntry : serviceEntryList) {
			VendorAuthItem vendorAuthItem = new VendorAuthItem(svcEntry.msbUrn);
			vendorAuthMap.attachVendorAuthItem(vendorAuthItem);
		}
		return vendorAuthMap;
	}

	public void refreshVendorAuthMap(VendorAuthMap vendorAuthMap) {
		AppInfo appInfo = this.retrieveAppInfo(vendorAuthMap.getAppInfo().getAppId());
		ServiceEntry[] serviceEntryList = m_Msbu.getUsrmMngr().getMsbUrnList();

		for (ServiceEntry svcEntry : serviceEntryList) {
			VendorAuthItem vendorAuthItem = new VendorAuthItem(svcEntry.msbUrn);
			vendorAuthMap.attachVendorAuthItem(vendorAuthItem);
		}
	}
}
