package ctop.v3.msb.usrm.constants;

public enum EMsbConectionType {
	UNKNOWN(0), JMS(1), RMI(2), SOCKET(3);

	/*=========================================================*/
	/*==================== Enumerate Value ====================*/
	/*=========================================================*/
	private int value = 0;

	private EMsbConectionType(int val) {
		value = val;
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}
}
