package app.rpc.utils;

import java.io.IOException;

import app.core.Remote;

public interface RemoteFile extends Remote {

	String getName();

	String getPath();

	boolean isDirectory();

	boolean isFile();

	RemoteInputStream newInputStream() throws IOException;

	long count();

	RemoteIterator<RemoteFile> listFilesIterator();

	boolean exists();
}
