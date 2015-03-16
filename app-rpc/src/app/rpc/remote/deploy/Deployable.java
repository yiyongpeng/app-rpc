package app.rpc.remote.deploy;

import app.rpc.remote.ServiceObject;

public interface Deployable {

	void onDeploy(DeployContext context, ServiceObject serviceObject);

	void onUndeploy();

}
