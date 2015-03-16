package app.rpc.remote.service;

import app.rpc.remote.ObjectRequest;
import app.rpc.remote.ObjectResponse;
import app.rpc.remote.ObjectServerHandler;
import app.rpc.remote.ObjectSession;
import app.util.ThreadContext;

public final class ServiceContext {
	public static final String AUTH_MANAGER = "__AUTH_MANAGER__";

	public static final ObjectServerHandler getApplication() {
		ObjectSession session = getSession();
		if (session != null) {
			return session.getServerHandler();
		}
		return (ObjectServerHandler) ThreadContext
				.getAttribute(ThreadContext.SCOPE_APP);
	}

	public static final ObjectSession getSession() {
		return (ObjectSession) ThreadContext
				.getAttribute(ThreadContext.SCOPE_SESSION);
	}

	public static final ObjectRequest getRequest() {
		return (ObjectRequest) ThreadContext
				.getAttribute(ThreadContext.SCOPE_REQUEST);
	}

	public static final ObjectResponse getResponse() {
		return (ObjectResponse) ThreadContext
				.getAttribute(ThreadContext.SCOPE_RESPONSE);
	}

	public static final LoginParameter getLoginParamter() {
		return (LoginParameter) getSession().getAttribute(
				ObjectSession.LOGIN_USER);
	}
}
