package ctop.v3.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ctop.v3.msb.managment.UsrmManager;
import ctop.v3.servlet.base.MsbuServlet;

@WebServlet(name = "UsrmManagement", urlPatterns = { "/Usrm" }, loadOnStartup = 1)
public class UsrmServlet extends MsbuServlet {
	private static final Logger g_Logger = Logger.getLogger(MsbuManagementServlet.class);
	private static final long serialVersionUID = 1L;

	/*========== Constant ==========*/
	/* GET Request Parameters */
	public static final String REQ_PARAM_MANAGER = "manager";
	public static final String REQ_PARAM_METHOD = "method";
	public static final String REQ_PARAM_ARGS = "param";
	/* */
	@SuppressWarnings("rawtypes")
	private static final Class[] PARAM_INPUT_JSON = new Class[] { String.class };

	/*========== Properties ==========*/
	protected UsrmManager m_UsrmManager = null;

	/*========== Constructor ===========*/
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		m_UsrmManager = g_msbu.getUsrmMngr();
	}

	/*========== HTTP : GET ==========*/
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*===== STEP 1. Regulate Encoding =====*/
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		/*===== STEP 2. Reflection Invoke =====*/
		/*===== Analyze Parameters =====*/
		String managerName = request.getParameter(REQ_PARAM_MANAGER);
		String methodName = request.getParameter(REQ_PARAM_METHOD);
		String arguments = request.getParameter(REQ_PARAM_ARGS);

		/*===== Reflection Invoke =====*/
		if (managerName != null) {
			if (managerName.equalsIgnoreCase("Usrm")) {
				String tmpJsonRet = invokeUsrmMngr(methodName, arguments);
				response.getWriter().write(tmpJsonRet);
			} else if (managerName.equalsIgnoreCase("VendorAuth")) {

			} else {
				g_Logger.debug("Unknown Request.");
			}
		} else {
			g_Logger.debug("Manager Request is Null.");
		}

	}

	/*==========================================================*/
	/*==================== USRM Management =====================*/
	/*==========================================================*/
	/**
	 * Only For Display
	 * 
	 * @param methodName
	 * @param arguments
	 * @return
	 */
	private final String invokeUsrmMngr(String methodName, String arguments) {
		String tmpJsonRet = null;
		if (m_UsrmManager != null) {
			try {
				/* Reflect Invoke the method of Manager Class */
				Method method = UsrmManager.class.getMethod(methodName, PARAM_INPUT_JSON);
				tmpJsonRet = (String) method.invoke(m_UsrmManager, arguments);
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

	/*========== HTTP :POST ==========*/
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*===== STEP 1. Regulate Encoding =====*/
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		/*===== STEP 2. Reflection Invoke =====*/
		String paramFormat = request.getHeader("Content-Type");
		String managerName = null, methodName = null, arguments = null;

		/*===== STEP 1. Analyze Parameters =====*/
		if ("application/x-www-form-urlencoded".equals(paramFormat)) {
			/* JQuery Default Request Parameters Format */
			managerName = request.getParameter(REQ_PARAM_MANAGER);
			methodName = request.getParameter(REQ_PARAM_METHOD);
			arguments = request.getParameter(REQ_PARAM_ARGS);
		} else if ("application/json".equals(paramFormat)) {
			/* Angular Default Request Parameters Format */
			BufferedReader reader = request.getReader();
			StringBuffer buffer = new StringBuffer();
			String string;
			while ((string = reader.readLine()) != null) {
				buffer.append(string);
			}
			reader.close();
			//TODO Parse JSON Format Arguments & Get required parameters.
			//JSON.parse(buffer);
		}

		/*===== STEP 2. Execute Request & Response JSON Return =====*/
		if (managerName != null && managerName.equalsIgnoreCase("Usrm")) {
			if (m_UsrmManager != null) {

				try {
					/* Reflect Invoke the method of Manager Class */
					Method method = UsrmManager.class.getMethod(methodName, PARAM_INPUT_JSON);
					String tmpJsonRet = (String) method.invoke(m_UsrmManager, arguments);
					/* Write Response */
					response.getWriter().write(tmpJsonRet);
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
		} else {
			g_Logger.debug("Unknown Request.");
		}
	}
}
