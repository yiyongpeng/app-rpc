package app.rpc.remote.impl;

import java.nio.ByteBuffer;

import app.net.AppSession;

public class MultiSessionObjectMessageFactory extends
		DefaultObjectMessageFactory {

	@Override
	protected DefaultObjectMessage newMessage() {
		return new MultiSessionObjectMessage();
	}

	@Override
	protected ByteBuffer onSendBefore(AppSession session, ByteBuffer msg) {
		int length = 4 + 1 + 4 + 4 + msg.remaining() + 8;
		msg = ByteBuffer.allocate(length);
		msg.putInt(session.isDefault() ? 0 : session.getSid());// sid
		return msg;
	}

	@Override
	protected void onSendAfter(AppSession session, ByteBuffer packet,
			ByteBuffer msg) {
		packet.putInt(msg.remaining());// 数据长度
		super.onSendAfter(session, packet, msg);
	}
}
