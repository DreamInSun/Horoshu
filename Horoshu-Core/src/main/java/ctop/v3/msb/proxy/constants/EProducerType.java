package ctop.v3.msb.proxy.constants;

public enum EProducerType {
	/** Point to Point */
	PTP_TX(1),
	/** Publish Subscribe */
	PNS_PUB(2),
	/** Temporary Point to Point, without Channel Name */
	PTP_TX_TMP(3),
	/** Temporary Publish Subscribe without Channel Name */
	PNS_PUB_TMP(4);

	private int value = 0;

	private EProducerType(int val) {
		value = val;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}
}
