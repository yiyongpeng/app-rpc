package app.rpc.remote.deploy;

import java.util.Properties;

import javassist.ClassPool;

import app.core.Context;

public interface DeployContext extends Context , Comparable<DeployContext>{
	byte STATUS_DESTORY = 0, STATUS_LOAD = 1, STATUS_DEPLOY = 2;
	
	String ATTR_BEAN_CREATOR = "__BEAN_CREATOR__";
	String ATTR_DEPLOY_CONTEXT = "__ATTR_DEPLOY_CONTEXT__";
	
	public void onLoad(DeployHandler contextHandler);

	public void doLoad(DeployHandler contextHandler);

	public void onDeploy(DeployHandler contextHandler);

	public void doDeploy(DeployHandler contextHandler);

	public void onMonitoring(DeployHandler contextHandler, String host);

	public void onUndeploy(DeployHandler contextHandler);

	public void doUndeploy(DeployHandler contextHandler);

	public void onDestroy(DeployHandler contextHandler, String host);

	public void doDestroy(DeployHandler contextHandler, String host);

	String getName();

	public String getContextKey();

	Properties getProperties();

	String getPath();
	String getHost();
	
	public boolean isDeployed();

	public byte getStatus();

	public ClassLoader getClassLoader();

	public DeployHandler getHandler();

	public int getStartupPriority();
	
	public ClassPool getClassPool();
	
}
