package app.rpc.remote;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import app.core.Remote;
import app.net.AppServer;
import app.rpc.remote.impl.DefaultArgConvertHandler;
import app.rpc.remote.impl.DefaultObjectServerHandler;
import app.rpc.remote.impl.MethodInvokeInterceptor;
import app.util.ThreadContext;

/**
 * 对象连接器
 * 
 * @author yiyongpeng
 * 
 */
public class DefaultObjectConnector extends AppServer implements
		ObjectConnector, Remote {
	private Logger log = Logger.getLogger(getClass());
	private boolean server = true;
	private String host = "0.0.0.0";
	private int port;
	private Exportor exportor;

	private ServerSocketChannel ssc;

	static {
		// 注册默认远程基本类型
		RemoteTypeMapper.regist(void.class, new RemoteType4Basic(void.class));
		RemoteTypeMapper.regist(byte.class, new RemoteType4Basic(byte.class));
		RemoteTypeMapper.regist(short.class, new RemoteType4Basic(short.class));
		RemoteTypeMapper.regist(int.class, new RemoteType4Basic(int.class));
		RemoteTypeMapper.regist(long.class, new RemoteType4Basic(long.class));
		RemoteTypeMapper.regist(float.class, new RemoteType4Basic(float.class));
		RemoteTypeMapper.regist(double.class,
				new RemoteType4Basic(double.class));
		RemoteTypeMapper.regist(boolean.class, new RemoteType4Basic(
				boolean.class));
		RemoteTypeMapper.regist(char.class, new RemoteType4Basic(char.class));

		// 增加远程引用支持
		InvokeInterceptor ii = new DefaultArgConvertHandler();
		MethodInvokeInterceptor.getInstance().addInvokeBeforeHandle(ii);
		MethodInvokeInterceptor.getInstance().addInvokeAfterHandle(ii);
	}

	public DefaultObjectConnector(int executorPoolSize, int readerPoolSize, int writerPoolSize) throws IOException {
		super(new DefaultObjectServerHandler(), executorPoolSize, readerPoolSize, writerPoolSize);
	}
	
	public DefaultObjectConnector() throws IOException {
		super(new DefaultObjectServerHandler(), 0, 0, 0);
	}

	public DefaultObjectConnector(int port) throws IOException {
		this();
		this.port = port;
	}

	public DefaultObjectConnector(String host, int port) throws IOException {
		this();
		this.host = host;
		this.port = port;
	}
	
	@Override
	public String toString() {
		return new StringBuilder("{").append(getServerHandler().getProtocol())
				.append(":").append(host).append(":").append(port).append("}")
				.toString();
	}

	@Override
	protected void destory() {
		boolean inited = ThreadContext.contains();
		Object app = null;
		try {
			if (!inited) {
				ThreadContext.init();
			}
			app = ThreadContext.setAttribute(ThreadContext.SCOPE_APP,
					getServerHandler());

			if (exportor != null)
				exportor.destory();

			super.destory();

		} finally {
			if (!inited) {
				ThreadContext.destory();
			} else {
				ThreadContext.setAttribute(ThreadContext.SCOPE_APP, app);
			}
		}
	}

	@Override
	protected void init() {
		InetSocketAddress addr = new InetSocketAddress(host, port);
		boolean inited = ThreadContext.contains();
		Object app = null;
		try {
			if (!inited) {
				ThreadContext.init();
			}
			app = ThreadContext.setAttribute(ThreadContext.SCOPE_APP,
					getServerHandler());

			super.init();

			if (exportor != null)
				exportor.init(getServerHandler());
			if (port > 0)
				if (server) {
					ssc = ServerSocketChannel.open();
					ssc.socket().bind(addr);
					registor(ssc);
					log.info("Listen to " + addr);
				} else {
					SocketChannel sc = SocketChannel
							.open(new InetSocketAddress(host, port));
					this.registor(sc);
				}
		} catch (IOException e) {
//			log.info("listen to " + addr + " failure!");
			this.notifier.fireOnError(null, e);
		} finally {
			if (!inited) {
				ThreadContext.destory();
			} else {
				ThreadContext.setAttribute(ThreadContext.SCOPE_APP, app);
			}
		}
	}

	@Override
	public DefaultObjectServerHandler getServerHandler() {
		return (DefaultObjectServerHandler) super.getHandler();
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isServer() {
		return server;
	}

	public void setServer(boolean server) {
		this.server = server;
	}

	public void setExportor(Exportor exportor) {
		this.exportor = exportor;
	}

	@Override
	public void stop() {
		super.stop();
		try {
			if (ssc != null)
				ssc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Exportor getExportor() {
		return exportor;
	}
}
