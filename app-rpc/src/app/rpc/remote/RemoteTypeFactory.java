package app.rpc.remote;

import java.io.IOException;
import java.io.ObjectInput;

public interface RemoteTypeFactory {

	RemoteType create(Class<?> clazz, boolean value);

	RemoteType create(ObjectInput in) throws IOException;

}
