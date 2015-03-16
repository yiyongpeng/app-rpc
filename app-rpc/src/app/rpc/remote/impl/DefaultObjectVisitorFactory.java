package app.rpc.remote.impl;

import app.net.AppSession;
import app.rpc.remote.RemoteVisitor;
import app.rpc.remote.RemoteVisitorFactory;

public class DefaultObjectVisitorFactory implements RemoteVisitorFactory {

	public RemoteVisitor create(AppSession session) {
		return DefaultRemoteVisitor.allocate(session);
	}

}
