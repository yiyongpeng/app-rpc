package app.rpc.remote.spring;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import app.rpc.remote.ObjectConnection;
import app.rpc.utils.ConfigUtils;

@SuppressWarnings("rawtypes")
public abstract class AbstractRemoteFactoryBean extends AbstractFactoryBean {

	@Override
	public Class<?> getObjectType() {
		try {
			return createInstance().getClass();
		} catch (Exception e) {
			throw new BeanCreationException("create failed. " + e.getMessage(),
					e);
		}
	}

	@Override
	public Object createInstance() throws Exception {
		if (object == null)
			synchronized (this) {
				if (object == null) {
					object = getConnection().getSession().getRemote(
							getHandle(),
							ConfigUtils.parseInterfaces(interfaces));
				}
			}
		return object;
	}

	protected abstract ObjectConnection getConnection();

	public String getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(String interfaces) {
		this.interfaces = interfaces;
	}

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	protected Object object;

	protected String interfaces;
	protected String handle;

}
