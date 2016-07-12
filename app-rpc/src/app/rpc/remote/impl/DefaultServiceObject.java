package app.rpc.remote.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import app.rpc.remote.ObjectResponse;
import app.rpc.remote.RemoteMethod;
import app.rpc.remote.RemoteMethodCollection;
import app.rpc.remote.ServiceObject;
import app.util.POJO;

/**
 * 已注册的服务对象实现类
 * 
 * @author yiyongpeng
 * 
 */
public class DefaultServiceObject extends POJO implements ServiceObject {
	public static boolean enableClassPool = true;
	private String handle;
	private Object instance;
	private RemoteMethodCollection methods;

	public DefaultServiceObject() {
	}

	// public DefaultServiceObject(String handle, Object instance,
	// RemoteMethodCollection methods) {
	// this();
	// this.handle = handle;
	// this.instance = instance;
	// this.methods = methods;
	// }

	public DefaultServiceObject(String handle, Object instance, Class<?>... interfaces) {
		this();
		this.handle = handle;
		this.instance = instance;
		ClassLoader loader = instance.getClass().getClassLoader();
//		if(loader==null){
//			System.err.println(instance+" , loader is null");
//		}
		this.methods = new DefaultRemoteMethodCollection(handle, loader, toInstanceInterfaces(instance, interfaces));
		init();
	}

	protected Class<?>[] toInstanceInterfaces(Object instance, Class<?>[] interfaces) {
		for (int i = 0; i < interfaces.length; i++) {
			String classname = interfaces[i].getName();
			interfaces[i] = searchInterface(instance.getClass(), classname);
			if (interfaces[i] == null) {
				throw new RuntimeException(instance.getClass() + " not found interface : " + classname);
			}
		}
		return interfaces;
	}

	protected Class<?> searchInterface(Class<?> clazz, String classname) {
		Class<?>[] inters = clazz.getInterfaces();
		for (int i = 0; i < inters.length; i++) {
			Class<?> interfac = inters[i];
			if (interfac.getName().equals(classname)) {
				return interfac;
			}
			Class<?>[] interfaces = interfac.getInterfaces();
			for (int j = 0; j < interfaces.length; j++) {
				interfac = interfaces[j];
				if (interfac.getName().equals(classname)) {
					return interfac;
				}
			}
		}
		clazz = clazz.getSuperclass();
		if (clazz != null) {
			return searchInterface(clazz, classname);
		}
		return null;
	}

	@Override
	public void invoke(int mid, Object[] args, ObjectResponse response) throws IOException {
		RemoteMethod method = methods.getMethod(mid);

		// Invoke method
		method.invoke(response, this, args);

	}

	public void init() {
		try {
			Method method = this.instance.getClass().getMethod("onRemoteInit");
			method.invoke(instance);
		} catch (NoSuchMethodException e) {
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		}
	}

	public void destroy() {
		try {
			Method method = this.instance.getClass().getMethod("onRemoteDestroy");
			method.invoke(instance);
		} catch (NoSuchMethodException e) {
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		}
	}

	public Object getInstance() {
		return instance;
	}

	public RemoteMethodCollection getMethodCollection() {
		return this.methods;
	}

	public String getHandle() {
		return handle;
	}

	public Object getInvokeHandle() {
		return methods.getInvokeHandle();
	}

	public RemoteMethodCollection getMethods() {
		return methods;
	}

	public void setMethods(RemoteMethodCollection methods) {
		this.methods = methods;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public void setInstance(Object instance) {
		this.instance = instance;
	}

}
