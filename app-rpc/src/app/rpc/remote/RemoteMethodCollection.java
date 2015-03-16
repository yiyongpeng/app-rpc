package app.rpc.remote;

import java.io.Externalizable;

public interface RemoteMethodCollection extends Externalizable {

	RemoteMethod getMethod(String method);

	RemoteMethod getMethod(int code);

	int getInvokeHandle();

}
