package app.rpc.remote.impl;

public class RemoteUrl {

	public static final String DEFAULT_PROTOCOL = "ro";

	private String protocol = DEFAULT_PROTOCOL;
	private String hostName = "localhost";
	private int port = 9000;
	private String path = "/";

	public RemoteUrl() {
	}

	/**
	 * 
	 * @param url
	 *            连接字符串 [protocol:]hostname[:port][/path]
	 * @param username
	 * @param password
	 */
	public RemoteUrl(String url) {
		parse(url);
	}

	public void parse(String url) {
		int ipro = url.indexOf(":");
		int ipath = url.indexOf("/");
		if (ipro != -1) {
			int iport = url.indexOf(":", ipro + 1);
			if (ipath != -1 && ipro > ipath)
				ipro = -1;
			if (ipath != -1 && iport > ipath)
				iport = -1;
			if (ipro != -1 && iport == -1) {
				String str2 = url.substring(ipro + 1,
						ipath != -1 ? ipath : url.length());
				if (!str2.matches("\\d+")) {
					protocol = url.substring(0, ipro);
					url = url.substring(ipro + 1);
				}
			} else if (iport != -1) {
				url = url.substring(ipro + 1);
			}
		}
		hostName = url;
		int iport = url.indexOf(":");
		ipath = url.indexOf("/");
		if (iport != -1 && (ipath == -1 || iport < ipath)) {
			hostName = url.substring(0, iport);
			if (ipath != -1) {
				port = Integer.parseInt(url.substring(iport + 1, ipath));
				path = url.substring(ipath);
			} else {
				port = Integer.parseInt(url.substring(iport + 1));
			}
		} else {
			if (ipath != -1) {
				hostName = url.substring(0, ipath);
				path = url.substring(ipath);
			}
		}
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostname) {
		this.hostName = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getServerAddress() {
		return this.hostName + ":" + this.port;
	}

	@Override
	public String toString() {
		return protocol + ":" + hostName + ":" + port + path;
	}

	private String adress;

	public String getDeployHost() {
		if (adress == null)
			adress = getHostName() + "-" + getPort();
		return adress;
	}

}
