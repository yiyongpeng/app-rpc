package app.rpc.client;

import app.core.Remote;

/**
 * 远程部署器
 * 
 * @author yiyongpeng
 * 
 */
public interface RemoteDeployer extends Remote {

	/**
	 * 上传部署项目
	 * 
	 * @param target
	 * @throws DeployException
	 *             已经存在时抛出异常
	 */
	void deployUpload(String name, DeployTarget target) throws DeployException;

	/**
	 * 加载并实例化待部署項目，如果已经加载那么不做任何操作。
	 * 
	 * @param name
	 * @throws DeployException
	 *             不存在时抛出异常
	 */
	void deployLoad(String name) throws DeployException;

	/**
	 * 将项目部署上线
	 * 
	 * @param name
	 * @throws DeployException
	 *             未加载时抛出异常
	 */
	void deployUp(String name) throws DeployException;

	/**
	 * 将项目部署下线
	 * 
	 * @param name
	 * @throws DeployException
	 *             未部署上线时抛出异常
	 */
	void deployDown(String name) throws DeployException;

	/**
	 * 将已经实例化且下线的项目销毁<br>
	 * 
	 * @param name
	 * @throws DeployException
	 *             未下线时抛出异常
	 */
	void deployDestroy(String name) throws DeployException;

	/**
	 * 刪除部署项目
	 * 
	 * @param target
	 * 
	 * @throws DeployException
	 *             未销毁部署实例时抛出异常
	 */
	void deployDelete(String name) throws DeployException;

	/**
	 * 是否已存在部署项目
	 * 
	 * @param name
	 * @return
	 */
	boolean exists(String name);

	/**
	 * 是否已经加载项目
	 * 
	 * @param name
	 * @return
	 */
	boolean isLoaded(String name);

	/**
	 * 判断项目是否已经部署上线
	 * 
	 * @param name
	 * @return
	 */
	boolean isDeployed(String name);

}
