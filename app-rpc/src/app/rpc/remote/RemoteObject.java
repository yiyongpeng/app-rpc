package app.rpc.remote;

import java.lang.reflect.Method;

public interface RemoteObject extends InetObject {

	void close();

	RemoteMethod mapping(Method method);

	Class<?>[] getInterfaces();

	void setMethods(RemoteMethodCollection methods);

	ObjectSession getSession();

	Object getProxy();

	int getInvokeHandle();
}
