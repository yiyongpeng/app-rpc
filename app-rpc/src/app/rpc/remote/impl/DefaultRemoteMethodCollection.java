package app.rpc.remote.impl;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import app.core.AccessException;
import app.rpc.remote.DefaultRemoteMethodFactory;
import app.rpc.remote.RemoteMethod;
import app.rpc.remote.RemoteMethodCollection;

/**
 * 远程方法列表
 * 
 * @author yiyongpeng
 * 
 */
public class DefaultRemoteMethodCollection extends ArrayList<RemoteMethod> implements RemoteMethodCollection, Comparator<RemoteMethod> {

	public int getInvokeHandle() {
		return invokeHandle;
	}

	public RemoteMethod getMethod(int index) {
		return this.get(index);
	}

	public void loadInterfaces(ClassLoader loader, Class<?>... interfaces) {
		loadInterface(loader, Object.class);
		for (Class<?> interfac : interfaces)
			loadInterface(loader, interfac);
	}

	public void loadInterface(ClassLoader loader, Class<?> interfac) {
		try {
			Class<?> clazz = loader.loadClass(DefaultRemoteMethodFactory.class.getName());
			Method fmethod = clazz.getMethod("createRemoteMethod", int.class, ClassLoader.class, Method.class);
			Method[] methds = interfac.getMethods();
			for (Method method : methds)
				try {
					String name = DefaultRemoteMethod.getMethodString(method);
					Map<Object, RemoteMethod> map = getMethodsMap();
					if (map.containsKey(name))
						continue;
					RemoteMethod rMethod = DefaultServiceObject.enableClassPool ? (RemoteMethod) fmethod.invoke(null, size(), loader, method) : DefaultRemoteMethodFactory.createRemoteMethod(size(), loader, method);
					if (add(rMethod)) {
						map.put(name, rMethod);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public int compare(RemoteMethod o1, RemoteMethod o2) {
		return o1.getString().compareTo(o2.getString());
	}

	public void readExternal(ObjectInput in) throws IOException {
		invokeHandle = in.readInt();/* 调用句柄 */
		int size = in.readInt();/* 方法数量 */
		for (int i = 0; i < size; i++) {
			RemoteMethod method = newDefaultRemoteMethod(in); /* 反序列化方法 */
			if (this.add(method)) {
				getMethodsMap().put(method.getString(), method);
			}
		}
	}

	private RemoteMethod newDefaultRemoteMethod(ObjectInput in) throws IOException {
		return new ClientRemoteMethod(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(invokeHandle);/* 调用句柄 */
		int size = this.size();
		out.writeInt(size);/* 方法数量 */
		for (int i = 0; i < size; i++) {
			RemoteMethod method = this.get(i);
			method.writeExternal(out);/* 序列化方法 */
		}
	}

	public void validate(Class<?>... interfaces) throws AccessException {
		for (Class<?> interfac : interfaces)
			for (Method method : interfac.getMethods()) {
				String methodStr = DefaultRemoteMethod.getMethodString(method);
				if (!getMethodsMap().containsKey(methodStr)) {
					log.warn(interfac+" Unkown method: "+methodStr);
				}
			}
	}

	public RemoteMethod getMethod(String methodString) {
		return this.getMethodsMap().get(methodString);
	}

	public Map<Object, RemoteMethod> getMethodsMap() {
		if (methodsMap == null)
			methodsMap = new HashMap<Object, RemoteMethod>();
		return methodsMap;
	}

	public DefaultRemoteMethodCollection(String handle, ClassLoader loader, Class<?>... interfaces) {
		this.invokeHandle = mappingInvokeHandle(handle);
		this.loadInterfaces(loader, interfaces);
	}

	public DefaultRemoteMethodCollection(ObjectInput in) throws IOException {
		this.readExternal(in);
	}

	private transient int invokeHandle;

	private transient Map<Object, RemoteMethod> methodsMap;

	// ---------------------------------------------------------------------

	/**
	 * 对象句柄映射或生成调用句柄
	 * 
	 * @param handle
	 * @return
	 */
	public static Integer mappingInvokeHandle(String handle) {
		Integer ih = hiTable.get(handle);
		if (ih == null)
			synchronized (hiTable) {
				ih = hiTable.get(handle);
				if (ih == null) {
					ih = ++invokeHandleCount;
					hiTable.put(handle, ih);
					log.info("Mapping Static handle: " + handle + "  =>   invoke-handle: " + ih);
				}
			}
		return ih;
	}

	private static Map<String, Integer> hiTable = new HashMap<String, Integer>();
	private static int invokeHandleCount = 1000;

	private static final Logger log = Logger.getLogger(DefaultServiceObject.class);

	private static final long serialVersionUID = -8909652727256247790L;

}
