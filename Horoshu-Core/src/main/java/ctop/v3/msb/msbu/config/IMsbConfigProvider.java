package ctop.v3.msb.msbu.config;

import ctop.v3.msb.bridge.data.MsbBridgeMap;
import ctop.v3.msb.msbu.data.MsbuInfo;
import ctop.v3.msb.proxy.conn.MsbConnParam;
import ctop.v3.msb.proxy.conn.MsbConnParamMap;

/**
 * Provide necessary configurations for MSBU initialization.
 * 
 * @author DreamInSun
 * 
 */
public interface IMsbConfigProvider {

	/**
	 * Get MSB Bridge Map to Conduct MSBU creation.
	 * 
	 * @return
	 */
	MsbBridgeMap getMsbBridgeMap();

	/**
	 * Get all the First MSBU Info Configured, each domain will be only exist one.
	 * 
	 * @return
	 */
	MsbuInfo[] getMsbuInfoList();

	/**
	 * Get the First MSBU Information Configured.
	 * 
	 * @return
	 */
	MsbuInfo getMsbuInfo();

	/**
	 * Get Basic MSBU Informations specified by domain name.
	 */
	MsbuInfo getMsbuInfo(String msbDomain);

	/**
	 * 
	 * @param msbDomain
	 * @return
	 */
	MsbConnParamMap getConnectionParamMap(String msbDomain);

	/**
	 * 
	 * @param msbDomain
	 * @param type
	 * @return
	 */
	MsbConnParam getConnectionParam(String msbDomain, MsbConnParam.EType type);

	/**
	 * 
	 * @return
	 */
	IAccessCtrlConfigProvider getAccessControlProvicer();
}
