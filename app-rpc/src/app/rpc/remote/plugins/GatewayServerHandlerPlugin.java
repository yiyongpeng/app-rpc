package app.rpc.remote.plugins;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import app.core.AccessException;
import app.core.Connection;
import app.core.ServerContext;
import app.core.Session;
import app.filter.FilterAdapter;
import app.filter.IAcceptorFilter;
import app.filter.IClosedFilter;
import app.filter.IFilterChain;
import app.filter.IFilterChain.FilterChain;
import app.filter.IMessageFilter;
import app.net.AppMessageFactory;
import app.net.AppResponse;
import app.net.AppSession;
import app.net.AppMessage;
import app.net.DefaultAppSession;
import app.rpc.remote.DriverManager;
import app.rpc.remote.ObjectServerHandler;
import app.rpc.remote.ObjectSession;
import app.rpc.remote.PluginHandler;
import app.rpc.remote.impl.DefaultClientObjectConnection;
import app.rpc.remote.impl.DefaultObjectConnection;
import app.rpc.remote.impl.MultiSessionObjectMessageFactory;

public class GatewayServerHandlerPlugin extends FilterAdapter implements
		PluginHandler, IMessageFilter, IAcceptorFilter, IClosedFilter {
	public static final String APP_ATTR_NAME = "GatewayHandler";
	private ReentrantLock lock = new ReentrantLock(true);
	private ObjectSession serverSession;
	protected ObjectServerHandler serverHandler;
	private Map<Integer, String> cachedSid2SessionIdMap;
	private Map<String, Integer> cachedSessionId2SidMap;
	private int count;

	public void messageReceived(Session session0, Object message0,
			FilterChain<IMessageFilter> chain) {
		if (!(message0 instanceof AppMessage)) {
			if (chain.hasNext())
				chain.nextFilter().messageReceived(session0, message0,
						chain.getNext());
			return;
		}
		AppMessage message = (AppMessage) message0;
		DefaultAppSession session = message.getSession();
		// log.debug((serverSession != null ? "{" +
		// serverSession.getInetAddress()
		// + "}  " : "=>")
		// + session.getInetAddress()
		// + "("
		// + session.getSessionId()
		// + ")  recev:" + message.getMessage());
		if (serverSession != null
				&& message.getSession() != serverSession
				&& message.getSession().getConnection() == serverSession
						.getConnection()) {

			// ================ 网关客户端子会话消息================
			try {
				Integer sid = session.getSid();// SessionId
				String sessionId = toSessionId(sid);
				// log.debug("Recevied Redirect: gateway-sid:" + sid
				// + " -> client-sid:" + sessionId + "  " + msg);
				DefaultAppSession sess = (DefaultAppSession) serverHandler
						.getSession(sessionId);
				if (sess == null || sess.getConnection().isClosed()) {
					log.warn(sessionId
							+ " client session is closed, return-redirect  failed!");
					message.setStatus(AppResponse.STATUS_NOTFOUND);
					message.setData(null);
					message.flush();
					serverSession.getRemote().closeSession(sid);
				} else {
					int mode = message.getMode();
					int sendId = message.getResponseId();
					sess.send(mode, sendId, message.getByteBuffer());
				}
			} finally {
				message.destory();
			}
		} else if (!(session instanceof DefaultClientObjectConnection)) {

			// ==================代理服务器客户消息==================

			try {
				Integer sid = toSid(session);
				DefaultObjectConnection sess;
				sess = (DefaultObjectConnection) serverSession.getSession(sid);

				// 缓存SessionId映射（写安全）

				if (!sess.contains(ObjectSession.GATEWAY_SESSION)) {
					sess.setAttribute(ObjectSession.GATEWAY_SESSION, session);
				}
				// log.debug(session.getInetAddress()
				// + "    SendTo  "
				// + sess.getInetAddress()
				// + "@"
				// + sess.getSessionId()
				// + "  Type: "
				// + (message.isResponse() ? "response" : "request-"
				// + message.getMode()));
				// ==========请求消息到达========
				ByteBuffer data = message.getByteBuffer();
				int mode = message.getMode();
				int sendId = message.getResponseId();
				sess.send(mode, sendId, data);
			} catch (Throwable e) {
				serverHandler.getNotifier().fireOnError(session, e);
				message.setStatus(AppResponse.STATUS_INNER_ERROR);// 内部未知错误
				message.flush();
			} finally {
				message.destory();// 销毁请求消息
			}
		} else if (chain.hasNext()) {
			chain.nextFilter().messageReceived(session0, message0,
					chain.getNext());
		}
	}

	public void sessionClosed(Connection conn,
			FilterChain<IClosedFilter> filterChain) {

		if (filterChain.hasNext())
			filterChain.nextFilter().sessionClosed(conn, filterChain.getNext());

		if (conn.getSession() instanceof AppSession) {
			int sid = toSid((AppSession) conn.getSession());
			log.debug("Del-Session: " + conn.getSession() + "  =>  "
					+ serverSession.getSession(sid));
			serverSession.closeSession(sid);
		}
	}

	public Connection sessionOpened(ServerContext serverHandler,
			Connection conn, FilterChain<IAcceptorFilter> filterChain)
			throws Exception {
		if (conn == null)
			return null;

		if (conn.getSession() instanceof AppSession) {
			int sid = toSid((AppSession) conn.getSession());
			serverSession.buildSession(sid);

			log.debug("Add-Session: " + conn.getSession() + "  =>  "
					+ serverSession.getSession(sid));
		}
		if (filterChain.hasNext())
			conn = filterChain.nextFilter().sessionOpened(serverHandler, conn,
					filterChain.getNext());
		return conn;
	}

	private Integer toSid(AppSession session) {
		String sessionId = session.getSessionId();
		Integer sid = cachedSessionId2SidMap.get(sessionId);
		if (sid != null)
			return sid;
		try {
			lock.lock();
			sid = cachedSessionId2SidMap.get(sessionId);
			if (sid != null) {
				return sid;
			} else {
				// 生成不重复sid
				count++;
				if (count == 0)
					count = 1;
				sid = count;

				// sessionId to sid
				cachedSessionId2SidMap.put(sessionId, sid);
				// sid to sessionId
				String t = cachedSid2SessionIdMap.put(sid, sessionId);

				if (t != null) {
					cachedSessionId2SidMap.remove(t);// 移除被顶替的映射
				}
				ObjectSession sess = serverSession.getOrCreateSession(sid);
				log.debug(new StringBuilder("New-Session: ").append(session)
						.append("  <=>  ").append(sess).toString());

				// 克隆父级Session的认证信息
				if (session.isDefault() == false) {
					AppSession parent = session.getParent();
					Integer psid = cachedSessionId2SidMap.get(parent
							.getSessionId());// 获取默认SessionId
					ObjectSession rsDef = serverSession.getRemote();
					ObjectSession rsNew = rsDef.getOrCreateSession(sid);
					Object v = rsDef.getOrCreateSession(psid).getAttribute(
							ObjectSession.LOGIN_USER);
					rsNew.setAttribute(ObjectSession.LOGIN_USER, v);
				}
			}
		} finally {
			lock.unlock();
		}
		return sid;
	}

	private String toSessionId(Integer sid) {
		String sessionId = cachedSid2SessionIdMap.get(sid);
		if (sessionId != null)
			return sessionId;
		lock.lock();
		sessionId = cachedSid2SessionIdMap.get(sid);
		lock.unlock();
		return sessionId;
	}

	public Connection sessionAccept(ServerContext serverHandler,
			SelectableChannel socket, FilterChain<IAcceptorFilter> filterChain)
			throws Exception {
		if (filterChain.hasNext())
			return filterChain.nextFilter().sessionAccept(serverHandler,
					socket, filterChain.getNext());
		return null;
	}

	protected String connString = System.getProperty("gateway.target-url");
	protected String password = System.getProperty("gateway.target-username");
	protected String username = System.getProperty("gateway.target-password");

	private AppMessageFactory mf;

	public void init(ObjectServerHandler serverHandler) {
		if (Boolean.parseBoolean(System.getProperty("rpc.support.gateway",
				"true")) == false)
			throw new UnsupportedOperationException(
					"rpc.support.gateway=false");

		this.serverHandler = serverHandler;

		if (serverHandler.contains(APP_ATTR_NAME))
			throw new IllegalStateException("Can't Repeat init GatewayHandler!");
		serverHandler.setAttribute(APP_ATTR_NAME, this);

		if (serverHandler.getConnector().isServer() == false) {
			// 网关客户端插件处理
			log.info("GatewayHandlerPlugin-Init: Client");
			mf = serverHandler.getMessageFactory();
			serverHandler
					.setMessageFactory(new MultiSessionObjectMessageFactory());

		} else if (connString == null) {
			// 网关服务器插件处理
			log.info("GatewayHandlerPlugin-Init: Server");
			mf = serverHandler.getMessageFactory();
			serverHandler
					.setMessageFactory(new MultiSessionObjectMessageFactory());

		} else {
			// 网关出口插件处理
			log.info("GatewayHandlerPlugin-Init: Gateway");

			cachedSessionId2SidMap = new HashMap<String, Integer>();
			cachedSid2SessionIdMap = new HashMap<Integer, String>();

			serverHandler.getFilterChain().addLastFilter(
					IFilterChain.FILTER_ACCEPTOR, this);
			serverHandler.getFilterChain().addFirstFilter(
					IFilterChain.FILTER_CLOSED, this);
			serverHandler.getFilterChain().addFirstFilter(
					IFilterChain.FILTER_MESSAGE, this);

			// 启动连接线程
			Thread t = new Thread() {
				@Override
				public void run() {
					while (getServerHandler().getConnector().isServer())
						try {
							serverSession = DriverManager.getConnection(
									connString, username, password)
									.getSession();
							break;
						} catch (AccessException e) {
							log.error("Connect " + connString + " failed: "
									+ e.getMessage() + ",  retry...");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
				}
			};
			t.setDaemon(true);
			t.start();

			for (; serverSession == null;)
				try {
					// log.debug("wait connect to server: " + connString);
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			serverSession.getServerHandler().getFilterChain()
					.addFirstFilter(IFilterChain.FILTER_MESSAGE, this);
		}
	}

	public ObjectServerHandler getServerHandler() {
		return serverHandler;
	}

	public void destroy() {
		// 不重复销毁
		if (serverHandler == null || !serverHandler.contains(APP_ATTR_NAME))
			return;
		serverHandler.removeAttribute(APP_ATTR_NAME);

		if (serverHandler.getConnector().isServer() == false) {
			// 网关客户端插件处理
			log.info("GatewayHandlerPlugin-Destroy: Client");
			serverHandler.setMessageFactory(mf);

		} else if (connString == null) {
			// 网关服务器插件处理
			log.info("GatewayHandlerPlugin-Destroy: Server");
			serverHandler.setMessageFactory(mf);

		} else {
			// 网关出口插件处理
			log.info("GatewayHandlerPlugin-Destroy: Gateway");

			if (serverSession != null) {
				serverSession.getServerHandler().getFilterChain()
						.removeFilter(IFilterChain.FILTER_MESSAGE, this);
				serverSession.close();
			}

			serverHandler.getFilterChain().removeFilter(
					IFilterChain.FILTER_MESSAGE, this);
			serverHandler.getFilterChain().removeFilter(
					IFilterChain.FILTER_CLOSED, this);
			serverHandler.getFilterChain().removeFilter(
					IFilterChain.FILTER_ACCEPTOR, this);

		}

		serverHandler = null;
		serverSession = null;
		cachedSessionId2SidMap = null;
		cachedSid2SessionIdMap = null;
		mf = null;
		count = 0;

	}

	private static final Logger log = Logger
			.getLogger(GatewayServerHandlerPlugin.class);

}
