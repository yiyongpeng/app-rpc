package app.rpc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class RemoteFileAdapter implements RemoteFile {
	protected File file;

	public RemoteFileAdapter(File file) {
		super();
		this.file = file;

	}

	public String getName() {
		return file.getName();
	}

	public String getPath() {
		return file.toString().replaceAll("\\\\+", "/");
	}

	public boolean isDirectory() {
		return file.isDirectory();
	}

	public boolean isFile() {
		return file.isFile();
	}

	public RemoteInputStream newInputStream() throws IOException {
		return new RemoteInputStreamAdapter(new FileInputStream(file));
	}

	public long count() {
		long count = 0;
		if (isDirectory()) {
			count = file.listFiles().length;
		} else if (isFile()) {
			count = file.length();
		}
		return count;
	}

	public RemoteIterator<RemoteFile> listFilesIterator() {
		if (isDirectory()) {
			return new RemoteIterator<RemoteFile>() {
				File[] files = file.listFiles();
				int i;

				public void remove() {
					throw new UnsupportedOperationException();
				}

				public RemoteFile next() {
					return new RemoteFileAdapter(files[i++]);
				}

				public boolean hasNext() {
					return i < files.length;
				}
			};
		}
		return null;
	}

	public boolean exists() {
		return file.exists();
	}
}
