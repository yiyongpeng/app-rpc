package app.rpc.remote.impl;

import java.util.Timer;
import java.util.TimerTask;

import app.rpc.remote.DriverManager;
import app.rpc.remote.ObjectConnection;

/**
 * 远程对象连接池
 * 
 * @author yiyongpeng
 * 
 */
public class DefaultObjectConnectionPool extends BaseConnectionPool {
	private Timer timer;

	private String url;
	private String username = "";
	private String password = "";

	private int minSize = 5;
	private int maxSize = 20;
	private long delay = 3000;

	@Override
	public ObjectConnection getConnection() {
		if (timer == null) {
			init();
		}
		ObjectConnection conn = null;

		if (isEmpty() && size() < maxSize)
			try {
				conn = createConnection();
				this.getRuningQueue().put(conn);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		else {
			conn = super.getConnection();
		}
		return conn;
	}

	private ObjectConnection createConnection() {
		ObjectConnection conn = new ConnectionWraper(
				DriverManager.getConnection(url, username, password));
		return conn;
	}

	@Override
	public void destory() {
		this.timer.cancel();
		this.timer = null;
		super.destory();
	}

	@Override
	public void init() {
		super.init();
		timer = new Timer();
		timer.schedule(new ConnPoolTimerTask(), delay);
	}

	/**
	 * 时钟任务
	 * 
	 * @author yiyongpeng
	 * 
	 */
	class ConnPoolTimerTask extends TimerTask {

		@Override
		public void run() {
			while (size() < minSize)
				try {
					idleQueue.put(createConnection());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}

	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String uri) {
		this.url = uri;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

}
