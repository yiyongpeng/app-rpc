package app.rpc.remote.impl;

import java.io.IOException;
import java.io.ObjectInput;

public class ClientRemoteMethod extends DefaultRemoteMethod {

	protected ClientRemoteMethod(ObjectInput in) throws IOException {
		super(in);
	}

}
