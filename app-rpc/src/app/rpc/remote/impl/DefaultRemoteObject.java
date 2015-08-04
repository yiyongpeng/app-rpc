package app.rpc.remote.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import app.rpc.remote.ObjectSession;
import app.rpc.remote.RemoteMethod;
import app.rpc.remote.RemoteMethodCollection;
import app.rpc.remote.RemoteObject;
import app.rpc.remote.RemoteVisitor;

/**
 * 远程对象信息
 * 
 * @author yiyongpeng
 * 
 */
public class DefaultRemoteObject implements RemoteObject, InvocationHandler {

	private RemoteVisitor rv;

	public int getInvokeHandle() {
		return invokeHandle;
	}

	public ObjectSession getSession() {
		return session;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (closed) {
			System.err.println("invoke fault! Object is Closed: " + session.getInetAddress() + "  handle: " + getHandle());
			return null;
		}
		Object value = rv.invoke(this, method, args);
		return value;
	}

	public Object getProxy() {
		return proxy;
	}

	public String getHandle() {
		return handle;
	}

	public Class<?>[] getInterfaces() {
		return interfaces;
	}

	public RemoteMethodCollection getMethodCollection() {
		return methods;
	}

	public void setMethods(RemoteMethodCollection methods) {
		this.methods = methods;
		this.invokeHandle = methods.getInvokeHandle();
	}

	public void close() {
		this.closed = true;
		if (rv != null) {
			rv.destory();
			rv = null;
		}
		cachedMethods = null;
		methods = null;
		handle = null;
		proxy = null;
		interfaces = null;
		session = null;
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public RemoteMethod mapping(Method method) {
		RemoteMethod remoteMethod = cachedMethods.get(method);
		if (remoteMethod == null) {
			synchronized (method) {
				remoteMethod = cachedMethods.get(method);
				if(remoteMethod!=null)
					return remoteMethod;
				String methodString = DefaultRemoteMethod.getMethodString(method);
				remoteMethod = methods.getMethod(methodString);
				if (remoteMethod != null) {
					remoteMethod.setMethod(method);
					cachedMethods.put(method, remoteMethod);
				}
			}
		}
		return remoteMethod;
	}

	public DefaultRemoteObject(ObjectSession session, String handle, ClassLoader loader, Class<?>... interfaces) {
		this.session = ((DefaultObjectConnection) session);
		this.handle = handle;
		this.interfaces = interfaces;

		boolean changeLoader = false;
		Thread t = Thread.currentThread();
		ClassLoader cl = t.getContextClassLoader();
		if (loader == null) {
			loader = cl;
			if (loader == ClassLoader.getSystemClassLoader()) {
				if (interfaces.length > 0) {
					loader = interfaces[0].getClassLoader();
					t.setContextClassLoader(loader);
					changeLoader = true;
				}
			}
		} else {
			t.setContextClassLoader(loader);
			changeLoader = true;
		}
		boolean suc = false;
		try {
			rv = this.session.newVisitor();
			rv.validate(this);
			suc = true;
		} finally {
			if (suc == false) {
				rv.destory();
				rv = null;
			}
			if (changeLoader) {
				t.setContextClassLoader(cl);
			}
		}
		this.proxy = Proxy.newProxyInstance(loader, interfaces, this);
		this.cachedMethods = new HashMap<Method, RemoteMethod>(1);

	}

	private boolean closed;

	private Map<Method, RemoteMethod> cachedMethods;
	private RemoteMethodCollection methods;
	private String handle;
	private int invokeHandle;

	private Object proxy;

	private Class<?>[] interfaces;

	private DefaultObjectConnection session;
}
