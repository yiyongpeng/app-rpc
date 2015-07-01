package app.rpc.remote.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import app.core.AccessException;
import app.net.AppCallResponse;
import app.net.AppSession;
import app.net.AppCallResponseAdapter;
import app.rpc.remote.RemoteMethod;
import app.rpc.remote.RemoteMethodCollection;
import app.rpc.remote.RemoteObject;
import app.rpc.remote.RemoteVisitor;
import app.rpc.remote.Scope;
import app.rpc.remote.service.RegistorParameter;
import app.util.BeanUtils;

/**
 * 远程对象访问器
 * 
 * @author yiyongpeng
 * 
 */
public class DefaultRemoteVisitor extends BaseRemoteVisitor {
	
	@Override
	protected Object invokeImpl(RemoteObject ro, Method method, Object[] args)
			throws Throwable {
		RemoteMethod mm = ro.mapping(method);
		if (mm == null)
			throw new AccessException("Unkown method: " + method);
		InnerByteArrayOutputStream _out = ((InnerObjectOutputStream) out)._out;
		_out.prepare(mm);

		// ----------- 开始调用 -----------

		// ObjectHandle oh = new ObjectHandle(ro.getHandle());
		((InnerObjectOutputStream)out)._out.mode = (MODE_INVOKATION);
		out.writeInt(ro.getInvokeHandle());/* 写入调用句柄 */
		// oh.writeExternal(out);/* 写入对象句柄 */
		// oh.destory();

		return mm.invoke(in, out, ro, args);

	}

	@Override
	protected RemoteMethodCollection validateImpl(String handle,
			Class<?>[] interfaces) throws AccessException {
		try {
			ObjectHandle oh = new ObjectHandle(handle);
			// 请求远程对象方法列表
			((InnerObjectOutputStream)out)._out.mode = (MODE_VALIDATE);
			oh.writeExternal(out); /* 写入对象句柄 */
			oh.destory();
			out.flush();

			boolean registed = in.readBoolean();
			if (registed == false)
				throw new AccessException("Not found Remote-Handle: " + handle);

			// 读取响应的方法列表数据
			DefaultRemoteMethodCollection methods = new DefaultRemoteMethodCollection(
					in);/* 读取方法清单 */

			// 校验接口方法
			methods.validate(interfaces);

			// 缓存对象的方法列表数据
			return methods;
		} catch (AccessException e) {
			throw e;
		} catch (Throwable e) {
			throw new AccessException("validate failed.", e);
		}
	}

	@Override
	public void registorImpl(String handle, Serializable instance,
			Class<?>[] interfaces, Scope scope) {
		RegistorParameter rp = new RegistorParameter(handle, instance,
				interfaces, scope);
		try {
			((InnerObjectOutputStream)out)._out.mode = (MODE_REGISTOR);
			rp.writeExternal(out);
			rp.destory();
			out.flush();

			if (in.readBoolean() == false)
				throw new AccessException(in.readUTF());
		} catch (Throwable e) {
			throw new AccessException("registor failed!", e);
		}
	}

	@Override
	public void connectImpl(String url, String user, String pwd) {
		try {
			if (user == null)
				user = "";
			if (pwd == null)
				pwd = "";

			DefaultClientObjectConnection conn = (DefaultClientObjectConnection) session;
			String sessionId = conn.getSessionId();

			((InnerObjectOutputStream)out)._out.mode = (MODE_LOGIN);
			out.writeUTF(url);
			out.writeUTF(user);
			out.writeUTF(pwd);
			if (conn.isReconnecting() && sessionId != null)
				out.writeUTF(sessionId.substring(String.valueOf(
						session.getInetAddress().hashCode()).length()));

			out.flush();

			if (in.readBoolean() == false)
				throw new AccessException(in.readUTF());
			else {
				int id = in.readInt();
				if (id == 0) {
					throw new AccessException(
							"Reconnect failed, sessionId timeout!");
				}
				String sid = session.getInetAddress().hashCode()
						+ String.valueOf(id);
				conn.setSessionId(sid);
			}
		} catch (Throwable e) {
			throw new AccessException("connect failed!  url=" + url
					+ "  username=" + user + "  password=" + pwd, e);
		}
	}

	protected ObjectOutput newObjectOutput(InnerObjectInputStream bin) {
		ObjectOutput _out = recycle4out.poll();
		if (_out == null) {
			try {
				_out = newObjectOutput();
				_out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		((InnerObjectOutputStream) _out).init(session, bin);
		return _out;
	}

	protected ObjectOutput newObjectOutput() {
		try {
			return new InnerObjectOutputStream(new InnerByteArrayOutputStream());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected InnerObjectInputStream newObjectInput() {
		InnerObjectInputStream _in = recycle4in.poll();// null;//
		if (_in == null){
			try {
				_in = new InnerObjectInputStream(new InnerByteArrayInputStream());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return _in;
	}

	@Override
	public void destory() {
		if (in != null) {
			InnerObjectInputStream _in = (InnerObjectInputStream) in;
			_in.close();
			in = null;
		}

		if (out != null) {
			InnerObjectOutputStream _out = (InnerObjectOutputStream) out;
			_out.close();
			out = null;
		}

		super.destory();

		recycle4this.offer(this);
	}

	@Override
	public void init(AppSession session) {
		super.init(session);
		InnerObjectInputStream in = newObjectInput();
		this.in = in;
		this.out = newObjectOutput(in);
	}

	public static RemoteVisitor allocate(AppSession session) {
		RemoteVisitor rv = recycle4this.poll();
		if (rv == null) {
			String clazz = System.getProperty("rpc.visitor",
					System.getenv("RPC_VISITOR_CLASS"));
			if (clazz != null)
				try {
					rv = (RemoteVisitor) Thread.currentThread().getContextClassLoader().loadClass(clazz).newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
			if (rv == null) {
				rv = new DefaultRemoteVisitor();
			}
		}
		rv.init(session);
		return rv;
	}

	private DefaultRemoteVisitor() {
	}

	protected ObjectInput in;
	protected ObjectOutput out;

	// ------------------------------------------

	/** 网关模块 */
	public static final byte MODE_GATEWAY = 0x05;
	/** 调用模块 */
	public static final byte MODE_INVOKATION = 0x04;
	/** 验证模块 */
	public static final byte MODE_VALIDATE = 0x03;
	/** 注册模块 */
	public static final byte MODE_REGISTOR = 0x02;
	/** 登录模块 */
	public static final byte MODE_LOGIN = 0x01;

	// /** 版本号 */
	// public static final short version = 0x01;

	// -----------------------------------------

	public static boolean OPEN_RECYCLE_IN = true;
	private static final int CAPACITY_RECYCLE = 1024;

	private static final BlockingQueue<InnerObjectInputStream> recycle4in = new ArrayBlockingQueue<InnerObjectInputStream>(
			CAPACITY_RECYCLE);
	private static final BlockingQueue<InnerObjectOutputStream> recycle4out = new ArrayBlockingQueue<InnerObjectOutputStream>(
			CAPACITY_RECYCLE);
	private static final BlockingQueue<RemoteVisitor> recycle4this = new ArrayBlockingQueue<RemoteVisitor>(
			CAPACITY_RECYCLE);

	/** 对象输入流头部字节集 */
	private static byte[] HEAD_BYTES_2_OBJECTINPUT;

	static {
		try {
			ByteArrayOutputStream _out2byte = new ByteArrayOutputStream();
			ObjectOutputStream _out2array = new ObjectOutputStream(_out2byte);
			HEAD_BYTES_2_OBJECTINPUT = _out2byte.toByteArray();
			_out2array.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// -------------------------------------

	protected static class InnerObjectOutputStream extends ObjectOutputStream {
		private InnerByteArrayOutputStream _out;

		protected InnerObjectOutputStream(InnerByteArrayOutputStream out)
				throws IOException, SecurityException {
			super(out);
			_out = out;
		}

		@Override
		public void close() {
			destory();
			
			recycle4out.offer(this);
		}
		
		public void destory() {
			try {
				reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
			_out.destory();
		}

		public void init(AppSession conn, InnerObjectInputStream bin) {
			_out.init(conn, bin);
		}
	}

	private static class InnerByteArrayOutputStream extends
			ByteArrayOutputStream {
		private boolean first = true;
		private boolean async;
		private int timeout;
		private AppSession session;
		private int mode;
		private InnerObjectInputStream bin;

		private int retryNum;
		private int retry;

		private AppCallResponse response = new AppCallResponseAdapter() {

			@Override
			public int getTimeout() {
				return timeout;
			}

			@Override
			protected boolean onTimeout() {
				return retry == -1 ? true : retry-- > 0;
			}

			public void onSuccess(ByteBuffer msg) {
				bin.init(msg);
			}
		};

		@Override
		public void reset() {
			super.reset();
			retry = retryNum;
		}

		public void prepare(RemoteMethod mm) {
			async = mm.isAsync();// 标记是否异步
			timeout = mm.getTimeout();// 调用超时时间
			retryNum = mm.getTimeoutRetry();// 超时重试次数
		}

		@Override
		public void flush() throws IOException {
			if (first) {
				first = false;
				reset();
				return;
			}
			ByteBuffer msg = ByteBuffer.wrap(toByteArray());
			reset();
			if (async) {
				session.send(mode, msg);
			} else if (!session.send(mode, msg, response, true)) {
				throw new AccessException(
						"send call message failed.  connection "
								+ session.getInetAddress() + " is timeout!");
			}
		}

		public void destory() {
			async = false;
			session = null;
			bin = null;
		}

		public void init(AppSession session, InnerObjectInputStream bin) {
			this.session = session;
			this.bin = bin;
			this.reset();
		}

	}

	private static class InnerObjectInputStream extends ObjectInputStream {
		private InnerByteArrayInputStream _in;

		private Method clearMethod;
		private Field passHandle;
		private Field defaultDataEnd;

		private Object binObj;
		private Field posField;
		private Field endField;
		private Field unreadField;
		private Field blkmode;
		
		private Object bin_inObj;
		private Field peekb;

		protected InnerObjectInputStream(InnerByteArrayInputStream in)
				throws IOException, SecurityException {
			super(in);
			_in = in;

			if(OPEN_RECYCLE_IN)
			try {
				binObj = BeanUtils.getValue(true, this, "bin");
				bin_inObj = BeanUtils.getValue(false, binObj, "in");
				Class<?> clazz = binObj.getClass();
				posField = clazz.getDeclaredField("pos");
				endField = clazz.getDeclaredField("end");
				unreadField = clazz.getDeclaredField("unread");
				blkmode = clazz.getDeclaredField("blkmode");
				blkmode.setAccessible(true);
				posField.setAccessible(true);
				endField.setAccessible(true);
				unreadField.setAccessible(true);
				clazz = ObjectInputStream.class;
				passHandle = clazz.getDeclaredField("passHandle");//-1;
				defaultDataEnd = clazz.getDeclaredField("defaultDataEnd");//false;
				clearMethod = clazz.getDeclaredMethod("clear");
				passHandle.setAccessible(true);
				defaultDataEnd.setAccessible(true);
				clearMethod.setAccessible(true);
				clazz = bin_inObj.getClass();
				peekb = clazz.getDeclaredField("peekb");
				peekb.setAccessible(true);
				
//				System.err.println("posField:"+posField.get(binObj)+", endField:"+endField.get(binObj)+", unreadField:"+unreadField.get(binObj)+", blkmode:"+blkmode.get(binObj)+"\n  passHandle:"+passHandle.get(this)+", defaultDataEnd:"+defaultDataEnd.get(this)+", peekb:"+peekb.get(bin_inObj));
			} catch (Exception e) {
				OPEN_RECYCLE_IN = false;
				e.printStackTrace();
			}
		}

		@Override
		public void close() {
			destory();
			
			if (OPEN_RECYCLE_IN)
				recycle4in.offer(this);
		}

		public void destory() {
			_in.destory();

			if (OPEN_RECYCLE_IN)
				try {
					posField.set(binObj, 0);
					endField.set(binObj, 0);
					unreadField.set(binObj, 0);
					blkmode.set(binObj, true);
					peekb.set(bin_inObj, -1);
					passHandle.set(this, -1);
					defaultDataEnd.set(this, false);
					clearMethod.invoke(this);
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
		}

		public void init(ByteBuffer msg) {
			_in.init(msg);
		}

		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc)
				throws IOException, ClassNotFoundException {
			String name = desc.getName();
			try {
				ClassLoader loader = Thread.currentThread()
						.getContextClassLoader();
				return Class.forName(name, false, loader);
			} catch (ClassNotFoundException ex) {
				Class<?> clazz = primClasses.get(name);
				if (clazz != null) {
					return clazz;
				} else {
					throw ex;
				}
			}
		}

		@Override
		public boolean markSupported() {
			return _in.markSupported();
		}
		
		@Override
		public synchronized void mark(int readlimit) {
			_in.mark(readlimit);
		}
		
		@Override
		public synchronized void reset() throws IOException {
			_in.reset();
		}
		
	}
	
	private static class InnerByteArrayInputStream extends ByteArrayInputStream {

		public InnerByteArrayInputStream() {
			super(HEAD_BYTES_2_OBJECTINPUT);
		}

		public void destory() {
			buf = null;
		}

		public void init(ByteBuffer msg) {
			int p = msg.position();
			buf = new byte[msg.remaining()];
			msg.get(buf);
			msg.position(p);
			pos = 0;
			count = buf.length;
		}
	}

	/** table mapping primitive type names to corresponding class objects */
	private static final HashMap<String, Class<?>> primClasses = new HashMap<String, Class<?>>(
			8, 1.0F);
	static {
		primClasses.put("boolean", boolean.class);
		primClasses.put("byte", byte.class);
		primClasses.put("char", char.class);
		primClasses.put("short", short.class);
		primClasses.put("int", int.class);
		primClasses.put("long", long.class);
		primClasses.put("float", float.class);
		primClasses.put("double", double.class);
		primClasses.put("void", void.class);
	}
}
