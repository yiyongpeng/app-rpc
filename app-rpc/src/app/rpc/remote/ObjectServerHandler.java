package app.rpc.remote;

import app.core.Connection;
import app.net.AppHandler;

public interface ObjectServerHandler extends AppHandler {

	/** 获取指定Session */
	ObjectSession getSession(String sessionId);

	/** 创建新的Session */
	ObjectSession createSession(Connection conn, Object sessionId);

	/** 移除Session */
	ObjectSession removeSession(String sid);

	/**
	 * 对象连接器
	 * 
	 * @return
	 */

	ObjectConnector getConnector();

	/**
	 * 获取服务管理器
	 * 
	 * @return
	 */
	ServiceManager getServiceManager();

	String getProtocol();

}
