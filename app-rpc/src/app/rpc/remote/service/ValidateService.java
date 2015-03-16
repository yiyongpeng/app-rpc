package app.rpc.remote.service;

import java.io.IOException;

import app.rpc.remote.ObjectRequest;
import app.rpc.remote.ObjectResponse;
import app.rpc.remote.ServiceObject;
import app.rpc.remote.impl.DefaultRemoteMethodCollection;
import app.rpc.remote.impl.ObjectHandle;

public class ValidateService extends GenericService<ObjectHandle> {

	@Override
	public void doService(ObjectHandle ohp, ObjectRequest request,
			ObjectResponse response) throws IOException {
		String handle = ohp.getHandle();
		int ih = DefaultRemoteMethodCollection.mappingInvokeHandle(handle);

		ServiceObject so = (ServiceObject) request.getSession()
				.getCoverAttributeOfApp(ih, null);

		boolean registed = so != null;

		response.writeBoolean(registed);
		if (registed) {
			so.getMethodCollection().writeExternal(response);
		}
	}
}
