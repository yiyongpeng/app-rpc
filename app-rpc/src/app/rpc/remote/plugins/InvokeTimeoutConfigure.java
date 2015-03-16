package app.rpc.remote.plugins;

import org.apache.log4j.Logger;

import app.net.AppSession;
import app.rpc.remote.ObjectServerHandler;
import app.rpc.remote.PluginHandler;

public class InvokeTimeoutConfigure implements PluginHandler {
	private static final Logger log = Logger
			.getLogger(InvokeTimeoutConfigure.class);

	protected ObjectServerHandler serverHandler;

	public void init(ObjectServerHandler serverHandler) {
		this.serverHandler = serverHandler;

		String time = System.getProperty("invoke.timeout");
		if (time == null) {
			time = System.getenv("INVOKE_TIMEOUT");
		}
		if (time == null) {
			time = "-1";
		}
		Long timeout = Long.parseLong(time);
		serverHandler.setAttribute(AppSession.SESSION_RECV_TIMEOUT, timeout);

		log.info("InvokeTimeoutConfigure: init\n    default.invoke.timeout:"
				+ timeout);

	}

	public void destroy() {
		log.info("InvokeTimeoutConfigure: destroy");

		serverHandler.removeAttribute(AppSession.SESSION_RECV_TIMEOUT);

	}

}
