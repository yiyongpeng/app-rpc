package app.rpc.remote;

import java.io.Externalizable;

public interface Parameter extends Cloneable, Externalizable {

	void init(ObjectRequest request) throws Exception;

	void destory();

}
