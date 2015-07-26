package app.rpc.remote.deploy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.core.Connection;
import app.core.Session;
import app.core.SessionFactory;
import app.rpc.remote.ObjectServerHandler;

public class FileSystemDeployContextHandler extends
		AbstractDeployContextHandler implements SessionFactory {
	public static final String DEPLOY_CONFIG_FILE_PATH = "META-INF/deploy.properties";
	
	protected ObjectServerHandler serverHandler;
	protected SessionFactory sessionFactory;
	
	protected String deployPath = "../deploy";
	protected DeployContextFactory contextFactory;

	@Override
	public void run() {
		// 自动部署新加入项目
		File ddir = new File(deployPath);
		if (ddir.exists() == false)
			return;
		File[] hostsDeploy = ddir.listFiles();
		if (hostsDeploy != null && hostsDeploy.length > 0) {
			for (int i = 0; i < hostsDeploy.length; i++)
				try {
					// Host
					File hostDir = hostsDeploy[i];
					if (!hostDir.isDirectory())
						continue;
					String host = hostDir.getName();
					// Deploy list
					File[] list = hostDir.listFiles();
					if (list == null)
						continue;
					
					List<DeployContext> contextList = new ArrayList<DeployContext>();
					for (int j = 0; j < list.length; j++) {
						File file = list[j];
						Object key = getContextKey(file.getName());
						if (getDeployContext(host, key) == null) {
							DeployContext context = loadContext(file);
							if (context == null)
								continue;
							contextList.add(context);
						}
					}
					Collections.sort(contextList);
					for(DeployContext context : contextList){
						log.debug("startup : "+context.getPath());
						context.onLoad(this);// 自动加载
						context.onDeploy(this);// 自动部署
						Object key = context.getContextKey();
						putDeployContext(host, key, context);
					}
					
				} catch (Throwable e) {
					e.printStackTrace();
				}
		}
		super.run();
	}

	@Override
	public void init(ObjectServerHandler serverHandler) {
		this.serverHandler = serverHandler;
		this.sessionFactory = serverHandler.getSessionFactory();

		serverHandler.setSessionFactory(this);

		contextFactory = new FileSystemDeployContextFactory();
		deployPath = (String) serverHandler.getAttribute(
				DeployHandler.APP_ATTR_DEPLOY_PATH, System.getProperty("deployPath", deployPath));

		super.init(serverHandler);
	}

	@Override
	public void destroy() {
		if (serverHandler != null && sessionFactory != null) {
			serverHandler.setSessionFactory(sessionFactory);
		}
		serverHandler = null;
		sessionFactory = null;
		super.destroy();
	}

	@Override
	protected void loadContextAll() {
		File hostsDir = new File(deployPath);
		if (hostsDir.exists() == false || hostsDir.isDirectory() == false) {
			log.warn("The deployment of directory does not exist.  deploy-path: "
					+ hostsDir);
			return;
		}
		File[] hosts = hostsDir.listFiles();
		if (hosts == null)
			return;
		for (File hostFile : hosts) {
			File[] list = hostFile.listFiles();
			if (list == null)
				return;
			
			String host = hostFile.getName();
			for (File file : list)
				if (file.exists() && file.isDirectory())
					try {
						DeployContext context = loadContext(file);
						if (context == null)
							continue;
						context.onLoad(this);
						String key = context.getContextKey();
						putDeployContext(host, key, context);
					} catch (Exception e) {
						e.printStackTrace();
					}
		}
	}

	private DeployContext loadContext(File pathFile) {
		File metaDirFile = new File(pathFile, DEPLOY_CONFIG_FILE_PATH);
		if (metaDirFile.exists()) {
			return contextFactory.createDelopyContext(pathFile);
		}
		return null;
	}

	public String getDeployPath() {
		return deployPath;
	}

	public void setDeployPath(String deployPath) {
		this.deployPath = deployPath;
	}

	public Session create(Connection conn, Object sid) {
		return new DeployObjectSession(conn, String.valueOf(sid));
	}

}
