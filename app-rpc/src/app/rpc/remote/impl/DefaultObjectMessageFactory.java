package app.rpc.remote.impl;

import app.net.DefaultAppMessageFactory;

public class DefaultObjectMessageFactory extends DefaultAppMessageFactory {
	// private static final Logger log = Logger
	// .getLogger(DefaultObjectAppMessageFactory.class);

	@Override
	protected DefaultObjectMessage newMessage() {
		return new DefaultObjectMessage();
	}
	
	
}
