package app.rpc.remote.impl;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import app.rpc.remote.Async;
import app.rpc.remote.RemoteMethod;
import app.rpc.remote.RemoteObject;
import app.rpc.remote.RemoteType;
import app.rpc.remote.RemoteType4Basic;
import app.rpc.remote.RemoteTypeMapper;
import app.rpc.remote.ServiceObject;
import app.rpc.remote.Timeout;
import app.rpc.remote.Value;
import app.util.POJO;

/**
 * 远程方法
 * 
 * @author yiyongpeng
 * 
 */
public class DefaultRemoteMethod extends POJO implements RemoteMethod {
	private int handle;
	private String name;
	private RemoteType[] paramTypes;
	private RemoteType returnType;

	private boolean async;
	private int timeout;
	private int retry;

	private Method method;
	private String string;

	public DefaultRemoteMethod() {
	}
	
	protected DefaultRemoteMethod(ObjectInput in) throws IOException {
		this.readExternal(in);
	}

	public void setHandle(int handle) {
		this.handle = handle;
	}

	public void parseMethod(Method method) {
		Class<?>[] types = method.getParameterTypes();
		Annotation[][] annos = method.getParameterAnnotations();
		boolean value = false;
		name = method.getName();
		paramTypes = new RemoteType[types.length];
		for (int i = 0; i < types.length; i++){
			value = false;
			Annotation[] pannos = annos[i];
			for (int j = 0; j < pannos.length; j++) {
				value=(pannos[j].annotationType()==Value.class);
			}
			paramTypes[i] = RemoteTypeMapper.mapping(types[i], value);
		}
		value = method.getAnnotation(Value.class)!=null;
		returnType = RemoteTypeMapper.mapping(method.getReturnType(), value);
		this.setMethod(method);
	}

	public void readExternal(ObjectInput in) throws IOException {
		handle = in.readInt(); /* 方法句柄 */
		returnType = RemoteTypeMapper.read(in); /* 返回类型 */
		name = in.readUTF(); /* 方法名 */

		int size = in.readInt();/* 参数数量 */
		paramTypes = new RemoteType[size];
		for (int i = 0; i < size; i++)
			paramTypes[i] = RemoteTypeMapper.read(in);/* 参数类型 */

		async = in.readBoolean();/* 是否异步 */
		timeout = in.readInt();/* 调用超时毫秒 */
		retry = in.readUnsignedByte();/* 超时重发次数 */
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(handle); /* 方法句柄 */
		RemoteTypeMapper.write(returnType, out);/* 返回类型 */
		out.writeUTF(name); /* 方法名 */
		out.writeInt(paramTypes.length);/* 参数数量 */
		for (int i = 0, size = paramTypes.length; i < size; i++)
			RemoteTypeMapper.write(paramTypes[i], out);/* 参数类型 */

		out.writeBoolean(async);/* 是否异步 */
		out.writeInt(timeout);/* 调用超时毫秒 */
		out.writeByte(retry);/* 超时重发次数 */
	}

	public Object invoke(ObjectInput in, ObjectOutput out, RemoteObject ro,
			Object[] args) throws Throwable {
		Thread t = null;
		ClassLoader cl = null;
		boolean changeCl = returnType.getType() == RemoteType4Basic.TYPE_OTHER;
		try {
			// 调用前准备
			if (changeCl) {
				t = Thread.currentThread();
				cl = t.getContextClassLoader();
				t.setContextClassLoader(ro.getProxy().getClass()
						.getClassLoader());
			}

			// 调用前参数过滤
			MethodInvokeInterceptor.getInstance().invokeBefore(ro, this, args);

			out.writeInt(handle);/* 方法句柄 */
			for (int i = 0; i < paramTypes.length; i++)
				paramTypes[i].writeValue(args[i], out); /* 写参数列表 */
			out.flush();/* 发送数据 */

			if (!async) {
				// 调用是否正常
				if (!in.readBoolean()) {
					throw (Throwable) in.readObject();/* 方法异常 */
				}
			} else {
				// System.out.println("async invoke: [" + handle + "]" +
				// getString()
				// + "  ><  " + Arrays.toString(args));
			}
			// 调用后处理
			return MethodInvokeInterceptor.getInstance().invokeAfter(
					returnType.readValue(ro.getSession(), in), ro, this, args);
		} finally {
			if (changeCl) {
				t.setContextClassLoader(cl);
			}
		}
	}

	public Object invoke(ObjectOutput out, ServiceObject ro, Object[] args)
			throws IOException {
		try {
			// 调用前参数过滤
			MethodInvokeInterceptor.getInstance().invokeBefore(ro, this, args);

			// 执行方法
			Object value = invoke(ro.getInstance(), args);

			// 调用后处理;
			value = MethodInvokeInterceptor.getInstance().invokeAfter(value,
					ro, this, args);
			if (!async) {
				out.writeBoolean(true);
			}
			returnType.writeValue(value, out);
			return value;
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			if (!async) {
				out.writeBoolean(false);
				out.writeObject(e.getTargetException());
			}
		} catch(ClassCastException e){
			e.printStackTrace();
//			System.err.println("instance: "+ro.getInstance().getClass().getClassLoader()+"  remote-method: "+this.getClass().getClassLoader());
//			for (int i=0; i<args.length; i++) {
//				Object obj = args[i];
//				if(obj==null){
//					System.err.println(i+"  null");
//				}else{
//					System.err.println(i+"  "+obj+ "  "+getInterface(obj, method.getParameterTypes()[i])+" : "+method.getParameterTypes()[i]+"("+method.getParameterTypes()[i].getClassLoader()+")");
//				}
//			}
		}catch (Throwable e) {
//			System.err.println("service invoke failed!  method: " + method
//					+ "   args: " + Arrays.toString(args) + "  this-method: "
//					+ isThisMethod(ro.getInstance(), method) + "  instance: "
//					+ ro.getInstance() + "   handle: " + ro.getHandle()+"  loader: "+this.getClass().getClassLoader());
			if (async) {
				e.printStackTrace();
			} else {
				out.writeBoolean(false);
				out.writeObject(e);
				e.printStackTrace();
			}
		}
		return null;
	}

//	private String getInterface(Object obj, Class<?> interfaze) {
//		for (Class<?> clazz : obj.getClass().getInterfaces())
//			if(clazz.getName().equals(interfaze.getName())) {
//				return clazz+"("+clazz.getClassLoader()+")";
//			}
//		return "unkwon";
//	}

	protected Object invoke(Object instance, Object[] args) throws Throwable {
		return method.invoke(instance, args);
	}

//	private boolean isThisMethod(Object instance, Method method) {
//		try {
//			return method == instance.getClass().getMethod(method.getName(),
//					method.getParameterTypes());
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//			for(Method method2 : instance.getClass().getMethods()){
//				if(method2.getName().equals(method.getName())){
//					Class<?>[] types = method.getParameterTypes();
//					Class<?>[] types2 = method2.getParameterTypes();
//					for(int i=0; i<types2.length; i++){
//						System.err.println(types2[i]+"("+types2[i].getClassLoader()+") <= "+types[i]+"("+types[i].getClassLoader()+")");
//					}
//				}
//			}
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}

	public RemoteType[] getParameterTypes() {
		return this.paramTypes;
	}

	public RemoteType getReturnType() {
		return this.returnType;
	}

	public boolean isAsync() {
		return async;
	}

	public int getTimeout() {
		return timeout;
	}

	public int getTimeoutRetry() {
		return retry;
	}

	public String getString() {
		if (string == null) {
			StringBuilder sb = new StringBuilder(returnType.getName());
			sb.append(" ");
			sb.append(name);
			sb.append("(");
			for (int i = 0; i < paramTypes.length; i++) {
				RemoteType type = paramTypes[i];
				if (i > 0)
					sb.append(", ");
				sb.append(type.getName());
			}
			sb.append(")");
			string = sb.toString();
		}
		return string;
	}

	public static String getMethodString(Method method) {
		Class<?> type = method.getReturnType();
		Class<?>[] types = method.getParameterTypes();

		StringBuilder name = new StringBuilder(type.getName());
		name.append(" ");
		name.append(method.getName());
		name.append("(");
		for (int i = 0; i < types.length; i++) {
			type = types[i];
			if (i > 0)
				name.append(", ");
			name.append(type.getName());
		}
		name.append(")");

		return name.toString();
	}

	public void setMethod(Method method) {
		this.method = method;
		Class<?> clazz = method.getDeclaringClass();
		async = false;
		if (returnType.getType() == RemoteType4Basic.TYPE_VOID) {
			Async asy = method.getAnnotation(Async.class);
			if (asy == null) {
				asy = clazz.getAnnotation(Async.class);
			}
			async = asy != null && asy.value();
		}
		Timeout t = method.getAnnotation(Timeout.class);
		if (t == null) {
			t = clazz.getAnnotation(Timeout.class);
		}
		if (t != null) {
			timeout = t.time();
			retry = t.retry();
		}
	}

	public Method getMethod() {
		return method;
	}
}
