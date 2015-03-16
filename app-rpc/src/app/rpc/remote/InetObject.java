package app.rpc.remote;

/**
 * 网络对象
 * 
 * @author yiyongpeng
 * 
 */
public interface InetObject {

	String getHandle();

	RemoteMethodCollection getMethodCollection();

}
