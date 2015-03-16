package app.rpc.remote.spring;

import app.rpc.remote.ObjectConnection;
import app.rpc.remote.ObjectConnectionSource;
import app.rpc.utils.ConfigUtils;

public class DataSourceRemoteFactoryBean extends AbstractRemoteFactoryBean {

	@Override
	public String getHandle() {
		if (handle == null) {
			String path = conn.getPath();
			handle = path.substring(path.lastIndexOf("/") + 1);
		}
		return super.getHandle();
	}

	@Override
	public Object createInstance() throws Exception {
		if (object == null)
			synchronized (this) {
				if (object == null) {
					conn = getConnection();
					try {
						object = conn.getSession().getRemote(getHandle(),
								ConfigUtils.parseInterfaces(interfaces));
					} finally {
						conn.close();//放回连接池
					}
				}
			}
		return object;
	}

	@Override
	protected ObjectConnection getConnection() {
		return getDataSource().getConnection();
	}

	public ObjectConnectionSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(ObjectConnectionSource dataSource) {
		this.dataSource = dataSource;
	}

	private ObjectConnection conn;

	private ObjectConnectionSource dataSource;

}
