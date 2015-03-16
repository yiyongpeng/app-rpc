package app.rpc.remote;

import java.io.IOException;

import app.rpc.remote.impl.RemoteUrl;

public interface ObjectConnectionFactory {

	ObjectConnection create(RemoteUrl url, String username, String password)
			throws IOException;

}
