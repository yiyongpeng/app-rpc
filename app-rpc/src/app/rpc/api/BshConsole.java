package app.rpc.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import app.rpc.remote.DriverManager;
import app.rpc.remote.ObjectSession;
import app.rpc.remote.impl.RemoteUrl;
import app.rpc.utils.RemoteInputStream;
import app.rpc.utils.RemoteInputStreamAdapter;
import app.rpc.utils.RemoteOutputStream;
import app.rpc.utils.RemoteOutputStreamAdapter;

public final class BshConsole {

	public static void main(String[] args) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		if (args.length == 0) {
			args = new String[3];
			System.out.print("[RPC-bsh]请输入连接地址：");
			args[0] = input.readLine();
			System.out.print("[RPC-bsh]请输入用户名：");
			args[1] = input.readLine();
			System.out.print("[RPC-bsh]请输入密码：");
			args[2] = input.readLine();
		}
		try {
			String connString = args[0].replace("\\", "/");
			String username = args.length > 1 ? args[1] : "";
			String password = args.length > 2 ? args[2] : "";
			String path = new RemoteUrl(connString).getPath();
			ObjectSession session = DriverManager.getConnection(connString, username, password).getSession();
			BshApi api = session.getRemote(path.substring(path.lastIndexOf("/") + 1), BshApi.class);
			RemoteInputStream in = new RemoteInputStreamAdapter(System.in);
			RemoteOutputStream out = new RemoteOutputStreamAdapter(System.out);
			RemoteOutputStream err = new RemoteOutputStreamAdapter(System.err);
			api.bsh(in, out, err);
			while (!session.isClosed()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("\n[RPC-bsh] exit.");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("\n[RPC-bsh] exit : " + e.getMessage());
			System.exit(1);
		}
	}
}
