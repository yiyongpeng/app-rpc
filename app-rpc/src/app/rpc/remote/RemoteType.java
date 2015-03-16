package app.rpc.remote;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public interface RemoteType extends Externalizable {

	void writeValue(Object value, ObjectOutput out) throws IOException;

	Object readValue(ObjectSession session, ObjectInput in) throws IOException;

	byte getType();

	String getName();

	boolean isInterface();

	boolean isValue();
}
