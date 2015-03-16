package app.rpc.remote;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;

public interface RemoteMethod extends Externalizable {

	/**
	 * 客户端调用
	 * 
	 * @param in
	 * @param out
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	Object invoke(ObjectInput in, ObjectOutput out, RemoteObject ro,
			Object[] args) throws Throwable;

	/**
	 * 服务端调用
	 * 
	 * @param obj
	 * @param args
	 * @param out
	 * @throws IOException
	 */
	Object invoke(ObjectOutput out, ServiceObject ro, Object[] args)
			throws IOException;

	/**
	 * 方法参数类型列表
	 * 
	 * @return
	 */
	RemoteType[] getParameterTypes();

	/**
	 * 方法返回类型
	 * 
	 * @return
	 */
	RemoteType getReturnType();

	/**
	 * 方法唯一字符串
	 * 
	 * @return
	 */
	String getString();

	/**
	 * 是否异步，方法返回值必须为void
	 * 
	 * @return
	 */
	boolean isAsync();

	/**
	 * 调用超时毫秒
	 * 
	 * @return
	 */
	int getTimeout();

	/**
	 * 超时重试次数
	 * 
	 * @return
	 */
	int getTimeoutRetry();

	void setMethod(Method method);

	Method getMethod();

}
