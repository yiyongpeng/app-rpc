package app.rpc.client;

import app.rpc.utils.RemoteFile;

public interface DeployTarget extends RemoteFile {

	RemoteFile getDeployLibrary();

	RemoteFile getDeployClasses();

	RemoteFile getDeployMetaInfo();

}
