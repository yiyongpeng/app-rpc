package app.rpc.remote.service;

import java.io.IOException;

import app.core.Context;
import app.rpc.remote.ObjectRequest;
import app.rpc.remote.ObjectResponse;
import app.rpc.remote.ServiceObject;
import app.rpc.remote.impl.DefaultRemoteMethodCollection;
import app.rpc.remote.impl.DefaultServiceObject;

/**
 * 远程对象注册服务
 * 
 * @author yiyongpeng
 * 
 */
public class RegistorService extends GenericService<RegistorParameter> {

	@Override
	public void doService(RegistorParameter param, ObjectRequest request,
			ObjectResponse response) throws IOException {
		Context context = param.getContext();
		String handle = param.getHandle();
		Object instance = param.getInstance();
		Class<?>[] interfaces = param.getInterfaces();

		int ih = DefaultRemoteMethodCollection.mappingInvokeHandle(handle);
		if (context.contains(ih) == false)
			try {
				ServiceObject so = new DefaultServiceObject(handle, instance,
						interfaces);
				context.setAttribute(so.getInvokeHandle(), so);
				response.writeBoolean(true);

				// log.debug("reg: " + handle + " interfaces:" +
				// Arrays.toString(interfaces));
			} catch (Throwable e) {
				response.writeBoolean(false);
				response.writeUTF(e.toString());
			}
		else {
			response.writeBoolean(false);
			response.writeUTF("The Remote handle exists,  handle: " + handle
					+ "  invoke-handle: " + ih);
		}
	}

}
