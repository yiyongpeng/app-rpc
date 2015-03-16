package app.rpc.remote;

public interface Service {

	void init(ObjectServerHandler handler);

	void doService(ObjectRequest request, ObjectResponse response)
			throws Exception;

	void destory();

}
