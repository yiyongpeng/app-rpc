package app.rpc.remote.impl;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import app.core.AccessException;
import app.core.Connection;
import app.core.Remote;
import app.core.ServerContext;
import app.core.Session;
import app.core.WriteRequest;
import app.net.DefaultAppSession;
import app.rpc.remote.DriverManager;
import app.rpc.remote.ObjectConnection;
import app.rpc.remote.ObjectServerHandler;
import app.rpc.remote.ObjectSession;
import app.rpc.remote.RemoteObject;
import app.rpc.remote.RemoteVisitor;
import app.rpc.remote.RemoteVisitorFactory;
import app.rpc.remote.Scope;
import app.rpc.remote.service.LoginParameter;

/**
 * 对象连接类
 * 
 * @author yiyongpeng
 * 
 */
public class DefaultObjectConnection extends DefaultAppSession implements
		Remote, ObjectConnection, ObjectSession {
	// private static final Logger log = Logger
	// .getLogger(DefaultObjectConnection.class);
	protected Map<Serializable, DefaultRemoteObject> cachedHandleMap = new ConcurrentHashMap<Serializable, DefaultRemoteObject>();
	protected RemoteVisitorFactory visitorFactory;

	@Override
	public ObjectSession getOrCreateSession(Integer sid) {
		return (ObjectSession) super.getOrCreateSession(sid);
	}

	@Override
	public ObjectSession getSession(Integer sid) {
		return (ObjectSession) super.getSession(sid);
	}

	@Override
	public ObjectSession createSession(Integer sid) {
		return (ObjectSession) super.createSession(sid);
	}

	@Override
	public ObjectSession createSession(String sessionId) {
		if (!(getServerHandler().getMessageFactory() instanceof MultiSessionObjectMessageFactory)) {
			throw new UnsupportedOperationException(
					"Please use message-factory: "
							+ MultiSessionObjectMessageFactory.class
									.getCanonicalName());
		}
		ObjectSession session = (ObjectSession) super.createSession(sessionId);
		session.setAttribute(ObjectSession.LOGIN_USER,
				getAttribute(ObjectSession.LOGIN_USER));
		return session;
	}

	// public synchronized void setGateway(boolean gateway) {
	// if (this.isGateway() == gateway)
	// return;
	// ObjectSession session = ((ObjectSession) conn.getSession());
	// ObjectSession rs = session.getRemote(Remote.SESSION,
	// ObjectSession.class);
	// if (gateway) {
	// rs.setProperty(ObjectSession.GATEWAY, true);
	// session.setProperty(ObjectSession.GATEWAY, true);
	// } else {
	// rs.removeProperty(ObjectSession.GATEWAY);
	// session.removeProperty(ObjectSession.GATEWAY);
	// }
	// log.debug("Open Gateway Mode: " + session.getInetAddress());
	// try {
	// Thread.sleep(1000);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }

	public String getPath() {
		String url = getUrl();
		return url.substring(url.indexOf("/"));
	}

	private String getUrl() {
		return ((LoginParameter) getAttribute(LOGIN_USER)).getUrl();
	}

	@Override
	public ObjectServerHandler getServerHandler() {
		return (ObjectServerHandler) super.getServerHandler();
	}
	
	@Override
	public void destory() {
		clearRemoteCached();
		
		super.destory();
	}

	/**
	 * 只在主动关闭的时候清除缓存
	 */
	public void clearRemoteCached() {
		if (cachedHandleMap != null) {
			List<RemoteObject> list = new ArrayList<RemoteObject>(
					cachedHandleMap.values());
			cachedHandleMap.clear();

			for (int i = 0; i < list.size(); i++)
				list.get(i).close();/* 关闭远程对像 */
		}
	}

	@Override
	public void close() {
		if (!isDefault()) {
			// 关闭子Session
			onClosed();
		} else {
			super.close();
		}
	}

	public ObjectSession getRemote() {
		return getRemote(Remote.SESSION, ObjectSession.class);
	}

	/* members begin */

	/**
	 * 获取新的远程访问器
	 */
	public RemoteVisitor newVisitor() {
		return visitorFactory.create(this);
	}

	/* members end */

	/* session begin */

	public void registor(String handle, Serializable instance,
			Class<?>... interfaces) throws AccessException {
		registor(Scope.SESSION, handle, instance, interfaces);
	}

	public void registor(Scope scope, String handle, Serializable instance,
			Class<?>... interfaces) throws AccessException {
		if (isClosed())
			throw new AccessException("Session is closed.");
		RemoteVisitor rv = newVisitor();
		try {
			rv.registor(handle, instance, interfaces, scope);
		} catch (AccessException e) {
			throw e;
		} catch (Exception e) {
			throw new AccessException("registor failed.", e);
		}finally{
			rv.destory();
		}
	}

	public void free(Object proxy) {
		RemoteObject ro = (RemoteObject) Proxy.getInvocationHandler(proxy);
		free(ro.getHandle());
	}

	public void free(String handle) {
		if (cachedHandleMap == null)
			return;
		RemoteObject ro = getHandlesCacheMap().remove(handle);
		if (ro != null)
			ro.close();
	}

	protected ClassLoader getDefaultClassLoader() {
		return null;
	}

	public <R> R getRemote(String handle, Class<R> interfac) {
		return getRemote(handle, getDefaultClassLoader(), interfac);
	}

	@SuppressWarnings("unchecked")
	public <R> R getRemote(String handle, ClassLoader loader, Class<R> interfac) {
		return (R) getRemote(handle, loader, new Class[] { interfac });
	}

	public Object getRemote(String handle, Class<?>... interfaces) {
		return getRemote(handle, getDefaultClassLoader(), interfaces);
	}

	public Object getRemote(String handle, ClassLoader loader,
			Class<?>... interfaces) {
		Map<Serializable, DefaultRemoteObject> cacheMap = getHandlesCacheMap();
		DefaultRemoteObject roi = null;
		Object proxy = null;

		String lockName = "__GET_REMOTE_LOCK__"+handle;
		Object lock = null;
		synchronized (this) {
			lock = getAttribute(lockName);
			if(lock==null){
				lock = new Object();
				setAttribute(lockName, lock);
			}
		}
		synchronized (lock) {
			roi = cacheMap.get(handle);
			if (roi != null)
				return roi.getProxy();
	
			// if (isClosed())
			// throw new AccessException("Session is closed.");
			roi = new DefaultRemoteObject(this, handle, loader, interfaces);
			proxy = roi.getProxy();
			if((Boolean)getCoverAttributeOfUser(ObjectSession.SESSION_ATTR_CACHE_REMOTE, true)){
				cacheMap.put(handle, roi);
			}
		}
		// log.debug(this.getInetAddress() + " has cached remote: " +
		// handle);
		return proxy;
	}

	public DefaultRemoteObject getRemoteObject(String handle) {
		if (cachedHandleMap == null)
			return null;
		return getHandlesCacheMap().get(handle);
	}

	private Map<Serializable, DefaultRemoteObject> getHandlesCacheMap() {
		return cachedHandleMap;
	}

	/* session end */

	public ObjectSession getSession() {
		return this;
	}

	protected void init0(String protocol) {
		visitorFactory = DriverManager.getVisitorFactory(protocol);
	}

	@Override
	public void init(ServerContext server) {
		super.init(server);
		init0(getServerHandler().getProtocol());
	}

	public DefaultObjectConnection(Connection conn, String sessionId) {
		this(sessionId);
		init(conn);
	}

	public DefaultObjectConnection(String sessionId) {
		super(sessionId);
	}

	public String getProtocol() {
		return getServerHandler().getProtocol();
	}

	public ByteBuffer read() throws IOException {
		return conn.read();
	}

	public void clearRecvBuffer() {
		conn.clearRecvBuffer();
	}

	public WriteRequest getWriteRequest() {
		return conn.getWriteRequest();
	}

	public boolean isBusy() {
		return conn.isBusy();
	}

	public void setBusy(boolean busy) {
		conn.setBusy(busy);
	}

	public void setSession(Session session) {
		Thread.dumpStack();
	}

}
