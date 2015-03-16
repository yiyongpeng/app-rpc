package app.rpc.remote;

public interface ObjectConnectionSource {

	void init();

	ObjectConnection getConnection();

	void destory();

	int size();

}
