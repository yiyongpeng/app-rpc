package app.rpc.remote.impl;

import java.io.Serializable;
import java.lang.reflect.Method;

import app.net.AppSession;
import app.rpc.remote.RemoteMethodCollection;
import app.rpc.remote.RemoteObject;
import app.rpc.remote.RemoteVisitor;
import app.rpc.remote.Scope;

/**
 * 远程对象调用访问器
 * 
 * @author yiyongpeng
 * 
 */
public abstract class BaseRemoteVisitor implements RemoteVisitor {

	protected abstract void connectImpl(String query, String user, String pwd);

	protected abstract void registorImpl(String handle, Serializable instance,
			Class<?>[] interfaces, Scope scope);

	protected abstract RemoteMethodCollection validateImpl(String handle,
			Class<?>[] interfaces);

	protected abstract Object invokeImpl(RemoteObject ro, Method method,
			Object[] args) throws Throwable;

	public void connect(String url, String user, String pwd) {
		connectImpl(url, user, pwd);
	}

	public void registor(String handle, Serializable instance,
			Class<?>[] interfaces, Scope scope) {
		registorImpl(handle, instance, interfaces, scope);
	}

	public void validate(RemoteObject ro) {
		ro.setMethods(validateImpl(ro.getHandle(), ro.getInterfaces()));
	}

	public Object invoke(RemoteObject ro, Method method, Object[] args)
			throws Throwable {
		return invokeImpl(ro, method, args);
	}

	public void destory() {
		session = null;
	}

	public void init(AppSession session) {
		this.session = session;
	}

	protected AppSession session;

}
