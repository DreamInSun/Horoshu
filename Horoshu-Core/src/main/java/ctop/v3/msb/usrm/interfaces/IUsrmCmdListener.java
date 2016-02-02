package ctop.v3.msb.usrm.interfaces;

import ctop.v3.msb.usrm.constants.EUsrmCmdCode;
import ctop.v3.msb.usrm.data.UsrmCommand;

public interface IUsrmCmdListener {

	/**
	 * Get the command codes which this listener will listening to.
	 * 
	 * @return
	 */
	EUsrmCmdCode getInterestCommand();

	/**
	 * On receiving the command codes listed below, reacting behavior.
	 * 
	 * @param cmd
	 */
	void onReceiveUsrmCmd(UsrmCommand cmd);
}
