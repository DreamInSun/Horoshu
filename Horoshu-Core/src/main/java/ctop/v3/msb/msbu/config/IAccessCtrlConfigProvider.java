package ctop.v3.msb.msbu.config;

import ctop.v3.msb.msbu.mngr.access.AccessCtrlBrief;
import ctop.v3.msb.msbu.mngr.access.AccessCtrlItem;

public interface IAccessCtrlConfigProvider {

	public AccessCtrlBrief getDomainAccessBrief(String domain);

	public AccessCtrlItem[] getAccessCtrlList();

	public AccessCtrlBrief[] getAccessCtrlBriefs();

	/*=========== Edit Functions ===========*/
	public boolean setAccessCtrlBiref(AccessCtrlBrief acBrief);

	public void setAccessCtrlItem(String domain, AccessCtrlItem acItem);

	abstract public void saveConfigFile();
}
