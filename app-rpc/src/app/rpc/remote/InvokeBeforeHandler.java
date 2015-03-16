package app.rpc.remote;

/**
 * 调用前处理器
 * 
 * @author yiyongpeng
 * 
 */
public interface InvokeBeforeHandler {

	void invokeBefore(InetObject ro, RemoteMethod method, Object[] args);
}
