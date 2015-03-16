package app.rpc.client;

import java.io.File;

import app.rpc.client.impl.DeployTargetImpl;
import app.rpc.remote.DriverManager;
import app.rpc.remote.impl.DefaultClientObjectConnection;

public class Deployer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("error: invalid arguments.\ne.g. args: upload name | load name | up name | down name | destroy name | del name | status name");
			System.err.println("set prop: rpc.url=?, rpc.username=?, rpc.password=?");
			System.err.println("set env: RPC_DEPLOY_URL=?, RPC_DEPLOY_USERNAME=?, RPC_DEPLOY_PASSWORD=？");
			return;
		}

		String url = "localhost";
		String username = null;
		String password = null;

		String cmd = args[0].trim();
		String name = args[1].trim();
		try {
			if (cmd.equals("upload")) {
				File file = new File(name);
				if (!file.exists()) {
					System.err.println("error: not found file: " + name);
					return;
				}
				RemoteDeployer rd = getRemoteDeployer(url, username, password);
				if(args.length>=3){
					name = args[2];
				}else{
					name = file.getName();
				}
				rd.deployUpload(name, new DeployTargetImpl(file));
			} else if (cmd.equals("load")) {
				RemoteDeployer rd = getRemoteDeployer(url, username, password);
				rd.deployLoad(name);
			} else if (cmd.equals("up")) {
				RemoteDeployer rd = getRemoteDeployer(url, username, password);
				rd.deployUp(name);
			} else if (cmd.equals("down")) {
				RemoteDeployer rd = getRemoteDeployer(url, username, password);
				rd.deployDown(name);
			} else if (cmd.equals("destroy")) {
				RemoteDeployer rd = getRemoteDeployer(url, username, password);
				rd.deployDestroy(name);
			} else if (cmd.equals("del")) {
				RemoteDeployer rd = getRemoteDeployer(url, username, password);
				rd.deployDelete(name);
			} else if (cmd.equals("status")) {
				RemoteDeployer rd = getRemoteDeployer(url, username, password);
				boolean contains = rd.exists(name);
				boolean loaded = rd.isLoaded(name);
				boolean deployed = rd.isDeployed(name);
				System.out.println("[status]\n  name: " + name + "\n  exists: "
						+ contains + "\n  loaded: " + loaded
						+ "\n  deploy-up: " + deployed);
			} else {
				System.err.println("unkown cmd: " + cmd);
			}
			System.exit(0);
		} catch (Throwable e) {
			e.printStackTrace();
			// System.err.println(e);
			System.exit(1);
		}
	}

	private static RemoteDeployer getRemoteDeployer(String url,
			String username, String password) {
		String value = null;
		// 系统环境变量
		value = System.getenv("RPC_DEPLOY_URL");
		if (value != null) {
			url = value;
		}
		value = System.getenv("RPC_DEPLOY_USERNAME");
		if (value != null) {
			username = value;
		}
		value = System.getenv("RPC_DEPLOY_PASSWORD");
		if (value != null) {
			password = value;
		}
		// 虚拟机属性
		url = System.getProperty("rpc.url", url);
		username = System.getProperty("rpc.username", username);
		password = System.getProperty("rpc.password", password);

		if (isDebug()) {
			System.out.println("connect  " + url + "  " + username + "  "
					+ password);
		}
		DefaultClientObjectConnection conn = (DefaultClientObjectConnection) DriverManager
				.getConnection(url, username, password);
		conn.setReconnect(false);
		return conn.getSession().getRemote("Deployer",
				RemoteDeployer.class);
	}

	private static boolean isDebug() {
		return System.getProperty("debug", "false").toLowerCase()
				.equals("true");
	}

}
