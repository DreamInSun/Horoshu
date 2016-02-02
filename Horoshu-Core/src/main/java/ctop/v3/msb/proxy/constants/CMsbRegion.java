package ctop.v3.msb.proxy.constants;

public interface CMsbRegion {
	/** Services Register : All Proxy Upload Binded Services Information to USRM via this channel. */
	String SVR_REGISTER = "Q_MSB_SvrRegister";

	/**
	 * Services Broadcast : Decentration mode of services sharing, all proxy subscribe this topic, and all proxy send
	 * services information by a heart beat to this topic.
	 */
	String SVR_BBS = "T_MSB_SvrBbs";

	/* Services Discover */
	
}
