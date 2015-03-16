package app.rpc.remote;

/**
 * 调用后处理器
 * 
 * @author yiyongpeng
 * 
 */
public interface InvokeAfterHandler {

	/**
	 * 调用后处理
	 * 
	 * @param value
	 *            返回值
	 * @param ro
	 *            远程对象
	 * @param method
	 *            方法
	 * @param args
	 *            参数列表
	 * @return 处理后返回值
	 */
	Object invokeAfter(Object value, InetObject ro, RemoteMethod method,
			Object[] args);
}
