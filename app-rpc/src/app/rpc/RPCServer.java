package app.rpc;

import java.io.IOException;

import app.rpc.remote.DefaultObjectConnector;
import app.rpc.remote.ServiceObject;
import app.rpc.remote.impl.DefaultServiceObject;

/**
 * RPC服务端
 * 
 * @author yiyongpeng
 * 
 */
public class RPCServer extends DefaultObjectConnector {

	public RPCServer() throws IOException {
		super();
	}

	/**
	 * 导出远程对象
	 * 
	 * @param handle 导出指针
	 * @param instance 导出实例
	 * @param interfaces 导出接口
	 * @return
	 */
	public ServiceObject export(String handle, Object instance, Class<?>... interfaces) {
		ServiceObject so = new DefaultServiceObject(handle, instance, interfaces);
		getHandler().setAttribute(so.getInvokeHandle(), so);
		return so;
	}
}
