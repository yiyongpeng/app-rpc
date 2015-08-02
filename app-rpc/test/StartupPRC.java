import java.io.IOException;

import app.rpc.Startup;
import app.rpc.remote.deploy.FileSystemDeployContextHandler;

public class StartupPRC {

	public static void main(String[] args) throws IOException {
		Startup.main(args);
		// 安装部署插件
		FileSystemDeployContextHandler deployPlugin = new FileSystemDeployContextHandler();
		Startup.getConector().getServerHandler().addPlugin(deployPlugin);
	}
}
