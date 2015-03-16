package app.rpc.utils;

import java.io.IOException;

import app.core.Remote;

public interface RemoteInputStream extends Remote {

	int available() throws IOException;

	void close() throws IOException;

	void mark(int readlimit);

	boolean markSupported();

	int read() throws IOException;

	byte[] read(int len) throws IOException;

	void reset() throws IOException;

	long skip(long n) throws IOException;

}
