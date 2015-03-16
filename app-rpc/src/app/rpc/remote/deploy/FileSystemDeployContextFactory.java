package app.rpc.remote.deploy;

import java.io.File;

public class FileSystemDeployContextFactory implements DeployContextFactory {

	public DeployContext createDelopyContext(Object path) {
		return new FileSystemDeployContext((File) path);
	}

}
