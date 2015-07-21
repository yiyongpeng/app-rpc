package app.rpc.remote.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;

import org.apache.log4j.Logger;

import app.core.AccessException;
import app.core.impl.DefaultConnection;
import app.rpc.remote.DefaultObjectConnector;
import app.rpc.remote.DriverManager;
import app.rpc.remote.RemoteVisitor;

/**
 * 远程连接类
 * 
 * @author yiyongpeng
 * 
 */
public class DefaultClientObjectConnection extends DefaultObjectConnection
		implements Runnable {
	private static final Logger log = Logger
			.getLogger(DefaultClientObjectConnection.class);
	/** 连接关闭后自动重连等待时间 */
	public static final Object SESSION_RECONNECT_WAIT = "__SESSION_RECONNECT_WAIT__";
	private static final long RECONNECT_WAIT_TIME = 3000;
	private static final ByteBuffer PING_MSG = ByteBuffer.allocate(0);

	/** 重新连接 */

	public void run() {
		try {
			if (isClosed() == false || !isReconnect())
				return;

			DefaultObjectConnector connector = (DefaultObjectConnector) getServerHandler()
					.getConnector();

			log.warn("Reconnect  " + getInetAddress() + "  ...");

			connect0();

			// onAccpet() closed改false
			connector.registor2wait(getSocketChannel(), this);

			reconnect();// 重新连接登录

			log.warn("Reconnect  " + getInetAddress() + "  success!");
			reconnecting = false;
		} catch (AccessException e) {
			e.printStackTrace();
			reconnecting = false;
			setClosed(true);
			try {
				conn.getSocketChannel().close();
			} catch (Exception e1) {
			}
			getServerHandler().getConnector().wakeup();
			getServerHandler().removeSession(sessionId);
			sessionId = null;
		} catch (IOException e) {
			if (isReconnect()) {
				long rewait = (Long) getCoverAttributeOfUser(
						SESSION_RECONNECT_WAIT, RECONNECT_WAIT_TIME);
				getServerHandler().execute(this, rewait);
			}
		} catch (Throwable e) {
			close();// 停止断线重连
			e.printStackTrace();
		}
	}

	private void reconnect() {
		Collection<Object> msg = getMessageOutputQueue().removeAll();

		connect();

		getMessageOutputQueue().putLastAll(msg);

		if (!getMessageOutputQueue().isEmpty()) {
			log.info(conn.getInetAddress() + " Continue flush msg: "
					+ msg.size());
			flush();// 继续发送剩余消息队列
		}
	}

	@Override
	public void onClosed() {
		if (isReconnect()) {
			setClosed(true);
			// m.clear4waiting();
			if (!reconnecting) {
				reconnecting = true;
				getServerHandler().execute(this, RECONNECT_WAIT_TIME);
			}
		} else {
			super.onClosed();
			new Thread() {
				@Override
				public void run() {
					DriverManager.notifyClosed(protocol);
				};
			}.start();
		}
	}
	
	public void connect() {
		RemoteVisitor rv = newVisitor();
		try{
			rv.connect(getURLString(), username, password);/* 登录验证 */
		}finally{
			rv.destory();
		}
		setAttribute(SESSION_KEPLIVE_TIMEOUT, 30000L);// 超时时间转义为ping间隔，默认30秒

		log.debug("Connect-success: " + getURLString() + "  username: "
				+ username + "  password: " + password + "  sessionId:"
				+ getSessionId());
	}

	@Override
	protected boolean onTimeout() {
		if (!isClosed()) {
			updateLastTime();
			send(PING_MSG);
			if(log.isDebugEnabled())
			log.debug(this + "  >> ping >>  " + getInetAddress());
			return false;
		}else{
			return !isReconnect();
		}
	}

	@Override
	public void close() {
		reconnect = false;
		reconnecting = false;
		super.close();
		getHandler().removeSession(getSessionId());
	}

	public String getPwd() {
		return password;
	}

	public String getUser() {
		return username;
	}

	@Override
	public String getPath() {
		return path;
	}

	public String getURLString() {
		return protocol + ":" + hostname + ":" + port + path;
	}

	/**
	 * 远程对象连接构造方法。<br>
	 * 采用默认会话和协议
	 * 
	 * @param host
	 * @param port
	 * @param path
	 * @param pwd
	 * @param user
	 * @throws IOException
	 */
	public DefaultClientObjectConnection(String host, int port, String path,
			String user, String pwd) throws IOException {
		this("ro", host, port, path, user, pwd);
	}

	public DefaultClientObjectConnection(String protocol, String host,
			int port, String path, String username, String password)
			throws IOException {
		super(null);
		this.protocol = protocol;
		this.hostname = host;
		this.port = port;
		this.path = path;
		this.username = username;
		this.password = password;
		this.sessionId = String.valueOf(hashCode());
		
		connect0();
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	private void connect0() throws IOException {
		DefaultConnection conn = (this.conn == null ? new DefaultConnection()
				: (DefaultConnection) this.conn);
		conn.init(SocketChannel.open(new InetSocketAddress(hostname, port)));
		conn.setSession(this);
		init(conn);
	}

	public boolean isReconnecting() {
		return reconnecting;
	}

	public boolean isReconnect() {
		return reconnect;
	}

	public void setReconnect(boolean reconnect) {
		this.reconnect = reconnect;
	}

	private String path;
	private String password;
	private String username;

	private int port;
	private String hostname;
	private String protocol;

	private volatile boolean reconnecting = false;
	private volatile boolean reconnect = true;
}
