package app.rpc.remote.deploy;

public interface IBeanCreator {

	Object create(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException;

	void destroy();
}
