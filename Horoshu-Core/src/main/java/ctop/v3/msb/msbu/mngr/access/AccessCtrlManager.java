package ctop.v3.msb.msbu.mngr.access;

import java.util.HashMap;
import java.util.Map;

import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.usrm.data.UsrmNodeInfo;

/**
 * Under Construction
 * @author DreamInSun
 *
 */
public class AccessCtrlManager implements IAccessCtrlManager {

	/*========== Constant ==========*/

	/*========== Properties ==========*/
	/**
	 * Key : Domain, Value : AccessCtrlDomainBrief
	 */
	private Map<String, AccessCtrlBrief> m_AcDomainBiref = new HashMap<String, AccessCtrlBrief>();

	/*========== Constructor ===========*/

	/*========================================================================*/
	/*==================== Interface : IAccessCtrlManager ====================*/
	/*========================================================================*/

	/*========== Access Control Domain Brief Management ==========*/
	@Override
	public void updateAccessDomainBrief(AccessCtrlBrief acBrief) {
		// TODO Auto-generated method stub
	}

	/*========== Assertion of Access Services ==========*/

	@Override
	public boolean isPermit(MsbUrn msbUrn, String callerDomain) {
		boolean tmpRet = false;
		AccessCtrlBrief tmpAcDomainBrief = m_AcDomainBiref.get(callerDomain);
		if (tmpAcDomainBrief != null) {
			//tmpRet = tmpAcDomainBrief.isPermit(msbUrn, callerDomain);
		}
		return tmpRet;
	}

	@Override
	public boolean isPermit(MsbUrn msbUrn, UsrmNodeInfo callerInfo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPermit(UsrmNodeInfo callerInfo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPermit(String callerDomain) {
		// TODO Auto-generated method stub
		return false;
	}

}
