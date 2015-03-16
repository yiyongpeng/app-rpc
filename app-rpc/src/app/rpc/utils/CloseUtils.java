package app.rpc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public final class CloseUtils {

	public static void close(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void close(Reader reader) {
		try {
			if (reader != null)
				reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
