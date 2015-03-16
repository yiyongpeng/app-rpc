package app.rpc.remote.deploy;

import org.apache.log4j.Logger;

public class DefaultBeanCreator implements IBeanCreator {
	private static final Logger log = Logger.getLogger(DefaultBeanCreator.class);
	protected DeployContext context;
	
	public DefaultBeanCreator(DeployContext context) {
		this.context = context;
	}

	@Override
	public Object create(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Object instance = null;
		instance = Thread.currentThread().getContextClassLoader()
				.loadClass(className).newInstance();
		if (instance instanceof FactoryBean) {
			FactoryBean factory = ((FactoryBean) instance);
			factory.init(context);
			instance = factory.createBean();
			log.debug("factory-bean: " + className + "  create-bean:"
					+ instance);
			factory.destroy();
		}
		return instance;
	}

	@Override
	public void destroy() {
		this.context = null;
	}

}
