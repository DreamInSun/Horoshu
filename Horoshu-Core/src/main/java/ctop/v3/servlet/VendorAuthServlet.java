package ctop.v3.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import ctop.v3.msb.common.exception.MsbException;
import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.managment.UsrmManager;
import ctop.v3.servlet.base.MsbuServlet;
import ctop.v3.vendor.authrization.VendorManager;
import ctop.v3.vendor.authrization.data.AppInfo;
import ctop.v3.vendor.authrization.data.VendorAuthItem;
import ctop.v3.vendor.authrization.data.VendorAuthMap;
import ctop.v3.vendor.authrization.interfaces.IVendorAuthPersistence;

/**
 * Servlet implementation class VendorAuthorization
 */
@WebServlet(description = "Vendor Authorization Management", urlPatterns = { "/VendorManagement/*" }, loadOnStartup = 1)
public class VendorAuthServlet extends MsbuServlet {
	private static final Logger g_Logger = Logger.getLogger(VendorAuthServlet.class);
	private static final long serialVersionUID = 1L;

	/*========== Constant ==========*/
	/* */
	@SuppressWarnings("rawtypes")
	private static final Class[] PARAM_MAP_INPUT = new Class[] { Map.class };

	/*========== Properties ==========*/
	private VendorManager m_VendorMngr;

	/*========== Constructor ==========*/
	/**
	 * Default constructor.
	 */
	public VendorAuthServlet() {
		g_Logger.debug("Starting VendorAuthServlet... ");

		/*========== STEP 1. Prepare Persistence Object =========*/
		VendorAuthPersistenceFile vendorPersistence = new VendorAuthPersistenceFile();
		
		/*========== STEP 2. Create Vendor Manager ==========*/
		try {
			m_VendorMngr = new VendorManager(g_msbu, vendorPersistence);
		} catch (Exception exp) {
			g_Logger.error(exp.getMessage());
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*===== STEP 1. Regulate Encoding =====*/
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		/*===== STEP 2. Reflection Invoke =====*/
		String func = request.getParameter("func");
		String tmpJsonRet = invokVendorMngr(func, request.getParameterMap());
		if (tmpJsonRet != null)
			response.getWriter().write(tmpJsonRet);
	}

	/**
	 * Only For Display
	 * 
	 * @param methodName
	 * @param arguments
	 * @return
	 */
	private final String invokVendorMngr(String methodName, Map<String, String[]> arguments) {
		String tmpJsonRet = null;
		if (m_VendorMngr != null) {
			try {
				g_Logger.debug("Invoke :" + methodName);
				/* Reflect Invoke the method of Manager Class */
				Method method = VendorManager.class.getMethod(methodName, PARAM_MAP_INPUT);
				tmpJsonRet = (String) method.invoke(m_VendorMngr, arguments);
				g_Logger.debug("Return :" + tmpJsonRet);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return tmpJsonRet;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String strRet = m_VendorMngr.getVendorAuthMap(request.getParameterMap());
		response.getWriter().write(strRet);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		m_VendorMngr.postVendorAuthMap(request.getParameterMap());
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		m_VendorMngr.putVendorAuthMap(request.getParameterMap());
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		m_VendorMngr.deleteVendorAuthMap(request.getParameterMap());
	}

	/*========== Assistant Function =========*/
	public String getResourcePath(HttpServletRequest request) {
		String path = request.getRequestURI();
		request.getContextPath();
		request.getMethod();

		return path;
	}

	/*====================================================================================*/
	/*==================== Vendor Authorization Persistence Interface ====================*/
	/*====================================================================================*/
	private class VendorAuthPersistenceFile implements IVendorAuthPersistence {
		/*========== Constant ==========*/
		private static final String ROOT_FOLDER = "/ctop/authorization";
		/* Default Value */
		public static final char DEFAULT_PATH_SEPERATOR = '/';
		public static final String DEFAULT_ENCODIN = "utf-8";
		public static final String FILE_HEADER = "file:/";

		/*========== Properties ==========*/
		private String m_rootPath;

		/*========== Constructor ==========*/
		public VendorAuthPersistenceFile() {
			/*===== STEP 1. Get Environment Root Path =====*/
			try {
				/* Class Loader path is always "WEB-INFO/classes/" */
				m_rootPath = Thread.currentThread().getContextClassLoader().getResource("/").toString();
				if (m_rootPath.startsWith(FILE_HEADER)) {
					m_rootPath = m_rootPath.substring(FILE_HEADER.length(), m_rootPath.length());
				}
				m_rootPath = new File(m_rootPath).getParent();
			} catch (SecurityException exp) {
				exp.printStackTrace();
			}
		}

		/*========== Assistant Function =========*/
		private final String formatFullPath(String vendorId, String appId) {
			String filePath = m_rootPath + ROOT_FOLDER + DEFAULT_PATH_SEPERATOR + vendorId + DEFAULT_PATH_SEPERATOR + appId + ".conf";
			return filePath.replace(DEFAULT_PATH_SEPERATOR, File.separatorChar);
		}

		private final String formatVendorFolerPath(String vendorId) {
			String filePath = m_rootPath + ROOT_FOLDER + DEFAULT_PATH_SEPERATOR + vendorId;
			return filePath.replace(DEFAULT_PATH_SEPERATOR, File.separatorChar);
		}

		private final File openVendorFolder(String vendorId) {
			String filePath = formatVendorFolerPath(vendorId);
			//filePath = "D:\\{WorkStation}\\{JSP_WorkSpace}\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\MSBU\\WEB-INF\\ctop\\authorization\\TEST";
			File file = new File(filePath);
			if (!file.exists()) {

				boolean res = file.mkdirs();
				if (res == false) {
					g_Logger.error("Error on Create Vendor Folder :" + vendorId);
				}
			}
			return file;
		}

		private final File openFile(String vendorId, String appId) {
			String filePath = formatFullPath(vendorId, appId);
			File file = new File(filePath);
			return file;
		}

		/*============================================================================*/
		/*==================== Interface : IVendorAuthPersistence ====================*/
		/*============================================================================*/
		@Override
		public String getProjectRoot() {
			return m_rootPath;
		}

		@Override
		public void createVendorAuthMap(String vendorId, String appId, VendorAuthMap vendorAuthMap) {
			File file = openFile(vendorId, appId);
			if (!file.exists()) {
				try {
					/* Create Folder */
					this.openVendorFolder(vendorId);
					/* Create File */
					file.createNewFile();
					String jsonString = JSON.toJSONString(vendorAuthMap);
					FileOutputStream out = new FileOutputStream(file, true);
					out.write(jsonString.getBytes(DEFAULT_ENCODIN));
					out.close();
				} catch (IOException e) {
					g_Logger.error("File " + file + " create Error: " + e.getMessage());
				}
			} else {
				g_Logger.error("File " + file + " has been created.");
			}
		}

		@Override
		public void updateVendorAuthMap(String vendorId, String appId, VendorAuthMap vendorAuthMap) {
			File file = openFile(vendorId, appId);
			if (!file.exists() || file.isDirectory()) {
				g_Logger.error("File " + file + " not found. Create New One");
				createVendorAuthMap(vendorId, appId, vendorAuthMap);
			} else {
				m_VendorMngr.refreshVendorAuthMap(vendorAuthMap);
				try {
					String jsonString = JSON.toJSONString(vendorAuthMap, SerializerFeature.DisableCircularReferenceDetect);
					FileOutputStream out = new FileOutputStream(file, false);
					out.write(jsonString.getBytes());
					out.close();
				} catch (IOException e) {
					g_Logger.error(e.getMessage());
				}
			}
		}

		@Override
		public VendorAuthMap retrieveVendorAuthMap(String vendorId, String appId) {

			VendorAuthMap tmpVendorAuthMap = null;
			File file = openFile(vendorId, appId);
			if (!file.exists() || file.isDirectory()) {
				tmpVendorAuthMap = m_VendorMngr.getDefaultVendorMap(appId);
				this.createVendorAuthMap(vendorId, appId, tmpVendorAuthMap);
			} else {
				String temp = null;
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(file));
					StringBuffer sb = new StringBuffer();
					temp = br.readLine();
					while (temp != null) {
						sb.append(temp + " ");
						temp = br.readLine();
					}
					temp = sb.toString();
					br.close();
				} catch (FileNotFoundException e) {
					g_Logger.error(e.getMessage());
				} catch (IOException e) {
					g_Logger.error(e.getMessage());
				}
				tmpVendorAuthMap = JSON.parseObject(temp, VendorAuthMap.class);
			}
			return tmpVendorAuthMap;
		}

		@Override
		public void deleteVendorAuthMap(String vendorId, String appId) {
			File file = openFile(vendorId, appId);
			if (file.exists()) {
				file.delete();
			}
		}

	}
}
