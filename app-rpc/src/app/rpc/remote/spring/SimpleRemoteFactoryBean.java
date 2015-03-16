package app.rpc.remote.spring;

import app.rpc.remote.DriverManager;
import app.rpc.remote.ObjectConnection;
import app.rpc.remote.impl.RemoteUrl;

public class SimpleRemoteFactoryBean extends AbstractRemoteFactoryBean {

	public SimpleRemoteFactoryBean() {
	}

	public SimpleRemoteFactoryBean(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}

	@Override
	public String getHandle() {
		if (handle == null) {
			String path = conn != null ? conn.getPath() : new RemoteUrl(url)
					.getPath();
			handle = path.substring(path.lastIndexOf("/") + 1);
		}
		return super.getHandle();
	}

	@Override
	protected ObjectConnection getConnection() {
		if (conn == null)
			synchronized (this) {
				if (conn == null) {
					conn = DriverManager.getConnection(url, username, password);
				}
			}
		return conn;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setConnection(ObjectConnection conn) {
		this.conn = conn;
	}

	private ObjectConnection conn;

	private String password = "";
	private String username = "";
	private String url;
}
