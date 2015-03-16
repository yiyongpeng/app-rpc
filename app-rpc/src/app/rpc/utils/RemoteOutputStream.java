package app.rpc.utils;

import java.io.IOException;

import app.core.Remote;

public interface RemoteOutputStream extends Remote {

	void write(byte[] data) throws IOException;

	void flush() throws IOException;

	void close() throws IOException;

}
