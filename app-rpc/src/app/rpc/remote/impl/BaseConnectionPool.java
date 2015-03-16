package app.rpc.remote.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import app.core.Session;
import app.core.WriteRequest;
import app.rpc.remote.ObjectConnection;
import app.rpc.remote.ObjectConnectionSource;
import app.rpc.remote.ObjectSession;

public abstract class BaseConnectionPool implements ObjectConnectionSource {
	protected BlockingQueue<ObjectConnection> runingQueue;
	protected BlockingQueue<ObjectConnection> idleQueue;
	private int capacityRuning = 20;
	private int capacityIdle = 20;

	public int getCapacityDoing() {
		return capacityRuning;
	}

	public void setCapacityDoing(int capacityDoing) {
		this.capacityRuning = capacityDoing;
	}

	public int getCapacityIdle() {
		return capacityIdle;
	}

	public void setCapacityIdle(int capacityIdle) {
		this.capacityIdle = capacityIdle;
	}

	public int size() {
		return getIdleQueue().size() + getRuningQueue().size();
	}

	public boolean isEmpty() {
		return this.getIdleQueue().isEmpty();
	}

	public boolean add(ObjectConnection conn) {
		try {
			if ((conn instanceof ConnectionWraper) == false)
				conn = new ConnectionWraper(conn);
			this.getIdleQueue().put(conn);
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	public ObjectConnection getConnection() {
		ObjectConnection conn = null;
		try {
			synchronized (this) {
				conn = this.getIdleQueue().take();
				this.getRuningQueue().put(conn);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return conn;
	}

	public void destory() {
		for (ObjectConnection conn : this.getRuningQueue())
			conn.close();
		for (ObjectConnection conn : this.getIdleQueue())
			conn.close();

		this.getRuningQueue().clear();
		this.getIdleQueue().clear();

		this.runingQueue = null;
		this.idleQueue = null;
	}

	protected BlockingQueue<ObjectConnection> getIdleQueue() {
		if (idleQueue == null)
			synchronized (this) {
				if (idleQueue == null) {
					this.idleQueue = new ArrayBlockingQueue<ObjectConnection>(
							capacityIdle);
				}
			}
		return idleQueue;
	}

	protected BlockingQueue<ObjectConnection> getRuningQueue() {
		if (runingQueue == null)
			synchronized (this) {
				if (runingQueue == null) {
					this.runingQueue = new ArrayBlockingQueue<ObjectConnection>(
							capacityRuning);
				}
			}
		return runingQueue;
	}

	public void init() {

	}

	protected class ConnectionWraper implements ObjectConnection {
		private ObjectConnection conn;

		public ConnectionWraper(ObjectConnection conn) {
			this.conn = conn;
		}

		public void close() {
			getRuningQueue().remove(this);
			if (((DefaultClientObjectConnection) this.conn).isReconnect()
					|| this.conn.isClosed() == false)
				try {
					idleQueue.put(this);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}

		@Override
		public int hashCode() {
			return this.conn.hashCode();
		}

		@Override
		public String toString() {
			return this.conn.toString();
		}

		@Override
		public boolean equals(Object obj) {
			return this.conn.equals(obj);
		}

		public ObjectSession getSession() {
			return this.conn.getSession();
		}

		public boolean isClosed() {
			return this.conn.isClosed();
		}

		public String getRemoteAddress() {
			return this.conn.getRemoteAddress();
		}

		public int getRemotePort() {
			return this.conn.getRemotePort();
		}

		public String getLocalAddress() {
			return conn.getLocalAddress();
		}

		public int getLocalPort() {
			return conn.getLocalPort();
		}

		public ByteChannel getSocketChannel() {
			return this.conn.getSocketChannel();
		}

		public String getInetAddress() {
			return conn.getInetAddress();
		}

		public String getProtocol() {
			return conn.getProtocol();
		}

		public ByteBuffer read() throws IOException {
			return conn.read();
		}

		public WriteRequest getWriteRequest() {
			return conn.getWriteRequest();
		}

		public boolean isBusy() {
			return conn.isBusy();
		}

		public void setBusy(boolean busy) {
			conn.setBusy(busy);
		}

		public void clearRecvBuffer() {
			conn.clearRecvBuffer();
		}

		public String getPath() {
			return conn.getPath();
		}

		public void setSession(Session session) {
			conn.setSession(session);
		}

		public ObjectSession createSession(String sessionId) {
			return conn.createSession(sessionId);
		}
	}

}
