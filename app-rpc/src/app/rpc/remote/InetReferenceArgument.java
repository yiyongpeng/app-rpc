package app.rpc.remote;

import java.io.Serializable;

public interface InetReferenceArgument extends Serializable {

	/**
	 * 初始化
	 * 
	 * @param session
	 *            Session 会话
	 * @param io
	 *            网络对象
	 * @param m
	 *            方法对象
	 * @param i
	 *            参数位置，-1是返回值
	 * @param v
	 *            参数值
	 */
	void init(ObjectSession session, InetObject io, RemoteMethod m, int i,
			Object v);

	void destroy();

}
