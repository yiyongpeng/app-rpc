package app.rpc;

import java.lang.reflect.UndeclaredThrowableException;

import app.rpc.remote.DriverManager;
import app.rpc.remote.ObjectSession;
import app.rpc.remote.impl.DefaultClientObjectConnection;

public class Shutdown {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String connString = args[0];
		String username = args[1];
		String password = args[2];
		try {
			System.out.print("shutdown...  ");
			DefaultClientObjectConnection conn = (DefaultClientObjectConnection) DriverManager
					.getConnection(connString, username, password);
			conn.setReconnect(false);
			ObjectSession session = conn.getRemote();
			session.getServerHandler().getConnector().stop();
		} catch (UndeclaredThrowableException e) {
			System.out.println("\tok!");
		} catch (Exception e) {
			System.out.println("\t" + e.getMessage());
		}
	}
}
