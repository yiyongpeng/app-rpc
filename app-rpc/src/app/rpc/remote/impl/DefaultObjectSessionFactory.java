package app.rpc.remote.impl;

import app.core.Connection;
import app.rpc.remote.ObjectSession;
import app.rpc.remote.ObjectSessionFactory;

public class DefaultObjectSessionFactory implements ObjectSessionFactory {

	public ObjectSession create(Connection conn, Object sessionId) {
		return new DefaultObjectConnection(conn, String.valueOf(sessionId));
	}

}
