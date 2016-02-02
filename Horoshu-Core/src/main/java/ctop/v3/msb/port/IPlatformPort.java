package ctop.v3.msb.port;

import org.jdom.Element;

import com.hztech.util.ComInput;
import com.hztech.util.ComOutput;

public interface IPlatformPort {
	public ComOutput invokePlatformService(String svcName, ComInput ci);

	public String[] getServiceList();

	public Element getServiceDesc(String svcName, String version);
}
