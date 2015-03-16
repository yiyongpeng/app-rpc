package app.rpc.remote;

import java.io.IOException;

/**
 * 已注册的服务对象
 * 
 * @author yiyongpeng
 * 
 */
public interface ServiceObject extends InetObject {

	void init();

	Object getInstance();

	void destroy();

	Object getInvokeHandle();

	String getHandle();

	void invoke(int mid, Object[] args, ObjectResponse response) throws IOException;
	
}
