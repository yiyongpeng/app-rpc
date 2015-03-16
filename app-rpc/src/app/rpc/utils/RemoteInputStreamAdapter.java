package app.rpc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class RemoteInputStreamAdapter extends InputStream implements
		RemoteInputStream {

	private InputStream in;

	public RemoteInputStreamAdapter(InputStream in) {
		this.in = in;
	}

	// private void log(String msg) {
	// System.err.println(msg);
	// }

	@Override
	public int available() throws IOException {
		// int value = in.available();
		// log("available()  " + value);
		// return value;
		return in.available();
	}

	@Override
	public void close() throws IOException {
		// log("close()");
		in.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		// log("mark: " + readlimit);
		in.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		// boolean value = in.markSupported();
		// log("markSupported: " + value);
		// return value;
		return in.markSupported();
	}

	@Override
	public int read() throws IOException {
		// int value = in.read();
		// log(value + "  read() ");
		// return value;
		return in.read();
	}

	public byte[] read(int len) throws IOException {
		byte[] buff = new byte[len];
		int size = in.read(buff);
		if (size == -1) {
			// log("null  read( " + len + " )");
			return null;
		} else {
			buff = Arrays.copyOf(buff, size);
			// log(Arrays.toString(buff) + "  read( " + len + " )");
			return buff;
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		// int size = in.read(b);
		// log(size + "  read(" + Arrays.toString(b) + ")");
		// return size;
		return in.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		// int size = in.read(b, off, len);
		// log(size + "  read(" + Arrays.toString(b) + ", " + off + ", " + len
		// + ")");
		// return size;
		return in.read(b, off, len);
	}

	@Override
	public synchronized void reset() throws IOException {
		// log("reset()");
		in.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		// long skip = in.skip(n);
		// log(skip + "  skip(" + n + ")");
		// return skip;
		return in.skip(n);
	}

	@Override
	public String toString() {
		return in.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return in.equals(obj);
	}

	@Override
	public int hashCode() {
		return in.hashCode();
	}

}
