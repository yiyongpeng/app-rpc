package app.rpc.remote.spring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;

import app.rpc.remote.DriverManager;
import app.rpc.remote.ObjectConnector;
import app.rpc.remote.ServiceObject;
import app.rpc.remote.impl.DefaultServiceObject;
import app.rpc.remote.impl.RemoteUrl;
import app.util.ThreadContext;
import app.rpc.utils.ConfigUtils;

public class RemoteServiceBean implements InitializingBean {
	private static final Map<String, ObjectConnector> servers = new ConcurrentHashMap<String, ObjectConnector>();
	private static final Map<String, RemoteServiceBean> serviceBeans = new ConcurrentHashMap<String, RemoteServiceBean>();

	public static ObjectConnector getObjectServer(String serverAddress) {
		return servers.get(serverAddress);
	}

	public static RemoteServiceBean getRemoteServiceBean(String url) {
		return serviceBeans.get(new RemoteUrl(url).toString());
	}

	public void afterPropertiesSet() throws Exception {
		if (bean == null)
			throw new BeanCreationException("No set bean!");

		String urlStr = getUrlString();
		if (!serviceBeans.containsKey(urlStr))
			synchronized (serviceBeans) {
				if (serviceBeans.containsKey(urlStr)) {
					throw new BeanCreationException("url already occupied: "
							+ urlStr);
				}
				String serverAddress = getServerAddress();
				ObjectConnector server = servers
						.get(serverAddress);

				ThreadContext.init();

				if (server == null) {
					synchronized (servers) {
						server = servers
								.get(serverAddress);
						if (server == null) {
							server = DriverManager.createConnector(url
									.getProtocol());
							server.setHost(url.getHostName());
							server.setPort(url.getPort());
							server.setServer(true);
							ThreadContext.setAttribute(ThreadContext.SCOPE_APP,
									server.getServerHandler());
							server.start();
							servers.put(serverAddress, server);
						}
					}
				} else {
					ThreadContext.setAttribute(ThreadContext.SCOPE_APP,
							server.getServerHandler());
				}
				
				String path = getPath(urlStr);
				serviceObject = new DefaultServiceObject(path, bean,
						ConfigUtils.parseInterfaces(interfaces));
				Object invokeHandle = serviceObject.getInvokeHandle();

				server.getServerHandler().setAttribute(invokeHandle,
						serviceObject);

				ThreadContext.destory();

				serviceBeans.put(urlStr, this);
			}
		else {
			throw new BeanCreationException("url already occupied: " + urlStr);
		}
	}

	private String getPath(String urlStr) {
		return urlStr.substring(urlStr.indexOf("/") + 1);
	}

	private String getUrlString() {
		String urlStr = this.url.toString();
		return urlStr
				+ (handle != null ? (!urlStr.endsWith("/")
						&& !handle.startsWith("/") ? "/" : "")
						+ handle : "");
	}

	public Object getBean() {
		return bean;
	}

	public void setBean(Object bean) {
		this.bean = bean;
	}

	public String getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(String interfaces) {
		this.interfaces = interfaces;
	}

	public String getUrl() {
		return url.toString();
	}

	public void setUrl(String url) {
		this.url = new RemoteUrl(url);
	}

	private String getServerAddress() {
		return url.getProtocol() + ":" + url.getServerAddress();
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public String getHandle() {
		return handle;
	}

	private Object bean;

	private String interfaces;

	private RemoteUrl url;

	private String handle;

	private ServiceObject serviceObject;

}
