package app.rpc.remote.deploy;

public interface FactoryBean {

	void init(DeployContext context);

	Object createBean();

	void destroy();
}
