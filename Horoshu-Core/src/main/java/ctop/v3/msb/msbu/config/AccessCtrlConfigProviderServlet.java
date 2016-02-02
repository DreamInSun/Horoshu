package ctop.v3.msb.msbu.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import ctop.v3.msb.common.exception.MsbException;

public class AccessCtrlConfigProviderServlet extends AccessCtrlConfigProviderXML {
	private static final long serialVersionUID = 1L;
	private final static Logger g_Logger = Logger.getLogger(MsbConfigProviderServlet.class);

	/*========== Constant ==========*/
	public static final char DEFAULT_PATH_SEPERATOR = '/';
	public static final String DEFAULT_ENCODIN = "UTF-8";

	public static final String CONFIG_FOLDER_DEFAULT = "/ctop/settings/config";
	public static final String CONFIG_FILE_DEFAULT = "AccessControl.xml";

	/*========== Properties ==========*/
	private String m_rootPath;
	private String m_resName;

	private String m_fullFilePath;

	/*========== Constructor ===========*/
	/**
	 * Dynamic Web Application, Load Configuration XML file takes WEB-INFO as root;
	 * 
	 * @param resName
	 * @throws MsbException
	 */
	public AccessCtrlConfigProviderServlet(String resName) throws MsbException {
		loadConfig(resName);
	}

	/**
	 * Load Configuration XML with input Resource Name.
	 * 
	 * @param resName
	 * @throws MsbException
	 */
	public void loadConfig(String resName) throws MsbException {
		if (resName != null && resName != m_resName) {
			m_resName = resName;
			this.reloadConfig(resName);
		}
	}

	/**
	 * Reload XML & Clear Cache.
	 * 
	 * @throws MsbException
	 */
	public void reloadConfig(String resName) throws MsbException {
		this.setConfigElemnt(loadXML(resName));
	}

	/*============================================================*/
	/*==================== Assistant Function ====================*/
	/*============================================================*/

	/*========== Assistant Function : loadElement ===========*/

	private Element loadXML(String filename) {

		/*===== STEP 1. Get Environment Root Path =====*/
		try {
			/* Class Loader path is always "WEB-INFO/classes/" */
			m_rootPath = Thread.currentThread().getContextClassLoader().getResource("/").toString();
			m_rootPath = new File(m_rootPath).getParent();
		} catch (SecurityException exp) {
			exp.printStackTrace();
		}

		/*========== STEP 2. Load XML File by relative filename ===========*/
		/* Format Filename (default is UNIX Format ) */
		String pathFilename = filename.replace(DEFAULT_PATH_SEPERATOR, File.separatorChar);
		m_fullFilePath = m_rootPath + pathFilename;
		/* Create Parser */
		SAXReader reader = new SAXReader();
		Document doc = null;
		try {
			doc = reader.read(m_fullFilePath);
			return doc.getRootElement();
		} catch (DocumentException e) {
			g_Logger.error("Load configuration file failed from " + filename);
			return null;
		}
	}

	@Override
	public void saveConfigFile() {
		if (m_fullFilePath != null) {
			try {
				OutputFormat fmt = OutputFormat.createPrettyPrint();
				XMLWriter xmlWriter = new XMLWriter(new OutputStreamWriter(new FileOutputStream(m_fullFilePath)), fmt);
				xmlWriter.write(m_elmtAcConfig);
				xmlWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
