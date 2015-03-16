package simple;

import app.core.Remote;
import app.rpc.remote.Async;
import app.rpc.remote.Timeout;

public interface Hello extends Remote {

	@Timeout(time = 3000, retry = 10)
	String say(char ch, boolean bool, byte bt, short sh, int in, long l,
			float fl, double doub);

	@Async
	void setName(String name);

	Hello getParent();

	void setParent(Hello hello);

	SeriModel getModel(SeriModel model);

	String getName();

}