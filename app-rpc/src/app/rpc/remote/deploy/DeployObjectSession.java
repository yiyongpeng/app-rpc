package app.rpc.remote.deploy;

import app.core.Connection;
import app.rpc.remote.ServiceObject;
import app.rpc.remote.impl.DefaultObjectConnection;

public class DeployObjectSession extends DefaultObjectConnection {

	public DeployObjectSession(Connection conn, String sessionId) {
		super(conn, sessionId);
	}

	@Override
	public Object getCoverAttributeOfApp(Object key, Object def) {
		if (key instanceof Integer
				&& getServerHandler().contains(
						DeployHandler.DEPLOY_CONTEXT_HANDLER)) {
			DeployHandler handler = (DeployHandler) getServerHandler()
					.getAttribute(DeployHandler.DEPLOY_CONTEXT_HANDLER);
			ServiceObject obj = handler.getServiceObject(this, key);
			if (obj != null)
				return obj;
		}
		return super.getCoverAttributeOfApp(key, def);
	}

	@Override
	public Object getCoverAttributeOfUser(Object key, Object def) {
		Object value = super.getCoverAttributeOfUser(key, def);
		if (value == def) {
			if (key instanceof Integer
					&& getServerHandler().contains(
							DeployHandler.DEPLOY_CONTEXT_HANDLER)) {
				DeployHandler handler = (DeployHandler) getServerHandler()
						.getAttribute(DeployHandler.DEPLOY_CONTEXT_HANDLER);
				ServiceObject obj = handler.getServiceObject(this, key);
				if (obj != null)
					value = obj;
			}
		}
		return value;
	}
}
