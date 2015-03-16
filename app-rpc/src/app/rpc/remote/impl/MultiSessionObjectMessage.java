package app.rpc.remote.impl;

import java.nio.ByteBuffer;

import app.net.AppSession;
import app.net.DefaultAppSession;

public class MultiSessionObjectMessage extends DefaultObjectMessage {

	private Integer sid;
	private int length;

	@Override
	public String toString() {
		return new StringBuilder("<").append("sid: ").append(sid)
				.append("  length:").append(length).append("  data:")
				.append(getByteBuffer()).append("  response: ").append(isResponse())
				.append("  mode:").append(getMode()).append(">").toString();
	}

	@Override
	public void init(DefaultAppSession session) {
		// 网关来的消息需要配置Session
		if (sid != 0) {
			AppSession session0 = session.getSession(sid);
			if (session0 == null) {
				session0 =  session.createSession(sid);
				// session.getRemote(Remote.SESSION,
				// ObjectSession.class).closeSession(id);
				// log.debug("New-Session from Gateway: "
				// + thisSession.getInetAddress() + " - "
				// + thisSession.getSessionId());
			}
			session0.updateLastTime();
			session = (DefaultAppSession) session0;
		}
		super.init(session);
	}

	public void setByteBuffer(ByteBuffer packet) {
		sid = packet.getInt();// sessionId
		length = packet.getInt();// 内容长度
		super.setByteBuffer(packet);
	}

	@Override
	public void destory() {
		super.destory();
		this.length = 0;
		this.sid = null;
	}

	@Override
	public int length() {
		return length;
	}
}
