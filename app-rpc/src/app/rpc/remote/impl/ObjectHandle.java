package app.rpc.remote.impl;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import app.rpc.remote.service.DefaultParameter;

public class ObjectHandle extends DefaultParameter {
	private String handle;

	public ObjectHandle() {
	}

	public ObjectHandle(String handle) {
		this.handle = handle;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		handle = in.readUTF();
	}

	public void superReadExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(handle);
	}

	public String getHandle() {
		return handle;
	}

	@Override
	public void destory() {
		handle = null;
		super.destory();
	}
}