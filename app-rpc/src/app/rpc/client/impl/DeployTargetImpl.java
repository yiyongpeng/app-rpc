package app.rpc.client.impl;

import java.io.File;

import app.rpc.client.DeployTarget;
import app.rpc.utils.RemoteFile;
import app.rpc.utils.RemoteFileAdapter;

public class DeployTargetImpl extends RemoteFileAdapter implements DeployTarget {

	public DeployTargetImpl(File file) {
		super(file);
	}

	public RemoteFile getDeployLibrary() {
		return new RemoteFileAdapter(new File(this.file, "lib"));
	}

	public RemoteFile getDeployClasses() {
		return new RemoteFileAdapter(new File(this.file, "classes"));
	}

	public RemoteFile getDeployMetaInfo() {
		return new RemoteFileAdapter(new File(this.file, "META-INF"));
	}

}
