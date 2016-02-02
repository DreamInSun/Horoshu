package ctop.v3.servlet.base;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.jdom.Element;

import com.hztech.platform.v3.agent.s2b2o.URS;
import com.hztech.util.ComInput;
import com.hztech.util.ComOutput;

import ctop.v3.msb.MSB;
import ctop.v3.msb.msbu.config.IMsbConfigProvider;
import ctop.v3.msb.msbu.config.MsbConfigProviderServlet;
import ctop.v3.msb.msbu.Msbu;
import ctop.v3.msb.port.IPlatformPort;

/**
 * Servlet 3.0 Annotation type of Configuration, instead of web.xml
 * 
 * @author DreamInSun
 * 
 */
@WebServlet(name = "Msbu", urlPatterns = { "/Msbu" }, asyncSupported = true, loadOnStartup = 1)
public class MsbuServlet extends HttpServlet {
	private static final long serialVersionUID = 1035596967246267640L;

	/*========== Properties ==========*/
	/* Async Call Thread Pool */
	//private ExecutorService m_ExsecutorService = Executors.newFixedThreadPool(10);
	/* */
	protected static Msbu g_msbu;
	protected IMsbConfigProvider m_configProvide;

	/*========== Constructor ==========*/
	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		/* Initializing MSBU */
		m_configProvide = new MsbConfigProviderServlet("/ctop/settings/config/MsbuConfig.xml");
		g_msbu = MSB.intialize(m_configProvide);
		/* Connect Platform Port */
		g_msbu.setPlatformPort(m_V3PlatformPort);
	}

	/*========== Deconstructor ==========*/
	@Override
	public void destroy() {
		super.destroy();
	}

	/*========== Platform Port ==========*/
	private IPlatformPort m_V3PlatformPort = new IPlatformPort() {

		@Override
		public ComOutput invokePlatformService(String svcName, ComInput ci) {
			ComOutput co = null;
			try {
				co = URS.invoke(null, svcName, ci);
			} catch (Exception exp) {
				exp.printStackTrace();
			}
			return co;
		}

		@Override
		public String[] getServiceList() {
			//TODO public static ArrayList<String[]> getAllServiceResource();
			return null;
		}

		@Override
		public Element getServiceDesc(String svcName, String version) {
			//TODO public static Element getDetailInfoByResourceName(String resourceName,String version);
			return null;
		}

	};
}
