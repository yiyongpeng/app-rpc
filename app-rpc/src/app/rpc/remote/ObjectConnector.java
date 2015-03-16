package app.rpc.remote;

import app.core.Connection;
import app.core.Connector;
import app.core.Session;

public interface ObjectConnector extends Connector<Connection, Session> {

	void stop();

	String getHost();

	void setHost(String hostname);

	int getPort();

	void setPort(int port);

	boolean isServer();

	void setServer(boolean server);

	ObjectServerHandler getServerHandler();

}
