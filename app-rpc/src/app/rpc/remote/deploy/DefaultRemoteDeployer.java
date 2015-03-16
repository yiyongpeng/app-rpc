package app.rpc.remote.deploy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import app.rpc.client.DeployException;
import app.rpc.client.DeployTarget;
import app.rpc.client.RemoteDeployer;
import app.core.AccessException;
import app.rpc.remote.ObjectSession;
import app.rpc.remote.service.LoginParameter;
import app.rpc.remote.service.ServiceContext;
import app.rpc.utils.InputStreamRemoteAdapter;
import app.rpc.utils.RemoteFile;
import app.rpc.utils.RemoteInputStream;
import app.rpc.utils.RemoteIterator;

public class DefaultRemoteDeployer implements RemoteDeployer {
	protected final Logger log = Logger.getLogger(this.getClass());

	public void deployUpload(String name, DeployTarget target)
			throws DeployException {
		DeployHandler deployHandler = getDeployHandler();

		// if (exists(name)) {
		// throw new DeployException("Already exists name: " + name);
		// }

		LoginParameter login = (LoginParameter) ServiceContext.getSession()
				.getAttribute(ObjectSession.LOGIN_USER);
		String host = login.getDeployHost();
		String deployPath = deployHandler.getDeployPath()+File.separator+host+File.separator+name;

		try {
			String parent = target.getPath();
//			int index = parent.lastIndexOf('/', parent.length() - 2);
//			parent = index == -1 ? "" : parent.substring(0, index + 1);
			copyRemoteFile(parent, target.getDeployClasses(), deployPath);
			copyRemoteFile(parent, target.getDeployLibrary(), deployPath);
			copyRemoteFile(parent, target.getDeployMetaInfo(), deployPath);
		} catch (IOException e) {
			e.printStackTrace();
			throw new DeployException("copy remote file failed: "
					+ e.getMessage(), e);
		}

	}

	public void deployLoad(String name) throws DeployException {
		DeployContext deployContext = getDeployContext(name);
		if (deployContext == null) {
			throw new DeployException("Does not exist: " + name);
		}
		deployContext.doLoad(getDeployHandler());
	}

	public void deployUp(String name) throws DeployException {
		DeployContext deployContext = getDeployContext(name);
		if (deployContext == null
				|| deployContext.getStatus() < DeployContext.STATUS_LOAD) {
			throw new DeployException("Not load: " + name);
		}

		if (deployContext.getStatus() == DeployContext.STATUS_LOAD) {
			deployContext.doDeploy(getDeployHandler());
		}
	}

	public void deployDown(String name) throws DeployException {
		DeployContext deployContext = getDeployContext(name);
		if (deployContext == null
				|| deployContext.getStatus() < DeployContext.STATUS_DEPLOY) {
			throw new DeployException("Not deploy up: " + name);
		}
		deployContext.doUndeploy(getDeployHandler());
	}

	public void deployDestroy(String name) throws DeployException {
		DeployContext deployContext = getDeployContext(name);
		if (deployContext != null
				&& deployContext.getStatus() >= DeployContext.STATUS_DEPLOY) {
			throw new DeployException("Not deploy down: " + name);
		}
		if (deployContext != null) {
			String host = ServiceContext.getLoginParamter().getDeployHost();
			deployContext.doDestroy(getDeployHandler(), host);
		}
	}

	public void deployDelete(String name) throws DeployException {
		DeployContext deployContext = getDeployContext(name);
		if (deployContext != null
				&& deployContext.getStatus() >= DeployContext.STATUS_LOAD) {
			throw new DeployException("Not destroy: " + name);
		}
		DeployHandler handler = getDeployHandler();
		String deployPath = handler.getDeployPath();
		String host = ServiceContext.getLoginParamter().getDeployHost();
		File file = new File(new File(deployPath, host), name);

		if (file.isDirectory()) {
			try {
				FileUtils.forceDelete(file);
			} catch (IOException e) {
				e.printStackTrace();
				throw new DeployException("When the delete errors: "
						+ e.getMessage(), e);
			}
		}
	}

	public boolean exists(String name) {
		DeployHandler deployHandler = getDeployHandler();
		LoginParameter login = ServiceContext.getLoginParamter();
		String host = login.getDeployHost();
		String deployPath = deployHandler.getDeployPath();
		File file = new File(new File(deployPath, host), name);
		return file.exists();
	}

	public boolean isLoaded(String name) {
		DeployContext deployContext = getDeployContext(name);
		if (deployContext != null) {
			return deployContext.getStatus() >= DeployContext.STATUS_LOAD;
		}
		return false;
	}

	public boolean isDeployed(String name) {
		DeployContext deployContext = getDeployContext(name);
		if (deployContext != null) {
			return deployContext.getStatus() >= DeployContext.STATUS_DEPLOY;
		}
		return false;
	}

	private DeployHandler getDeployHandler() {
		DeployHandler deployHandler = (DeployHandler) ServiceContext
				.getApplication().getAttribute(
						DeployHandler.DEPLOY_CONTEXT_HANDLER);
		if (deployHandler == null) {
			throw new DeployException("Not support remote deployer");
		}
		return deployHandler;
	}

	protected void copyRemoteFile(String targetParent, RemoteFile deployFile,
			String deployPath) throws IOException {
		if (deployFile.exists()) {
			String path = deployFile.getPath();
			if (targetParent.length() > 0) {
				path = path.substring(targetParent.length());
			}
			File upfile = new File(new File(deployPath), path);
			log.debug("Upload remote file: " + upfile);
			if (deployFile.isDirectory()) {
				if (!upfile.exists() && !upfile.mkdirs()) {
					throw new AccessException("create dir failed: " + upfile);
				}
				RemoteIterator<RemoteFile> ite = deployFile.listFilesIterator();
				while (ite.hasNext()) {
					RemoteFile remoteFile = ite.next();
					copyRemoteFile(targetParent, remoteFile, deployPath);
				}
			} else if (deployFile.isFile()) {
				InputStream in = null;
				OutputStream out = null;
				try {
					RemoteInputStream in0 = deployFile.newInputStream();
					in = new InputStreamRemoteAdapter(in0);
					out = FileUtils.openOutputStream(upfile);
					IOUtils.copy(in, out);
				} finally {
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(out);
				}
			}
		}
	}

	private DeployContext getDeployContext(String name) {
		DeployHandler handler = getDeployHandler();
		String host = ServiceContext.getLoginParamter().getDeployHost();
		return handler
				.getDeployContext(host, handler.getDeployContextKey(name));
	}
}
