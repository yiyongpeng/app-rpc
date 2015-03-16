package app.rpc.remote;

import java.io.ObjectInput;

import app.core.Context;

public interface ObjectRequest extends Context, ObjectInput {

	public String getRemoteAddress();

	public int getRemotePort();

	public ObjectSession getSession();

	public ObjectServerHandler getServletContext();

}
