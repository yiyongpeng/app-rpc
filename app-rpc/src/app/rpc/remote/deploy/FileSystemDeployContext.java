package app.rpc.remote.deploy;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javassist.ClassPool;
import javassist.LoaderClassPath;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import app.rpc.client.DeployException;
import app.core.AccessException;
import app.core.impl.DefaultContext;
import app.rpc.remote.ServiceObject;
import app.rpc.remote.impl.DefaultRemoteMethodCollection;
import app.rpc.remote.impl.DefaultServiceObject;
import app.util.ThreadContext;
import app.rpc.utils.ConfigUtils;

public class FileSystemDeployContext extends DefaultContext implements DeployContext {
	private static final Logger log = Logger.getLogger(FileSystemDeployContext.class);

	private static final long DEPLOY_SPARE_TIME = 5000;

	protected DeployHandler handler;
	private long lastDeployTime;
	private File deployFile;
	private File pathFile;

	private Properties props;
	private ClassLoader classLoader;
	private ClassPool pool;

	private IConfigHandler redeployUndeployHandler = new IConfigHandler() {

		public void handle(String name, String className, String handle, String interfaces, Properties props0) throws Exception {
			// log.debug("redeploy scan:" + name);

			if (props.containsKey(name + ".interfaces") == false) {
				// 已删除的，取消部署
				log.info("redeploy removed: " + name + "  handle:" + handle + "  interfaces:" + interfaces);
				if (status == STATUS_DEPLOY) {
					ServiceObject so = (ServiceObject) removeAttribute(DefaultRemoteMethodCollection.mappingInvokeHandle(handle));
					if (so != null) {
						handleUndeployInstance(so);
						so.destroy();
					}
				}
			} else {
				// 更换handle，取消部署
				String handle0 = props.getProperty(name + ".handle", name).trim();
				if (handle0.equals(handle) == false) {
					log.info("redeploy change-handle: " + name + "  handle:" + handle + " -> " + handle0);
					ServiceObject so = (ServiceObject) removeAttribute(DefaultRemoteMethodCollection.mappingInvokeHandle(handle));
					if (so != null) {
						if (status == STATUS_DEPLOY) {
							handleUndeployInstance(so);
						}
						so.destroy();
					}
				}
			}
		}
	};

	private byte status;

	private String host;

	public FileSystemDeployContext(File path) {
		this.host = path.getParentFile().getName();
		this.pathFile = path;
		this.deployFile = new File(path, FileSystemDeployContextHandler.DEPLOY_CONFIG_FILE_PATH);
		loadConfig();
	}

	public String getHost() {
		return host;
	}

	private void loadConfig() {
		lastDeployTime = deployFile.lastModified();
		props = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(deployFile);
			props.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}
	}

	public void onLoad(DeployHandler handler) {
		this.handler = handler;
		if (Boolean.parseBoolean(props.getProperty("auto.load", "true"))) {
			doLoad(handler);
		}
	}

	@SuppressWarnings("unchecked")
	public void doLoad(final DeployHandler handler) {
		// if (status >= STATUS_LOAD)
		// throw new DeployException("Have been loaded: " + path);
		loadConfig();

		log.info("Deploy onload:  " + pathFile);

		// try {
		// if (classLoader != null)
		// classLoader.close();
		// } catch (Throwable e) {
		// }

		try {
			URL[] urls = getClassPathUrls();
			if (classLoader != null) {
				destroyClassLoader();
			}
			classLoader = newClassLoader(urls);
			pool = new ClassPool(true);
			pool.insertClassPath(new LoaderClassPath(classLoader));
			log.debug(classLoader + "  classpath: " + Arrays.toString(urls));
		} catch (Throwable e) {
			throw new DeployException("Create the deployment of the class loader failure.  deploy:" + pathFile, e);
		}

		// 实例化
		Thread thread = Thread.currentThread();
		ClassLoader loader = thread.getContextClassLoader();

		boolean inited = ThreadContext.contains();
		Object app = null;
		IBeanCreator creatorOld = null;
		try {
			thread.setContextClassLoader(classLoader);
			if (!inited) {
				ThreadContext.init();
			}
			app = ThreadContext.setAttribute(ThreadContext.SCOPE_APP, handler.getServerHandler());
			ThreadContext.setAttribute(ATTR_DEPLOY_CONTEXT, this);
			ThreadContext.setAttribute("ClassPool", pool);
			// load set
			if (props.containsKey("creator-class"))
				try {
					Class<IBeanCreator> creatorClass = (Class<IBeanCreator>) classLoader.loadClass(props.getProperty("creator-class"));
					IBeanCreator creator = creatorClass.getConstructor(DeployContext.class).newInstance(this);
					creatorOld = (IBeanCreator) setAttribute(ATTR_BEAN_CREATOR, creator);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			final List<ServiceObject> replaces = new ArrayList<ServiceObject>();
			final List<ServiceObject> newsolist = new ArrayList<ServiceObject>();
			// load instance
			handleDeployConfigAll(new IConfigHandler() {
				public void handle(String name, String className, String handle, String interfaces, Properties props) throws Exception {
					Object instance = createObject(className);
					Class<?>[] proxyInterfaces = ConfigUtils.parseInterfaces(interfaces, classLoader);

					DefaultServiceObject so = new DefaultServiceObject(handle, instance, proxyInterfaces);

					log.info("Loaded instance: " + name + "  handle:" + handle + "  interfaces:" + interfaces + "  (" + className + ")");
					newsolist.add(so);

					Object redeploy = removeAttribute(so.getInvokeHandle());
					if (redeploy != null && redeploy instanceof ServiceObject) {
						ServiceObject so0 = (ServiceObject) redeploy;
						replaces.add(so0);
					}
				}
			});
			// reverse destroy
			Collections.reverse(replaces);
			for (ServiceObject so : replaces)
				try {
					log.debug("Undeploy for reload: " + so.getHandle() + "  ->  " + so.getInstance());
					if (status == STATUS_DEPLOY) {
						handleUndeployInstance(so);
					}
					so.destroy();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			// deploy up
			for (ServiceObject so : newsolist) {
				if (status == STATUS_DEPLOY) {
					handleDeployInstance(so);
				}
				setAttribute(so.getInvokeHandle(), so);
			}
			if (status < STATUS_LOAD)
				status = STATUS_LOAD;
		} finally {
			thread.setContextClassLoader(loader);

			if (creatorOld != null)
				try {
					creatorOld.destroy();
				} catch (Exception e) {
					e.printStackTrace();
				}

			ThreadContext.removeAttribute(ATTR_DEPLOY_CONTEXT);
			ThreadContext.removeAttribute("ClassPool");
			if (!inited) {
				ThreadContext.destory();
			} else {
				ThreadContext.setAttribute(ThreadContext.SCOPE_APP, app);
			}
		}
	}

	protected Object createObject(String className) {
		try {
			if (!contains(ATTR_BEAN_CREATOR)) {
				IBeanCreator creator = new DefaultBeanCreator(this);
				setAttribute(ATTR_BEAN_CREATOR, creator);
			}
			IBeanCreator creator = (IBeanCreator) getAttribute(ATTR_BEAN_CREATOR);
			return creator.create(className);
		} catch (Exception e) {
			throw new AccessException("Unable instantion: " + className, e);
		}

	}

	private URL[] getClassPathUrls() throws IOException {
		File libDirFile = new File(pathFile, "lib/");
//		if (libDirFile.exists() == false)
//			libDirFile.mkdirs();

		File cacheFile = new File(pathFile, "_cache/");
		if (cacheFile.exists() == false)
			cacheFile.mkdirs();

		File libDirCacheFile = new File(cacheFile, "lib/");
		if (libDirCacheFile.exists())
			FileUtils.deleteDirectory(libDirCacheFile);

		if (libDirFile.exists())
			FileUtils.copyDirectory(libDirFile, libDirCacheFile);

		List<URL> urls = new ArrayList<URL>();

		File[] list = libDirCacheFile.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".jar");
			}
		});
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				try {
					urls.add(list[i].toURI().toURL());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}

		File classesDirFile = new File(pathFile, "classes/");
		if (classesDirFile.exists()) {
			File classesDirFileCache = new File(cacheFile, "classes/");
			if (classesDirFileCache.exists())
				FileUtils.deleteDirectory(classesDirFileCache);
			FileUtils.copyDirectory(classesDirFile, classesDirFileCache);

			urls.add(classesDirFileCache.toURI().toURL());
		}
		handleURLs(urls);
		return urls.toArray(new URL[urls.size()]);
	}

	public static interface IConfigHandler {

		void handle(String name, String className, String handle, String interfaces, Properties props) throws Exception;

	}

	private void handleDeployConfigAll(IConfigHandler handler) {
		handleDeployConfigAll(props, false, handler);
	}

	private void handleDeployConfigAll(boolean reverse, IConfigHandler handler) {
		handleDeployConfigAll(props, reverse, handler);
	}

	private static void handleDeployConfigAll(Properties props, boolean reverse, IConfigHandler handler) {
		String export = props.getProperty("export", "");
		List<String> arr = new ArrayList<String>();
		for (String name : export.split(","))
			arr.add(name);
		if (reverse)
			Collections.reverse(arr);

		for (String name : arr)
			try {
				name = name.trim();
				String className = props.getProperty(name, name);
				String handle = props.getProperty(name + ".handle", name);
				String interfaces = props.getProperty(name + ".interfaces");
				if (interfaces != null) {
					className = className.trim();
					handle = handle.trim();
					interfaces = interfaces.trim();
					handler.handle(name, className, handle, interfaces, props);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public void onDeploy(DeployHandler handler) {
		if (Boolean.parseBoolean(props.getProperty("auto.deploy", "true"))) {
			doDeploy(handler);
		}
	}

	public void doDeploy(final DeployHandler handler) {
		if (status != STATUS_LOAD)
			throw new DeployException("Not loading or have been deployed.");
		log.info("Deploy: " + pathFile + "    start...");
		Thread t = Thread.currentThread();
		ClassLoader cl = t.getContextClassLoader();
		boolean inited = ThreadContext.contains();
		if (inited == false) {
			ThreadContext.init();
		}
		try {
			ThreadContext.setAttribute(ATTR_DEPLOY_CONTEXT, this);
			ThreadContext.setAttribute("ClassPool", pool);
			t.setContextClassLoader(getClassLoader());

			handleDeployConfigAll(new IConfigHandler() {
				@Override
				public void handle(String name, String className, String handle, String interfaces, Properties props) throws Exception {
					int ih = DefaultRemoteMethodCollection.mappingInvokeHandle(handle);
					Object obj = getAttribute(ih);
					if (obj != null && obj instanceof ServiceObject) {
						ServiceObject so = (ServiceObject) obj;
						handleDeployInstance(so);
					}
				}
			});

		} finally {
			t.setContextClassLoader(cl);
			ThreadContext.removeAttribute(ATTR_DEPLOY_CONTEXT);
			ThreadContext.removeAttribute("ClassPool");
			if (inited == false) {
				ThreadContext.destory();
			}
		}
		status = STATUS_DEPLOY;
		log.info("Deploy: " + pathFile + "    finished.");
	}

	private void handleDeployInstance(ServiceObject so) {
		Object instance = so.getInstance();

		log.debug("handle deploy Instance: " + instance);

		if (instance instanceof Deployable)
			try {
				((Deployable) instance).onDeploy(this, so);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public void onMonitoring(DeployHandler handler, String host) {
		if (deployFile.exists() == false) {
			if (Boolean.parseBoolean(props.getProperty("auto.undeploy", "true"))) {
				if (status == STATUS_DEPLOY) {
					onUndeploy(handler);// 取消部署
				} else if (status == STATUS_LOAD) {
					onDestroy(handler, host);
				}
			}
		} else if (status == STATUS_DEPLOY) {
			long lastTime = deployFile.lastModified();
			if (lastTime > lastDeployTime + DEPLOY_SPARE_TIME) {
				onRedeploy(handler, host);
			}
		}
	}

	public void onDestroy(DeployHandler contextHandler, String host) {
		if (status == STATUS_LOAD) {
			doDestroy(contextHandler, host);
		}
	}

	public void doDestroy(DeployHandler handler, String host) {
		if (status >= STATUS_DEPLOY) {
			throw new DeployException("Have been deployed online.");
		}
		status = STATUS_DESTORY;
		handler.removeDeployContext(host, getContextKey());// 注销DeployContext

		boolean inited = ThreadContext.contains();
		Object app = null;
		try {
			if (!inited) {
				ThreadContext.init();
			}
			app = ThreadContext.setAttribute(ThreadContext.SCOPE_APP, handler.getServerHandler());

			doDestroy();

		} finally {
			if (!inited) {
				ThreadContext.destory();
			} else {
				ThreadContext.setAttribute(ThreadContext.SCOPE_APP, app);
			}
		}
	}

	public String getContextKey() {
		return AbstractDeployContextHandler.getContextKey(pathFile.getName());
	}

	protected void onRedeploy(DeployHandler handler, String host) {
		if (status >= STATUS_LOAD) {
			if (!Boolean.parseBoolean(props.getProperty("auto.reload", "true"))) {
				return;
			}
			log.info("Redeploy: " + pathFile + "  prev-deploy-date:" + new Date(lastDeployTime) + "    start...");

			if(!Boolean.parseBoolean(props.getProperty("auto.reload.cover", "false"))){
				onUndeploy(handler);
				onDestroy(handler, host);
				
				handler.putDeployContext(host, getContextKey(), this);
				
				onLoad(handler);
				onDeploy(handler);
				log.info("Redeploy: " + pathFile + "  modified-date:" + new Date(deployFile.lastModified()) + "    finished!");
				return;
			}
			
			Properties oldprops = this.props;
			ClassLoader oldloader = classLoader;
			classLoader = null;
			
			onLoad(handler);

			boolean inited = ThreadContext.contains();
			Object app = null;
			try {
				if (!inited) {
					ThreadContext.init();
				}
				app = ThreadContext.setAttribute(ThreadContext.SCOPE_APP, handler.getServerHandler());

				// 取消部署已不存在的对象
				handleDeployConfigAll(oldprops, true, redeployUndeployHandler);
			} finally {

				destroyClassLoader(oldloader);

				if (!inited) {
					ThreadContext.destory();
				} else {
					ThreadContext.setAttribute(ThreadContext.SCOPE_APP, app);
				}
			}
			log.info("Redeploy: " + pathFile + "  deploy-date:" + new Date(lastDeployTime) + "   finished.");
		}

	}

	public void onUndeploy(DeployHandler contextHandler) {
		if (status < STATUS_DEPLOY)
			return;

		doUndeploy(contextHandler);

	}

	public void doUndeploy(DeployHandler contextHandler) {
		if (status < STATUS_DEPLOY)
			throw new DeployException("No deployment online.");

		log.info("Deploy  undeploy: " + pathFile + "    start...");

		status = STATUS_LOAD;

		handleDeployConfigAll(true, new IConfigHandler() {
			@Override
			public void handle(String name, String className, String handle, String interfaces, Properties props) throws Exception {
				doUndeploy(handle);
			}
		});

		log.info("Deploy  undeploy: " + pathFile + "    finished.");
	}

	protected void doUndeploy(String handle) {
		int ih = DefaultRemoteMethodCollection.mappingInvokeHandle(handle);
		Object obj = getAttribute(ih);
		if (obj != null && obj instanceof ServiceObject) {
			ServiceObject so = (ServiceObject) obj;

			log.info("undeploy instance,   handle: " + handle + "  service-object:" + so);

			handleUndeployInstance(so);
		}
	}

	private void handleUndeployInstance(ServiceObject so) {
		Object instance = so.getInstance();
		log.debug("handle undeploy Instance: " + instance);
		if (instance instanceof Deployable) {
			((Deployable) instance).onUndeploy();
		}
	}

	public void doDestroy() {
		log.info("Deploy destroy:  " + pathFile);

		handleDeployConfigAll(true, new IConfigHandler() {
			@Override
			public void handle(String name, String className, String handle, String interfaces, Properties props) throws Exception {
				int ih = DefaultRemoteMethodCollection.mappingInvokeHandle(handle);
				ServiceObject so = (ServiceObject) removeAttribute(ih);
				if (so == null) {
					log.error("Not found SO : " + handle + "(" + ih + ")");
					return;
				}
				so.destroy();
			}
		});

		destroyBeanCreator();

		clear();

		destroyClassLoader();
		this.pool = null;
	}

	protected void destroyBeanCreator() {
		IBeanCreator creatorOld = (IBeanCreator) removeAttribute(ATTR_BEAN_CREATOR);
		if (creatorOld != null)
			try {
				creatorOld.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	protected void handleURLs(List<URL> urls) {
		String classpath = props.getProperty("classpath");
		if (classpath != null) {
			String[] urlsStr = classpath.split(";");
			for (String urlStr : urlsStr) {
				if (urlStr.indexOf("://") != -1) {
					try {
						urls.add(new URL(urlStr));
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				} else {
					try {
						urls.add(new File(urlStr).toURI().toURL());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	protected ClassLoader newClassLoader(URL[] urls) {
		Class<?> clazz = null;
		String classStr = props.getProperty("class-loader");
		ClassLoader loader = null;
		if (classStr != null) {
			try {
				log.info("Custm class-loader: " + classStr + "  deploy: " + getName());

				clazz = Thread.currentThread().getContextClassLoader().loadClass(classStr);
				Constructor<?> cons = clazz.getConstructor(URL[].class);
				loader = (ClassLoader) cons.newInstance(new Object[] { urls });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (loader == null) {
			loader = new DefaultDeployClassLoader(urls);
		}
		if (loader instanceof DeployClassLoader) {
			DeployClassLoader dcl = (DeployClassLoader) loader;
			dcl.init(this);
		}
		return loader;
	}

	public String getName() {
		return pathFile.getName();
	}

	protected void destroyClassLoader() {
		destroyClassLoader(classLoader);
		this.classLoader = null;
	}

	protected void destroyClassLoader(ClassLoader classLoader) {
		if (classLoader instanceof DeployClassLoader) {
			((DeployClassLoader) classLoader).destroy();
		}
	}

	@Override
	public ClassPool getClassPool() {
		return this.pool;
	}

	public DeployHandler getHandler() {
		return handler;
	}

	public String getPath() {
		return pathFile.getPath();
	}

	public Properties getProperties() {
		return props;
	}

	public boolean isDeployed() {
		return status == STATUS_DEPLOY;
	}

	public byte getStatus() {
		return status;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public int compareTo(DeployContext o) {
		return getStartupPriority() - o.getStartupPriority();
	}

	public int getStartupPriority() {
		return Integer.parseInt(props.getProperty("startup.priority", "0"));
	}
}
