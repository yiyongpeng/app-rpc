package app.rpc.remote.deploy;

public interface DeployClassLoader {

	void init(DeployContext context);

	void destroy();
}
