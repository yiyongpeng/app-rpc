package app.rpc.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OutputStreamRemoteAdapter extends ByteArrayOutputStream {
	private RemoteOutputStream out;
	
	public OutputStreamRemoteAdapter(RemoteOutputStream out) {
		this.out = out;
	}

	@Override
	public void flush() throws IOException {
		out.write(toByteArray());
		out.flush();
		reset();
	}

	@Override
	public void close() throws IOException {
		super.close();
		out.close();
	}
}
