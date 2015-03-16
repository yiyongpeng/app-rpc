package app.rpc.remote.service;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import app.core.Context;
import app.rpc.remote.ObjectRequest;
import app.rpc.remote.Scope;
import app.rpc.remote.impl.ObjectHandle;

public class RegistorParameter extends ObjectHandle {
	private static final byte SCOPE_APPLICATION = 1, SCOPE_SESSION = 2;

	private ObjectRequest request;

	private Scope scope;
	private Object instance;
	private Class<?>[] interfaces;

	public RegistorParameter() {
	}

	@Override
	public void destory() {
		this.scope = null;
		this.request = null;
		this.instance = null;
		this.interfaces = null;
		super.destory();
	}

	public RegistorParameter(String handle, Serializable instance,
			Class<?>[] interfaces, Scope scope) {
		super(handle);
		this.scope = scope;
		this.instance = instance;
		this.interfaces = interfaces;
	}

	@Override
	public void init(ObjectRequest request) throws Exception {
		this.request = request;
		super.init(request);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		instance = in.readObject();
		interfaces = (Class<?>[]) in.readObject();
		readScope(in);
		superReadExternal(in);
	}

	private void readScope(ObjectInput in) throws IOException {
		byte value = in.readByte();
		switch (value) {
		case SCOPE_APPLICATION:
			scope = Scope.APPLICATION;
			break;
		case SCOPE_SESSION:
			scope = Scope.SESSION;
			break;
		default:
			throw new IOException("未知作用域参数。");
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(instance);
		out.writeObject(interfaces);
		writeScope(out);
	}

	private void writeScope(ObjectOutput out) throws IOException {
		if (scope == Scope.APPLICATION)
			out.writeByte(SCOPE_APPLICATION);
		else if (scope == Scope.SESSION)
			out.writeByte(SCOPE_SESSION);
		else
			throw new IOException("未知作用域参数。");
	}

	public Object getInstance() {
		return instance;
	}

	public Class<?>[] getInterfaces() {
		return interfaces;
	}

	public Context getContext() {
		if (scope == Scope.APPLICATION)
			return request.getServletContext();
		else if (scope == Scope.SESSION)
			return request.getSession();

		return null;
	}

}
