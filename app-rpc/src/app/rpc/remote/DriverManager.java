package app.rpc.remote;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import app.core.AccessException;
import app.rpc.remote.impl.DefaultClientObjectConnection;
import app.rpc.remote.impl.DefaultClientObjectConnectionFactory;
import app.rpc.remote.impl.DefaultObjectVisitorFactory;
import app.rpc.remote.impl.RemoteUrl;
import app.rpc.utils.ConfigUtils;

/**
 * 驱动管理器
 * 
 * @author yiyongpeng
 * 
 */
public class DriverManager {
	/** 是否自动实现远程引用 */
	public static boolean AUTO_REMOTE = false;

	private static Map<String, ObjectConnector> connectors = new ConcurrentHashMap<String, ObjectConnector>();
	private static Map<String, ObjectConnectionFactory> connFactoryMap = new ConcurrentHashMap<String, ObjectConnectionFactory>();

	public synchronized static ObjectConnector getConnector(String protocol) throws IOException {
		ObjectConnector connector = connectors.get(protocol);
		if (connector == null) {
			connector = createConnector(protocol);
			connector.setServer(false);
			connector.start();
			connectors.put(protocol, connector);
		}
		return connector;
	}

	/**
	 * 获取远程对象连接
	 * 
	 * @param connString
	 *            连接字符串 [protocol:]hostname[:port][/path]
	 * @param username
	 * @param password
	 * @return
	 * @throws IOException
	 */
	public static ObjectConnection getConnection(String connString,
			String username, String password) throws AccessException {
		RemoteUrl url = new RemoteUrl(connString);
		String protocol = url.getProtocol();
		DefaultObjectConnector connector;
		try {
			connector = ((DefaultObjectConnector) getConnector(protocol));
		} catch (IOException e) {
			throw new AccessException("创建连接器失败！",e);
		}
		ObjectConnectionFactory connFactory = getConnectionFactory(protocol);// 在Connector初始化之后获取
		if (connFactory == null)
			throw new AccessException("未找到协议驱动程序：" + url.getProtocol());

		ObjectConnection conn = null;
		try {
			conn = connFactory.create(url, username, password);
			connector.registor2wait(conn.getSocketChannel(), conn);
			((DefaultClientObjectConnection) conn).connect();
			return conn;
		} catch (Exception e) {
			if (conn != null)
				conn.close();
			throw new AccessException("Connect failed: " + connString,e);
		}
	}

	public static ObjectConnectionFactory setConnectionFactory(String protocol,
			ObjectConnectionFactory factory) {
		return connFactoryMap.put(protocol, factory);
	}

	public synchronized static ObjectConnectionFactory getConnectionFactory(
			String protocol) {
		ObjectConnectionFactory cf = connFactoryMap.get(protocol);
		if (cf != null)
			return cf;
		Properties props = ConfigUtils.loadRemoteHandlersProps();
		String key = "protocol." + protocol + ".connfactory";
		if (props.containsKey(key))
			try {
				cf = (ObjectConnectionFactory) Thread.currentThread().getContextClassLoader().loadClass(
						props.getProperty(key)).newInstance();
			} catch (Exception e) {
				throw new AccessException(
						"Create ObjectConnectionFactory failed.  class: "
								+ props.getProperty(key) + "  protocol: "
								+ protocol, e);
			}
		else if ("ro".equals(protocol)) {
			cf = new DefaultClientObjectConnectionFactory();
		} else {
			throw new AccessException(
					"Not found ObjectConnectionFactory.  Unknown protocol: "
							+ protocol);
		}
		connFactoryMap.put(protocol, cf);
		return cf;
	}

	public static RemoteVisitorFactory getVisitorFactory(String protocol) {
		Properties props = ConfigUtils.loadRemoteHandlersProps();
		String key = "protocol." + protocol + ".vistor";
		if (props.containsKey(key))
			try {
				return (RemoteVisitorFactory) Thread.currentThread().getContextClassLoader().loadClass(
						props.getProperty(key)).newInstance();
			} catch (Exception e) {
				throw new AccessException(
						"Create RemoteVisitorFactory failed.  class: "
								+ props.getProperty(key) + "  protocol: "
								+ protocol, e);
			}
		else if ("ro".equals(protocol)) {
			return new DefaultObjectVisitorFactory();
		}
		throw new AccessException(
				"Not found RemoteVisitorFactory.  Unknown protocol: "
						+ protocol);
	}

	public synchronized static void notifyClosed(String protocol) {
		ObjectConnector connector = connectors.get(protocol);
		if (connector != null
				&& connector.getServerHandler().getConnections().size() == 0) {
			connectors.remove(protocol);
			connector.stop();
		}
	}

	public static ObjectConnector createConnector(String protocol) {
		Properties props = ConfigUtils.loadRemoteHandlersProps();
		String className = props.getProperty("protocol." + protocol
				+ ".connector");
		if (className == null) {
			if (!"ro".equals(protocol)) {
				throw new AccessException("Unknown transfer protocol: "
						+ protocol);
			} else {
				try {
					return new DefaultObjectConnector();
				} catch (IOException e) {
					throw new AccessException(
							"create default connector failed!", e);
				}
			}
		}
		try {
			Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
			return (ObjectConnector) clazz.newInstance();
		} catch (Throwable e) {
			throw new AccessException(
					"ObjectConnector create failure! protocol=" + protocol
							+ "  class: " + className, e);
		}
	}

}
