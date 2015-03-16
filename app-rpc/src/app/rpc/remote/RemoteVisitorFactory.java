package app.rpc.remote;

import java.io.IOException;

import app.net.AppSession;

/**
 * 调用访问器工厂
 * 
 * @author yiyongpeng
 * 
 */
public interface RemoteVisitorFactory {

	/**
	 * 创建新的远程访问器
	 * 
	 * @return
	 * @throws IOException
	 */
	RemoteVisitor create(AppSession session);

}
