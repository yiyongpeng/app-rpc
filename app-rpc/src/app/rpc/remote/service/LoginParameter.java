package app.rpc.remote.service;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import app.core.Remote;
import app.rpc.remote.ObjectRequest;
import app.rpc.remote.deploy.DeployContext;
import app.rpc.remote.deploy.DeployHandler;
import app.rpc.remote.impl.RemoteUrl;

public class LoginParameter extends DefaultParameter implements Remote {
	private String url;
	private String uname;
	private String pwd;
	private String sessionId;

	private boolean success;
	private String message;

	private RemoteUrl remoteUrl;
	private DeployContext deploy;

	private String deployPath;

	@Override
	public void destory() {
		this.url = null;
		this.uname = null;
		this.pwd = null;
		this.sessionId = null;
		this.success = false;
		this.message = null;
		this.remoteUrl = null;
		this.deploy = null;
		this.deployPath = null;
		super.destory();
	}

	@Override
	public void init(ObjectRequest request) throws Exception {
		super.init(request);

		this.remoteUrl = new RemoteUrl(url);

		DeployHandler handler = (DeployHandler) request.getServletContext()
				.getAttribute(DeployHandler.DEPLOY_CONTEXT_HANDLER);
		if (handler != null) {
			deploy = handler.getDeployContext(getDeployHost(), getDeployPath());
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		this.url = in.readUTF();
		this.uname = in.readUTF();
		this.pwd = in.readUTF();
		this.sessionId = in.available() > 0 ? in.readUTF() : null;
		super.readExternal(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(url);
		out.writeUTF(uname);
		out.writeUTF(pwd);
	}

	@Override
	public LoginParameter clone() {
		try {
			return (LoginParameter) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getSessionId() {
		return sessionId;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public String getUrl() {
		return url;
	}

	public String getUname() {
		return uname;
	}

	public String getPwd() {
		return pwd;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public RemoteUrl getRemoteUrl() {
		return remoteUrl;
	}

	public String getDeployHost() {
		return remoteUrl.getDeployHost();
	}

	public String getDeployPath() {
		if (deployPath != null)
			return deployPath;
		String path = getRemoteUrl().getPath();
		int index = path.indexOf("/", 1);
		if (index != -1) {
			path = path.substring(0, index);
		} else {
			path = "/";
		}
		return this.deployPath = path;
	}

	public ClassLoader getClassLoader() {
		if (deploy != null) {
			ClassLoader loader = deploy.getClassLoader();
			if (loader != null)
				return loader;
		}
		return Thread.currentThread().getContextClassLoader();
	}

	public DeployContext getDeployContext() {
		return deploy;
	}

}
