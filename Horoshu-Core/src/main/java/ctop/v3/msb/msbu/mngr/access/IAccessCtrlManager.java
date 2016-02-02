package ctop.v3.msb.msbu.mngr.access;

import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.usrm.data.UsrmNodeInfo;

public interface IAccessCtrlManager {

	/*=========== Access Control Brief Management ===========*/
	void updateAccessDomainBrief(AccessCtrlBrief acBrief);

	/*=========== Access Control Permission ===========*/
	/**
	 * 
	 * @param msbUrn
	 * @param callerDomain
	 * @return
	 */
	boolean isPermit(MsbUrn msbUrn, String callerDomain);

	/**
	 * 
	 * @param domain
	 * @param callerInfo
	 * @return
	 */
	boolean isPermit(MsbUrn msbUrn, UsrmNodeInfo callerInfo);

	/**
	 * 
	 * @param callerInfo
	 * @return
	 */
	boolean isPermit(UsrmNodeInfo callerInfo);

	/**
	 * 
	 * @param callerDomain
	 * @return
	 */
	boolean isPermit(String callerDomain);
}
