package ctop.v3.msb.proxy.constants;

public enum EConsumerType {
	/** Point to Point */
	PTP_RX(1),
	/** Publish Subscribe */
	PNS_SUB(2),
	/** Temporary Point to Point, without Channel Name */
	PTP_RX_TMP(3),
	/** Temporary Publish Subscribe without Channel Name */
	PNS_SUB_TMP(4);

	private int value = 0;

	private EConsumerType(int val) {
		value = val;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}

}
