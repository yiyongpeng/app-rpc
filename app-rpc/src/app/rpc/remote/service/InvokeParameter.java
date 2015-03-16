package app.rpc.remote.service;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import app.core.AccessException;
import app.rpc.remote.ObjectRequest;
import app.rpc.remote.ObjectResponse;
import app.rpc.remote.RemoteMethod;
import app.rpc.remote.RemoteMethodCollection;
import app.rpc.remote.RemoteType;
import app.rpc.remote.RemoteType4Custom;
import app.rpc.remote.ServiceObject;

public class InvokeParameter extends DefaultParameter {
	private ObjectRequest request;
	private ServiceObject serviceObject;

	private int handle;
	private int method;
	private Object[] args;

	private Object handleInteger;

	public InvokeParameter() {
	}

	@Override
	public void init(ObjectRequest request) throws Exception {
		this.request = request;
		super.init(request);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		Thread t = null;
		ClassLoader loader = null;
		try {
			handle = in.readInt();
			method = in.readInt();
			// 获取调用对象
			serviceObject = getServiceObject();
			if (serviceObject == null) {
				throw new AccessException(new StringBuffer(
						" Not found Remote object reference.  invoke-handle: ")
						.append(handle).append("  session: ")
						.append(request.getSession().getInetAddress())
						.toString());
			}
			// 输入参数
			RemoteMethodCollection mc = serviceObject.getMethodCollection();
			RemoteMethod methd = mc.getMethod(method);
			RemoteType[] types = methd.getParameterTypes();
			args = new Object[types.length];
			for (int i = 0; i < types.length; i++){
				if((t == null) && (types[i] instanceof RemoteType4Custom)){
					t = Thread.currentThread();
					loader = t.getContextClassLoader();
					t.setContextClassLoader(serviceObject.getInstance().getClass().getClassLoader());
				}
				args[i] = types[i].readValue(request.getSession(), in);
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			ObjectResponse resp = (ObjectResponse) in;
			resp.writeBoolean(false);
			resp.writeObject(ex);
			throw new AccessException(ex);
		} finally {
			if(t!=null){
				t.setContextClassLoader(loader);
			}
		}
	}

	private Object getInvokeHandle() {
		if (handleInteger != null)
			return handleInteger;
		return handleInteger = handle;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(handle);
		out.writeInt(method);

		// 输出参数
		RemoteMethod methd = serviceObject.getMethodCollection().getMethod(
				method);
		RemoteType[] types = methd.getParameterTypes();
		for (int i = 0; i < types.length; i++)
			types[i].writeValue(args[i], out);
	}

	public int getMethod() {
		return method;
	}

	public Object[] getArguments() {
		return args;
	}

	public ServiceObject getServiceObject() {
		if (serviceObject == null) {
			serviceObject = (ServiceObject) request.getSession()
					.getCoverAttributeOfApp(getInvokeHandle(), null);
		}
		return serviceObject;
	}

	@Override
	public void destory() {
		method = 0;
		handle = 0;
		handleInteger = null;
		serviceObject = null;
		args = null;
		request = null;
		super.destory();
	}

}
