import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import simple.Hello;
import simple.HelloImpl2;
import simple.SeriModel;

import app.rpc.remote.DriverManager;
import app.rpc.remote.ObjectConnection;
import app.rpc.remote.ObjectServerHandler;
import app.rpc.remote.ObjectSession;
import app.rpc.remote.ServiceObject;
import app.rpc.remote.impl.DefaultServiceObject;
import app.rpc.remote.impl.RemoteUrl;
import app.rpc.remote.plugins.GatewayServerHandlerPlugin;

public class Client {

	private static int CONN_NUM = 1;
	private static int CONN_THREAD = 1;
	private static int THREAD_TEST_NUM = 0;

	private static String URL = "localhost/dtest/";// "s1.my-rice.org/deploy-test/";//
	private static String HANDLE = "hello";
	private static boolean EXE = true;
	private static boolean RS = false;
	private static boolean PRINT = true;
	private static boolean GATEWAY = false;

	public static void main(String[] args) throws Exception {
		// ObjectConnection conn = DriverManager.getConnection("localhost",
		// "myrice", "pwd");
		// Hello hello = (Hello) conn.getSession().getRemote("hello",
		// Hello.class);
		// System.err.println(hello.say('d', true, (byte) 127, (short) 12, 231,
		// 323, 342, 23423.234));
		gogo();

	}

	private static void gogo() throws IOException {
		RemoteUrl url = new RemoteUrl(URL);
		String protocol = url.getProtocol();
		GatewayServerHandlerPlugin gateway = (GatewayServerHandlerPlugin) DriverManager.getConnector(protocol).getServerHandler().getAttribute(GatewayServerHandlerPlugin.APP_ATTR_NAME);
		if (gateway != null) {
			gateway.destroy();
		}

		// 测试连接数
		for (int i = 1; i <= CONN_NUM; i++) {
			gotoTest(i);
		}
	}

	private static void gotoTest(final int c) throws IOException {
		final ObjectConnection conn = DriverManager.getConnection(URL, "myrice", "mypwd");

		if (!EXE) {
			return;
		}
		// 设置是否自动重连
		// ((DefaultClientObjectConnection) conn).setReconnect(false);//默认true
		final ObjectSession session = conn.getSession();

		// 注册对象
		Serializable instance = new HelloImpl2("Hello world!");
		ServiceObject so = new DefaultServiceObject(HANDLE, instance, Hello.class);
		session.setAttribute(so.getInvokeHandle(), so);// 注册到本地服务
		// session.registor("hello", instance, Hello.class);// 注册到远程服务
		final int[] n = { 0 };

		// 测试每个连接的线程数
		for (int i = 0; i < CONN_THREAD; i++) {
			synchronized (n) {
				final int t = n[0]++;
				exe.execute(new Runnable() {
					@Override
					public void run() {
						goTest(conn, session, n, c, t);
					}
				});
			}
		}

		// 定时断开连接
		// conn.getSession().getServerHandler().execute(new Runnable() {
		// private int count = 0;
		//
		// @Override
		// public void run() {
		// try {
		// conn.getSocketChannel().close();
		// conn.getSession().getServerHandler().getConnector()
		// .wakeup();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// if (n[0] > 0 && count-- > 0) {
		// conn.getSession().getServerHandler().execute(this, 30000);
		// }
		// }
		// }, 10000);
	}

	private static void goTest(final ObjectConnection conn, ObjectSession session, final int[] n, int c, int t) {

		// 每个线程测试次数
		int ago = 0;
		long time = 1;
		for (int i = 0; THREAD_TEST_NUM == 0 || i < THREAD_TEST_NUM; i++) {
			ago++;
			time += testRSHello(session, HANDLE, RS, c, t);
			// testRA(session);
			if (i % 1000 == 999) {
				int ns = (int) (ago * 1000000000f / time);
				double msn = time / 1000000d / ago;
				System.out.println(String.format("%d  time:%d(ns)  %d(n/s)  %.2f(ms/n)  %d(n/day)", ago, time, ns,msn, 1L * ns * 24 * 60 * 60));
			}
		}

		System.out.println(ago + "  time:" + time + "ns" + "   avg(n/s):" + (ago * 1000000000f / time));
		synchronized (n) {
			n[0]--;
		}
		if (n[0] == 0) {
			conn.close();
		}
	}

	static SeriModel model = new SeriModel("skywind", 1);

	protected static long testRSHello(ObjectSession session, String handle, boolean rs, int conn, int i) {
		// 通过远程服务器上的Session获取本机客户端连接公开的对象引用
		if (rs) {
			session = session.getRemote();
		} else if (GATEWAY) {
			int sid = (int) (conn * Math.pow(10, String.valueOf(CONN_THREAD).length() - 1) + i);
			session = session.getOrCreateSession(sid);
		}
		// System.err.println(Arrays.toString(obj.getClass().getInterfaces()));
		Hello hello = session.getRemote(handle, Hello.class);
		// hello = hello.getParent();
		hello.setName("skywind" + System.currentTimeMillis() % 100);
		String msg = "";
		long time = System.nanoTime();
		 msg = hello.say('a', true, (byte) 1, (short) 153, 234, 435345l,
		 12.55f, 454.545d);
//		hello.setParent(hello);
//		msg = hello.getParent().getName();
//		msg += hello.getModel(model).toString();
		 time = System.nanoTime() - time;
		if (GATEWAY)
			session.close();

		if (PRINT) {
			System.out.println("[" + session.getSessionId() + "] " + hello.getClass() + "\t" + msg + "\t time: " + (1.0 * time / 1000000));
		}
		// System.out.println(msg + hello.getModel(model) + "   time: "
		// + (System.nanoTime() - time));

		// if (i % 10 == 0)
		// System.out.println();
		// System.out.println(session.getInetAddress() + hello);
		return time;
	}

	protected static void testRA(ObjectSession session) {
		// 获取远程服务器上当前连接的Session
		ObjectSession rs = session.getRemote();
		// 获取远程服务器其他Session
		ObjectServerHandler sh = rs.getServerHandler();
		// rs = sh.getSession(rs.getSessionId());
		String[] sessids = sh.getSessionIds();
		for (String sid : sessids) {
			rs = sh.getSession(sid);
			if (rs != null) {
				// session.getSessionId();
				System.out.println(rs.getSessionId());
			} else {
				System.out.println("session not found, sessionId:" + sid);
			}
		}
	}

	static final ThreadPoolExecutor exe = new ThreadPoolExecutor(20, 1024, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

}
