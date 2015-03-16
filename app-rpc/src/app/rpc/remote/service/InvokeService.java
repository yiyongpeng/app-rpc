package app.rpc.remote.service;

import java.io.IOException;

import app.rpc.remote.ObjectRequest;
import app.rpc.remote.ObjectResponse;
import app.rpc.remote.ServiceObject;

/**
 * 远程对象调用服务
 * 
 * @author yiyongpeng
 * 
 */
public class InvokeService extends GenericService<InvokeParameter> {

	@Override
	public void doService(InvokeParameter param, ObjectRequest request,
			ObjectResponse response) throws IOException {
		int mid = param.getMethod();
		Object[] args = param.getArguments();
		ServiceObject so = param.getServiceObject();
		
		// Invoke
		so.invoke(mid, args, response);
		
	}

}
