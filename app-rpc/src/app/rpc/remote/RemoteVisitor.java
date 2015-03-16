package app.rpc.remote;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;

import app.core.AccessException;
import app.net.AppSession;
import app.rpc.remote.service.RegistorParameter;

public interface RemoteVisitor {

	/**
	 * 校验远程对象
	 * 
	 * @param ro
	 * @throws AccessException
	 */
	void validate(RemoteObject ro) throws AccessException;

	/**
	 * 注册到指定作用域范围
	 * 
	 * @param handle
	 * @param instance
	 * @param interfaces
	 * @param scope
	 *            {@link RegistorParameter}.SCOPE_...
	 * @throws IOException
	 */
	void registor(String handle, Serializable instance, Class<?>[] interfaces,
			Scope scope) throws AccessException;

	/**
	 * 调用远程方法
	 * 
	 * @param remoteObject
	 * @param method
	 * @param args
	 * @return
	 * @throws IOException
	 */
	Object invoke(RemoteObject remoteObject, Method method, Object[] args)
			throws Throwable;

	/**
	 * 建立连接
	 * 
	 * @param query
	 * @param user
	 * @param pwd
	 * @throws IOException
	 */
	void connect(String url, String user, String pwd) throws AccessException;

	void init(AppSession session);

	void destory();

}
