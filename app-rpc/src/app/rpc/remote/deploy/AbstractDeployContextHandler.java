package app.rpc.remote.deploy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import app.rpc.remote.ObjectServerHandler;
import app.rpc.remote.ObjectSession;
import app.rpc.remote.ServiceObject;
import app.rpc.remote.service.LoginParameter;

public abstract class AbstractDeployContextHandler implements DeployHandler,
		Runnable {

	private static final long DEPLOY_UPDATE_TIME = 3000;

	protected final Logger log = Logger.getLogger(getClass());

	private ScheduledFuture<?> monitorFuture;

	public ServiceObject getServiceObject(ObjectSession session, Object handle) {
		LoginParameter login = (LoginParameter) session
				.getAttribute(ObjectSession.LOGIN_USER);
		if (login == null) {
			return null;
		}
		String host = login.getDeployHost();
		String path = login.getDeployPath();
		DeployContext deploy = getDeployContext(host, path);
		if (deploy == null || deploy.isDeployed() == false)
			return null;
		Object value = deploy.getAttribute(handle);
		if (value != null && value instanceof ServiceObject) {
			return (ServiceObject) value;
		}
		return null;
	}

	public void destroy() {
		serverHandler.removeAttribute(DEPLOY_CONTEXT_HANDLER);

		shutdownMonitor();

		undeployContextAll();

		destroyContextAll();

	}

	public void init(ObjectServerHandler serverHandler) {
		this.serverHandler = serverHandler;

		contextMap = new ConcurrentHashMap<String, Map<Object, DeployContext>>();

//		// 加载所有
//		loadContextAll();
//
//		// 加载后自动全部部署
//		if (contextMap.isEmpty() == false) {
//			deployContextAll();
//		}

		// 启动监控
		startupMonitor();

		serverHandler.setAttribute(DEPLOY_CONTEXT_HANDLER, this);

	}

	protected void startupMonitor() {
		log.info("Deploy monitor startup: " + this);

		monitorFuture = serverHandler.schedule(this, DEPLOY_UPDATE_TIME,
				DEPLOY_UPDATE_TIME);
	}

	protected void shutdownMonitor() {
		if (monitorFuture != null) {
			boolean suc = monitorFuture.cancel(false);
			monitorFuture = null;
			log.info("Deploy monitor shutdown:" + suc);
		}
	}

	public void run() {
		if (contextMap.isEmpty() == false)
			for (Entry<String, Map<Object, DeployContext>> entry : contextMap
					.entrySet()) {
				String host = entry.getKey();
				for (DeployContext context : entry.getValue().values())
					try {
						context.onMonitoring(this, host);
					} catch (Throwable e) {
						e.printStackTrace();
					}
			}
	}

	protected abstract void loadContextAll();

	public DeployContext putDeployContext(String host, Object key,
			DeployContext context) {
		return getHostContextMap(host).put(key, context);
	}

	public DeployContext getDeployContext(String host, Object key) {
		return getHostContextMap(host).get(key);
	}

	private Map<Object, DeployContext> getHostContextMap(String host) {
		Map<Object, DeployContext> map = contextMap.get(host);
		if (map == null)
			synchronized (contextMap) {
				map = contextMap.get(host);
				if (map == null) {
					map = new ConcurrentHashMap<Object, DeployContext>();
					contextMap.put(host, map);
				}
			}
		return map;
	}

	public DeployContext removeDeployContext(String host, Object key) {
		return getHostContextMap(host).remove(key);
	}

	public String getDeployContextKey(String name) {
		return getContextKey(name);
	}

	public static String getContextKey(String name) {
		return name.equalsIgnoreCase("root") ? "/" : ("/" + name);
	}

	protected void deployContextAll() {
		if (contextMap.isEmpty() == false)
			for (Map<Object, DeployContext> map : contextMap.values())
				for (DeployContext context : map.values())
					try {
						context.onDeploy(this);
					} catch (Exception e) {
						e.printStackTrace();
					}
	}

	protected void undeployContextAll() {
		if (contextMap.isEmpty() == false)
			for (Map<Object, DeployContext> map : contextMap.values()){
				List<DeployContext> list = new ArrayList<DeployContext>(map.values());
				Collections.sort(list);
				Collections.reverse(list);
				for (DeployContext context : list)
					try {
						context.onUndeploy(this);
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
	}

	protected void destroyContextAll() {
		if (contextMap.isEmpty() == false) {
			for (Entry<String, Map<Object, DeployContext>> entry : contextMap
					.entrySet()) {
				String host = entry.getKey();
				List<DeployContext> list = new ArrayList<DeployContext>(entry.getValue().values());
				Collections.sort(list);
				Collections.reverse(list);
				for (DeployContext context : list)
					try {
						context.onDestroy(this, host);
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
			contextMap.clear();
		}
	}

	public ObjectServerHandler getServerHandler() {
		return serverHandler;
	}

	private ObjectServerHandler serverHandler;

	/** "host-port" => context */
	private Map<String, Map<Object, DeployContext>> contextMap;

}
