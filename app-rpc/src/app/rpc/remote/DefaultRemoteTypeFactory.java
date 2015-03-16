package app.rpc.remote;

import java.io.IOException;
import java.io.ObjectInput;

public class DefaultRemoteTypeFactory implements RemoteTypeFactory {
	@Override
	public RemoteType create(Class<?> clazz, boolean value) {
		return new RemoteType4Custom(clazz, value);
	}
	
	@Override
	public RemoteType create(ObjectInput in) throws IOException {
		return new RemoteType4Custom(in);
	}
}
