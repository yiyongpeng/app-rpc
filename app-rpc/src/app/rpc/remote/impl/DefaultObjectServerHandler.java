package app.rpc.remote.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import app.core.Connection;
import app.core.Connector;
import app.core.Remote;
import app.core.Session;
import app.net.AppFilter;
import app.net.AppMessageFactory;
import app.net.DefaultAppHandler;
import app.rpc.remote.DefaultObjectConnector;
import app.rpc.remote.ObjectServerHandler;
import app.rpc.remote.ObjectSession;
import app.rpc.remote.ObjectSessionFactory;
import app.rpc.remote.PluginHandler;
import app.rpc.remote.ServiceManager;
import app.rpc.remote.ServiceObject;
import app.rpc.utils.ConfigUtils;

public class DefaultObjectServerHandler extends DefaultAppHandler implements Remote, ObjectServerHandler {

	@Override
	public void destory() {
		destroyPlugins();
		super.destory();
		this.removeFilter((AppFilter) serviceManager);
	}

	@Override
	public void init(Connector<Connection, Session> server) {
		this.addLastFilter((AppFilter) serviceManager);// 必须放在第一行，以便得到过滤器的初始化调用
		super.init(server);
		initPlugins();
	}

	private void initPlugins() {
		String className=null;
		Properties props = ConfigUtils.loadRemoteHandlersProps();
		for (int i = 0; i < 256; i++)
			try {
				className = props.getProperty(getPropertyPrefix() + ".hanlder.plugin-" + i);
				if (className == null)
					continue;
				
				Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
				PluginHandler handler = (PluginHandler) clazz.newInstance();
				
				addPlugin(handler);
			} catch (Exception e) {
				log.error("Init-Plugin failed: " + className, e);
			}
	}

	public void addPlugin(PluginHandler handler) {
		log.info("Load-Plugin: " + handler);
		handler.init(this);
		plugins.add(handler);
	}

	public void removePlugin(PluginHandler handler){
		plugins.remove(handler);
		handler.destroy();
	}
	
	public String getPropertyPrefix() {
		return "protocol." + getProtocol();
	}

	private void destroyPlugins() {
		for (; plugins.isEmpty()==false;)
			try {
				removePlugin(plugins.get(0));
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	@Override
	public ObjectSessionFactory instanceSessionFactory() {
		Properties props = ConfigUtils.loadRemoteHandlersProps();
		String value = props.getProperty(getPropertyPrefix() + ".session-factory");
		if (value != null) {
			try {
				log.debug("ObjectSessionFactory-class: " + value);
				return (ObjectSessionFactory) Thread.currentThread().getContextClassLoader().loadClass(value).newInstance();
			} catch (Exception e) {
				log.error("ObjectSessionFactory instantiation failed: " + value + "  message: " + e);
			}
		}
		return new DefaultObjectSessionFactory();
	}

	@Override
	public AppMessageFactory instanceMessageFactory() {
		Properties props = ConfigUtils.loadRemoteHandlersProps();
		String value = props.getProperty(getPropertyPrefix() + ".message-factory");
		if (value != null) {
			try {
				log.debug("AppMessageFactory-class: " + value);
				return (AppMessageFactory) Thread.currentThread().getContextClassLoader().loadClass(value).newInstance();
			} catch (Exception e) {
				log.error("AppMessageFactory instantiation failed: " + value + "  message: " + e);
			}
		}
		return new DefaultObjectMessageFactory();
	}

	@Override
	public ObjectSession removeSession(String sid) {
		return (ObjectSession) super.removeSession(sid);
	}

	@Override
	protected void onSessionOpened(Session session) {
		super.onSessionOpened(session);
		// 开启远程Session调用
		ServiceObject so = new DefaultServiceObject(Remote.SESSION, session, ObjectSession.class);
		session.setAttribute(so.getInvokeHandle(), so);
		// log.debug("Opened session: " + session.getInetAddress() + " - "
		// + session.getSessionId());
	}

	@Override
	public ObjectSession createSession(Connection conn, Object sessionId) {
		return (ObjectSession) super.createSession(conn, sessionId);
	}

	@Override
	public ObjectSession getSession(String sessionId) {
		return (ObjectSession) super.getSession(sessionId);
	}

	public ServiceManager getServiceManager() {
		return serviceManager;
	}

	public String getProtocol() {
		return protocol;
	}

	@Override
	public DefaultObjectConnector getConnector() {
		return (DefaultObjectConnector) super.getConnector();
	}

	public DefaultObjectServerHandler() {
		this.serviceManager = new DefaultObjectServiceManager();
		this.plugins = new ArrayList<PluginHandler>();
		this.setMessageFlow(false);
	}

	protected List<PluginHandler> plugins;
	protected ServiceManager serviceManager;
	protected String protocol = RemoteUrl.DEFAULT_PROTOCOL;

	protected final Logger log = Logger.getLogger(this.getClass());

}
