package app.rpc.remote.service;

import java.io.IOException;

import app.core.Connection;
import app.net.DefaultAppSession;
import app.rpc.remote.ObjectRequest;
import app.rpc.remote.ObjectResponse;
import app.rpc.remote.ObjectServerHandler;
import app.rpc.remote.ObjectSession;

/**
 * 登录认证服务
 * 
 * @author yiyongpeng
 * 
 */
public class LoginService extends GenericService<LoginParameter> {
	/** 允许重连不存在的SessionId */
	public static final String ATTR_RECONNECT_NOT_SID = "__RECONNECT_NOT_SID__";

	@Override
	public void doService(LoginParameter login, ObjectRequest request, ObjectResponse response) throws IOException {
		ObjectSession session = request.getSession();
		String sid = login.getSessionId();
		// 网关用户断线重连失败，Session已销毁
		if (sid != null && session.getSessionId().indexOf('@') != -1) {
			if (!session.contains(ObjectSession.LOGIN_USER)) {
				response.writeBoolean(true);
				response.writeInt(0);// 需要重新登录
				return;
			}
		}
		// 验证用户名密码
		validate(login);
		boolean success = login.isSuccess();
		response.writeBoolean(success);
		if (success) {
			// 断线重连时需要重新链接旧Session
			int sessionid = configureSession(login, request);
			response.writeInt(sessionid);
		} else {
			response.writeUTF(login.getMessage());
		}
	}

	protected void validate(LoginParameter login) {
		// 使用认证管理器
		IAuthManager auth = (IAuthManager) ServiceContext.getApplication().getAttribute(ServiceContext.AUTH_MANAGER);
		if (auth != null) {
			auth.validate(login);
		} else {
			login.setSuccess(true);
		}
		log.debug(new StringBuffer("connect-validate: ").append(login.getUrl()).append("  username: ").append(login.getUname()).append("  password: ").append(login.getPwd()).append("  sessionId: ").append(login.getSessionId()).append("  success: ").append(login.isSuccess()));
	}

	protected void onSuccess(LoginParameter login, ObjectSession session) throws IOException {
		// 记录用户登录信息
		session.setAttribute(ObjectSession.LOGIN_USER, login.clone());

	}

	protected int configureSession(LoginParameter login, ObjectRequest request) throws IOException {
		ObjectSession session = request.getSession();
		Connection conn = session.getConnection();
		String sessionId = login.getSessionId();

		// 断线重连，重新链接Session
		if (sessionId != null) {
			ObjectServerHandler server = request.getServletContext();
			DefaultAppSession session0 = (DefaultAppSession) server.getSession(sessionId);
			boolean linkup = session0 != null;
			if (linkup) {
				if (session.isDefault()) {
					Connection conn0 = session0.getConnection();
					if (!conn0.isClosed() && conn0 != session.getConnection())
						conn0.close();
					session0.init(conn);// 替换成当前连接
					session0.onAccpeted();// 使Session复活
					conn.setSession(session0);// 替换当前连接的默认Session
					server.removeSession(session.getSessionId());// 清除被覆盖的Session
				} else {
					sessionId = session.getSessionId();
					session0.setSessionId(sessionId);
				}
				session = (ObjectSession) session0;
				log.debug("configure Session-change, link-up: " + linkup + "  session: " + session + "  sessions-count: " + session.getServerHandler().getSessionCount());
			} else {
				linkup = ((Boolean) session.getAttribute(ATTR_RECONNECT_NOT_SID, true));
				log.warn("Reconnect the Session not found, link-up: " + linkup + "  session: " + session + "  sessionId: " + sessionId);
				if (!linkup) {
					return 0;
				}
			}
		} else {
			sessionId = session.getSessionId();
			log.debug("configure Session-self, address: " + conn.getInetAddress() + "  sessionId:" + sessionId);
		}

		// 登录成功，返回新的SessionId
		onSuccess(login, session);

		int id = session.getSid();
		if (id == 0) {
			id = Integer.parseInt(session.getSessionId());
		} else {
			// 激活子Session
			((DefaultAppSession) session).onAccpeted();
			log.debug("Gateway-Session Accpeted: " + session);// 网关用户的SessionId
		}
		return id;
	}
}
