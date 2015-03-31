package app.rpc.remote.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import app.net.AppMessage;
import app.rpc.remote.ObjectRequest;
import app.rpc.remote.ObjectResponse;
import app.rpc.remote.ObjectServerHandler;
import app.util.BeanUtils;

public class DefaultObjectMessage extends AppMessage implements
		ObjectRequest, ObjectResponse {

	private static final int CACHE_RECYCLE_MAX = 1024;

	/* Request member begin */

	private ObjectInput in;
	private ObjectOutput out;

	/* Request member end */

	/**
	 * 读取消息命令
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException {
		out = newObjectOutput(this);
		in = newObjectInput(getByteBuffer());
	}

	@Override
	public void destory() {
		if (out != null)
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		if (in != null)
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		out = null;
		in = null;
		super.destory();
	}

	public ObjectServerHandler getServletContext() {
		return getSession().getServerHandler();
	}

	@Override
	public DefaultObjectConnection getSession() {
		return (DefaultObjectConnection) super.getSession();
	}

	public void close() {
		throw new UnsupportedOperationException();
	}

	public static ObjectOutput newObjectOutput(ObjectResponse resp)
			throws IOException {
		ObjectOutput _out = recycle4out.poll();
		if (_out == null) {
			_out = new InnerObjectOutputStream(new InnerByteArrayOutputStream());
			_out.flush();
		}
		((InnerObjectOutputStream) _out).init(resp);
		return _out;
	}

	public static ObjectInput newObjectInput(ByteBuffer message)
			throws IOException {
		ObjectInput _in = recycle4in.poll();// null;//
		if (_in == null)
			_in = new InnerObjectInputStream(new InnerByteArrayInputStream());
		((InnerObjectInputStream) _in).init(message);
		return _in;
	}

	/* in begin */

	public int available() throws IOException {
		return in.available();
	}

	public int read() throws IOException {
		return in.read();
	}

	public int read(byte[] b) throws IOException {
		return in.read(b);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	public Object readObject() throws ClassNotFoundException, IOException {
		return in.readObject();
	}

	public long skip(long n) throws IOException {
		return in.skip(n);
	}

	public boolean readBoolean() throws IOException {
		return in.readBoolean();
	}

	public byte readByte() throws IOException {
		return in.readByte();
	}

	public char readChar() throws IOException {
		return in.readChar();
	}

	public double readDouble() throws IOException {
		return in.readDouble();
	}

	public float readFloat() throws IOException {
		return in.readFloat();
	}

	public void readFully(byte[] b) throws IOException {
		in.readFully(b);
	}

	public void readFully(byte[] b, int off, int len) throws IOException {
		in.readFully(b, off, len);
	}

	public int readInt() throws IOException {
		return in.readInt();
	}

	public String readLine() throws IOException {
		return in.readLine();
	}

	public long readLong() throws IOException {
		return in.readLong();
	}

	public short readShort() throws IOException {
		return in.readShort();
	}

	public String readUTF() throws IOException {
		return in.readUTF();
	}

	public int readUnsignedByte() throws IOException {
		return in.readUnsignedByte();
	}

	public int readUnsignedShort() throws IOException {
		return in.readUnsignedShort();
	}

	public int skipBytes(int n) throws IOException {
		return in.skipBytes(n);
	}

	/* in end */

	/* out begin */

	@Override
	public void flush() {
		try {
			if (out != null)
				out.flush();
			super.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(int b) throws IOException {
		out.write(b);
	}

	public void write(byte[] b) throws IOException {
		out.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}

	public void writeObject(Object obj) throws IOException {
		out.writeObject(obj);
	}

	public void writeBoolean(boolean v) throws IOException {
		out.writeBoolean(v);
	}

	public void writeByte(int v) throws IOException {
		out.writeByte(v);
	}

	public void writeBytes(String s) throws IOException {
		out.writeBytes(s);
	}

	public void writeChar(int v) throws IOException {
		out.writeChar(v);
	}

	public void writeChars(String s) throws IOException {
		out.writeChars(s);
	}

	public void writeDouble(double v) throws IOException {
		out.writeDouble(v);
	}

	public void writeFloat(float v) throws IOException {
		out.writeFloat(v);
	}

	public void writeInt(int v) throws IOException {
		out.writeInt(v);
	}

	public void writeLong(long v) throws IOException {
		out.writeLong(v);
	}

	public void writeShort(int v) throws IOException {
		out.writeShort(v);
	}

	public void writeUTF(String s) throws IOException {
		out.writeUTF(s);
	}

	/* out end */

	public void reset() {
		((InnerObjectInputStream) in)._in.reset();
	}

	private static final BlockingQueue<ObjectInput> recycle4in = new ArrayBlockingQueue<ObjectInput>(
			CACHE_RECYCLE_MAX);
	private static final BlockingQueue<ObjectOutput> recycle4out = new ArrayBlockingQueue<ObjectOutput>(
			CACHE_RECYCLE_MAX);
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

	private static class InnerObjectOutputStream extends ObjectOutputStream {
		private InnerByteArrayOutputStream _out;

		protected InnerObjectOutputStream(InnerByteArrayOutputStream out)
				throws IOException, SecurityException {
			super(out);
			_out = out;
		}

		@Override
		public void close() throws IOException {
			// super.close();
			recycle4out.offer(this);
		}

		public void init(ObjectResponse conn) {
			try {
				reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
			_out.init(conn);
		}
	}

	private static class InnerByteArrayOutputStream extends
			ByteArrayOutputStream {
		boolean async;
		private boolean first = true;
		ObjectResponse response;

		@Override
		public void flush() throws IOException {
			if (first) {
				first = false;
				reset();
				return;
			}
			if (!async) {
				byte[] data = buf;
				buf = new byte[32];
				response.setData(ByteBuffer.wrap(data, 0, count));
			}
			reset();
		}

		public void init(ObjectResponse conn) {
			this.response = conn;
			this.async = !conn.isValid();
			reset();
		}

	}

	public static boolean OPEN_RECYCLE_IN = false;// 启用时有概率出现java.io.EOFException

	private static class InnerObjectInputStream extends ObjectInputStream {
		private InnerByteArrayInputStream _in;

		private Object binObj;
		private Field posField;
		private Field endField;
		private Field unreadField;
		private Method clearMethod;

		protected InnerObjectInputStream(InnerByteArrayInputStream in)
				throws IOException, SecurityException {
			super(in);
			_in = in;

			if(OPEN_RECYCLE_IN)
			try {
				binObj = BeanUtils.getValue(true, this, "bin");
				Class<?> clazz = binObj.getClass();
				posField = clazz.getDeclaredField("pos");
				endField = clazz.getDeclaredField("end");
				unreadField = clazz.getDeclaredField("unread");
				posField.setAccessible(true);
				endField.setAccessible(true);
				unreadField.setAccessible(true);
				clazz = ObjectInputStream.class;
				clearMethod = clazz.getDeclaredMethod("clear");
				clearMethod.setAccessible(true);
			} catch (Exception e) {
				OPEN_RECYCLE_IN = false;
				e.printStackTrace();
			}
		}

		@Override
		public void close() throws IOException {
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

	//	public static void main(String[] args) {
	//		try {
	//			ObjectOutput out0 = newObjectOutput(new DefaultObjectMessage() {
	//				
	//				public void setData(ByteBuffer msg) {
	//					try {
	//						ObjectInput in0 = newObjectInput(msg);
	//						boolean suc = in0.readBoolean();
	//						Object obj = in0.readObject();
	//						System.err.println(suc + "  " + obj);
	//						in0.close();
	//					} catch (IOException e) {
	//						e.printStackTrace();
	//					} catch (ClassNotFoundException e) {
	//						e.printStackTrace();
	//					}
	//				}
	//
	//				
	//				public boolean isValid() {
	//					return true;
	//				}
	//			});
	//			out0.writeBoolean(true);
	//			out0.writeObject("dd");
	//			out0.flush();
	//			out0.writeBoolean(true);
	//			out0.writeObject("dd");
	//			out0.flush();
	//			out0.writeBoolean(true);
	//			out0.writeObject("dd");
	//			out0.flush();
	//
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//	}
}
