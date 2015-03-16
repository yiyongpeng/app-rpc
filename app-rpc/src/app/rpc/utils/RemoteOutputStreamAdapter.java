package app.rpc.utils;

import java.io.IOException;
import java.io.OutputStream;

public class RemoteOutputStreamAdapter extends OutputStream implements RemoteOutputStream {
	private OutputStream out;

	public RemoteOutputStreamAdapter(OutputStream out) {
		this.out = out;
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	public void close() throws IOException {
		out.close();
	}
}
