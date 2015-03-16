package app.rpc.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public final class ConfigUtils {

	public static Properties load(String configPath, String charsetName) {
		Properties p = null;
		InputStream fin = null;
		Reader reader = null;
		try {
			fin = new FileInputStream(configPath);
			reader = new InputStreamReader(fin, charsetName);
			p = new Properties();
			p.load(reader);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseUtils.close(fin);
			CloseUtils.close(reader);
		}
		return p;
	}

	public static Class<?>[] parseInterfaces(String interfacesArg) {
		return parseInterfaces(interfacesArg, Thread.currentThread()
				.getContextClassLoader());
	}

	public static Class<?>[] parseInterfaces(String interfacesArg,
			ClassLoader cl) {
		String[] classNames = interfacesArg.split("\\s*,\\s*");
		Set<Class<?>> list = new HashSet<Class<?>>(classNames.length);
		for (int i = 0; i < classNames.length; i++)
			try {
				if (classNames[i].equals(""))
					continue;
				Class<?> interfaceClass = cl.loadClass(classNames[i]);
				if (!interfaceClass.isInterface()) {
					throw new IllegalArgumentException(classNames[i]
							+ "' isn't interface");
				}
				list.add(interfaceClass);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return list.toArray(new Class<?>[list.size()]);
	}

	public static Properties loadRemoteHandlersProps() {
		Properties props = new Properties();
		try {
			Enumeration<URL> enums = Thread.currentThread()
					.getContextClassLoader()
					.getResources("META-INF/remote.handlers");
			InputStream in = null;
			while (enums.hasMoreElements())
				try {
					URL url = enums.nextElement();
					in = url.openStream();
					props.load(in);
				} finally {
					if (in != null)
						try {
							in.close();
						} catch (Exception e) {
						}
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}

	public static String toInterfacesString(Class<?>[] classes, StringBuffer sb) {
		sb.setLength(0);
		for (int i = 0; i < classes.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(classes[i].getName());
		}
		return sb.toString();
	}
}
