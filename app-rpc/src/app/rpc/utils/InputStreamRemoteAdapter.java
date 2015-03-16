package app.rpc.utils;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamRemoteAdapter extends InputStream implements
		RemoteInputStream {
	private RemoteInputStream in;

	public InputStreamRemoteAdapter(RemoteInputStream in) {
		this.in = in;
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		in.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return in.markSupported();
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	public byte[] read(int len) throws IOException {
		return in.read(len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		byte[] buff = in.read(len);
		if (buff == null)
			return -1;
		System.arraycopy(buff, 0, b, off, buff.length);
		return buff.length;
	}

	@Override
	public synchronized void reset() throws IOException {
		in.reset();
	}

	@Override
	public long skip(long n) throws IOException {
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
