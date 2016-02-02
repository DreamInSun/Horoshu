package ctop.v3.msb.usrm.data;

import java.io.Serializable;
import java.util.ArrayList;

public class MsbRouteBulletin extends ArrayList<MsbRouteItem> implements Serializable {
	private static final long serialVersionUID = 1L;

	/*========== Properties ===========*/

	/*========== Data Field ==========*/
	public UsrmNodeInfo m_UsrmNodeInfo;

	/*========== Constructor ==========*/
	public MsbRouteBulletin(UsrmNodeInfo nodeInfo) {
		m_UsrmNodeInfo = nodeInfo;
	}

	/*==========  ==========*/

	/*==========  ==========*/

}
