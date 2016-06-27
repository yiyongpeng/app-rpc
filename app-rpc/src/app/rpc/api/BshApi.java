package app.rpc.api;

import app.core.Remote;
import app.rpc.remote.Async;
import app.rpc.utils.RemoteInputStream;
import app.rpc.utils.RemoteOutputStream;

public interface BshApi extends Remote {

	@Async
	void bsh(RemoteInputStream in, RemoteOutputStream out, RemoteOutputStream err);
	
	String bsheval(String code);
}
