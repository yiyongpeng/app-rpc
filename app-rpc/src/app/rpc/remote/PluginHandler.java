package app.rpc.remote;

public interface PluginHandler {

	void init(ObjectServerHandler serverHandler);

	void destroy();

}
