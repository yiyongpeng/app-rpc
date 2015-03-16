package app.rpc.remote;

public interface ServiceManager {

	void registor(int mode, Service service);

	Service unregistor(int mode);

}
