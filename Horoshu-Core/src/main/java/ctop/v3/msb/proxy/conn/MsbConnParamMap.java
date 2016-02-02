package ctop.v3.msb.proxy.conn;

import java.util.HashMap;
import java.util.Map;

import ctop.v3.msb.proxy.conn.MsbConnParam.EType;

public class MsbConnParamMap extends HashMap<EType, MsbConnParam> {
	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128 * this.size());
		for (Map.Entry<EType, MsbConnParam> connEntry : this.entrySet()) {
			sb.append("\r\n[").append(connEntry.getKey().name()).append("] :").append(connEntry.getValue().toString());
		}
		return sb.toString();
	}
}
