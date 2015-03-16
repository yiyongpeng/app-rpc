package app.rpc.remote.service;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import app.rpc.remote.ObjectRequest;
import app.rpc.remote.Parameter;
import app.util.POJO;

public class DefaultParameter extends POJO implements Parameter {

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
	}

	public void writeExternal(ObjectOutput out) throws IOException {
	}

	public void destory() {
	}

	public void init(ObjectRequest request) throws Exception {
		readExternal(request);

	}

}
