package ctop.v3.msb.msbu.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import ctop.v3.msb.common.exception.MsbException;
import ctop.v3.msb.msbu.config.AccessCtrlConfigProviderXML.ISaveConfigFile;

/**
 * This kind of configuration provider can only work on platform.
 * 
 * @author DreamInSun
 * 
 */
public class MsbConfigProviderServlet extends MsbConfigProviderXml {
	private static final long serialVersionUID = 1L;

	private final static Logger g_Logger = Logger.getLogger(MsbConfigProviderServlet.class);

	/*========== Constant ==========*/
	public static final char DEFAULT_PATH_SEPERATOR = '/';
	public static final String CONFIG_FOLDER_DEFAULT = "/h/platform/v3/ttop/";
	public static final String CONFIG_FILE_DEFAULT = "MsbuConfig.xml";

	public static final String ACCESS_CONTROL_FILE_DEFAULT = "AccessControl.xml";

	public static final String MSB_DOMAIN_DEFAULT = "LOCAL";
	public static final String MSB_SVC_GROUP_DEFAULT = "DEFAULT";

	/*========== Properties ==========*/
	private String m_rootPath;
	private String m_configPath;
	private String m_resName;

	/*========== Constructor ===========*/
	/**
	 * Dynamic Web Application, Load Configuration XML file takes WEB-INFO as root;
	 * 
	 * @param resName
	 */
	public MsbConfigProviderServlet(String resName) {
		loadConfig(resName);
	}

	/**
	 * Load Configuration XML with input Resource Name.
	 * 
	 * @param resName
	 */
	public void loadConfig(String resName) {
		/*=====  =====*/
		int pos = resName.lastIndexOf(DEFAULT_PATH_SEPERATOR);
		m_configPath = resName.substring(0, pos + 1);
		/*=====  =====*/
		if (resName != null && resName != m_resName) {
			m_resName = resName;
			this.reloadConfig(resName);
		}
	}

	/**
	 * Reload XML & Clear Cache.
	 */
	public void reloadConfig(String resName) {
		setConfigElement(loadXML(resName));
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
			m_rootPath = java.net.URLDecoder.decode(m_rootPath);
		} catch (SecurityException exp) {
			exp.printStackTrace();
		}

		/*========== STEP 2. Load XML File by relative filename ===========*/
		/* Format Filename (default is UNIX Format ) */
		String pathFilename = filename.replace(DEFAULT_PATH_SEPERATOR, File.separatorChar);

		/* Create Parser */
		SAXReader reader = new SAXReader();
		Document doc = null;
		try {
			doc = reader.read(m_rootPath + pathFilename);
			return doc.getRootElement();
		} catch (DocumentException e) {
			g_Logger.error("Load configuration file failed from " + filename);
			return null;
		}finally{
			
		}

		/*========== STEP 2. Load XML File by relative filename ===========*/

	}

	private void saveXML(String filename, Element elmt) {
		if (filename != null) {
			String fullFilePath = m_rootPath + filename;
			fullFilePath = fullFilePath.replace(DEFAULT_PATH_SEPERATOR, File.separatorChar);
			//fullFilePath = new File(fullFilePath).toURI().getPath();
			if( fullFilePath.startsWith("file:") ){
				fullFilePath = fullFilePath.substring(6);
			}
			try {
				OutputFormat fmt = OutputFormat.createPrettyPrint();
				//Writer writer = new FileWriter(fullFilePath);
				OutputStream outputStream = new FileOutputStream(fullFilePath);
				Writer writer = new OutputStreamWriter(outputStream);
				XMLWriter xmlWriter = new XMLWriter(writer, fmt);
				xmlWriter.write(elmt);
				xmlWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 */
	@Override
	public IAccessCtrlConfigProvider getAccessControlProvicer() {
		if (m_AccessCtrlConfigProvider == null) {
			/* Get Path */
			Element elmtAccessControl = loadXML(m_configPath + ACCESS_CONTROL_FILE_DEFAULT);
			/* Try to Load Configuration Provider */
			try {
				AccessCtrlConfigProviderXML accessCtrlConfigProviderXml = new AccessCtrlConfigProviderXML(elmtAccessControl);
				m_AccessCtrlConfigProvider = accessCtrlConfigProviderXml;
				accessCtrlConfigProviderXml.setSaveConfigFile(new ISaveConfigFile() {

					@Override
					public void saveConfigFile(Element elmtAcConfig) {
						saveXML(m_configPath + ACCESS_CONTROL_FILE_DEFAULT, elmtAcConfig);
					}

				});
			} catch (MsbException e) {
				e.printStackTrace();
			}
		}
		return m_AccessCtrlConfigProvider;
	}
}
