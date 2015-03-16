package app.rpc.remote.impl;

import java.io.IOException;

import app.rpc.remote.ObjectConnection;
import app.rpc.remote.ObjectConnectionFactory;

public class DefaultClientObjectConnectionFactory implements
		ObjectConnectionFactory {

	/**
	 * hostname[:9000][/]
	 */

	public ObjectConnection create(RemoteUrl url, String username,
			String password) throws IOException {
		return new DefaultClientObjectConnection(url.getProtocol(),
				url.getHostName(), url.getPort(), url.getPath(), username,
				password);
	}
}
