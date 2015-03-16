package app.rpc.remote;

import java.io.IOException;
import java.io.Serializable;

import app.core.Remote;
import app.net.AppSession;

/**
 * 远程对象会话
 * 
 * @author yiyongpeng
 * 
 */
public interface ObjectSession extends AppSession, Remote {
	/** Login info, class:com.remote.service.LoginParameter */
	String LOGIN_USER = "__LOGIN_USER__";

	/** 网关Session */
	String GATEWAY_SESSION = "__GATEWAY_SESSION__";

	/**
	 * 清除Remote缓存
	 */
	void clearRemoteCached();

	/**
	 * 获取服务器处理器
	 * 
	 * @return
	 */

	ObjectServerHandler getServerHandler();

	/**
	 * 获取远程对象引用
	 * 
	 * @param handle
	 *            远程对象句柄
	 * @param loader
	 *            自定义类加载器
	 * @param interfaces
	 *            接口列表
	 * @return
	 */
	Object getRemote(String handle, ClassLoader loader, Class<?>... interfaces);

	/**
	 * 获取远程对象引用
	 * 
	 * @param handle
	 *            远程对象句柄
	 * @param interfaces
	 *            接口列表
	 * @return
	 */
	Object getRemote(String handle, Class<?>... interfaces);

	/**
	 * 获取远程对象引用
	 * 
	 * @param handle
	 *            远程对象句柄
	 * @param loader
	 *            自定义类加载器
	 * @param interfaces
	 *            接口列表
	 * @return
	 */
	<R> R getRemote(String handle, ClassLoader loader, Class<R> interfac);

	/**
	 * 获取远程对象引用
	 * 
	 * @param handle
	 *            远程对象句柄
	 * @param interfaces
	 *            接口列表
	 * @return
	 */
	<R> R getRemote(String handle, Class<R> interfac);

	/**
	 * 获取远程Session
	 * 
	 * @return
	 */
	ObjectSession getRemote();

	/**
	 * 注册对象到默认SESSION作用域范围
	 * 
	 * @param handle
	 *            对象句柄
	 * @param instance
	 *            需要注册的对象
	 * @param interfaces
	 *            允许访问的接口列表
	 * @throws IOException
	 */
	void registor(String handle, Serializable instance, Class<?>... interfaces)
			throws IOException;

	/**
	 * 注册远程对象到指定作用域范围
	 * 
	 * @param scope
	 *            作用域 {@link Scope} 枚举
	 * @param handle
	 *            对象句柄
	 * @param instance
	 *            需要注册的对象
	 * @param interfaces
	 *            允许访问的接口列表
	 * @throws IOException
	 */
	void registor(Scope scope, String handle, Serializable instance,
			Class<?>... interfaces);

	/** 获取已连接远程对象信息对象 */
	RemoteObject getRemoteObject(String handle);

	/** 释放远程对象 */
	void free(String handle);

	/** 释放远程对象 */
	void free(Object proxy);

	/** 关闭会话 */
	void close();

	/** 是否已关闭 */
	boolean isClosed();

	/** 创建服务器同步Session */
	ObjectSession getSession(Integer sid);

	/** 创建服务器同步Session */
	ObjectSession createSession(Integer sid);

	/** 获取或创建Session */
	ObjectSession getOrCreateSession(Integer sid);

}
