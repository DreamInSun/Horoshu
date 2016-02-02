package ctop.v3.msb.usrm.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import ctop.v3.msb.usrm.constants.EUsrmCmdCode;

public class UsrmCommand implements Externalizable {
	private static final long serialVersionUID = 1L;

	/*========== Properties ==========*/
	/** For reply uses */
	private EUsrmCmdCode code;
	private UsrmNodeInfo sender;
	private Serializable load;
	private long timestamp;

	/*========== Constructor ==========*/
	/** Only for deserialization */
	@Deprecated
	public UsrmCommand() {
	}

	public UsrmCommand(UsrmNodeInfo sender, EUsrmCmdCode code, Serializable load) {
		this.sender = sender;
		this.code = code;
		this.load = load;
		this.timestamp = System.currentTimeMillis();
	}

	/*========== Getter & Setter : sender ==========*/
	public UsrmNodeInfo getSender() {
		return sender;
	}

	public void setSender(UsrmNodeInfo sender) {
		this.sender = sender;
	}

	/*========== Getter & Setter : Code ==========*/
	public EUsrmCmdCode getCode() {
		return code;
	}

	public void setCode(EUsrmCmdCode code) {
		this.code = code;
	}

	/*========== Getter & Setter : Load ==========*/
	public Object getLoad() {
		return load;
	}

	public void setLoad(Serializable load) {
		this.load = load;
	}

	/*========== Getter & Setter : Timestamp ==========*/
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/*=========== Interface : Externalizable ===========*/
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(code.getValue());
		out.writeObject(this.sender);
		out.writeObject(this.load);
		out.writeLong(timestamp);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.code = EUsrmCmdCode.getEnum(in.readInt());
		this.sender = (UsrmNodeInfo) in.readObject();
		this.load = (Serializable) in.readObject();
		this.timestamp = in.readLong();
	}
}
