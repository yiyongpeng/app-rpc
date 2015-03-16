package app.rpc.client;

import app.core.AccessException;

/**
 * 部署异常
 * 
 * @author yiyongpeng
 * 
 */
@SuppressWarnings("serial")
public class DeployException extends AccessException {

	public DeployException(String message) {
		super(message);
	}

	public DeployException(String message, Throwable e) {
		super(message, e);
	}
}
