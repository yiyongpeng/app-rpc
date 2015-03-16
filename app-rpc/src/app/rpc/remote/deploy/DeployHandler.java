package app.rpc.remote.deploy;

import app.rpc.remote.ObjectServerHandler;
import app.rpc.remote.ObjectSession;
import app.rpc.remote.PluginHandler;
import app.rpc.remote.ServiceObject;

public interface DeployHandler extends PluginHandler {

	String APP_ATTR_DEPLOY_PATH = "__APP_ATTR_DEPLOY_PATH__";
	String DEPLOY_CONTEXT_HANDLER = "__DEPLOY_CONTEXT_HANDLER__";

	ObjectServerHandler getServerHandler();

	ServiceObject getServiceObject(ObjectSession session, Object key);

	DeployContext getDeployContext(String host, Object key);

	DeployContext putDeployContext(String host, Object key,
			DeployContext context);

	DeployContext removeDeployContext(String host, Object key);

	String getDeployPath();

	Object getDeployContextKey(String name);
}
