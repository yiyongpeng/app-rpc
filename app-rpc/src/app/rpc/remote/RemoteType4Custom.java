package app.rpc.remote;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import app.core.AccessException;

public class RemoteType4Custom implements RemoteType {

	public boolean isValue() {
		return value;
	}
	
	public boolean isInterface() {
		return interfaze;
	}

	public void readExternal(ObjectInput in) throws IOException {
		name = in.readUTF();
		interfaze = in.readBoolean();
		value = in.readBoolean();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(getName());
		out.writeBoolean(isInterface());
		out.writeBoolean(isValue());
	}

	public Object readValue(ObjectSession session, ObjectInput in) throws IOException {
		try {
			return in.readObject();
		} catch (ClassNotFoundException e) {
			throw new AccessException("Reverse serialization failure: " + e);
		}
	}

	public void writeValue(Object value, ObjectOutput out) throws IOException {
		out.writeObject(value);

		// 销毁网络引用对象
		if (value != null && value instanceof InetReferenceArgument)
			((InetReferenceArgument) value).destroy();
	}

	public byte getType() {
		return RemoteType4Basic.TYPE_OTHER;
	}

	public String getName() {
		return name;
	}

	public RemoteType4Custom(ObjectInput in) throws IOException {
		this.readExternal(in);
	}

	public RemoteType4Custom(Class<?> clazz, boolean value) {
		this.name = clazz.getName();
		this.interfaze = clazz.isInterface();
		this.value = value;
	}

	/** 是否传值，可变值 */
	private boolean value;
	private boolean interfaze;
	private String name;
}
