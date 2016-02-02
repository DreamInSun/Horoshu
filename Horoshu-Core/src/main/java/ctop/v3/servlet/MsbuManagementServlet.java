package ctop.v3.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hztech.platform.v3.common.V3Util;
import com.hztech.util.ComInput;
import com.hztech.util.ComOutput;
import com.other.demo.DemoServices;

import ctop.v3.msb.MSB;
import ctop.v3.msb.MSB.IMsbRpcCallback;
import ctop.v3.msb.MSB.IMsbRpcHandler;
import ctop.v3.msb.common.exception.MsbException;
import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.msbu.data.MsbuInfo;
import ctop.v3.msb.usrm.data.SvcDescriptor;
import ctop.v3.servlet.base.MsbuServlet;

/**
 * NSBU Management Servlet
 * 
 * @author DreamInSun
 * 
 */
@WebServlet(name = "MsbuManagment", urlPatterns = { "/MsbuManagment" }, loadOnStartup = 1)
public class MsbuManagementServlet extends MsbuServlet {
	private static final long serialVersionUID = 1L;

	/* ========== Constant ========== */

	/* ========== Properties ========== */

	/* Add Services to USRM */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		registerServices();
	}

	/* ========== HTTP Services ========== */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doGet(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {

			/*===== STEP 1. Assemble Input =====*/
			MsbUrn msbUrn = MsbUrn.parse(request.getParameter("MsbUrn"));
			String InputString = request.getParameter("Input");
			ComInput ci = new ComInput();
			ci.setPm("Load", InputString);

			/*===== STEP 2. Execute Service Access =====*/
			ComOutput co = (ComOutput) g_msbu.invokeMethodSync(msbUrn, (ComInput) ci);
			if (co != null) {
				String retStr;
				if (co.getRetCode() >= 0)
					retStr = co.getRetValue();
				else
					retStr = co.getRetDesc();

				response.getWriter().println(retStr);
			}

			/* ========== STEP 2. Execute Service Access Asynchornized ========== */
			/* NOTE : tomcat 6.0 not support this mode of service. */
			/*
			 * response.setContentType("text/html; charset=UTF8"); AsyncContext
			 * ctx = request.startAsync(); m_EexecutorService.submit(new
			 * AsyncRequest(ctx));
			 */
		} catch (MsbException exp) {
			response.getWriter().println(exp.getMessage());
		}
	}

	/* ========== Inner Class ========== */
	public class AsyncRequest implements Runnable {
		private AsyncContext ctx;

		public AsyncRequest(AsyncContext ctx) {
			this.ctx = ctx;
		}

		@Override
		public void run() {
			try {
				/* TODO Invoke Something time costing */
				ServletRequest request = ctx.getRequest();

				/*===== STEP 1. Assemble Input =====*/
				MsbUrn msbUrn = MsbUrn.parse(request.getParameter("MsbUrn"));
				String InputString = request.getParameter("Input");
				ComInput ci = new ComInput();
				ci.setPm("Load", InputString);

				/*===== STEP 2. Execute Service Access =====*/
				g_msbu.invokeMethodAsync(msbUrn, (ComInput) ci, new IMsbRpcCallback() {
					@Override
					public void onReplyMsg(ComOutput co) {
						String retStr;
						/*Check If Return Is OK*/
						if (co.getRetCode() > 0) {
							retStr = co.getRetValue();
						} else {
							retStr = co.getRetDesc();
						}

						PrintWriter out;
						try {
							out = ctx.getResponse().getWriter();
							out.println(retStr);
						} catch (IOException e) {
							e.printStackTrace();
						}
						ctx.complete();
					}

				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/* ========== Assistant : registerServices ========== */
	/**
	 * For Demonstration Service.
	 */
	private final void registerServices() {
		if (true == g_msbu.isSvcProvider()) {
			try {
				MsbuInfo msbuInfo = MSB.getMsbuInfo("35000000");
				MsbuInfo msbuInfo2 = MSB.getMsbuInfo("35010000");
				if (msbuInfo != null) {
					String MsbuName = msbuInfo.getName();

					if (MsbuName.startsWith("SB_35010000")) {
						/* Register Platform Services */
						ArrayList<String[]> svcList = V3Util.getAllServiceResource();
						for (String[] svcName : svcList) {
							g_msbu.bindPlatformService(svcName[0], null);
						}
						/* Register Normal Services */
						g_msbu.bindRpcService("syssv.ctop.convert.reverseWord", new SvcDescriptor("Reverse Input String. \r\n"), new IMsbRpcHandler() {

							@Override
							public ComOutput onInvokeSvr(ComInput msgIn) {
								return (ComOutput) DemoServices.reverseString((ComInput) msgIn);
							}

						});

						g_msbu.bindRpcService("syssv.ctop.convert.toUpperCase", new SvcDescriptor("Convert Input to Upper Case. \r\n"), new IMsbRpcHandler() {

							@Override
							public ComOutput onInvokeSvr(ComInput msgIn) {
								return (ComOutput) DemoServices.toUpperCase((ComInput) msgIn);
							}
						});

						g_msbu.bindRpcService("syssv.ctop.convert.toLowerCase", new SvcDescriptor("Convert Input to Lower Case. \r\n "), new IMsbRpcHandler() {

							@Override
							public ComOutput onInvokeSvr(ComInput msgIn) {
								return (ComOutput) DemoServices.toLowerCase((ComInput) msgIn);
							}
						});
					} else if (MsbuName.equals("SM_35000000")) {
						g_msbu.bindRpcService("toLowerCase", new SvcDescriptor("Convert Input to Lower Case. \r\n "), new IMsbRpcHandler() {

							@Override
							public ComOutput onInvokeSvr(ComInput msgIn) {
								return (ComOutput) DemoServices.toLowerCase((ComInput) msgIn);
							}
						});
					}
				} else if (msbuInfo2 != null) {
					String MsbuName = msbuInfo2.getName();

					if (MsbuName.startsWith("SB_35000000")) {
						g_msbu.bindRpcService("toLowerCase2", new SvcDescriptor("Convert Input to Lower Case. \r\n "), new IMsbRpcHandler() {

							@Override
							public ComOutput onInvokeSvr(ComInput msgIn) {
								return (ComOutput) DemoServices.toLowerCase((ComInput) msgIn);
							}
						});
					} else if (MsbuName.startsWith("SM_35010000")) {
						g_msbu.bindRpcService("toUpperCase2", new SvcDescriptor("Convert Input to Upper Case. \r\n "), new IMsbRpcHandler() {

							@Override
							public ComOutput onInvokeSvr(ComInput msgIn) {
								return (ComOutput) DemoServices.toUpperCase((ComInput) msgIn);
							}
						});

						g_msbu.bindRpcService("toLowerCase2", new SvcDescriptor("Convert Input to Lower Case. \r\n "), new IMsbRpcHandler() {

							@Override
							public ComOutput onInvokeSvr(ComInput msgIn) {
								return (ComOutput) DemoServices.toLowerCase((ComInput) msgIn);
							}
						});
					}
				} else {
					g_msbu.bindRpcService("syssv.ctop.convert.toBase64", new SvcDescriptor("Convert Input to Base64 String. \r\n "), new IMsbRpcHandler() {
						@Override
						public ComOutput onInvokeSvr(ComInput msgIn) {
							return (ComOutput) DemoServices.toBase64(msgIn);
						}
					});
				}
				g_msbu.commitServicesChange();
			} catch (MsbException e) {
				e.printStackTrace();
			}
		}
	}
}
