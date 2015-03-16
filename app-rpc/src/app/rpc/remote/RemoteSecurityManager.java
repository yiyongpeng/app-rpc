package app.rpc.remote;

import java.rmi.RMISecurityManager;

import org.apache.log4j.Logger;

public class RemoteSecurityManager extends RMISecurityManager implements
		PluginHandler {
	private static final Logger log = Logger
			.getLogger(RemoteSecurityManager.class);

	private SecurityManager sm;

	private boolean enabled;

	public RemoteSecurityManager() {
	}

	public void init(ObjectServerHandler serverHandler) {
		String policy = System.getProperty("java.security.policy");

		enabled = policy != null;

		log.info("RemoteSecurityManager-Init  "
				+ (enabled ? "enabled    java.security.policy: " + policy
						: "disabled"));

		if (!enabled)
			return;

		sm = System.getSecurityManager();
		System.setSecurityManager(this);

	}

	public void destroy() {
		if (!enabled)
			return;

		log.info("RemoteSecurityManager-Destroy");

		System.setSecurityManager(sm);
	}

}
