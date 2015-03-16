package app.rpc.remote.spring;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import app.rpc.remote.DriverManager;
import app.rpc.remote.ObjectConnection;

@SuppressWarnings("rawtypes")
public class ObjectConnectionFactoryBean extends AbstractFactoryBean {
	private static final Logger log = Logger
			.getLogger(ObjectConnectionFactoryBean.class);

	private String url;
	private String username = "";
	private String password = "";

	private ObjectConnection conn;

	@Override
	protected void destroyInstance(Object instance) throws Exception {
		((ObjectConnection)instance).close();
	}
	
	@Override
	protected Object createInstance() {
		if (conn == null)
			synchronized (this) {
				while (conn == null)
					try {
						conn = DriverManager.getConnection(url, username,
								password);
					} catch (Exception e) {
						log.warn("Retry connect " + url + "  Reason: "
								+ e.getMessage());
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
			}
		return conn;
	}

	@Override
	public Class<?> getObjectType() {
		return createInstance().getClass();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
