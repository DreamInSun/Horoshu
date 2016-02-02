package ctop.v3.msb.usrm.data;

import java.io.Serializable;

import org.jdom.Element;

public class SvcDescriptor implements Serializable {
	private static final long serialVersionUID = 1L;

	/*========== Constant ==========*/
	public static final int SVC_TYPE_UNKOWN = 0;
	public static final int SVC_TYPE_RPC = 1;
	public static final int SVC_TYPE_MSG = 2;
	public static final int SVC_TYPE_PLATFORM = 3;

	/*========== Properties ==========*/
	/* Basic Info */
	private int SvcType = SVC_TYPE_UNKOWN; // TODO remove it
	private String brief = null;
	private Element description = null;

	/* Input Parameters */

	/* Output Parameters */

	/*========== Constructors ==========*/
	public SvcDescriptor() {
	}

	public SvcDescriptor(String brief) {
		this.setBrief(brief);
	}

	public SvcDescriptor(String brief, int type) {
		this.setBrief(brief);
		this.setSvcType(type);
	}

	public SvcDescriptor(Element elmt) {
		this.setDescription(elmt);
	}

	/*========== Getter & Setter ==========*/
	public String getBrief() {
		return brief;
	}

	public void setBrief(String brief) {
		this.brief = brief;
	}

	public Element getDescription() {
		return description;
	}

	public void setDescription(Element description) {
		this.description = description;
	}

	public int getSvcType() {
		return SvcType;
	}

	public void setSvcType(int svcType) {
		SvcType = svcType;
	}

}
