package app.rpc.remote;

import java.io.IOException;

import app.core.Connection;

/**
 * 远程对象连接类
 * 
 * @author yiyongpeng
 * 
 */
public interface ObjectConnection extends Connection {

	/** 创建新的Session */
	ObjectSession createSession(String sessionId);

	/**
	 * 获取会话实例
	 * 
	 * @return
	 * @throws IOException
	 */

	ObjectSession getSession();

	String getPath();

}
